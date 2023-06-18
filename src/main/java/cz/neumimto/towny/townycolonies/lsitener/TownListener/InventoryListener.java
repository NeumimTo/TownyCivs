package cz.neumimto.towny.townycolonies.lsitener.TownListener;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import cz.neumimto.towny.townycolonies.StructureInventoryService;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.SubclaimService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InventoryListener implements Listener {

    @Inject
    private StructureInventoryService sis;

    @Inject
    private StructureService ss;

    @Inject
    private SubclaimService sus;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player p) {
            sis.closeInvenotory(p);
        }
    }

    @EventHandler
    public void onInventoryOpen(PlayerInteractEvent event) {
        int i = 0;
        Player player = (Player) event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
            Location location = event.getClickedBlock().getLocation();

            Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
            if (resident.getTownOrNull() == null) {
                return;
            }

            TownBlock tb = TownyUniverse.getInstance().getTownBlockOrNull(WorldCoord.parseWorldCoord(location));
            if (tb == null) {
                return;
            }

            if (tb.getTownOrNull() != resident.getTownOrNull()) {
                return;
            }

            Optional<LoadedStructure> structureAt = sus.getStructureAt(location);
            if (structureAt.isEmpty()) {
                return;
            }

            LoadedStructure structure = structureAt.get();
            sis.openInventory(player, structure);
        }
    }
}
