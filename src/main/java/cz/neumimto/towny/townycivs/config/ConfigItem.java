package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ConfigItem {

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

    private ItemStack cache;

    public ItemStack toItemStack() {
        if (cache != null) {
            return cache.clone();
        }
        cache = new ItemStack(Material.matchMaterial(material), amount == null ? 1 : amount);

        cache.editMeta(itemMeta -> {
            if (customModelData != null) {
                itemMeta.setCustomModelData(customModelData);
            }
            if (customName != null) {
                itemMeta.displayName(MiniMessage.miniMessage().deserialize(customName));
            }
        });
        return cache;
    }
}
