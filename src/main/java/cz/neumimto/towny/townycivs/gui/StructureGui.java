package cz.neumimto.towny.townycivs.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.StructureService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.config.Structure;
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
import java.util.Optional;

@Singleton
public class StructureGui extends TCGui {

    @Inject
    private StructureService structureService;

    @Inject
    private ConfigurationService configurationService;

    public StructureGui() {
        super("Region.conf", TownyCivs.INSTANCE.getDataFolder().toPath());
    }

    @Override
    protected String getTitle(CommandSender commandSender, GuiConfig guiConfig, String param) {
        Town town = TownyAPI.getInstance().getResident((Player) commandSender).getTownOrNull();
        return town.getName() + " - " + param;
    }

    public void display(Player player, String structureId) {
        ChestGui chestGui = loadGui(player, structureId);
        chestGui.show(player);
    }

    @Override
    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param, GuiConfig guiConfig) {
        Town town = TownyAPI.getInstance().getResident((Player) commandSender).getTownOrNull();
        Map<String, List<GuiCommand>> map = new HashMap<>();

        Optional<Structure> structureById = configurationService.findStructureById(param);
        if (structureById.isPresent()) {
            StructureAndCount sc = structureService.findTownStructureById(town, structureById.get());
            ItemStack itemStack = structureService.toItemStack(sc.structure, sc.count);
            map.put("Structure", List.of(new GuiCommand(itemStack)));
        }
        return map;
    }
}
