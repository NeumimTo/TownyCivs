package cz.neumimto.towny.townycolonies.mechanics.common;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemList implements Wrapper {

    @Path("Items")
    public List<ConfigItem> configItems = new ArrayList<>();

    public static class ConfigItem {

        @Path("Material")
        public String material;

        @Path("CustomModelData")
        @Optional
        public Integer customModelData;

        @Path("CustomName")
        @Optional
        public String customName;

        @Path("Amount")
        @Optional
        public Integer amount;

        @Path("Fuel")
        @Optional
        public Integer fuel;

        public ItemStack toItemStack() {
            var itemStack = new ItemStack(Material.matchMaterial(material), amount);

            itemStack.editMeta(itemMeta -> {
                if (customModelData != null) {
                    itemMeta.setCustomModelData(customModelData);
                }
                if (customName != null) {
                    itemMeta.displayName(MiniMessage.miniMessage().deserialize(customName));
                }
            });
            return itemStack;
        }
    }

    @Override
    public boolean isObject() {
        return true;
    }
}
