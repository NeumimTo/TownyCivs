package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.model.EditSession;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ManagementService {

    private Map<UUID, EditSession> editSessions = new HashMap<>();

    @Inject
    private StructureService structureService;

    @Inject
    private SubclaimService subclaimService;

    @Inject
    private ConfigurationService configurationService;

    public EditSession startNewEditSession(Player player, Structure structure, Location location) {
        var es = new EditSession(structure, location);
        es.structure = structure;
        editSessions.put(player.getUniqueId(), es);
        return es;
    }

    public boolean moveTo(Player player, Location location) {
        if (editSessions.containsKey(player.getUniqueId())) {
            EditSession editSession = editSessions.get(player.getUniqueId());
            editSession.center = location.clone().add(0,editSession.structure.area.y - 1, 0);
            Region region = subclaimService.createRegion(editSession.structure, editSession.center);



            Set<Location> locations = prepareVisualBox(player, editSession.center, editSession.structure.area);
            if (editSession.currentStructureBorder != null) {
                removeAreaBorder(player, editSession.currentStructureBorder);
            }
            editSession.currentStructureBorder = locations;


            boolean isOk = true;

            Optional<Region> overlaps = subclaimService.overlaps(region);
            if (overlaps.isPresent()) {
                Region region1 = overlaps.get();
                Structure overlapingStruct = configurationService.findStructureById(region1.structureId).get();
                player.sendMessage(editSession.structure.name + " region overlaps with " + overlapingStruct.name);
                isOk = false;

                if (editSession.overlappintStructureBorder != null) {
                    removeAreaBorder(player, editSession.overlappintStructureBorder);
                    editSession.overlappintStructureBorder = null;
                }
                editSession.overlappintStructureBorder = prepareVisualBox(player, overlaps.get().boundingBox.getCenter().toLocation(player.getWorld()), overlapingStruct.area);
                sendBlockChange(player, editSession.overlappintStructureBorder, Material.YELLOW_STAINED_GLASS);

            } else if (editSession.overlappintStructureBorder != null){
                removeAreaBorder(player, editSession.overlappintStructureBorder);
                editSession.overlappintStructureBorder = null;
            }

            Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();
            if (subclaimService.isOutsideTownClaim(region, town)) {
                player.sendMessage(editSession.structure.name + " is outside town claim");
                isOk = false;
            }

            sendBlockChange(player, editSession.currentStructureBorder, isOk ? Material.GREEN_STAINED_GLASS : Material.RED_STAINED_GLASS);


            return isOk;
        }
        return false;
    }

    public void endSessionWithoutPlacement(Player player) {
        EditSession remove = editSessions.remove(player.getUniqueId());
        if (remove != null) {
            if (remove.overlappintStructureBorder != null) {
                removeAreaBorder(player, remove.overlappintStructureBorder);
            }
            if (remove.currentStructureBorder != null) {
                removeAreaBorder(player, remove.currentStructureBorder);
            }
        }
    }

    public void endSession(Player player, Location location) {
        if (editSessions.containsKey(player.getUniqueId())) {
            EditSession editSession = editSessions.get(player.getUniqueId());
            editSession.center = location;
            if (moveTo(player, editSession.center)) {
                placeBlueprint(player, editSession.center, editSession.structure);
                editSessions.remove(player.getUniqueId());
            }
        }
    }

    public boolean hasEditSession(Player player) {
        return editSessions.containsKey(player.getUniqueId()) && editSessions.get(player.getUniqueId()).structure != null;
    }

    public EditSession getSession(Player player) {
        return editSessions.get(player.getUniqueId());
    }

    public void removeAreaBorder(Player player, Set<Location> locs) {
        Map<Location, BlockData> blockDataMap = new HashMap<>();
        for (Location location : locs) {
            BlockData blockData = location.getWorld().getBlockData(location);
            blockDataMap.put(location, blockData);
        }
        player.sendMultiBlockChange(blockDataMap, true);
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

    public void placeBlueprint(Player player, Location location, Structure structure) {
        Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();


        Location center = new Location(player.getWorld(), location.getX(),location.getY(),location.getZ());

        LoadedStructure loadedStructure = new LoadedStructure();
        loadedStructure.uuid = UUID.randomUUID();
        loadedStructure.id = structure.id;
        loadedStructure.center = center;

        loadedStructure.structure = structure;

        structureService.addToTown(town, loadedStructure);

        Region lreg = subclaimService.createRegion(loadedStructure).get();
        subclaimService.registerRegion(lreg);

        TownyMessaging.sendPrefixedTownMessage(town, player.getName() + " placed " + structure.name + " at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    private void sendBlockChange(Player player, Set<Location> locations, Material mat) {
        BlockData data = mat.createBlockData();
        Map<Location, BlockData> map = new HashMap<>();
        for (Location location : locations) {
            map.put(location, data);
        }
        player.sendMultiBlockChange(map, false);
    }
}
