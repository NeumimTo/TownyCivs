package cz.neumimto.towny.townycolonies;

import cz.neumimto.towny.townycolonies.mechanics.TownContext;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Singleton
public class StructureInventoryService {

    private static Map<Location, UUID> structsAndPlayers = new ConcurrentHashMap<>();
    private static Map<UUID, StructAndInv> playersAndInv = new ConcurrentHashMap<>();
    @Inject
    private ItemService itemService;

    private static void putToInventory(Inventory inventory, Set<ItemStack> itemStackSet, CountDownLatch cdl, Set<ItemStack> couldNotFit) {
        try {
            HashMap<Integer, ItemStack> map = inventory.addItem(itemStackSet.toArray(ItemStack[]::new));
            couldNotFit.addAll(map.values());
        } finally {
            cdl.countDown();
        }
    }

    private static void checkItemsForUpkeepAndWait(Inventory inventory1, Map<Material, AmountAndModel> fulfilled, CountDownLatch cdl) {
        try {
            checkItemsForUpkeep(inventory1, fulfilled);
        } finally {
            cdl.countDown();
        }
    }

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

        TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                .run(() -> player.openInventory(inv), null);
    }

    public void closeInvenotory(Player player) {
        StructAndInv sai = playersAndInv.remove(player.getUniqueId());
        if (sai != null) {
            structsAndPlayers.remove(sai.location);
        }
    }

    public void addItemProduction(LoadedStructure loadedStructure, Set<ItemStack> itemStackSet) {
        Map<Location, Inventory> inventories = loadedStructure.inventory;
        Set<ItemStack> remaining = new HashSet<>();
        for (Map.Entry<Location, Inventory> e : inventories.entrySet()) {
            UUID uuid = structsAndPlayers.get(e.getKey());
            Inventory inventory1 = e.getValue();
            if (uuid != null) {
                Player vplayer = Bukkit.getPlayer(uuid);
                addItemProductionAndWait(vplayer, inventory1, itemStackSet, remaining);

            } else {
                HashMap<Integer, ItemStack> map = inventory1.addItem(itemStackSet.toArray(ItemStack[]::new));
                remaining.addAll(map.values());
            }
            if (remaining.isEmpty()) {
                break;
            }
        }
    }

    private void addItemProductionAndWait(Player player, Inventory inventory, Set<ItemStack> itemStackSet, Set<ItemStack> couldNotFit) {
        CountDownLatch cdl = new CountDownLatch(1);

        TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                .run(() -> putToInventory(inventory, itemStackSet, cdl, couldNotFit),
                        () -> putToInventory(inventory, itemStackSet, cdl, couldNotFit)
                );
        try {
            cdl.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TownyColonies.logger.log(Level.WARNING, "An error occurred while waiting for a lock. Could not process addItemProduction ", e);
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

    public boolean checkUpkeep(TownContext townContext, Set<ItemStack> upkeep) {
        Map<Material, AmountAndModel> fulfilled = new HashMap<>();

        for (ItemStack itemStack : upkeep) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            Integer model = null;
            if (itemMeta != null) {
                if (itemMeta.hasCustomModelData()) {
                    model = itemMeta.getCustomModelData();
                }
            }
            fulfilled.put(itemStack.getType(), new AmountAndModel(itemStack.getAmount(), model));
        }

        for (Map.Entry<Location, Inventory> e : townContext.loadedStructure.inventory.entrySet()) {
            UUID uuid = structsAndPlayers.get(e.getKey());
            Inventory inventory1 = e.getValue();


            if (uuid != null) {
                Player vplayer = Bukkit.getPlayer(uuid);
                checkItemsForUpkeepAndWait(vplayer, inventory1, fulfilled);

            } else {
                checkItemsForUpkeep(inventory1, fulfilled);
            }

            if (!fulfilled.isEmpty()) {
                return false;
            }

        }
        return true;
    }

    private void checkItemsForUpkeepAndWait(Player player, Inventory inventory1,Map<Material, AmountAndModel> fulfilled) {
        CountDownLatch cdl = new CountDownLatch(1);
        TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                .run(
                        () -> checkItemsForUpkeepAndWait(inventory1, fulfilled, cdl),
                        () -> checkItemsForUpkeepAndWait(inventory1, fulfilled, cdl)
                );
        try {
            cdl.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TownyColonies.logger.log(Level.WARNING, "Could not wait for lock checkItemsForUpkeepAndWait", e);
        }
    }

    public void processUpkeep(LoadedStructure loadedStructure, Set<ItemStack> upkeep) {
        Map<Material, AmountAndModel> fulfilled = new HashMap<>();

        for (ItemStack itemStack : upkeep) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            Integer model = null;
            if (itemMeta != null) {
                if (itemMeta.hasCustomModelData()) {
                    model = itemMeta.getCustomModelData();
                }
            }
            fulfilled.put(itemStack.getType(), new AmountAndModel(itemStack.getAmount(), model));
        }

        for (Map.Entry<Location, Inventory> e : loadedStructure.inventory.entrySet()) {
            UUID uuid = structsAndPlayers.get(e.getKey());

            Inventory inventory = e.getValue();

            if (uuid != null) {
                Player vplayer = Bukkit.getPlayer(uuid);

                processUpkeepAndWait(vplayer, inventory, fulfilled);

            } else {
                processUpkeep(inventory, fulfilled);
            }
        }

    }

    private void processUpkeepAndWait(Player player, Inventory inventory, Map<Material, AmountAndModel> fulfilled) {
        CountDownLatch cdl = new CountDownLatch(1);
        TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                .run(
                        () -> processUpkeepAndWait(inventory, fulfilled, cdl),
                        () -> processUpkeepAndWait(inventory, fulfilled, cdl)
                );
        try {
            cdl.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TownyColonies.logger.log(Level.WARNING, "Could not wait for lock checkItemsForUpkeepAndWait", e);
        }
    }

    private void processUpkeepAndWait(Inventory inventory, Map<Material, AmountAndModel> fulfilled, CountDownLatch cdl) {
        try {
            processUpkeep(inventory, fulfilled);
        } finally {
            cdl.countDown();
        }
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
                d.setDamage(d.getDamage() + 1);
                if (d.getDamage() >= content.getType().getMaxDurability()) {
                    inventory.getContents()[i] = null;

                } else {
                    fulfilled.remove(content.getType());
                }
            } else {
                int amount = content.getAmount();

                if (amount > amountAndModel.amount) {
                    content.setAmount(amount - amountAndModel.amount);
                    fulfilled.remove(content.getType());

                } else {
                    inventory.getContents()[i] = null;
                    amountAndModel.amount -= amount;
                }

            }
        }

    }

    public boolean anyInventoryIsBeingAccessed(LoadedStructure structure) {
        for (Map.Entry<Location, Inventory> e : structure.inventory.entrySet()) {
            if (structsAndPlayers.containsKey(e.getKey())) {
                return true;
            }
        }
        return false;
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
