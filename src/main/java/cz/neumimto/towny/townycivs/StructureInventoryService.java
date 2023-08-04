package cz.neumimto.towny.townycivs;

import cz.neumimto.towny.townycivs.config.ConfigItem;
import cz.neumimto.towny.townycivs.mechanics.TownContext;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.*;
import java.util.logging.Level;

@Singleton
public class StructureInventoryService {

    private static Map<Location, UUID> structsAndPlayers = new ConcurrentHashMap<>();
    private static Map<UUID, StructAndInv> playersAndInv = new ConcurrentHashMap<>();
    @Inject
    private ItemService itemService;

    private void checkItemsForUpkeep(Inventory inventory1, Map<Material, AmountAndModel> fulfilled) {
        ItemStack inventoryBlocker = itemService.getInventoryBlocker();
        for (ItemStack i : inventory1.getContents()) {
            if (i == null) {
                continue;
            }
            if (i.equals(inventoryBlocker)) {
                break;
            }
            AmountAndModel amountAndModel = fulfilled.get(i.getType());
            if (amountAndModel == null) {
                continue;
            }
            Integer modelData = null;
            ItemMeta itemMeta = i.getItemMeta();

            if (itemMeta.hasCustomModelData()) {
                modelData = itemMeta.getCustomModelData();
            }

            if (!Objects.equals(amountAndModel.model, modelData)) {
                continue;
            }

            if (itemMeta instanceof Damageable d) {
                if (d.getDamage() + 1 <= i.getType().getMaxDurability()) {
                    fulfilled.remove(i.getType());
                }
            } else {
                fulfilled.computeIfPresent(i.getType(), (material, a) -> {
                    a.amount = a.amount - i.getAmount();
                    return a.amount <= 0 ? null : a;
                });
            }
        }
    }

    public void openInventory(Player player, Location location, LoadedStructure structure) {
        structsAndPlayers.put(location, player.getUniqueId());

        Inventory inv = getStructureInventory(structure, location);
        playersAndInv.put(player.getUniqueId(), new StructAndInv(structure.uuid, inv, location));

        TownyCivs.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                .run(() -> player.openInventory(inv), null);
    }

    public void closeInvenotory(Player player) {
        StructAndInv sai = playersAndInv.remove(player.getUniqueId());
        if (sai != null) {
            structsAndPlayers.remove(sai.location);
        }
    }

    public void addItemProduction(LoadedStructure loadedStructure, Collection<ItemStack> itemStackSet) {
        Map<Location, Inventory> inventories = loadedStructure.inventory;

        for (Map.Entry<Location, Inventory> e : inventories.entrySet()) {
            UUID uuid = structsAndPlayers.get(e.getKey());
            Inventory inventory1 = e.getValue();

            CompletableFuture<Map<Integer, ItemStack>> future = new CompletableFuture<>();
            future.completeOnTimeout(Collections.emptyMap(), 1500L, TimeUnit.MILLISECONDS);

            Collection<ItemStack> itemStackSet2 = itemStackSet;

            Runnable fn = () -> {
                HashMap<Integer, ItemStack> remaining = inventory1.addItem(itemStackSet2.toArray(ItemStack[]::new));
                future.complete(remaining);
            };

            Map<Integer, ItemStack> integerItemStackMap = waitForResult(uuid, future, fn);

            if (integerItemStackMap.isEmpty()) {
                break;
            }

            itemStackSet = integerItemStackMap.values();
        }
    }

    private Inventory getStructureInventory(LoadedStructure loadedStructure, Location location) {
        Inventory inventory = loadedStructure.inventory.get(location);
        return inventory;
    }

    private Inventory createStructureInventory(LoadedStructure structure) {
        Inventory inventory = Bukkit.getServer().createInventory(null, 27, Component.text(structure.structureDef.name));
        if (structure.structureDef.inventorySize > 0) {
            for (int i = structure.structureDef.inventorySize; i < inventory.getSize(); i++) {
                inventory.setItem(i, itemService.getInventoryBlocker());
            }
        }
        return inventory;
    }

    public Inventory loadStructureInventory(LoadedStructure structure, Location location, ItemStack[] itemStacks) {
        Inventory structureInventory = createStructureInventory(structure);
        structureInventory.addItem(itemStacks);
        structure.inventory.put(location, structureInventory);
        return structureInventory;
    }

    public UUID getPlayerViewingInventory(LoadedStructure structure) {
        return structsAndPlayers.get(structure.uuid);
    }

    private void processUpkeep(Inventory inventory, Map<Material, AmountAndModel> fulfilled) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack content = inventory.getContents()[i];
            if (content == null) {
                continue;
            }
            AmountAndModel amountAndModel = fulfilled.get(content.getType());
            if (amountAndModel == null) {
                continue;
            }
            Integer modelData = null;
            ItemMeta itemMeta = content.getItemMeta();

            if (itemMeta.hasCustomModelData()) {
                modelData = itemMeta.getCustomModelData();
            }

            if (!Objects.equals(amountAndModel.model, modelData)) {
                continue;
            }

            if (itemMeta instanceof Damageable d) {
                d.setDamage(d.getDamage() + amountAndModel.amount);
                if (d.getDamage() >= content.getType().getMaxDurability()) {
                    inventory.setItem(i, null);
                } else {
                    content.setItemMeta(d);
                    fulfilled.remove(content.getType());
                }
            } else {
                int amount = content.getAmount();

                if (amount > amountAndModel.amount) {
                    content.setAmount(amount - amountAndModel.amount);
                    fulfilled.remove(content.getType());

                } else {
                    inventory.setItem(i, null);
                    amountAndModel.amount -= amount;
                }

            }
        }

    }

    public boolean canTakeAtLeastOne(LoadedStructure loadedStructure, Collection<ItemStack> output) {

        for (Map.Entry<Location, Inventory> e : loadedStructure.inventory.entrySet()) {
            UUID uuid = structsAndPlayers.get(e.getKey());

            Inventory inventory = e.getValue();

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeOnTimeout(false, 1500L, TimeUnit.MILLISECONDS);

            Runnable fn = () -> {
                for (ItemStack configItem : output) {
                    future.complete(inventory.first(configItem) != -1);
                }
            };

            if (waitForResult(uuid, future, fn)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(LoadedStructure loadedStructure, Collection<ItemStack> list, boolean damageToolsOnly) {
        for (Map.Entry<Location, Inventory> e : loadedStructure.inventory.entrySet()) {
            UUID uuid = structsAndPlayers.get(e.getKey());

            Inventory inventory = e.getValue();

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeOnTimeout(false, 1500L, TimeUnit.MILLISECONDS);

            Runnable fn = () -> {
                ItemStack inventoryBlocker = itemService.getInventoryBlocker();
                for (ItemStack i : inventory.getContents()) {
                    if (i == null) {
                        continue;
                    }
                    if (i.equals(inventoryBlocker)) {
                        break;
                    }
                    for (ItemStack configItem : list) {
                        //todo
                    }
                }
                future.complete(false);
            };

            if (waitForResult(uuid, future, fn)) {
                return true;
            }
        }

        return false;
    }

    public <T> T waitForResult(UUID playerInv, CompletableFuture<T> future, Runnable fn) {
        if (playerInv != null) {
            Player vplayer = Bukkit.getPlayer(playerInv);

            TownyCivs.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(vplayer)
                    .run(fn, fn);

        } else {
            fn.run();
        }

        return future.join();
    }

    public boolean anyInventoryIsBeingAccessed(LoadedStructure structure) {
        for (Map.Entry<Location, Inventory> e : structure.inventory.entrySet()) {
            if (structsAndPlayers.containsKey(e.getKey())) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(LoadedStructure structure, Collection<ItemStack> items) {
        for (Map.Entry<Location, Inventory> e : structure.inventory.entrySet()) {
            UUID uuid = structsAndPlayers.get(e.getKey());

            Inventory inventory = e.getValue();

            if (uuid != null) {
                CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
                Player vplayer = Bukkit.getPlayer(uuid);

                Runnable fn = () -> completableFuture.complete(contains(inventory, items));

                TownyCivs.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(vplayer)
                        .run(fn, fn);

                if (completableFuture.join()) {
                    return true;
                }
            } else {

                if (contains(inventory, items)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean contains(Inventory inventory, Collection<ItemStack> items) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack content = inventory.getContents()[i];
            if (content == null) {
                continue;
            }
            if (itemService.isInventoryBlocker(content)) {
                return false;
            }
            for (ItemStack item : items) {

            }
        }
        return false;
    }

    public void takeItems(LoadedStructure structure, Collection<ItemStack> items, boolean damageToolsOnly) {


    }

    private record StructAndInv(UUID structureId, Inventory inventory, Location location) {
    }

    private static class AmountAndModel {
        int amount;
        Integer model;

        public AmountAndModel(int amount, Integer model) {
            this.amount = amount;
            this.model = model;
        }

    }
}
