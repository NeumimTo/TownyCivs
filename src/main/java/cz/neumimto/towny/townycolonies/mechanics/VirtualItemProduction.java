package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import cz.neumimto.towny.townycolonies.model.VirtualContent;
import org.bukkit.Material;

import java.util.Map;

public class VirtualItemProduction implements Mechanic<ItemList> {

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {
        return true;
    }

    @Override
    public void postAction(TownContext townContext, ItemList configContext) {

        for (ItemList.ConfigItem configItem : configContext.configItems) {
            String item = VirtualItem.toVirtualItem(configItem);
            int amount = configItem.amount;
            if (townContext.loadedStructure.containers != null) {
                VirtualContainer virtualContainer = null;

                int i = 0;
                for (VirtualContainer container : townContext.loadedStructure.containers) {
                    VirtualContent virtualContent = townContext.loadedStructure.storage.get(i);

                    if (container.full) {
                        continue;
                    }

                    if (container.inputFilter != null) {
                        boolean consumes = container.inputFilter.isEmpty() || container.inputFilter.stream().anyMatch(a->a.equals(item));
                        if (consumes) {
                            virtualContainer = container;

                            Material material = Material.matchMaterial(configItem.material);
                            int maxStackSize = material.getMaxStackSize();
                            for (VirtualItem entry : virtualContent.content) {

                                //if (entry.getKey().equals(VirtualItem.empty_slot)) {
                                //    while (amount != 0) {
                                //        //todo use iterator and remove the key
                                //        entry.setValue(entry.getValue() + 1);
                                //        if (entry.getValue() == maxStackSize) {
                                //            continue;
                                //        }
                                //        amount=-1;
                                //    }
                                //    continue;
                                //}
                                //
                                //if (entry.getKey().equals(item)) {
                                //    if (entry.getValue() == maxStackSize) {
                                //        continue;
                                //    }
                                //    while (amount != 0) {
                                //        entry.setValue(entry.getValue() + 1);
                                //        if (entry.getValue() == maxStackSize) {
                                //            continue;
                                //        }
                                //        amount=-1;
                                //    }
                                //}

                            }
                        }
                        break;
                    }
                    virtualContainer = container;
                    i++;
                }

            } else {

            }

        }

    }

    @Override
    public ItemList getNew() {
        return new ItemList();
    }
}
