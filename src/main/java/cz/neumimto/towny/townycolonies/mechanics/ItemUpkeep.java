package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.model.VirtualContent;

import java.util.List;

public class ItemUpkeep implements Mechanic<ItemList> {

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {
        List<VirtualContent> storage = townContext.loadedStructure.storage;
        if (storage == null) {
            return false;
        }
        for (VirtualContent virtualContent : storage) {
            if (virtualContent.content == null) {
                continue;
            }

        }
        return true;
    }


    @Override
    public ItemList getNew() {
        return new ItemList();
    }
}
