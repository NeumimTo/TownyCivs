package cz.neumimto.towny.townycolonies.schedulers;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.StructureInventoryService;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.db.Storage;
import cz.neumimto.towny.townycolonies.mechanics.Mechanic;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Singleton
public class FolliaScheduler implements Runnable, Listener {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureService structureService;

    @Inject
    private StructureInventoryService inventoryService;

    @Override
    public void run() {
        for (Map.Entry<UUID, Set<LoadedStructure>> entry : structureService.getAllStructuresByTown().entrySet()) {
            Town t = TownyUniverse.getInstance().getTown(entry.getKey());

            TownContext townContext = new TownContext();
            townContext.town = t;

            for (LoadedStructure structure : entry.getValue()) {

                if (structure.nextTickTime <= System.currentTimeMillis()
                        && !structure.editMode.get()
                        && structure.structureDef.period > 0) {
                    structure.nextTickTime = System.currentTimeMillis() + structure.structureDef.period * 1000;
                    structure.lastTickTime = System.currentTimeMillis();
                    townContext.structure = structure.structureDef;
                    townContext.loadedStructure = structure;


                    UUID playerViewingInventory = inventoryService.getPlayerViewingInventory(structure);
                    if (playerViewingInventory != null) {
                        Player player = Bukkit.getPlayer(playerViewingInventory);
                        TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                                .run(() -> handleTick(structure, townContext),
                                     () -> TownyColonies.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> handleTick(structure, townContext)));
                    } else {
                        TownyColonies.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> handleTick(structure, townContext));
                    }
                }
            }
        }

    }

    private void handleTick(LoadedStructure structure, TownContext ctx) {
        TownyColonies.logger.log(Level.INFO, "Ticking region " + structure.structureDef.id);

        List<Structure.LoadedPair<Mechanic<Object>, Object>> upkeep = structure.structureDef.upkeep;


        for (Structure.LoadedPair<Mechanic<Object>, Object> m : upkeep) {
            if (!m.mechanic.check(ctx, m.configValue)) {
                return;
            }
        }

        for (Structure.LoadedPair<Mechanic<Object>, Object> m : upkeep) {
            m.mechanic.postAction(ctx, m.configValue);
        }

        List<Structure.LoadedPair<Mechanic<Object>,Object>> production = structure.structureDef.production;
        for (Structure.LoadedPair<Mechanic<Object>,Object> m : production) {
            m.mechanic.postAction(ctx, m.configValue);
        }

        structure.unsavedTickCount++;
        if (structure.unsavedTickCount % structure.structureDef.saveEachNTicks == 0) {
            Storage.scheduleSave(structure);
        }
    }
}
