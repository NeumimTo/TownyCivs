package cz.neumimto.towny.townycivs.db;

import com.google.gson.Gson;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.db.TownySQLSource;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class Database implements IStorage {

    public static final int currentSchema = 1;

    private static String prefix;

    private static Gson gson = new Gson();

    private static String all_structures_sql;
    private static String save_sql;

    @Inject
    private ConfigurationService configurationService;

    private static int schemaVersion() {
        try {
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();
            String sql = queryFromFile("townycivs_checkversion.sql");
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

    private static String queryFromFile(String fileName) {
        try (InputStream is = TownyCivs.class.getClassLoader().getResourceAsStream(fileName)) {
            return new String(is.readAllBytes()).replaceAll("%prefix%", prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        prefix = TownySettings.getSQLTablePrefix().toUpperCase();
        TownyDataSource dataSource = TownyAPI.getInstance().getDataSource();
        if (!(dataSource instanceof TownySQLSource)) {
            TownyCivs.logger.log(Level.SEVERE, "Townycivs require towny to use SQL database, if you want a flatfile support feel free to make a pr");
            return;
        }

        int dbSchema = schemaVersion();
        while (dbSchema != currentSchema) {
            TownyCivs.logger.info("Updating db schema to ver." + dbSchema);
            String queries = queryFromFile("townycivs_" + dbSchema + ".sql");
            String[] split = queries.split("--split");
            for (String query : split) {
                TownyCivs.logger.info(query);
                TownySQLSource sqlSource = (TownySQLSource) dataSource;
                try (var stmt = sqlSource.getHikariDataSource().getConnection().prepareStatement(query)) {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            dbSchema++;
        }
        save_sql = queryFromFile("townycivs_insert.sql");
        all_structures_sql = queryFromFile("townycivs_all_structures.sql");
    }

    @Override
    public Collection<LoadedStructure> allStructures() {
        Set<LoadedStructure> set = new HashSet<>();
        try {
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();

            String qry = all_structures_sql;

            try (var statement = connection.prepareStatement(qry)) {
                try (var rs = statement.executeQuery()) {
                    while (rs.next()) {

                        String[] center = rs.getString("center").split(";");
                        String structureId = rs.getString("structure_id");

                        var ls = new LoadedStructure(
                                UUID.fromString(rs.getString("structure_uuid")),
                                UUID.fromString(rs.getString("town_uuid")),
                                structureId,
                                new Location(Bukkit.getWorld(center[0]), Integer.parseInt(center[1]), Integer.parseInt(center[2]), Integer.parseInt(center[3])),
                                configurationService.findStructureById(structureId).orElse(null)
                        );

                        ls.lastTickTime = rs.getLong("last_tick_time");
                        ls.editMode.set(rs.getBoolean("edit_mode"));
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

    @Override
    public void save(LoadedStructure structure) {
        try {
            TownyCivs.logger.info("Saving structure " + structure.uuid);
            String sql = save_sql;
            ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getContext();
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();
            try (var stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, structure.uuid.toString()); //"structure_uuid"
                stmt.setString(2, structure.town.toString()); //"town_uuid"
                stmt.setLong(3, structure.lastTickTime); //"last_tick_time"
                stmt.setString(4, structure.structureId); //"structure_id"
                Location center = structure.center;
                stmt.setString(5, center.getWorld().getName() + ";" + center.getBlockX() + ";" + center.getBlockY() + ";" + center.getBlockZ()); //"center"
                stmt.setBoolean(6, structure.editMode.get()); //"edit_mode"
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(UUID uuid) {
        try {
            TownyCivs.logger.info("Removing Structure " + uuid);
            String sql = queryFromFile("townycivs_delete.sql");
            ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getContext();
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();
            try (var stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
