package cz.neumimto.towny.townycivs.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.ManagementService;
import cz.neumimto.towny.townycivs.StructureService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.config.Structure;
import cz.neumimto.towny.townycivs.gui.api.GuiCommand;
import cz.neumimto.towny.townycivs.gui.api.GuiConfig;
import cz.neumimto.towny.townycivs.mechanics.TownContext;
import cz.neumimto.towny.townycivs.model.ActionResult;
import net.kyori.adventure.text.Component;
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
        super("BuyBlueprints.conf", TownyCivs.INSTANCE.getDataFolder().toPath());
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


            int buildCount = structureService.findTownStructureById(town, townContext.structure).count;
            ItemStack itemStack = structureService.toItemStack(townContext.structure, buildCount);
            list.add(new GuiCommand(itemStack, event -> {
                event.setCancelled(true);
                ActionResult actionResult = structureService.checkBuyRequirements(player, town, structure);
                if (actionResult.isOk()) {
                    ItemStack clone = structureService.buyBlueprint(townContext);
                    HumanEntity whoClicked = event.getWhoClicked();
                    HashMap<Integer, ItemStack> noFitItems = whoClicked.getInventory().addItem(clone);
                    for (Map.Entry<Integer, ItemStack> entry : noFitItems.entrySet()) {
                        whoClicked.getLocation().getWorld().dropItemNaturally(whoClicked.getLocation(), entry.getValue());
                    }
                } else {
                    if (player.getOpenInventory() != null) {
                        player.getOpenInventory().close();
                    }
                    for (Component component : actionResult.msg()) {
                        player.sendMessage(component);
                    }
                }
            }));

        }

        map.put("Blueprint", list);
        return map;
    }

}
