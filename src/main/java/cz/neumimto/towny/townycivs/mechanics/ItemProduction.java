package cz.neumimto.towny.townycivs.mechanics;

import cz.neumimto.towny.townycivs.StructureInventoryService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.mechanics.common.ItemList;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ItemProduction implements Mechanic<ItemList> {


    @Override
    public String id() {
        return Mechanics.ITEM_PRODUCTION;
    }

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {
        return true;
    }

    @Override
    public void postAction(TownContext townContext, ItemList configContext) {

        Set<ItemStack> itemStackSet = new HashSet<>();
        for (ItemList.ConfigItem configItem : configContext.configItems) {
            ItemStack itemStack = configItem.toItemStack();
            itemStackSet.add(itemStack);
        }

        TownyCivs.injector.getInstance(StructureInventoryService.class).addItemProduction(townContext.loadedStructure, itemStackSet);
    }


    @Override
    public ItemList getNew() {
        return new ItemList();
    }
}
