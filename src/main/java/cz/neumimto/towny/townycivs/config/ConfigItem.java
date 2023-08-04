package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigItem {

    @Path("Material")
    @Conversion(Structure.MaterialConversion.class)
    public Material material;

    @Path("CustomModelData")
    @Optional
    public Integer customModelData;

    @Path("CustomName")
    @Optional
    public String customName;

    @Path("Amount")
    @Optional
    public Integer amount;

    @Path("Lore")
    @Optional
    public List<String> lore = new ArrayList<>();

    private ItemStack cache;

    public ItemStack toItemStack() {
        if (cache != null) {
            return cache.clone();
        }
        cache = new ItemStack(material, amount == null ? 1 : amount);

        cache.editMeta(itemMeta -> {
            if (customModelData != null) {
                itemMeta.setCustomModelData(customModelData);
            }
            MiniMessage mm = MiniMessage.miniMessage();
            if (customName != null) {
                itemMeta.displayName(mm.deserialize(customName));
            }
            itemMeta.lore(lore.stream().map(mm::deserialize).collect(Collectors.toList()));
        });
        return cache;
    }
}
