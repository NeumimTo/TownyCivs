package cz.neumimto.towny.townycolonies;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Singleton;

@Singleton
public class ItemService {

    public void registerRecipes() {
        NamespacedKey recipe = NamespacedKey.fromString("townycolonies:town_book");
        if (Bukkit.getServer().getRecipe(recipe) != null) {
            Bukkit.getServer().removeRecipe(recipe);
        }
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        itemStack.editMeta(meta -> {
            meta.setCustomModelData(3077);
            meta.displayName(Component.text("Town administration"));
        });
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(recipe, itemStack);
        shapelessRecipe.addIngredient(Material.BOOK);
        shapelessRecipe.addIngredient(Material.EMERALD);

        Bukkit.getServer().addRecipe(shapelessRecipe);

        recipe = NamespacedKey.fromString("townycolonies:structure_tool");
        if (Bukkit.getServer().getRecipe(recipe) != null) {
            Bukkit.getServer().removeRecipe(recipe);
        }
        itemStack = new ItemStack(Material.PAPER);
        itemStack.editMeta(meta -> {
            meta.setCustomModelData(3078);
            meta.displayName(Component.text("Structure Edit Tool"));
        });
        ShapelessRecipe editTool = new ShapelessRecipe(recipe, itemStack);
        editTool.addIngredient(Material.PAPER);
        editTool.addIngredient(Material.WOODEN_SHOVEL);
        Bukkit.getServer().addRecipe(editTool);
    }

    public StructureTool getItemType(ItemStack itemInUse) {
        if (itemInUse == null) {
            return null;
        }

        if (itemInUse.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta itemMeta = itemInUse.getItemMeta();
            if (itemMeta.hasCustomModelData()) {
                if (itemMeta.getCustomModelData() == 3077) {
                    return StructureTool.TOWN_TOOL;
                }
            }
        }

        if (itemInUse.getType() == Material.PAPER) {
            ItemMeta itemMeta = itemInUse.getItemMeta();
            if (itemMeta.hasCustomModelData()) {
                if (itemMeta.getCustomModelData() == 3078) {
                    return StructureTool.EDIT_TOOL;
                }
            }
        }

        return null;
    }

    /**
     * Called from async
     * @param key
     * @param value
     * @return
     */
    public ItemStack toItemStack(String key, Integer value) {
        Material material = Material.matchMaterial(key);
        return new ItemStack(material, value);
    }

    public enum StructureTool {
        EDIT_TOOL, TOWN_TOOL
    }
}
