package cz.neumimto.towny.townycivs.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.StructureService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.gui.api.GuiCommand;
import cz.neumimto.towny.townycivs.gui.api.GuiConfig;
import cz.neumimto.towny.townycivs.model.StructureAndCount;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class StructuresGui extends TCGui {

    @Inject
    private StructureService structureService;

    @Inject
    private ConfigurationService configurationService;

    public StructuresGui() {
        super("Structures.conf", TownyCivs.INSTANCE.getDataFolder().toPath());
    }

    @Override
    protected String getTitle(CommandSender commandSender, GuiConfig guiConfig, String param) {
        Town town = TownyAPI.getInstance().getResident((Player) commandSender).getTownOrNull();
        return town.getName() + " - " + param;
    }

    public void display(Player player) {
        ChestGui chestGui = loadGui(player, "");

        chestGui.show(player);
    }

    @Override
    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param, GuiConfig guiConfig) {
        Town town = TownyAPI.getInstance().getResident((Player) commandSender).getTownOrNull();
        Map<String, List<GuiCommand>> map = new HashMap<>();

        List<StructureAndCount> structures = structureService.findTownStructures(town);

        for (StructureAndCount sc : structures) {
            ItemStack itemStack = structureService.toItemStack(sc.structure, sc.count);
            map.put("Structures", List.of(new GuiCommand(itemStack, "townycivs structure " + sc.structure.id)));
        }

        return map;
    }
}
