package cz.neumimto.towny.townycivs.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import cz.neumimto.towny.townycivs.StructureService;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.gui.*;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@CommandAlias("townycivs|toco|tco|toc")
public class StructureCommands extends BaseCommand {

    @Inject
    private StructuresGui structuresGui;

    @Inject
    private StructureGui structureGui;

    @Inject
    private MainMenuGui mainMenuGui;

    @Inject
    private HelpMenu1Gui helpMenu1Gui;

    @Inject
    private HelpMenu2Gui helpMenu2Gui;

    @Inject
    private StructureService structureService;

    @Inject
    private ConfigurationService configurationService;

    @CommandPermission("townycivs.admin")
    @Subcommand("reload")
    public void reload() {
        TownyCivs.INSTANCE.onEnable();
    }

    @Default
    @CommandPermission("townycivs.commands.common.mainmenu")
    public void mainMenuCommand(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        mainMenuGui.display(player, resident.getTownOrNull().getName());
    }

    @Subcommand("structures|ss")
    @CommandPermission("townycivs.commands.common.mainmenu")
    public void structures(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        structuresGui.display(player);
    }

    @Subcommand("structure|s")
    @CommandPermission("townycivs.commands.common.mainmenu")
    public void structureInfo(Player player, String structureId) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        structureGui.display(player, structureId);
    }

    @HelpCommand
    @CommandPermission("townycivs.commands.common.mainmenu")
    public void displayHelp(Player player, @Default(value = "1") int page) {
        if (page == 1) {
            helpMenu1Gui.display(player);
        } else if (page == 2) {
            helpMenu2Gui.display(player);
        }
    }

}
