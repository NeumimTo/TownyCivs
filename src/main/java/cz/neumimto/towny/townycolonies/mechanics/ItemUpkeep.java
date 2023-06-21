package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.StructureInventoryService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemUpkeep implements Mechanic<ItemList> {

    @Override
    public boolean check(TownContext townContext, ItemList configContext) {

        Set<ItemStack> upkeep = configContext.configItems.stream().map(ItemList.ConfigItem::toItemStack).collect(Collectors.toSet());

        TownyColonies.injector.getInstance(StructureInventoryService.class)
                .upkeep(townContext.town, townContext.loadedStructure, upkeep);


        return true;
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
