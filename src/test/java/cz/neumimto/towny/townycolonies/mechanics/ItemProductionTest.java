package cz.neumimto.towny.townycolonies.mechanics;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class ItemProductionTest {

    World world;

    Location location;

    @BeforeEach
    public void prepareMock() {
        ServerMock server = MockBukkit.mock();

        world = server.addSimpleWorld("test");
        location = new Location(world, 1, 1, 1);
        world.setBlockData(location, new BlockDataMock(Material.BARREL));
    }

    @Test
    void loadedstructure_null_containers() {

        TownContext townContext = new TownContext();
        townContext.loadedStructure = new LoadedStructure();

        ItemList itemList = new ItemList();
        itemList.configItems = new ArrayList<>();
        itemList.configItems.add(new ItemList.ConfigItem() {{
            material = Material.CACTUS.name();
            amount = 10;
        }});

        ItemProduction production = new ItemProduction();
        production.postAction(townContext, itemList);
    }

    @Test
    void loadedstructure_production_ok() {

        TownContext townContext = new TownContext();
        townContext.loadedStructure = new LoadedStructure();

        ItemList itemList = new ItemList();
        itemList.configItems = new ArrayList<>();
        itemList.configItems.add(new ItemList.ConfigItem() {{
            material = Material.CACTUS.name();
            amount = 10;
        }});

        ItemProduction production = new ItemProduction();
        production.postAction(townContext, itemList);

    }
}