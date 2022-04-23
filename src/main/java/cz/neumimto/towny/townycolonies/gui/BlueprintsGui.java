package cz.neumimto.towny.townycolonies.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.ManagementService;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.gui.api.GuiCommand;
import cz.neumimto.towny.townycolonies.gui.api.GuiConfig;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class BlueprintsGui extends TCGui {

    @Inject
    private StructureService structureService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ManagementService managementService;

    public BlueprintsGui() {
        super("BuyBlueprints.conf", TownyColonies.INSTANCE.getDataFolder().toPath());
    }

    public void display(Player player) {
        ChestGui chestGui = loadGui(player, "Blueprints");
        chestGui.show(player);
    }

    @Override
    protected String getTitle(CommandSender commandSender, GuiConfig guiConfig, String param) {
        Town town = TownyAPI.getInstance().getResident((Player) commandSender).getTownOrNull();
        return town.getPrefix() + " " + town.getName() + " - " + param;
    }

    @Override
    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param) {
        Player player = (Player) commandSender;
        Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();
        Map<String, List<GuiCommand>> map = new HashMap<>();

        List<GuiCommand> list = new ArrayList<>();

        for (Structure structure : configurationService.getAll()) {

            TownContext townContext = new TownContext();
            townContext.town = town;
            townContext.resident = TownyAPI.getInstance().getResident(player);
            townContext.player = player;
            townContext.structure = structure;

            if (structureService.canBuy(townContext)) {
                int buildCount = structureService.findTownStructureById(town, townContext.structure).count;
                ItemStack itemStack = structureService.toItemStack(townContext.structure, buildCount);
                list.add(new GuiCommand(itemStack, event -> {
                    event.setCancelled(true);
                    if (structureService.canBuy(townContext)) {
                        ItemStack clone = structureService.buyBlueprint(townContext);
                        HumanEntity whoClicked = event.getWhoClicked();
                        HashMap<Integer, ItemStack> noFitItems = whoClicked.getInventory().addItem(clone);
                        for (Map.Entry<Integer, ItemStack> entry : noFitItems.entrySet()) {
                            whoClicked.getLocation().getWorld().dropItemNaturally(whoClicked.getLocation(), entry.getValue());
                        }
                    }
                }));
            }
        }

        map.put("Blueprint", list);
        return map;
    }

}
