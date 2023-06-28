package cz.neumimto.towny.townycivs.mechanics;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.Towny;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.Structure;
import cz.neumimto.towny.townycivs.mechanics.common.ItemList;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import net.milkbowl.vault.Vault;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

public class ItemProductionTest {

    TownyCivs plugin;

    World world;

    Location location;

    private static ServerMock server;

    @Test
    public void prepareMock() {
        server = MockBukkit.mock();
        Vault vault = MockBukkit.load(Vault.class);
        Assertions.assertNotNull(vault);
        Towny towny = MockBukkit.load(Towny.class);
        Assertions.assertNotNull(towny);
    }

    //@BeforeEach
    public void before() {
        world = server.addSimpleWorld("test");

        location = new Location(world, 1, 1, 1);
        world.setBlockData(location, new BlockDataMock(Material.BARREL));
    }

    @Test
    public void loadedstructure_null_containers() {

        TownContext townContext = new TownContext();
        townContext.loadedStructure = new LoadedStructure(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "structureId",
                location,
                new Structure()
        );

        ItemList itemList = new ItemList();
        itemList.configItems = new ArrayList<>();
        itemList.configItems.add(new ItemList.ConfigItem() {{
            material = Material.CACTUS.name();
            amount = 10;
        }});

        ItemProduction production = new ItemProduction();
        production.postAction(townContext, itemList);
    }

    //@Test
    public void loadedstructure_production_ok() {

        TownContext townContext = new TownContext();
        townContext.loadedStructure = new LoadedStructure(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "structureId",
                location,
                new Structure()
        );


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