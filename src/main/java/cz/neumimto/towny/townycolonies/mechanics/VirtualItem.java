package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.ItemList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class VirtualItem {

    public static final VirtualItem empty_slot = new VirtualItem("X");

    public final String data;

    public VirtualItem(String data) {
        this.data = data;
    }

    public static String toVirtualItem(ItemList.ConfigItem configItem) {
        String result;

        result = configItem.material.toLowerCase(Locale.ROOT);
        if (configItem.customModelData != null) {
            result += ";cmd=" + configItem.customModelData;
        }

        return result;
    }

    public static VirtualItem toVirtualItem(ItemStack itemStack) {
        Map<String, Object> serialize = itemStack.serialize();
        String data = new String(itemStack.serializeAsBytes());
        return new VirtualItem(data);
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

    public ItemStack toItemStack() {
        return ItemStack.deserializeBytes(data.getBytes(StandardCharsets.UTF_8));
    }
}
