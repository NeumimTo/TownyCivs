package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class VirtualItem {

    public static final String empty_slot = "X";

    public static String toVirtualItem(ItemList.ConfigItem configItem) {
        String result;

        result = configItem.material.toLowerCase(Locale.ROOT);
        if (configItem.customModelData != null) {
            result += ";cmd=" + configItem.customModelData;
        }

        return result;
    }

    public static String toVirtualItemFilter(ItemStack item) {
        String result = item.getType().getKey().getKey();
        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta.hasCustomModelData()) {
                result += ";cmd=" + itemMeta.getCustomModelData();
            }
        }
        return result;
    }
}
