package cz.neumimto.towny.townycolonies.db;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.db.TownySQLSource;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.TownyColonies;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseInit {

    public static final int currentSchema = 1;

    private String prefix;

    public void init() {
        this.prefix = TownySettings.getSQLTablePrefix().toUpperCase();
        TownyDataSource dataSource = TownyAPI.getInstance().getDataSource();
        if (!(dataSource instanceof TownySQLSource)) {
            // ?? TownyColonies.logger.log(Level.SEVERE,"TownyColonies require towny to use SQL database, if you wish flatfile support feel free to make a pr");
            return;
        }

        int dbSchema = schemaVersion();
        while (dbSchema != currentSchema) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("townycolonies_" + dbSchema + ".sql")){
                String s = new String(is.readAllBytes()).replaceAll("%prefix%", prefix);
                TownySQLSource sqlSource = (TownySQLSource) dataSource;
                try (var stmt = sqlSource.getHikariDataSource().getConnection().createStatement()){
                    stmt.execute(s);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dbSchema++;
        }
    }

    public int schemaVersion() {
        try {
            Connection connection = ((TownySQLSource) TownyAPI.getInstance().getDataSource()).getHikariDataSource().getConnection();

            try (var statement = connection.createStatement()) {
                try (var rs = statement.executeQuery("select version from " + prefix + "townycolonies_version");){
                    if (rs.first()) {
                        int version = rs.getInt("version");
                        return version;
                    }
                }
            }

        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }
}
