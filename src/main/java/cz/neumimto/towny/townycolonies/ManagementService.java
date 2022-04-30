package cz.neumimto.towny.townycolonies;

import com.github.stefvanschie.inventoryframework.HumanEntityCache;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.lsitener.TownListener.VirtualStorageHelper;
import cz.neumimto.towny.townycolonies.model.*;
import cz.neumimto.towny.townycolonies.schedulers.SetEditModeCommand;
import cz.neumimto.towny.townycolonies.schedulers.StructureScheduler;
import cz.neumimto.towny.townycolonies.schedulers.ViewVirtualContainerCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ManagementService {

    private Map<UUID, EditSession> editSessions = new HashMap<>();

    private Map<Location, UUID> contaienrs = new HashMap<>();

    Set<UUID> structuresBeingEdited = new HashSet<>();

    @Inject
    private StructureService structureService;

    @Inject
    private SubclaimService subclaimService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureScheduler structureScheduler;
    private Map<Location, UUID> managedContainerBlocks = new HashMap<>();


    public EditSession startNewEditSession(Player player, Structure structure, Location location) {
        var es = new EditSession(structure, location);
        es.structure = structure;
        editSessions.put(player.getUniqueId(), es);
        MiniMessage miniMessage = MiniMessage.miniMessage();
        player.sendMessage(miniMessage.deserialize("<gold>[TownyColonies]</gold> <green>Right click again to change blueprint location, Left click to confirm selection and place blueprint</green>"));
        return es;
    }

    public boolean moveTo(Player player, Location location) {
        if (editSessions.containsKey(player.getUniqueId())) {
            EditSession editSession = editSessions.get(player.getUniqueId());
            editSession.center = location.clone().add(0, editSession.structure.area.y + 1, 0);
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
                MiniMessage miniMessage = MiniMessage.miniMessage();
                player.sendMessage(miniMessage.deserialize("<gold>[TownyColonies]</gold> <red>"+editSession.structure.name + " region overlaps with " + overlapingStruct.name+"</red>"));
                isOk = false;

                if (editSession.overlappintStructureBorder != null) {
                    removeAreaBorder(player, editSession.overlappintStructureBorder);
                    editSession.overlappintStructureBorder = null;
                }
                editSession.overlappintStructureBorder = prepareVisualBox(player, overlaps.get().boundingBox.getCenter().toLocation(player.getWorld()), overlapingStruct.area);
                sendBlockChange(player, editSession.overlappintStructureBorder, Material.YELLOW_STAINED_GLASS);

            } else if (editSession.overlappintStructureBorder != null) {
                removeAreaBorder(player, editSession.overlappintStructureBorder);
                editSession.overlappintStructureBorder = null;
            }

            Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();
            if (subclaimService.isOutsideTownClaim(region, town)) {
                MiniMessage miniMessage = MiniMessage.miniMessage();
                player.sendMessage(miniMessage.deserialize("<gold>[TownyColonies]</gold> <red>"+editSession.structure.name + " is outside town claim"+"</red>"));
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
        double maxX = location.getX() + area.x;
        double minX = location.getX() - area.x - 1;

        double maxY = location.getY() + area.y;
        double minY = location.getY() - area.y - 1;

        double maxZ = location.getZ() + area.z;
        double minZ = location.getZ() - area.z - 1;

        World world = location.getWorld();

        Set<Location> set = new HashSet<>();
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
            blockChange(world, minX, y, minZ, set);
            blockChange(world, maxX, y, maxZ, set);
            blockChange(world, minX, y, maxZ, set);
            blockChange(world, maxX, y, minZ, set);
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

        Location center = new Location(player.getWorld(), location.getX(), location.getY(), location.getZ());

        LoadedStructure loadedStructure = new LoadedStructure();
        loadedStructure.uuid = UUID.randomUUID();
        loadedStructure.structureId = structure.id;
        loadedStructure.center = center;

        loadedStructure.town = town.getUUID();
        loadedStructure.structureDef = structure;
        loadedStructure.editMode = true;

        Region lreg = subclaimService.createRegion(loadedStructure).get();

        Collection<Material> materials = Materials.getMaterials("tc:container");
        Collection<Block> map = subclaimService.blocksWithinRegion(materials, lreg);
        Map<VirtualContainer, VirtualContent> inv = VirtualStorageHelper.prepareVirtualinventory(lreg, map);
        for (Map.Entry<VirtualContainer, VirtualContent> entry : inv.entrySet()) {
            loadedStructure.containers.add(entry.getKey());
            loadedStructure.storage.add(entry.getValue());
            registerManagedContainerBlock(entry.getKey());
        }


        subclaimService.registerRegion(lreg, loadedStructure);

        structureService.addToTown(town, loadedStructure);

        TownyMessaging.sendPrefixedTownMessage(town, player.getName() + " placed " + structure.name + " at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        structureService.save(loadedStructure);
    }

    public void registerManagedContainerBlock(VirtualContainer key) {
        managedContainerBlocks.put(new Location(Bukkit.getServer().getWorld(key.world), key.x, key.y, key.z), key.id);
    }

    public void toggleEditMode(LoadedStructure loadedStructure, Player player) {
        Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();

        if (!structuresBeingEdited.contains(loadedStructure.uuid)) {
            structuresBeingEdited.add(loadedStructure.uuid);
            TownyMessaging.sendPrefixedTownMessage(town, player.getName() + " put " + loadedStructure.structureDef.name +  " into edit mode ");

            structureScheduler.addCommand(new SetEditModeCommand(loadedStructure.uuid, true));

        } else {

            Region region = subclaimService.getRegion(loadedStructure);
            Map<String, Integer> remainingBlocks = subclaimService.remainingBlocks(region);
            if (subclaimService.noRemainingBlocks(remainingBlocks, loadedStructure)) {
                structuresBeingEdited.remove(loadedStructure.uuid);

                structureScheduler.addCommand(new SetEditModeCommand(loadedStructure.uuid, false));
            } else {
                MiniMessage miniMessage = MiniMessage.miniMessage();
                player.sendMessage(miniMessage.deserialize("<gold>[TownyColonies]</gold> <red>" +loadedStructure.structureDef.name + " do not meet its build requirements to be enabled.</red>"));
            }
        }
    }

    public boolean isBeingEdited(LoadedStructure loadedStructure) {
        return structuresBeingEdited.contains(loadedStructure.uuid);
    }

    private void sendBlockChange(Player player, Set<Location> locations, Material mat) {
        BlockData data = mat.createBlockData();
        Map<Location, BlockData> map = new HashMap<>();
        for (Location location : locations) {
            map.put(location, data);
        }
        player.sendMultiBlockChange(map, false);
    }
    public void openVirtualContainer(LoadedStructure loadedStructure, UUID container, Location location, Player player) {
        UUID uuid = contaienrs.get(location);
        structureScheduler.addCommand(new ViewVirtualContainerCommand(loadedStructure.uuid, uuid, player, location.clone()));
    }

    public UUID getVirtualContainer(Location location) {
        return managedContainerBlocks.get(location);
    }

    public void removeManagedContainerBlock(LoadedStructure loadedStructure) {
        if (loadedStructure.containers != null) {
            for (VirtualContainer key : loadedStructure.containers) {
                managedContainerBlocks.remove(new Location(Bukkit.getServer().getWorld(key.world), key.x, key.y, key.z));
            }
        }
    }
}
