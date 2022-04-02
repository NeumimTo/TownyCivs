package cz.neumimto.towny.townycolonies.gui;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import cz.neumimto.towny.townycolonies.StructureMetadata;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.gui.api.GuiCommand;
import cz.neumimto.towny.townycolonies.gui.api.GuiConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class BlueprintsGui extends TCGui {

    @Inject
    private StructureService structureService;

    @Inject
    private ConfigurationService configurationService;

    public BlueprintsGui() {
        super("Blueprints.conf", TownyColonies.INSTANCE.getDataFolder().toPath());
    }

    public void display(Player player) {
        ChestGui chestGui = loadGui(player, "Blueprints");
        chestGui.show(player);
    }

    @Override
    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param) {
        Town town = TownyAPI.getInstance().getResident((Player)commandSender).getTownOrNull();
        Map<String, List<GuiCommand>> map = new HashMap<>();

        StructureMetadata metadata = structureService.getMetadata(town);
        List<GuiCommand> list = new ArrayList<>();
        if (metadata != null) {
            Map<String, Integer> blueprints = metadata.getValue().blueprints;
            for (Map.Entry<String, Integer> stringIntegerEntry : blueprints.entrySet()) {
                Optional<Structure> structureById = configurationService.findStructureById(stringIntegerEntry.getKey());
                if (structureById.isPresent()) {
                    Structure structure = structureById.get();
                    ItemStack itemStack = structureService.structureToItemstack(structure, town, stringIntegerEntry.getValue());
                    list.add(new GuiCommand(itemStack, "townycolonies place " + structure.id));
                }
            }
        }
        map.put("Blueprint", list);
        return map;
    }
}
