package cz.neumimto.towny.townycolonies.lsitener.TownListener;

import cz.neumimto.towny.townycolonies.schedulers.ViewVirtualContainerCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import javax.inject.Singleton;

@Singleton
public class PlayerListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        ViewVirtualContainerCommand.handleInventoryClose(event);
    }
}
