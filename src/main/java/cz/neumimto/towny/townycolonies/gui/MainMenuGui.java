package cz.neumimto.towny.townycolonies.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.TownyUniverse;
import cz.neumimto.towny.townycolonies.TownyColonies;
import org.bukkit.entity.Player;

import javax.inject.Singleton;

@Singleton
public class MainMenuGui extends TCGui {

    public MainMenuGui() {
        super("Main.conf", TownyColonies.INSTANCE.getDataFolder().toPath());
    }

    public void display(Player player, String townName) {
        ChestGui chestGui = loadGui(player, townName);
        chestGui.show(player);
    }
}
