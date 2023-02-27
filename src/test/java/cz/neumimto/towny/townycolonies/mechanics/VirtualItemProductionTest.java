package cz.neumimto.towny.townycolonies.mechanics;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import cz.neumimto.towny.townycolonies.model.VirtualContent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VirtualItemProductionTest {

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

    @Test
    void loadedstructure_production_ok() {

        TownContext townContext = new TownContext();
        townContext.loadedStructure = new LoadedStructure();
        townContext.loadedStructure.containers = new ArrayList<>();
        townContext.loadedStructure.storage = new ArrayList<>();

        townContext.loadedStructure.storage.add(VirtualContent.empty(32, UUID.randomUUID()));

        VirtualContainer vc = VirtualContainer.from(location.getBlock(), UUID.randomUUID(), Collections.emptyList());
        townContext.loadedStructure.containers.add(vc);

        ItemList itemList = new ItemList();
        itemList.configItems = new ArrayList<>();
        itemList.configItems.add(new ItemList.ConfigItem() {{
            material = Material.CACTUS.name();
            amount = 10;
        }});

        VirtualItemProduction production = new VirtualItemProduction();
        production.postAction(townContext, itemList);

        Assertions.assertFalse(townContext.loadedStructure.storage.isEmpty());
    }
}