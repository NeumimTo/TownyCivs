package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.ItemService;
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

    private static Map<Inventory, VirtualContent> openedInventories = new HashMap<>();

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

        Map<String, Integer> content = new HashMap<>();
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
        for (Map.Entry<String, Integer> entry : content.entrySet()) {
            ItemStack is = VirtualItem.empty_slot.equals(entry.getKey()) ? new ItemStack(Material.AIR) : itemService.toItemStack(entry.getKey(), entry.getValue());
            itemStacks.add(is);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(TownyColonies.INSTANCE, () -> {
            Inventory inventory = Bukkit.createInventory(null, InventoryType.BARREL, loadedStructure.structureDef.name);
            for (ItemStack itemStack : itemStacks) {
                inventory.addItem(itemStack);
            }
            player.openInventory(inventory);
            openedInventories.put(inventory, fVirtualContent);
        });
    }

    public static void handleInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        VirtualContent virtualInventory = openedInventories.get(inventory);
        if (virtualInventory == null) {
            return;
        }

        for (ItemStack itemStack : event.getInventory()) {
            itemStack.
        }

        virtualInventory.content.
        TownyColonies.injector.getInstance(ItemService.class);

        openedInventories.remove(inventory);
    }

}
