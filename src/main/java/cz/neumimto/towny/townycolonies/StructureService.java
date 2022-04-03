package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.mechanics.RequirementMechanic;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;
import cz.neumimto.towny.townycolonies.model.Region;
import cz.neumimto.towny.townycolonies.model.StructureAndCount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class StructureService {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SubclaimService subclaimService;

    private Map<UUID, Set<Location>> areaBorders = new HashMap<>();

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

        town.addMetaData(structureMetadata, true);
    }

    public void placeBlueprint(Player player, String structureId) {
        Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();

        Optional<Structure> structureById = configurationService.findStructureById(structureId);
        if (structureById.isEmpty()) {
            return;
        }

        StructureMetadata metadata = getMetadata(town);
        Structure structure = structureById.get();

        Map<String, Integer> blueprints = metadata.getValue().blueprints;
        if (!blueprints.containsKey(structure.id) && blueprints.get(structure.id) < 1) {
            return;
        }

        Location location = player.getLocation();

        Location center = new Location(player.getWorld(), location.getX(),location.getY() + structure.area.y - 1,location.getZ());

        Optional<Region> region = subclaimService.createRegion(structureId, center);
        if (region.isPresent()) {
            removeAreaBorder(player, true);


            Optional<Region> overlaps = subclaimService.overlaps(region.get());
            BlockData borderBlock = Material.GREEN_STAINED_GLASS.createBlockData();

            boolean continueToEditMode = true;

            if (overlaps.isPresent()) {

                Region region1 = overlaps.get();
                borderBlock = Material.YELLOW_STAINED_GLASS.createBlockData();
                Structure overlapingStruct = configurationService.findStructureById(region1.structureId).get();
                player.sendMessage(structure.name + " region overlaps with " + overlapingStruct.name);
                continueToEditMode = false;
            }

            if (subclaimService.isOutsideTownClaim(region.get(), town)) {
                borderBlock = Material.RED_STAINED_GLASS.createBlockData();
                player.sendMessage(structure.name + " is outside town claim");
                continueToEditMode = false;
            }

            Set<Location> border = prepareVisualBox(player,center, structure.area);
            areaBorders.put(player.getUniqueId(), border);
            Map<Location, BlockData> blockChanges = new HashMap();
            for (Location bloc : border) {
                blockChanges.put(bloc, borderBlock);
            }
            player.sendMultiBlockChange(blockChanges, true);

            if (continueToEditMode) {
                blueprints.merge(structureId, -1, Integer::sum);
                StructureMetadata.LoadedStructure loadedStructure = new StructureMetadata.LoadedStructure();
                loadedStructure.uuid = UUID.randomUUID();
                loadedStructure.id = structure.id;
                loadedStructure.center = center;

                metadata.getValue().editMode.add(loadedStructure);

                Region lreg = subclaimService.createRegion(loadedStructure).get();
                subclaimService.registerRegion(lreg);

                town.addMetaData(metadata, true);
                player.sendMessage("You have found a valid location for " + structure.name);
            }
        }


        player.getOpenInventory().close();
    }

    public Set<Location> prepareVisualBox(Player player, Location location, Structure.Area area) {
        double maxX = location.getX() + area.x + 1;
        double minX = location.getX() - area.x - 1;
        
        double maxY = location.getY() + area.y + 1;
        double minY = location.getY() - area.y - 1;
        
        double maxZ = location.getZ() + area.z + 1;
        double minZ = location.getZ() - area.z - 1;

        World world = location.getWorld();

        Set<Location> set = new HashSet<>();
        //todo all in one cycle
        for (double x = minX; x <= maxX; x++) {
            blockChange(world, x, minY, minZ, set);
            blockChange(world, x, maxY, maxZ, set);
            blockChange(world, x, minY, maxZ, set);
            blockChange(world, x, maxY, minZ, set);
        }

        for (double z = minZ; z <= maxZ; z++) {
            blockChange(world, minX, minY, z, set);
            blockChange(world, maxX, maxY, z, set);
            blockChange(world, minX, maxY, z, set);
            blockChange(world, maxX, minY, z, set);
        }

        for (double y = minY; y <= maxY; y++) {
            blockChange(world, minX, y, minZ,set);
            blockChange(world, maxX, y, maxZ,set);
            blockChange(world, minX, y, maxZ,set);
            blockChange(world, maxX, y, minZ,set);
        }

        return set;
    }

    private void blockChange(World world, double x, double y, double z, Set<Location> set) {
        if (world.getMinHeight() > y && world.getMaxHeight() < y) {
            return;
        }
        set.add(new Location(world, x, y, z));
    }

    public void removeAreaBorder(Player player, boolean sendBlockUpdate) {
        Set<Location> locations = areaBorders.get(player.getUniqueId());
        if (locations == null) {
            return;
        }
        areaBorders.remove(player.getUniqueId());
        if (locations.isEmpty()) {
            return;
        }
        if (sendBlockUpdate) {
            Map<Location, BlockData> blockDataMap = new HashMap<>();
            for (Location location : locations) {
                BlockData blockData = location.getWorld().getBlockData(location);
                blockDataMap.put(location, blockData);
            }
            player.sendMultiBlockChange(blockDataMap, true);
        }
    }

}
