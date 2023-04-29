package cz.neumimto.towny.townycolonies.lsitener.TownListener;

import cz.neumimto.towny.townycolonies.StructureInventoryService;
import cz.neumimto.towny.townycolonies.StructureService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InventoryListener implements Listener {

    @Inject
    private StructureInventoryService sis;

    @Inject
    private StructureService ss;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player p) {
            sis.closeInvenotory(p);
        }
    }

    @EventHandler
    public void onInventoryOpen(EntityInteractEvent event) {
        ;
    }
}
