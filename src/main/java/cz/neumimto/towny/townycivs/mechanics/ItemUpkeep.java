package cz.neumimto.towny.townycivs.mechanics;

import cz.neumimto.towny.townycivs.StructureInventoryService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.mechanics.common.ItemList;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;

public class ItemUpkeep implements Mechanic<ItemList> {

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {
        Set<ItemStack> upkeep = configContext.configItems.stream().map(ItemList.ConfigItem::toItemStack).collect(Collectors.toSet());


        return TownyCivs.injector.getInstance(StructureInventoryService.class)
                .checkUpkeep(townContext, upkeep);

    }

    @Override
    public void postAction(TownContext townContext, ItemList configContext) {
        Set<ItemStack> upkeep = configContext.configItems.stream().map(ItemList.ConfigItem::toItemStack).collect(Collectors.toSet());

        TownyCivs.injector.getInstance(StructureInventoryService.class)
                .processUpkeep(townContext.loadedStructure, upkeep);
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
