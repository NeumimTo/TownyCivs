package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.model.StructureAndCount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class StructureService {

    @Inject
    private ConfigurationService configurationService;

    public List<StructureMetadata.LoadedStructure> getAllStructures(Town town) {
        CustomDataField<?> metadata = town.getMetadata(TownyColonies.METADATA_KEY);
        if (metadata != null) {
            return ((StructureMetadata)metadata).getValue().structures;
        }
        return Collections.emptyList();
    }

    public void isValidLocation(Structure structure, Location location) {

    }

    public void addToTown(Structure structure, Town town,  Location location) {

    }

    public ItemStack structureToItemstack(Structure structure, Town context, int count) {
        ItemStack itemStack = new ItemStack(Material.matchMaterial(structure.material));
        ItemMeta itemMeta = itemStack.getItemMeta();

        var mm = MiniMessage.miniMessage();
        itemMeta.displayName(mm.deserialize(structure.name));
        itemMeta.setCustomModelData(structure.customModelData);

        List<Component> lore = configurationService.buildStructureLore(structure, count, structure.maxCount, context);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }



    public List<StructureAndCount> findTownStructures(Town town) {
        Collection<Structure> allStructures = configurationService.getAll();
        List<StructureMetadata.LoadedStructure> townStructures = getAllStructures(town);

        Map<Structure,Integer> alreadyBuilt = new HashMap<>();
        List<Structure> avalaible = new ArrayList<>();

        for (Structure structure : allStructures) {
            boolean found = false;
            for (StructureMetadata.LoadedStructure townStructure : townStructures) {
                if (townStructure.id.equalsIgnoreCase(structure.id)) {
                    alreadyBuilt.merge(structure, 1, Integer::sum);
                    found = true;
                }
            }

            if (!found) {
                avalaible.add(structure);
            }
        }

        List<StructureAndCount> merged = new ArrayList<>();
        for (Map.Entry<Structure, Integer> entry : alreadyBuilt.entrySet()) {
            merged.add(new StructureAndCount(entry.getKey(), entry.getValue()));
        }
        avalaible.sort(Comparator.comparing(o -> o.name));

        for (Structure structure : avalaible) {
            merged.add(new StructureAndCount(structure, 0));
        }

        return merged;
    }

    public StructureAndCount findTownStructureById(Town town, Structure structure) {
        List<StructureMetadata.LoadedStructure> townStructures = getAllStructures(town);
        int count = 0;
        for (StructureMetadata.LoadedStructure townStructure : townStructures) {
            if (townStructure.id.equalsIgnoreCase(structure.id)) {
                count ++;
            }
        }
        return new StructureAndCount(structure, count);
    }
}
