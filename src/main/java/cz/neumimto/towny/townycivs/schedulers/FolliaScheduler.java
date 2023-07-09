package cz.neumimto.towny.townycivs.schedulers;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.StructureInventoryService;
import cz.neumimto.towny.townycivs.StructureService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.ConfigItem;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.config.TMechanic;
import cz.neumimto.towny.townycivs.db.Storage;
import cz.neumimto.towny.townycivs.mechanics.TownContext;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

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


                    handleTick(structure, townContext);
                }
            }
        }

    }

    private void handleTick(LoadedStructure structure, TownContext ctx) {

        for (TMechanic tMechanic : structure.structureDef.onTick) {

            Collection<ItemStack> input = items(tMechanic.input);
            Collection<ItemStack> output = items(tMechanic.output);
            Collection<ItemStack> reagent = items(tMechanic.reagent);

            // check

            if (!output.isEmpty()) {
                if (!inventoryService.canTakeAtLeastOne(structure, output)) {
                    continue;
                }
            }

            if (tMechanic.reagentPrice > 0) {
                if (ctx.town.getAccount().getCachedBalance() < tMechanic.reagentPrice) {
                    continue;
                }
            }

            if (!tMechanic.requiredStructures.isEmpty()) {
                Collection<LoadedStructure> allStructures = structureService.getAllStructures(ctx.town);
                boolean containsAll = allStructures.stream().map(a -> a.structureDef.id).collect(Collectors.toSet()).containsAll(tMechanic.requiredStructures);
                if (!containsAll) {
                    continue;
                }
            }

            if (!reagent.isEmpty()) {
                if (!inventoryService.contains(structure, reagent, true)) {
                    continue;
                }
            }

            if (!input.isEmpty()) {
                if (!inventoryService.contains(structure, input, false)) {
                    continue;
                }
            }

            // process

            if (tMechanic.reagentPrice > 0) {
                ctx.town.getAccount().withdraw(tMechanic.giveMoney, "TownyCivs  -" + structure.structureDef.name);
            }

            for (String command : tMechanic.commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("%town%", ctx.town.getName())
                                .replace("%name%", ctx.town.getNationOrNull() == null ? "" : ctx.town.getNationOrNull().getName())
                );
            }

            if (!reagent.isEmpty()) {
                inventoryService.takeItems(structure, reagent, false);
            }

            if (!input.isEmpty()) {
                inventoryService.takeItems(structure, input, true);
            }

            if (!output.isEmpty()) {
                inventoryService.addItemProduction(structure, output);
            }

            if (tMechanic.giveMoney > 0) {
                ctx.town.getAccount().deposit(tMechanic.giveMoney, "TownyCivs  +" + structure.structureDef.name);
            }
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

    private static Collection<ItemStack> items(List<ConfigItem> tMechanic) {
        Set<ItemStack> output = new HashSet<>();
        if (tMechanic != null) {
            output = tMechanic.stream().map(ConfigItem::toItemStack).collect(Collectors.toSet());
        }
        return output;
    }
}
