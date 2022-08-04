package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.ItemService;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.mechanics.VirtualItem;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import cz.neumimto.towny.townycolonies.model.VirtualContent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.swing.plaf.InsetsUIResource;
import java.util.*;

public class ViewVirtualContainerCommand extends ScheduledCommand {
    private UUID container;
    private Player player;

    private Location realContainer;

    record InvData(VirtualContent virtualContent, LoadedStructure loadedStructure){

    }

    private static Map<Inventory, InvData> openedInventories = new HashMap<>();

    public ViewVirtualContainerCommand(UUID uuid, UUID container, Player player, Location realContainer) {
        super(uuid);
        this.container = container;
        this.player = player;
        this.realContainer = realContainer;
    }

    @Override
    public void executeFromAsyncThread(LoadedStructure loadedStructure) {
        if (loadedStructure.containers == null) {
            return;
        }

        List<VirtualItem> content = new ArrayList<>();
        int x = realContainer.getBlockX();
        int y = realContainer.getBlockY();
        int z = realContainer.getBlockZ();
        String w = realContainer.getWorld().getName();
        VirtualContent _virtualContent = null;
        for (VirtualContainer virtualContainer : loadedStructure.containers) {
            if (virtualContainer.world.equalsIgnoreCase(w) && virtualContainer.x == x && virtualContainer.z == z && virtualContainer.y == y) {
                for (VirtualContent virtualContent : loadedStructure.storage) {
                    if (virtualContent.containerUUID.equals(virtualContainer.id)) {
                        content = virtualContent.content;
                        _virtualContent = virtualContent;
                    }
                }
            }
        }

        final VirtualContent fVirtualContent = _virtualContent;

        if (content == null) {
            TownyColonies.logger.info("No virtual container found for mc world counterpart at " + w + "," + x + "," + y + "," + z);
            return;
        }

        List<ItemStack> itemStacks = new ArrayList<>();
        ItemService itemService = TownyColonies.injector.getInstance(ItemService.class);

        for (VirtualItem virtualItem : content) {
            if (virtualItem.data.equals(VirtualItem.empty_slot.data)) {
                itemStacks.add(new ItemStack(Material.AIR));
            } else {
                itemStacks.add(virtualItem.toItemStack());
            }
        }


        Bukkit.getScheduler().scheduleSyncDelayedTask(TownyColonies.INSTANCE, () -> {
            Inventory inventory = Bukkit.createInventory(null, InventoryType.BARREL, loadedStructure.structureDef.name);
            for (ItemStack itemStack : itemStacks) {
                inventory.addItem(itemStack);
            }
            player.openInventory(inventory);
            openedInventories.put(inventory, new InvData(fVirtualContent, loadedStructure));
        });
    }

    public static void handleInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InvData invData = openedInventories.get(inventory);
        if (invData == null) {
            return;
        }

        final List<VirtualItem> items = new ArrayList<>();

        for (ItemStack itemStack : event.getInventory()) {
            VirtualItem serialized = VirtualItem.toVirtualItem(itemStack);
            items.add(serialized);
        }

        inventory.clear();
        openedInventories.remove(inventory);

        if (TownyColonies.INSTANCE.structureService.findStructureByUUID(invData.loadedStructure.uuid).isEmpty()) {
            return;
        }

        var command = new VirtualInventoryUpdateCommand(invData.loadedStructure,invData.virtualContent.containerUUID,items);
        TownyColonies.INSTANCE.structureScheduler.addCommand(command);
    }

}
