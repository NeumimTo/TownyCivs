package cz.neumimto.towny.townycivs.gui.api;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.hocon.HoconParser;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class ConfigurableGui {

    static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private final String fileName;
    private Path workingDir;
    private GuiConfig guiConfig;

    public ConfigurableGui(String fileName, Path workingDir) {
        this.fileName = fileName;
        this.workingDir = workingDir;
    }

    public ChestGui loadGui() {
        return loadGui(null, null);
    }

    public ChestGui loadGui(Player commandSender) {
        return loadGui(commandSender, null);
    }

    public ChestGui loadGui(Player commandSender, String param) {
        Path path = getPath();

        if (guiConfig == null) {
            reloadGuiConfig(path);
        }

        return createPane(guiConfig, commandSender, getPaneData(commandSender, param, guiConfig), param);
    }

    public void reloadGuiConfig() {
        reloadGuiConfig(getPath());
    }

    protected abstract String getAssetAsString(String guiFileName);

    private void reloadGuiConfig(Path path) {
        if (!Files.exists(path)) {
            String assetAsString = getAssetAsString("gui/" + fileName);

            HoconParser hoconParser = new HoconParser();
            try (StringReader stringReader = new StringReader(assetAsString)) {
                CommentedConfig parsed = hoconParser.parse(stringReader);
                guiConfig = new ObjectConverter().toObject(parsed, GuiConfig::new);
            }
        } else {
            try (FileConfig fileConfig = FileConfig.of(path)) {
                fileConfig.load();
                guiConfig = new ObjectConverter().toObject(fileConfig, GuiConfig::new);
            }
        }
    }

    protected abstract String translateKey(CommandSender commandSender, String translationKey);

    protected String getTitle(CommandSender commandSender, GuiConfig guiConfig, String param) {
        if (guiConfig.translationkey != null) {
            return getPrefix(guiConfig) + translateKey(commandSender, guiConfig.translationkey);
        } else {
            return getPrefix(guiConfig) + guiConfig.name;
        }
    }

    protected String getPrefix(GuiConfig guiConfig) {
        return guiConfig.prefix == null ? "" : ChatColor.WHITE + guiConfig.prefix;
    }

    protected ChestGui createPane(GuiConfig guiConfig, CommandSender commandSender, Map<String, List<GuiCommand>> data, String param) {
        String title = getTitle(commandSender, guiConfig, param);
        ChestGui chestGui = new ChestGui(6, title);

        int alphabetidx = 0;
        List<String> actualContent = new ArrayList<>();

        Map<Character, GuiCommand> mask = new HashMap<>();

        Map<String, Iterator<GuiCommand>> dataIt = toIterator(data);
        for (String row : guiConfig.inventory) {
            StringBuilder stringBuilder = new StringBuilder();
            for (char slot : row.toCharArray()) {

                a:
                for (Map.Entry<String, Iterator<GuiCommand>> content : dataIt.entrySet()) {
                    String replaceKey = content.getKey(); //classType
                    Iterator<GuiCommand> value = content.getValue();
                    for (GuiConfig.MaskConfig maskConfig : guiConfig.mask) {
                        if (maskConfig.C.toCharArray()[0] == slot && replaceKey.equalsIgnoreCase(maskConfig.supplier) && value.hasNext()) {
                            GuiCommand next = value.next();
                            char c = alphabet[alphabetidx];
                            mask.put(c, next);
                            alphabetidx++;
                            slot = c;
                            break a;
                        }
                    }
                }
                stringBuilder.append(slot);
            }

            actualContent.add(stringBuilder.toString());
        }


        PatternPane pane = new PatternPane(9, 6, new Pattern(
                actualContent.toArray(new String[0])
        ));

        for (Map.Entry<Character, GuiCommand> e : mask.entrySet()) {
            pane.bindItem(e.getKey(), e.getValue());
        }

        for (GuiConfig.MaskConfig maskConfig : guiConfig.mask) {
            GuiConfig.OnClick onClick = maskConfig.onClick;
            char maskKez = maskConfig.C.toCharArray()[0];
            if (onClick != null && onClick.command != null) {
                ItemStack item = i(commandSender, maskConfig);
                if (maskConfig.tags != null) {
                    for (String tag : maskConfig.tags) {
                        handleTag(tag, commandSender, item);
                    }
                }

                if (commandSender == null) {
                    pane.bindItem(maskKez, new GuiCommand(i(null, maskConfig), onClick.command.replaceAll("%ui_param%", param)));
                } else {
                    pane.bindItem(maskKez, new GuiCommand(i(commandSender, maskConfig), onClick.command.replaceAll("%ui_param%", param), commandSender));
                }
            } else {
                if (!maskConfig.id.toLowerCase().contains("minecraft:air")) {
                    pane.bindItem(maskKez, new GuiCommand(i(commandSender, maskConfig)));
                }
            }
        }
        chestGui.addPane(pane);
        return chestGui;
    }

    protected void handleTag(String tag, CommandSender commandSender, ItemStack item) {

    }

    public void initialize() {
    }

    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param, GuiConfig guiConfig) {
        return getPaneData(commandSender, param);
    }

    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender, String param) {
        return getPaneData(commandSender);
    }

    public Map<String, List<GuiCommand>> getPaneData(CommandSender commandSender) {
        return Collections.emptyMap();
    }

    private Map<String, Iterator<GuiCommand>> toIterator(Map<String, List<GuiCommand>> b) {
        Map<String, Iterator<GuiCommand>> a = new HashMap<>();
        for (Map.Entry<String, List<GuiCommand>> c : b.entrySet()) {
            a.put(c.getKey(), c.getValue().iterator());
        }
        return a;
    }

    public void install() {
        Path path = getPath();
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //assetService.copyToFile("gui/" + fileName, getPath());
    }

    private Path getPath() {
        return workingDir.resolve("guis/" + fileName);
    }

    protected ItemStack i(CommandSender commandSender, GuiConfig.MaskConfig maskConfig) {
        ItemStack itemStack = new ItemStack(Material.matchMaterial(maskConfig.id));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (maskConfig.model != null) {
            itemMeta.setCustomModelData(maskConfig.model);
        }
        if (maskConfig.translationKey != null) {

            itemMeta.setDisplayName(translateKey(commandSender, maskConfig.translationKey));
        } else {
            itemMeta.setDisplayName(" ");
        }
        itemMeta.addItemFlags(ItemFlag.values());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void clearCache() {
        guiConfig = null;
    }

    public void clearCache(UUID uuid) {

    }

    public String getFileName() {
        return fileName;
    }
}
