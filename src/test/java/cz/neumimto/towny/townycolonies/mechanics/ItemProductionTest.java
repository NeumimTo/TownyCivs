package cz.neumimto.towny.townycolonies.mechanics;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyUniverse;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import net.milkbowl.vault.Vault;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ItemProductionTest {

    TownyColonies plugin;

    World world;

    Location location;

    private static ServerMock server;

    @BeforeAll
    public static void prepareMock() {
        server = MockBukkit.mock();
        MockBukkit.load(Vault.class);
        MockBukkit.load(Towny.class);

    }

    @BeforeEach
    public void before() {
        plugin = MockBukkit.load(TownyColonies.class);
        world = server.addSimpleWorld("test");
        location = new Location(world, 1, 1, 1);
        world.setBlockData(location, new BlockDataMock(Material.BARREL));
    }

    @Test
    public void loadedstructure_null_containers() {

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
    public void loadedstructure_production_ok() {

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