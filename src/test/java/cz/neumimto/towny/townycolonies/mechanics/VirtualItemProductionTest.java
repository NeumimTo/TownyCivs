package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class VirtualItemProductionTest {

    @Test
    void loadedstructure_null_containers() {

        TownContext townContext = new TownContext();
        townContext.loadedStructure = new LoadedStructure();
        townContext.loadedStructure.containers = null;

        ItemList itemList = new ItemList();
        itemList.configItems = new ArrayList<>();
        itemList.configItems.add(new ItemList.ConfigItem() {{
            material = Material.CACTUS.name();
            amount = 10;
        }});

        VirtualItemProduction production = new VirtualItemProduction();
        production.postAction(townContext, itemList);
    }

}