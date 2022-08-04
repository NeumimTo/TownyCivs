package cz.neumimto.towny.townycolonies;

import co.aikar.commands.PaperCommandManager;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.palmergames.bukkit.towny.TownyAPI;
import cz.neumimto.towny.townycolonies.commands.StructureCommands;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.lsitener.TownListener.TownListener;
import cz.neumimto.towny.townycolonies.mechanics.MechanicService;
import cz.neumimto.towny.townycolonies.schedulers.StructureScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TownyColonies extends JavaPlugin {

    public static Logger logger;

    public static TownyColonies INSTANCE;

    public static Injector injector;

    private static BukkitTask task;

    public boolean reloading;

    @Inject
    public ConfigurationService configurationService;

    @Inject
    public StructureScheduler structureScheduler;

    @Inject
    public StructureService structureService;

    @Inject
    public MechanicService mechanicService;

    @Inject
    private ItemService itemService;

    public TownyColonies() {
        super();
    }

    protected TownyColonies(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }


    @Override
    public void onEnable() {
        TownyColonies.logger = getLogger();
        INSTANCE = this;
        getLogger().info("""
                  
                  ______                          ______      __            _         \s
                 /_  __/___ _      ______  __  __/ ____/___  / /___  ____  (_)__  _____
                  / / / __ \\ | /| / / __ \\/ / / / /   / __ \\/ / __ \\/ __ \\/ / _ \\/ ___/
                 / / / /_/ / |/ |/ / / / / /_/ / /___/ /_/ / / /_/ / / / / /  __(__  )\s
                /_/  \\____/|__/|__/_/ /_/\\__, /\\____/\\____/_/\\____/_/ /_/_/\\___/____/ \s
                                        /____/                                        \s
                """);
        getLogger().info("TownyColonies starting");

        if (!reloading) {
            injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(ConfigurationService.class);
                    bind(StructureScheduler.class);
                    bind(StructureService.class);
                    bind(MechanicService.class);
                    bind(ItemService.class);
                }
            });
        }

        injector.injectMembers(this);

        injector.getInstance(MechanicService.class).registerDefaults();
        ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
        try {
            configurationService.load(getDataFolder().toPath());
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Unable to load configuration " + e.getMessage());
        }

        injector.getInstance(StructureService.class).loadAll();
        if (!reloading) {
            PaperCommandManager manager = new PaperCommandManager(this);
            manager.registerCommand(injector.getInstance(StructureCommands.class));

            Map<String, Map<String, String>> translations = new HashMap<>();
            try (var is = getClass().getClassLoader().getResourceAsStream("lang/en-US.properties")) {
                Properties properties = new Properties();
                properties.load(is);

                translations.put("en_US", new HashMap<>((Map) properties));
            } catch (IOException e) {
                e.printStackTrace();
            }
            TownyAPI.getInstance().addTranslations(this, translations);


            Bukkit.getPluginManager().registerEvents(injector.getInstance(TownListener.class), this);
        }

        injector.getInstance(ItemService.class).registerRecipes();

        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                injector.getInstance(StructureScheduler.class),
                0L, 20);

        reloading = true;
        getLogger().info("TownyColonies started");
    }



    @Override
    public void onDisable() {
        getLogger().info("TownyColonies disabled");
    }
}
