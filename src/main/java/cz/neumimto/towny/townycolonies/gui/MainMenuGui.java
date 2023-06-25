package cz.neumimto.towny.townycolonies.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.gui.api.GuiConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Singleton;

@Singleton
public class MainMenuGui extends TCGui {

    public MainMenuGui() {
        super("Main.conf", TownyColonies.INSTANCE.getDataFolder().toPath());
    }

    @Override
    protected String getTitle(CommandSender commandSender, GuiConfig guiConfig, String param) {
        return "TownyColonies";
    }

    public void display(Player player, String townName) {
        ChestGui chestGui = loadGui(player, townName);
        chestGui.show(player);
    }
}
