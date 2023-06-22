package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class StructureInventoryService {

    private record StructAndInv(UUID structureId, Inventory inventory){};

    private static Map<UUID, UUID> structsAndPlayers = new ConcurrentHashMap<>();
    private static Map<UUID, StructAndInv> playersAndInv = new ConcurrentHashMap<>();
    private static Map<UUID, Inventory> structsAndInv = new ConcurrentHashMap<>();

    public void openInventory(Player player, LoadedStructure structure) {
        structsAndPlayers.put(structure.uuid, player.getUniqueId());
        Inventory inv = getStructureInventory(structure);
        playersAndInv.put(player.getUniqueId(), new StructAndInv(structure.uuid, inv));
        TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(player)
                .run(() -> player.openInventory(inv), null);
    }

    public void closeInvenotory(Player player) {
        StructAndInv sai = playersAndInv.remove(player.getUniqueId());
        if (sai != null) {
            structsAndPlayers.remove(sai.structureId);
        }
    }

    public void addItemProduction(Town town, LoadedStructure loadedStructure, Set<ItemStack> itemStackSet) {
        UUID player = structsAndPlayers.get(loadedStructure.uuid);
        if (player != null) {
            Player vplayer = Bukkit.getPlayer(player);
            TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(vplayer)
                    .run(() -> vplayer.getOpenInventory().getTopInventory().addItem(itemStackSet.toArray(ItemStack[]::new)),
                         () -> putToInventory(loadedStructure, itemStackSet));
        } else {
            putToInventory(loadedStructure, itemStackSet);
        }
    }

    private void putToInventory(LoadedStructure loadedStructure, Set<ItemStack> itemStackSet) {
        Inventory inventory = getStructureInventory(loadedStructure);
        inventory.addItem(itemStackSet.toArray(ItemStack[]::new));
    }

    private Inventory getStructureInventory(LoadedStructure loadedStructure) {
        Inventory inventory = structsAndInv.get(loadedStructure.uuid);
        if (inventory == null) {
            inventory = createStructureInventory(loadedStructure);
            structsAndInv.put(loadedStructure.uuid, inventory);
        }
        return inventory;
    }

    private Inventory createStructureInventory(LoadedStructure structure) {
        return Bukkit.getServer().createInventory(null, 27, Component.text(structure.structureDef.name));
    }

    public List<ItemStack> getStructureInventoryContent(LoadedStructure structure) {
        ItemStack[] contents = structsAndInv.get(structure.uuid).getContents();
        if (contents == null) {
            contents = new ItemStack[0];
        }
        return List.of(contents);
    }

    public void loadStructureInventory(LoadedStructure structure, ItemStack[] itemStacks) {
        Inventory structureInventory = createStructureInventory(structure);
        structureInventory.addItem(itemStacks);
        structsAndInv.put(structure.uuid, structureInventory);
    }

    public UUID getPlayerViewingInventory(LoadedStructure structure) {
        return structsAndPlayers.get(structure.uuid);
    }

    public void upkeep(TownContext townContext, Set<ItemStack> upkeep) {

        UUID player = structsAndPlayers.get(townContext.loadedStructure.uuid);
        if (player != null) {
            Player vplayer = Bukkit.getPlayer(player);
            TownyColonies.MORE_PAPER_LIB.scheduling().entitySpecificScheduler(vplayer)
                    .run(() -> {
                        try {
                            for (ItemStack itemStack : upkeep) {
                                if (!vplayer.getOpenInventory().getTopInventory().containsAtLeast(itemStack, itemStack.getAmount())) {
                                    townContext.cdlResult = false;
                                    return;
                                }
                            }
                            townContext.cdlResult = true;

                        } finally {
                            townContext.cdl.countDown();
                        }
                    },
                    () -> {
                        checkUpkeep(townContext, upkeep);
                    });
        } else {
            checkUpkeep(townContext, upkeep);
        }
    }

    private void checkUpkeep(TownContext townContext, Set<ItemStack> upkeep) {
        Inventory inv = getStructureInventory(townContext.loadedStructure);
        try {
            for (ItemStack itemStack : upkeep) {
                if (!inv.containsAtLeast(itemStack, itemStack.getAmount())) {
                    townContext.cdlResult = false;
                    return;
                }
            }
            townContext.cdlResult = true;
        } finally {
            townContext.cdl.countDown();
        }
    }

    public void upkeepProcess(LoadedStructure loadedStructure, Set<ItemStack> upkeep) {
        Inventory inv = structsAndInv.get(loadedStructure.uuid);
        for (ItemStack itemStack : upkeep) {
            //todo convert to fuel or damage tools
            inv.remove(itemStack);
        }
    }
}
