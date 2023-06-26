package cz.neumimto.towny.townycivs.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.object.Translatable;
import cz.neumimto.towny.townycivs.ItemService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.gui.api.GuiCommand;
import cz.neumimto.towny.townycivs.gui.api.GuiConfig;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class HelpMenu2Gui extends TCGui {

    public HelpMenu2Gui() {
        super("Help2.conf", TownyCivs.INSTANCE.getDataFolder().toPath());
    }

    public void display(Player player) {
        ChestGui chestGui = loadGui(player, "toco_menu_help_2");
        chestGui.show(player);
    }

    @Override
    protected String getTitle(CommandSender commandSender, GuiConfig guiConfig, String param) {
        return Translatable.of(param).forLocale(commandSender);
    }

    @Override
    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param, GuiConfig guiConfig) {
        Map<String, List<GuiCommand>> map = new HashMap<>();
        map.put("Recipe", List.of(new GuiCommand(new ItemStack(Material.PAPER)), new GuiCommand(new ItemStack(Material.WOODEN_SHOVEL))));
        map.put("Result", List.of(new GuiCommand(ItemService.getStructureTool())));
        return map;
    }
}
