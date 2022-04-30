package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.ItemService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import cz.neumimto.towny.townycolonies.model.VirtualContent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ViewVirtualContainerCommand extends ScheduledCommand {
    private UUID container;
    private Player player;

    private Location realContainer;

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
        for (VirtualContainer virtualContainer : loadedStructure.containers) {
            if (virtualContainer.world.equalsIgnoreCase(w) && virtualContainer.x == x && virtualContainer.z == z && virtualContainer.y == y) {
                for (VirtualContent virtualContent : loadedStructure.storage) {
                    if (virtualContent.containerUUID.equals(virtualContainer.id)) {
                        content = virtualContent.content;
                    }
                }
            }
        }

        if (content == null) {
            TownyColonies.logger.info("No virtual container found for mc world counterpart at " + w + "," + x + "," + y + "," + z);
            return;
        }

        List<ItemStack> itemStacks = new ArrayList<>();
        ItemService itemService = TownyColonies.injector.getInstance(ItemService.class);
        for (Map.Entry<String, Integer> entry : content.entrySet()) {
            ItemStack is = itemService.toItemStack(entry.getKey(), entry.getValue());
            itemStacks.add(is);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(TownyColonies.INSTANCE, () -> {
            Inventory inventory = Bukkit.createInventory(null, InventoryType.BARREL, loadedStructure.structureDef.name);
            for (ItemStack itemStack : itemStacks) {
                inventory.addItem(itemStack);
            }
        });
    }
}
