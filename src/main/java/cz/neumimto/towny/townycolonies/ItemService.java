package cz.neumimto.towny.townycolonies;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class ItemService {

    public void registerRecipes() {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        itemStack.editMeta(meta -> {
            meta.setCustomModelData(3077);
            meta.displayName(Component.text("Town administration"));
        });
        ShapedRecipe shapedRecipe = new ShapedRecipe(NamespacedKey.fromString("townycolonies:town_book"), itemStack);
        shapedRecipe.shape(
                "-S-",
                "SPS",
                "-S-"
        );
        shapedRecipe.setIngredient('P', Material.BOOK);
        shapedRecipe.setIngredient('S', Material.GOLD_NUGGET);
        Bukkit.getServer().addRecipe(shapedRecipe);
    }

    public boolean isTownBook(ItemStack itemInUse) {
        if (itemInUse == null) {
            return false;
        }
        if (itemInUse.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }
        ItemMeta itemMeta = itemInUse.getItemMeta();
        if (itemMeta.hasCustomModelData()) {
            return itemMeta.getCustomModelData() == 3077;
        }
        return false;
    }
}
