package cz.neumimto.towny.townycivs.gui.api;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import cz.neumimto.towny.townycivs.TownyCivs;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class GuiCommand extends GuiItem {

    public GuiCommand(@NotNull ItemStack item, @NotNull String command, @NotNull CommandSender viewer) {
        super(item, e -> {
            e.setCancelled(true);
            TownyCivs.MORE_PAPER_LIB.scheduling().entitySpecificScheduler((Entity) viewer)
                    .run(() -> Bukkit.dispatchCommand(viewer, command), null);
        });
    }

    public GuiCommand(@NotNull ItemStack item, @NotNull String command) {
        super(item, e -> {
            e.setCancelled(true);
            HumanEntity whoClicked = e.getWhoClicked();
            TownyCivs.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(whoClicked)
                    .run(() -> Bukkit.dispatchCommand(whoClicked, command), null);
        });
    }

    public GuiCommand(@NotNull ItemStack item) {
        super(item, e -> e.setCancelled(true));
    }

    public GuiCommand(@NotNull ItemStack item, Consumer<InventoryClickEvent> listener) {
        super(item, listener);
    }
}
