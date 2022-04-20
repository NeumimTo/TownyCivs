package cz.neumimto.towny.townycolonies;

import co.aikar.commands.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.db.TownySQLSource;
import com.palmergames.bukkit.towny.object.metadata.MetadataLoader;
import cz.neumimto.towny.townycolonies.commands.StructureCommands;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.lsitener.TownListener.TownListener;
import cz.neumimto.towny.townycolonies.mechanics.MechanicService;
import cz.neumimto.towny.townycolonies.schedulers.StructureScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TownyColonies extends JavaPlugin {

    public static String METADATA_KEY = "townycolonies-town-structures";

    public static Logger logger;

    public static TownyColonies INSTANCE;

    public static Injector injector;

    @Override
    public void onEnable() {
        TownyColonies.logger = getLogger();
        INSTANCE = this;
        getLogger().info("TownyColonies starting");

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

        //TownyDataSource dataSource = TownyAPI.getInstance().getDataSource();
        //if (!(dataSource instanceof TownySQLSource)) {
        //    getLogger().log(Level.SEVERE,"TownyColonies require towny to use SQL database, if you wish flatfile support feel free to make a pr");
        //    return;
        //}
//
        //((TownySQLSource) dataSource).getHikariDataSource().getConnection
        //TownySettings.getSQLTablePrefix().toUpperCase();

        injector.getInstance(MechanicService.class).registerDefaults();
        ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
        try {
            configurationService.load(getDataFolder().toPath());
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Unable to load configuration " + e.getMessage());
        }

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(injector.getInstance(StructureCommands.class));

        Map<String, Map<String, String>> translations = new HashMap<>();
        try (var is = getClass().getClassLoader().getResourceAsStream("lang/en-US.properties")) {
            Properties properties = new Properties();
            properties.load(is);

            translations.put("en_US", new HashMap<>((Map)properties));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TownyAPI.getInstance().addTranslations(this, translations);

        Bukkit.getPluginManager().registerEvents(injector.getInstance(TownListener.class), this);

        MetadataLoader.getInstance()
                .registerDeserializer(StructureMetadata.typeID(), new StructureMetadataDeserializer());

        injector.getInstance(ItemService.class).registerRecipes();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                injector.getInstance(StructureScheduler.class),
                0L,
                configurationService.smallestPeriod() * 20);

        getLogger().info("TownyColonies started");
    }

    @Override
    public void onDisable() {
        getLogger().info("TownyColonies disabled");
    }
}
