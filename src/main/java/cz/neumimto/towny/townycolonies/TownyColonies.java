package cz.neumimto.towny.townycolonies;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.schedulers.StructureScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TownyColonies extends JavaPlugin {

    public static String METADATA_KEY = "townycolonies-town-structures";

    public static Logger logger;

    @Override
    public void onEnable() {
        TownyColonies.logger = getLogger();
        getLogger().info("TownyColonies starting");

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ConfigurationService.class);
                bind(StructureScheduler.class);
                bind(StructureService.class);
            }
        });

        ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
        try {
            configurationService.load(getDataFolder().toPath());
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Unable to load configuration " + e.getMessage());
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                injector.getInstance(StructureScheduler.class),
                0L,
                configurationService.smallestPeriod());

        getLogger().info("TownyColonies started");
    }

    @Override
    public void onDisable() {
        getLogger().info("TownyColonies disabled");
    }
}
