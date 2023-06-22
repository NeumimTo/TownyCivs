package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.StructureInventoryService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ItemUpkeep implements Mechanic<ItemList> {

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {
        Set<ItemStack> upkeep = configContext.configItems.stream().map(ItemList.ConfigItem::toItemStack).collect(Collectors.toSet());

        CountDownLatch countDownLatch = new CountDownLatch(1);
        townContext.cdl = countDownLatch;

        try {
            countDownLatch.await(1L, TimeUnit.SECONDS);
            TownyColonies.injector.getInstance(StructureInventoryService.class)
                    .upkeep(townContext, upkeep);

        } catch (InterruptedException e) {
            TownyColonies.logger.log(Level.SEVERE, "Could not process tick of upkeep region " + townContext.loadedStructure.uuid);
        }
        townContext.cdl = null;
        return townContext.cdlResult;
    }

    @Override
    public void postAction(TownContext townContext, ItemList configContext) {
        Set<ItemStack> upkeep = configContext.configItems.stream().map(ItemList.ConfigItem::toItemStack).collect(Collectors.toSet());

        TownyColonies.injector.getInstance(StructureInventoryService.class)
                .upkeepProcess(townContext.loadedStructure, upkeep);
    }

    @Override
    public String id() {
        return Mechanics.UPKEEP;
    }


    @Override
    public ItemList getNew() {
        return new ItemList();
    }
}
