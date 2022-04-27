package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.model.VirtualInventory;

import java.util.List;

public class ItemUpkeep implements Mechanic<ItemList> {

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {
        List<VirtualInventory> storage = townContext.loadedStructure.storage;
        if (storage == null) {
            return false;
        }
        for (VirtualInventory virtualInventory : storage) {
            if (virtualInventory.content == null) {
                continue;
            }

        }
        return true;
    }


    @Override
    public void postAction(TownContext townContext, ItemList configContext) {
        Mechanic.super.postAction(townContext, configContext);
    }

    @Override
    public ItemList getNew() {
        return new ItemList();
    }
}
