package cz.neumimto.towny.townycolonies.lsitener.TownListener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import cz.neumimto.towny.townycolonies.*;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.gui.BlueprintsGui;
import cz.neumimto.towny.townycolonies.model.BlueprintItem;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.Region;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;

@Singleton
public class TownListener implements Listener {

    @Inject
    private SubclaimService subclaimService;

    @Inject
    private StructureService structureService;

    @Inject
    private ItemService itemService;

    @Inject
    private ManagementService managementService;

    @Inject
    private BlueprintsGui blueprintsGui;

    @Inject
    private ConfigurationService configurationService;

    @EventHandler
    public void onTownLoad(TownyLoadedDatabaseEvent event) {
        Collection<Town> towns = TownyUniverse.getInstance().getTowns();
        for (Town town : towns) {
            Collection<LoadedStructure> structures = structureService.getAllStructures(town);
            for (LoadedStructure structure : structures) {
                Optional<Region> region = subclaimService.createRegion(structure);
                region.ifPresent(value -> subclaimService.registerRegion(value));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(Permissions.ROLE_TOWN_ADMINISTRATIVE) && itemService.isTownBook(event.getItem())) {
            event.setCancelled(true);
            handleTownBlookInteraction(player);
            return;
        }

        Optional<BlueprintItem> blueprintItem = configurationService.getBlueprintItem(event.getItem());

        if (player.hasPermission(Permissions.ROLE_ARCHITECT) && blueprintItem.isPresent()) {
            event.setCancelled(true);
            Location location = null;
            if (event.getClickedBlock() == null) {
                location = player.getLocation();
            } else {
                location = event.getClickedBlock().getLocation();
            }

            handleBlueprintPlacement(event, blueprintItem.get(), location);
            return;
        }

    }

    private void handleTownBlookInteraction(Player player) {
        blueprintsGui.display(player);
    }

    private void handleBlueprintPlacement(PlayerInteractEvent event, BlueprintItem blueprintItem, Location location) {
        Player player = event.getPlayer();
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident.hasTown()) {
            WorldCoord worldCoord = WorldCoord.parseWorldCoord(location);
            if (worldCoord.getTownOrNull() == resident.getTownOrNull()) {

                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (managementService.hasEditSession(player)) {
                        managementService.endSession(player, location);
                        EquipmentSlot hand = event.getHand();


                        ItemStack itemInUse = event.getItem();
                        if (itemInUse.getAmount() > 1) {
                            itemInUse.setAmount(itemInUse.getAmount() - 1);
                        } else {
                            itemInUse = null;
                        }
                        player.getInventory().setItem(hand, itemInUse);
                    } else {
                        managementService.startNewEditSession(player, blueprintItem.structure, location);
                    }
                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (managementService.hasEditSession(player)) {
                        managementService.moveTo(player, location);
                    }
                }
            }
        }
    }

    @EventHandler
    public void dropEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItemDrop();

        Optional<BlueprintItem> blueprintItem = configurationService.getBlueprintItem(itemDrop.getItemStack());
        blueprintItem.ifPresent(blueprintItem1 -> managementService.endSessionWithoutPlacement(player));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        handleBlockEditingWithinRegion(player, block);
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        handleBlockEditingWithinRegion(player, block);
    }

    private void handleBlockEditingWithinRegion(Player player, Block block) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            return;
        }
        Town town = resident.getTownOrNull();
        if (town == null) {
            return;
        }
        WorldCoord worldCoord = WorldCoord.parseWorldCoord(block);
        Town currentTown = worldCoord.getTownOrNull();
        if (town != currentTown) {
            return;
        }

        Optional<Region> structureAt = subclaimService.regionAt(block.getLocation());
        if (structureAt.isPresent()) {
            Region region = structureAt.get();
            if (!region.editingAllowed) {
                Structure structure = configurationService.findStructureById(region.structureId).get();
                player.sendMessage(Component.text("Editing of " + structure.name + " is not allowed"));
                player.sendMessage(Component.text("If you wish to edit " + structure.name + " craft an editing tool and righclick within this region"));
                return;
            }
        }
    }
}
