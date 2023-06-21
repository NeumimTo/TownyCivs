package cz.neumimto.towny.townycolonies.config;


import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.palmergames.bukkit.towny.object.Translatable;
import cz.neumimto.towny.townycolonies.Materials;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.mechanics.Mechanic;
import cz.neumimto.towny.townycolonies.mechanics.Mechanics;
import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.mechanics.common.StringList;
import cz.neumimto.towny.townycolonies.model.BlueprintItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;

@Singleton
public class ConfigurationService {

    private final Map<String, Structure> structures = new HashMap<>();

    private final Map<Material, BlueprintItem[]> blueprintItems = new HashMap();

    public PluginConfig config;

    private void registerBlueprint(Structure structure, Material material, int customModelData) {
        if (blueprintItems.containsKey(material)) {
            BlueprintItem[] ints = blueprintItems.get(material);

            BlueprintItem[] result = new BlueprintItem[ints.length + 1];
            System.arraycopy(ints, 0, result, 0, ints.length);
            result[result.length - 1] = new BlueprintItem(structure.customModelData, structure);

        } else {
            blueprintItems.put(material, new BlueprintItem[]{new BlueprintItem(structure.customModelData, structure)});
        }
    }

    public Optional<BlueprintItem> getBlueprintItem(ItemStack itemStack) {
        if (itemStack == null) {
            return Optional.empty();
        }
        BlueprintItem[] blueprintItems = this.blueprintItems.get(itemStack.getType());
        if (blueprintItems == null) {
            return Optional.empty();
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasCustomModelData()) {
            return Optional.empty();
        }
        for (BlueprintItem blueprintItem : blueprintItems) {
            if (blueprintItem.customModelData == itemMeta.getCustomModelData()) {
                return Optional.of(blueprintItem);
            }
        }
        return Optional.empty();
    }

    public Optional<Structure> findStructureById(String id) {
        return Optional.ofNullable(structures.get(id.toLowerCase(Locale.ROOT)));
    }

    public Collection<Structure> getAll() {
        return structures.values();
    }
    public void load(Path path) throws IOException {
        structures.clear();
        Path structures = path.resolve("structures");
        if (!Files.exists(structures)) {
            Files.createDirectories(structures);
        }

        Path settingsPath = path.resolve("settings.conf");
        if (!settingsPath.toFile().exists()) {
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("settings.conf")) {
                Files.write(settingsPath, resourceAsStream.readAllBytes(), StandardOpenOption.CREATE_NEW);
            }
        }

        try (var f = FileConfig.of(path.resolve("settings.conf"))) {
            f.load();

            PluginConfig config = new PluginConfig();
            new ObjectConverter().toObject(f, config);
            this.config = config;


            Config a = f.get("block_database");
            Materials.init(a);
        }

        if (config.copyDefaults) {
            copy(structures, "cactus-farm.conf");
        }

        try (var paths = Files.newDirectoryStream(structures)) {
            paths.forEach(this::loadStructure);
        }

    }

    private void loadStructure(Path path) {
        Structure structure;
        try (var f = FileConfig.of(path)) {
            f.load();
            structure = new Structure();
            new ObjectConverter().toObject(f, structure);

        } catch (Throwable t) {
            t.printStackTrace();
            TownyColonies.logger.log(Level.SEVERE, "Unable to read structure file " + path.getFileName());
            return;
        }
        structures.put(structure.id, structure);
        registerBlueprint(structure, structure.material, structure.customModelData);
        TownyColonies.logger.info("Loaded structure " + path.getFileName());
    }

    public void copy(Path structures, String file) {
        Path resolve = structures.resolve(file);
        if (!Files.exists(resolve)) {
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("structures/" + file)) {
                Files.write(resolve, resourceAsStream.readAllBytes(), StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Component> buildStructureLore(Structure structure,
                                              int townCount,
                                              int maxCount) {
        var mm = MiniMessage.miniMessage();

        List<Component> list = new ArrayList<>();

        for (String line : structure.description) {
            List<String> expanded = expandLine(line, structure);
            for (String s : expanded) {
                s = replaceSingleLine(s,"%toco_lore_tickrate%", Translatable.of("toco_lore_tickrate").translate());
                s = replaceSingleLine(s,"%toco_lore_production%", Translatable.of("toco_lore_production").translate());
                s = replaceSingleLine(s,"%toco_lore_banned_biomes%", Translatable.of("toco_lore_banned_biomes").translate());
                s = replaceSingleLine(s, "%tickrate%", String.valueOf(structure.period));
                s = replaceSingleLine(s, "%maxcount%", String.valueOf(structure.maxCount));
                list.add(mm.deserialize(s));
            }
        }

        return list;
    }

    private List<String> expandLine(String line, Structure structure) {
        List<String> list = new ArrayList<>();
        if (line.contains("%toco_lore_production_list%")) {
            line = replaceSingleLine(line, "%toco_lore_production_list%", Translatable.of("toco_lore_production_list").translate());
            for (Structure.LoadedPair<Mechanic<Object>, Object> l : structure.production) {
                if (l.mechanic.id().equals(Mechanics.ITEM_PRODUCTION)) {
                    ItemList itemList = (ItemList) l.configValue;
                    for (ItemList.ConfigItem e : itemList.configItems) {
                        String matName = e.customName;
                        if (matName == null) {
                            Material material = Material.matchMaterial(e.material);
                            if (material != null) {
                                matName = material.name();
                            } else {
                                matName = "UNKNOWN";
                            }
                        }
                        int amount = e.amount;
                        String matLine = replaceSingleLine(line, "%amount%", String.valueOf(amount));
                        matLine = replaceSingleLine(matLine, "%item%", matName.substring(0, 1).toUpperCase() + matName.substring(1).replaceAll("_", " "));
                        list.add(matLine);
                    }
                }
            }
        } else if (line.contains("%toco_lore_banned_biomes_list%")) {
            if (structure.placeRequirements == null || structure.placeRequirements.isEmpty()) {
                return Collections.emptyList();
            }
            line = replaceSingleLine(line, "%toco_lore_banned_biomes_list%", Translatable.of("toco_lore_banned_biomes_list").translate());
            for (Structure.LoadedPair<Mechanic<?>,?> pair : structure.placeRequirements) {
                if (pair.mechanic.id().equals(Mechanics.BIOME)) {
                    StringList stringList = (StringList) pair.configValue;
                    for (String biome : stringList.configItems) {
                        list.add(replaceSingleLine(line, "%biome%", biome));
                    }
                }
            }
        } else {
            return List.of(line);
        }

        return list;
    }

    private String replaceSingleLine(String line, String s, String tocoLoreBannedBiomes) {
        return line.replaceFirst(s, tocoLoreBannedBiomes);
    }

}
