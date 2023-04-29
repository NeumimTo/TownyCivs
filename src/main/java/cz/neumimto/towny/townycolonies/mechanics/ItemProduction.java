package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.StructureInventoryService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ItemProduction implements Mechanic<ItemList> {

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {
        return true;
    }

    @Override
    public void postAction(TownContext townContext, ItemList configContext) {

        Set<ItemStack> itemStackSet = new HashSet<>();
        for (ItemList.ConfigItem configItem : configContext.configItems) {
            ItemStack itemStack = new ItemStack(Material.matchMaterial(configItem.material), configItem.amount);

            itemStack.editMeta(itemMeta -> {
                if (configItem.customModelData != null) {
                    itemMeta.setCustomModelData(configItem.customModelData);
                }
                if (configItem.customName != null) {
                    itemMeta.displayName(MiniMessage.miniMessage().deserialize(configItem.customName));
                }
            });
            itemStackSet.add(itemStack);
        }

        TownyColonies.injector.getInstance(StructureInventoryService.class).addItemProduction(townContext.town, townContext.loadedStructure, itemStackSet);
    }


    @Override
    public ItemList getNew() {
        return new ItemList();
    }
}
