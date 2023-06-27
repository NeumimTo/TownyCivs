package cz.neumimto.towny.townycivs.schedulers;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.StructureInventoryService;
import cz.neumimto.towny.townycivs.StructureService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.config.Structure;
import cz.neumimto.towny.townycivs.db.Storage;
import cz.neumimto.towny.townycivs.mechanics.Mechanic;
import cz.neumimto.towny.townycivs.mechanics.TownContext;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Level;

@Singleton
public class FolliaScheduler implements Runnable, Listener {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureService structureService;

    @Inject
    private StructureInventoryService inventoryService;

    private Set<UUID> forceSaveNextTick = new HashSet<>();

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
                        TownyCivs.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                                .run(() -> handleTick(structure, townContext),
                                        () -> TownyCivs.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> handleTick(structure, townContext)));
                    } else {
                        TownyCivs.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> handleTick(structure, townContext));
                    }
                }
            }
        }

    }

    private void handleTick(LoadedStructure structure, TownContext ctx) {
        TownyCivs.logger.log(Level.INFO, "Ticking region " + structure.structureDef.id);

        List<Structure.LoadedPair<Mechanic<Object>, Object>> upkeep = structure.structureDef.upkeep;


        for (Structure.LoadedPair<Mechanic<Object>, Object> m : upkeep) {
            if (!m.mechanic.check(ctx, m.configValue)) {
                return;
            }
        }

        for (Structure.LoadedPair<Mechanic<Object>, Object> m : structure.structureDef.production) {
            if (!m.mechanic.check(ctx, m.configValue)) {
                return;
            }
        }

        for (Structure.LoadedPair<Mechanic<Object>, Object> m : upkeep) {
            m.mechanic.postAction(ctx, m.configValue);
        }

        List<Structure.LoadedPair<Mechanic<Object>, Object>> production = structure.structureDef.production;
        for (Structure.LoadedPair<Mechanic<Object>, Object> m : production) {
            m.mechanic.postAction(ctx, m.configValue);
        }

        structure.unsavedTickCount++;
        if (structure.unsavedTickCount % structure.structureDef.saveEachNTicks == 0 || forceSaveNextTick.contains(structure.uuid)) {
            if (!inventoryService.anyInventoryIsBeingAccessed(structure)) {
                Storage.scheduleSave(structure);
                structure.unsavedTickCount = 0;
                forceSaveNextTick.remove(structure.uuid);
            } else {
                forceSaveNextTick.add(structure.uuid);
            }
        }
    }
}
