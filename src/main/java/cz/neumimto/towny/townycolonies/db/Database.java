package cz.neumimto.towny.townycolonies.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.db.TownySQLSource;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class Database {

    public static final int currentSchema = 1;

    private static String prefix;

    private static Gson gson = new Gson();

    private static String all_structures_sql;
    private static String save_sql;

    public static void init() {
        prefix = TownySettings.getSQLTablePrefix().toUpperCase();
        TownyDataSource dataSource = TownyAPI.getInstance().getDataSource();
        if (!(dataSource instanceof TownySQLSource)) {
            TownyColonies.logger.log(Level.SEVERE, "TownyColonies require towny to use SQL database, if you want a flatfile support feel free to make a pr");
            return;
        }

        int dbSchema = schemaVersion();
        while (dbSchema != currentSchema) {
            TownyColonies.logger.info("Updating db schema to ver." + dbSchema);
            String queries = queryFromFile("townycolonies_" + dbSchema + ".sql");
            String[] split = queries.split("--split");
            for (String query : split) {
                TownyColonies.logger.info(query);
                TownySQLSource sqlSource = (TownySQLSource) dataSource;
                try (var stmt = sqlSource.getHikariDataSource().getConnection().prepareStatement(query)) {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            dbSchema++;
        }
        save_sql = queryFromFile("townycolonies_insert.sql");
        all_structures_sql = queryFromFile("townycolonies_all_structures.sql");
    }

    private static int schemaVersion() {
        try {
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();
            String sql = queryFromFile("townycolonies_checkversion.sql");
            try (var statement = connection.prepareStatement(sql)) {
                try (var rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("version");
                    }
                }
            }

        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    public static Collection<LoadedStructure> allStructures() {
        Set<LoadedStructure> set = new HashSet<>();
        try {
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();

            String qry = all_structures_sql;

            try (var statement = connection.prepareStatement(qry)) {
                try (var rs = statement.executeQuery()) {
                    while (rs.next()) {
                        var ls = new LoadedStructure();
                        ls.uuid = UUID.fromString(rs.getString("structure_uuid"));
                        ls.town = UUID.fromString(rs.getString("town_uuid"));
                        ls.lastTickTime = rs.getLong("last_tick_time");
                        ls.structureId = rs.getString("structure_id");
                        String[] center = rs.getString("center").split(";");
                        ls.center = new Location(Bukkit.getWorld(center[0]),Integer.parseInt(center[1]),Integer.parseInt(center[2]),Integer.parseInt(center[3]));
                        ls.editMode = new AtomicBoolean(rs.getBoolean("edit_mode"));
                        set.add(ls);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return set;
    }

    private static String queryFromFile(String fileName) {
        try (InputStream is = TownyColonies.class.getClassLoader().getResourceAsStream(fileName)) {
            return new String(is.readAllBytes()).replaceAll("%prefix%", prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveAll(Collection<LoadedStructure> values) {
        for (LoadedStructure structure : values) {
            save(structure);
        }
    }

    public static void save(LoadedStructure structure) {
        try {
            TownyColonies.logger.info("Saving structure " + structure.uuid);
            String sql = save_sql;
            ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getContext();
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();
            try (var stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, structure.uuid.toString()); //"structure_uuid"
                stmt.setString(2, structure.town.toString()); //"town_uuid"
                stmt.setLong(3, structure.lastTickTime); //"last_tick_time"
                stmt.setString(4, structure.structureId); //"structure_id"
                Location center = structure.center;
                stmt.setString(5, center.getWorld().getName()+";"+center.getBlockX() + ";" + center.getBlockY() + ";" + center.getBlockZ()); //"center"
                stmt.setBoolean(7, structure.editMode.get()); //"edit_mode"
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void remove(UUID uuid) {
        try {
            TownyColonies.logger.info("Removing Structure " + uuid);
            String sql = queryFromFile("townycolonies_delete.sql");
            ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getContext();
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();
            try (var stmt = connection.prepareStatement(sql)){
                stmt.setString(1, uuid.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void scheduleSave(LoadedStructure structure) {
        LoadedStructure clone = structure.clone();

        Bukkit.getScheduler().runTaskAsynchronously(TownyColonies.INSTANCE, () -> save(clone));
    }

    public static void scheduleRemove(LoadedStructure structure) {
        Bukkit.getScheduler().runTaskAsynchronously(TownyColonies.INSTANCE, () -> remove(structure.uuid));
    }
}
