package cz.neumimto.towny.townycolonies.schedulers;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.gui.SelectionGUI;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.mechanics.Mechanic;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

@Singleton
public class StructureScheduler implements Runnable {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureService structureService;

    private Map<UUID, Queue<ScheduledCommand>> taskQueue = new ConcurrentHashMap<>();
    private Queue<ScheduledCommand> toRegister = new ConcurrentLinkedQueue<>();

    Map<UUID, Set<LoadedStructure>> structuresByTown = new HashMap<>();

    public void addCommand(ScheduledCommand command) {
        if (command.uuid == null) {
            toRegister.offer(command);
        } else {
            Queue<ScheduledCommand> scheduledCommands = taskQueue.get(command.uuid);
            if (scheduledCommands == null) {
                scheduledCommands = new ConcurrentLinkedQueue<>();
                taskQueue.put(command.uuid, scheduledCommands);
            }
            scheduledCommands.add(command);
        }
    }

    @Override
    public void run() {

        while (true) {
            ScheduledCommand poll = toRegister.poll();
            if (poll == null) {
                break;
            }
            poll.executeFromAsyncThread(null);
        }

        for (Map.Entry<UUID, Set<LoadedStructure>> entry : structuresByTown.entrySet()) {
            Town t = TownyUniverse.getInstance().getTown(entry.getKey());

            TownContext townContext = new TownContext();
            townContext.town = t;

            for (LoadedStructure structure : entry.getValue()) {

                Queue<ScheduledCommand> scheduledCommands = taskQueue.get(structure.uuid);
                if (scheduledCommands != null) {
                    while (true) {
                        ScheduledCommand poll = scheduledCommands.poll();
                        if (poll == null) {
                            break;
                        }
                        poll.executeFromAsyncThread(structure);
                    }
                    taskQueue.remove(structure.uuid);
                }

                if (structure.nextTickTime <= System.currentTimeMillis()
                        && !structure.editMode
                        && structure.structureDef.period > 0) {
                    structure.nextTickTime = System.currentTimeMillis() + structure.structureDef.period * 1000;
                    structure.lastTickTime = System.currentTimeMillis();
                    townContext.structure = structure.structureDef;
                    handleTick(structure, townContext);
                    structure.unsavedTickCount++;
                    if (structure.unsavedTickCount % structure.structureDef.saveEachNTicks == 0) {
                        structureService.save(structure);
                    }
                }
            }
        }


        for (Queue<ScheduledCommand> value : taskQueue.values()) {
            while (true) {
                ScheduledCommand poll = value.poll();
                if (poll == null){
                    break;
                }
                poll.executeFromAsyncThread(null);
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
    }
}
