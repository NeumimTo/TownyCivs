package cz.neumimto.towny.townycolonies.db;

import com.electronwill.nightconfig.core.Config;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public final class Flatfile implements IStorage {

    private Path storage = null;

    @Inject
    private ConfigurationService configurationService;

    @Override
    public void init() {
        storage = TownyColonies.INSTANCE.getDataFolder().toPath().resolve("storage");
        storage.toFile().mkdirs();
    }

    @Override
    public void save(LoadedStructure structure) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("uuid", structure.uuid.toString());
        yaml.set("town", structure.town.toString());
        yaml.set("structureId", structure.structureId);

        yaml.set("center", structure.center);
        yaml.set("editMode", structure.editMode.get());
        yaml.set("lastTickTime", structure.lastTickTime);
        try {
            yaml.save(storage.resolve(structure.uuid.toString() + ".yml").toFile());
        } catch (IOException e) {
            TownyColonies.logger.log(Level.SEVERE, "Could not save structure " + structure.uuid, e);
        }
    }

    @Override
    public void remove(UUID uuid) {
        TownyColonies.logger.info("Removing Structure " + uuid);

        File file = storage.resolve(uuid.toString() + ".yml").toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public Collection<LoadedStructure> allStructures() {
        Set<LoadedStructure> set = new HashSet<>();
        File[] files = storage.toFile().listFiles((dir, name) -> name.endsWith(".yml"));
        for (File file : files) {
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.load(file);

                var struct = new LoadedStructure(
                        UUID.fromString(yaml.getString("uuid")),
                        UUID.fromString(yaml.getString("town")),
                        yaml.getString("structureId"),
                        yaml.getLocation("center"),
                        configurationService.findStructureById(yaml.getString("structureId")).orElse(null)
                );

                struct.editMode.set(yaml.getBoolean("editMode"));
                struct.lastTickTime = yaml.getLong("lastTickTime");
                set.add(struct);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }

        }
        return set;
    }
}
