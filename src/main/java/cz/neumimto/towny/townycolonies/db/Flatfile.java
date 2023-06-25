package cz.neumimto.towny.townycolonies.db;

import cz.neumimto.towny.townycolonies.ItemService;
import cz.neumimto.towny.townycolonies.StructureInventoryService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public final class Flatfile implements IStorage {

    private Path storage = null;

    @Inject
    private ItemService itemService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureInventoryService structureInventoryService;

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
        List<YamlConfiguration> invlist = new ArrayList<>();

        ItemStack inventoryBlocker = itemService.getInventoryBlocker();
        for (Map.Entry<Location, Inventory> e : structure.inventory.entrySet()) {
            YamlConfiguration inv = new YamlConfiguration();
            inv.set("location", e.getKey());
            List<ItemStack> itemStacks = new ArrayList<>();
            for (ItemStack itemStack : e.getValue().getContents()) {
                if (itemStack == null) {
                    continue;
                }
                if (itemStack.equals(inventoryBlocker)) {
                    continue;
                }
                itemStacks.add(itemStack);
            }
            inv.set("content", itemStacks);
            invlist.add(inv);
        }

        yaml.set("inventory", invlist);
        try {
            yaml.save(storage.resolve(structure.uuid + ".yml").toFile());
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

                List<Map<String, ?>> csection = (List<Map<String, ?>>) yaml.getList("inventory");
                if (csection != null) {
                    for (Map<String, ?> map : csection) {
                        Location location = (Location) map.get("location");
                        List<ItemStack> items = (List<ItemStack>) map.get("content");
                        structureInventoryService.loadStructureInventory(struct, location, items.toArray(ItemStack[]::new));
                    }
                }
                set.add(struct);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }

        }
        return set;
    }
}
