package cz.neumimto.towny.townycolonies.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenuGui {


    public static void display(Player player) {
        try {
            Town town = TownyAPI.getInstance().getResident(player).getTown();
            ChestGui gui = new ChestGui(6, town.getName());
            StaticPane pane = new StaticPane(0,0,9,6);
            ItemStack itemStack = new ItemStack(Material.IRON_PICKAXE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(Component.text("structures"));
            pane.addItem(new GuiItem(itemStack, e -> {
                Player pl = (Player) e.getWhoClicked();
                Bukkit.dispatchCommand(pl, "toco structures");
            }), 0, 0);
            gui.show(player);
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }
}
