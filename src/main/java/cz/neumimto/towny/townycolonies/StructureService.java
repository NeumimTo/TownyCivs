package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.mechanics.RequirementMechanic;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;
import cz.neumimto.towny.townycolonies.model.StructureAndCount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class StructureService {

    @Inject
    private ConfigurationService configurationService;

    public StructureMetadata getMetadata(Town town) {
        return (StructureMetadata) town.getMetadata(TownyColonies.METADATA_KEY);
    }

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

    public void buyBlueprint(Player player, String structureId) {
        Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();

        Optional<Structure> structureById = configurationService.findStructureById(structureId);
        if (structureById.isEmpty()) {
            return;
        }

        Structure structure = structureById.get();

        TownContext townContext = new TownContext();
        townContext.structure = structure;
        townContext.town = town;
        townContext.resident = TownyAPI.getInstance().getResident(player);
        townContext.player = player;

        boolean pass = true;
        for (Structure.LoadedPair<RequirementMechanic<?>, ?> requirement : structure.buyRequirements) {
            Object configValue = requirement.configValue;
            var mechanic = (RequirementMechanic<Object>) requirement.mechanic;
            if (!mechanic.check(townContext,configValue)) {
                pass = false;
                mechanic.nokmessage(townContext, configValue);
                break;
            }
        }

        if (!pass) {
            return;
        }

        StructureMetadata structureMetadata = getMetadata(town);
        if (structureMetadata == null) {
            structureMetadata = new StructureMetadata(new StructureMetadata.Data());
            town.addMetaData(structureMetadata, false);
        }
        structureMetadata.getValue().blueprints.merge(structure.id, 1, Integer::sum);

        for (Structure.LoadedPair<RequirementMechanic<?>, ?> requirement : structure.buyRequirements) {
            Object configValue = requirement.configValue;
            var mechanic = (RequirementMechanic<Object>) requirement.mechanic;
            mechanic.postAction(townContext, configValue);
            mechanic.okmessage(townContext, configValue);
        }

        town.save();
    }

    public void placeBlueprint(Player player, String structureId) {
        Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();

        Optional<Structure> structureById = configurationService.findStructureById(structureId);
        if (structureById.isEmpty()) {
            return;
        }

        StructureMetadata metadata = getMetadata(town);
        Map<String, Integer> blueprints = metadata.getValue().blueprints;
        if (!blueprints.containsKey(structureId)) {
            return;
        }

        Structure structure = structureById.get();
        draw(player, player.getLocation(), structure.area);
        player.getLocation().getWorld().setBlockData(player.getLocation(), Material.STRUCTURE_BLOCK.createBlockData());
        player.getOpenInventory().close();
    }

    public void draw(Player player, Location location, Structure.Area area) {
        Map<Location, BlockData> square = new HashMap<>();

        Location location1 = location.clone();
        for (int x = (area.x/2) -1; x < area.x +1; x++) {
            for (int z = (area.z/2) -1; z < area.z +1; z++) {
                for (int y = (area.y) -1; y < area.y +1; y++) {
                    if (x == z && x == y) {
                        Location loc = location1.clone().add(x, y, z);
                        square.put(loc, Material.RED_STAINED_GLASS.createBlockData());
                    }
                }
            }
        }

        player.sendMultiBlockChange(square);
    }
}
