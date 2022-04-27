package cz.neumimto.towny.townycolonies.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.*;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.gui.api.GuiCommand;
import cz.neumimto.towny.townycolonies.gui.api.GuiConfig;
import cz.neumimto.towny.townycolonies.model.Region;
import cz.neumimto.towny.townycolonies.model.StructureAndCount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class RegionGui extends TCGui {

    @Inject
    private SubclaimService subclaimService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureService structureService;

    @Inject
    private ManagementService managementService;

    public RegionGui() {
        super("Region.conf", TownyColonies.INSTANCE.getDataFolder().toPath());
    }

    public void display(Player player, Region region) {
        ChestGui chestGui = loadGui(player, region.uuid.toString());
        chestGui.show(player);
    }

    @Override
    protected String getTitle(CommandSender commandSender, GuiConfig guiConfig, String param) {
        Town town = TownyAPI.getInstance().getResident((Player) commandSender).getTownOrNull();

        Region region = subclaimService.getRegion(UUID.fromString(param));
        Structure structureById = configurationService.findStructureById(region.structureId).get();


        return town.getPrefix() + " " + town.getName() + " - " + structureById.name;
    }

    @Override
    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param) {
        Player player = (Player) commandSender;
        Town town = TownyAPI.getInstance().getResident(player).getTownOrNull();
        Map<String, List<GuiCommand>> map = new HashMap<>();

        Region region = subclaimService.getRegion(UUID.fromString(param));


        StructureAndCount count = structureService.findTownStructureById(town, region.loadedStructure.structureDef);
        ItemStack structureInfoStack = structureService.toItemStack(region.loadedStructure.structureDef, count.count);

        map.put("Structure", List.of(new GuiCommand(structureInfoStack, e -> e.setCancelled(true))));

        MiniMessage mm = MiniMessage.miniMessage();
        ItemStack editMode = new ItemStack(managementService.isBeingEdited(region.loadedStructure) ? Material.RED_WOOL : Material.GREEN_WOOL);
        editMode.editMeta(itemMeta -> {
            var lore = new ArrayList<Component>();

            String editModeS = null;
            if (managementService.isBeingEdited(region.loadedStructure)) {
                editModeS = "<red>Active<red>";
            } else {
                editModeS = "<green>Inactive<green>";
            }
            itemMeta.displayName(mm.deserialize("<gold>Edit mode</gold> : " +editModeS));

            lore.add(Component.empty());
            lore.add(mm.deserialize("<red>Active<red><white>- structure is disabled & its region may be edited</white>"));
            lore.add(mm.deserialize("<green>Inactive<green><white>- structure is enabled & its region may not be edited</white>"));
            itemMeta.lore(lore);
        });
        map.put("EditModeToggle", List.of(new GuiCommand(editMode, e->{
            e.setCancelled(true);
            boolean prev = managementService.isBeingEdited(region.loadedStructure);
            managementService.toggleEditMode(region.loadedStructure, (Player) e.getWhoClicked());
            if (prev == managementService.isBeingEdited(region.loadedStructure)) {
                e.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                return;
            }

            display((Player) e.getWhoClicked(), region);
        })));

        ItemStack delete = new ItemStack(Material.BARRIER);
        delete.editMeta(itemMeta -> {
            itemMeta.displayName(mm.deserialize("<red>Delete</red>"));
        });
        map.put("Delete", List.of(new GuiCommand(delete, e->{
            structureService.delete(region, (Player)e.getWhoClicked());
            e.setCancelled(true);
            player.getOpenInventory().close();
        })));


        ItemStack remBlocks = new ItemStack(Material.IRON_AXE);
        remBlocks.editMeta(itemMeta -> {
            itemMeta.displayName(mm.deserialize("<yellow>Remaining build requirements</yellow>"));
            itemMeta.addItemFlags(ItemFlag.values());
        });
        map.put("RemainingBlocks", List.of(new GuiCommand(remBlocks, e ->{
            e.setCancelled(true);
            ChestGui chestGui = remainingBlocksGui(region);
            chestGui.show(e.getWhoClicked());
        })));

        return map;
    }

    @NotNull
    private ChestGui remainingBlocksGui(Region region) {
        MiniMessage mm = MiniMessage.miniMessage();
        ChestGui chestGui = new ChestGui(6, "Remaining Blocks");

        StaticPane staticPane = new StaticPane(9, 6);
        chestGui.addPane(staticPane);
        int x = 0;
        int y = 0;
        Map<String, Integer> requirements = region.loadedStructure.structureDef.blocks;

        for (Map.Entry<String, Integer> entry : subclaimService.remainingBlocks(region).entrySet()) {
            String key = entry.getKey();
            Integer remaining = entry.getValue();
            if (key.startsWith("tc:")) {
                if (remaining <= 0) {
                    continue;
                }
                ItemStack itemStack = new ItemStack(Material.BUNDLE);
                itemStack.editMeta(itemMeta -> {
                    String group = key.substring(3);
                    Integer req = requirements.get(key);
                    int current = req - remaining;
                    itemMeta.displayName(mm.deserialize(group + " - " + current + "/"+req+"x"));
                    BundleMeta bundleMeta = (BundleMeta) itemMeta;

                    Collection<Material> blockGroup = Materials.getMaterials(key);
                    for (Material material : blockGroup) {
                        bundleMeta.addItem(new ItemStack(material));
                    }
                });
                staticPane.addItem(new GuiItem(itemStack, ice -> ice.setCancelled(true)), x, y);
            } else if (key.startsWith("!tc:")) {
                    if (remaining == 0) {
                        continue;
                    }
                    ItemStack itemStack = new ItemStack(Material.BUNDLE);
                    itemStack.editMeta(itemMeta -> {
                        String group = key.substring(4);
                        Integer req = requirements.get(key);
                        int current = req - remaining;
                        itemMeta.displayName(mm.deserialize(group + " " + current+ "/ exactly " + req + "x"));
                        BundleMeta bundleMeta = (BundleMeta) itemMeta;

                        Collection<Material> blockGroup = Materials.getMaterials(key.substring(1));
                        for (Material material : blockGroup) {
                            bundleMeta.addItem(new ItemStack(material));
                        }
                    });
                    staticPane.addItem(new GuiItem(itemStack, ice -> ice.setCancelled(true)), x, y);
            } else {
                if (remaining <= 0) {
                    continue;
                }
                Material material = Material.matchMaterial(entry.getKey());
                ItemStack itemStack = new ItemStack(material);
                itemStack.editMeta(itemMeta -> {
                    Integer req = requirements.get(key);
                    int current = req - remaining;
                    itemMeta.displayName(mm.deserialize(material.name() + " - " + current + "/"+req+"x"));
                });
                staticPane.addItem(new GuiItem(itemStack, ice -> ice.setCancelled(true)),x,y);
            }
            x++;
            if (x == 8) {
                x = 0;
                y++;
            }
        }
        return chestGui;
    }

}