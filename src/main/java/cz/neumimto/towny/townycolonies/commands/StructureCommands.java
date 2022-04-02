package cz.neumimto.towny.townycolonies.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.gui.BlueprintsGui;
import cz.neumimto.towny.townycolonies.gui.MainMenuGui;
import cz.neumimto.towny.townycolonies.gui.StructureGui;
import cz.neumimto.towny.townycolonies.gui.StructuresGui;
import org.bukkit.block.data.type.StructureBlock;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@CommandAlias("townycolonies|toco|tco|toc")
public class StructureCommands extends BaseCommand {

    @Inject
    private StructuresGui structuresGui;

    @Inject
    private StructureGui structureGui;

    @Inject
    private MainMenuGui mainMenuGui;

    @Inject
    private StructureService structureService;

    @Inject
    private BlueprintsGui blueprintsGui;

    @Default
    @CommandPermission("townycolonies.commands.common.mainmenu")
    public void mainMenuCommand(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        mainMenuGui.display(player);
    }

    @Subcommand("structures|ss")
    @CommandPermission("townycolonies.commands.common.mainmenu")
    public void structures(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        structuresGui.display(player);
    }

    @Subcommand("structure|s")
    @CommandPermission("townycolonies.commands.common.mainmenu")
    public void structureInfo(Player player, String structureId) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        structureGui.display(player, structureId);
    }

    @Subcommand("blueprints|bp")
    @CommandPermission("townycolonies.commands.common.mainmenu")
    public void placeStructure(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        blueprintsGui.display(player);
    }

    @Subcommand("buy|b")
    @CommandPermission("townycolonies.commands.mayor.buy")
    public void buy(Player player, String structureId) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        structureService.buyBlueprint(player, structureId);
    }

    @Subcommand("place|p")
    @CommandPermission("townycolonies.commands.architect.place")
    public void place(Player player, String structureId) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(Translatable.of("toco_town_required").forLocale(player));
            return;
        }
        structureService.placeBlueprint(player, structureId);
    }
}
