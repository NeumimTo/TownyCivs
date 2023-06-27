package cz.neumimto.towny.townycivs.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.object.Translatable;
import cz.neumimto.towny.townycivs.ItemService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.gui.api.GuiCommand;
import cz.neumimto.towny.townycivs.gui.api.GuiConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


        ItemStack itemStack = new ItemStack(Material.PAPER);
        String translated = Translatable.of("toco_lore_structure_info_help").forLocale(commandSender);

        List<Component> components = Stream.of(translated.split(":n")).map(m -> MiniMessage.miniMessage().deserialize(m)).collect(Collectors.toList());
        itemStack.editMeta(itemMeta -> {
            itemMeta.lore(components);
            itemMeta.displayName(Component.empty());
        });
        map.put("Info", List.of(new GuiCommand(itemStack)));

        return map;
    }
}
