package cz.neumimto.towny.townycolonies.lsitener.TownListener;

import cz.neumimto.towny.townycolonies.mechanics.VirtualItem;
import cz.neumimto.towny.townycolonies.model.Region;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import cz.neumimto.towny.townycolonies.model.VirtualContent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VirtualStorageHelper {

    public static final int CHEST_SIZE = 27;

    public static Map<VirtualContainer, VirtualContent> prepareVirtualinventory(Region region,
                                                                                Collection<Block> regionBlocks) {
        Collection<Entity> nearbyEntities = region.loadedStructure.center.getWorld()
                .getNearbyEntities(region.boundingBox,
                                    entity -> entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME);

        Map<VirtualContainer, VirtualContent> vinv = new LinkedHashMap<>();

        for (Block regionBlock : regionBlocks) {
            if (regionBlock.getType() == Material.CHEST) {
                Collection<String> attachedItemFrames = findAttachedItemFrames(regionBlock, nearbyEntities);

                VirtualContent virtualContent = VirtualContent.empty(CHEST_SIZE, UUID.randomUUID());
                VirtualContainer vc = VirtualContainer.from(regionBlock, virtualContent.containerUUID, attachedItemFrames);
                vinv.put(vc, virtualContent);

            } else if (regionBlock.getType() == Material.BARREL) {
                VirtualContent virtualContent = VirtualContent.empty(CHEST_SIZE, UUID.randomUUID());
                VirtualContainer vc = VirtualContainer.from(regionBlock, virtualContent.containerUUID, new HashSet<>());
                vinv.put(vc, virtualContent);
            }
        }

        return vinv;
    }

    public static Collection<String> findAttachedItemFrames(Block block, Collection<Entity> entities) {
        Set<String> set = new HashSet<>();
        Collection<ItemFrame> itemFrames = new HashSet<>();
        for (Entity entity : entities) {
            itemFrames.add((ItemFrame) entity);
        }

        findAttachedItemFrames(block, set, itemFrames);

        if (block.getState() instanceof Chest c) {

            if (c.getInventory() instanceof DoubleChestInventory dci) {
                if (block.getLocation().equals(dci.getLeftSide().getLocation())) {

                    findAttachedItemFrames(dci.getRightSide().getLocation().getBlock(),set, itemFrames);
                } else {

                    findAttachedItemFrames(dci.getLeftSide().getLocation().getBlock(), set,itemFrames);
                }
            }
        }

        return set;
    }

    private static void findAttachedItemFrames(Block block, Set<String> set, Collection<ItemFrame> itemFrames) {
        BlockFace blockFace = BlockFace.WEST;
        Block relative = block.getRelative(blockFace);
        ItemFrame itemFrame = itemFrame(relative, blockFace, itemFrames);
        parseContent(itemFrame, set);

        blockFace = BlockFace.EAST;
        relative = block.getRelative(blockFace);
        itemFrame = itemFrame(relative, blockFace, itemFrames);
        parseContent(itemFrame, set);

        blockFace = BlockFace.NORTH;
        relative = block.getRelative(blockFace);
        itemFrame = itemFrame(relative, blockFace, itemFrames);
        parseContent(itemFrame, set);

        blockFace = BlockFace.SOUTH;
        relative = block.getRelative(blockFace);
        itemFrame = itemFrame(relative, blockFace, itemFrames);
        parseContent(itemFrame, set);
    }

    private static void parseContent(ItemFrame itemFrame, Set<String> set) {
        if (itemFrame == null) {
            return;
        }
        ItemStack item = itemFrame.getItem();
        if (item.getType() == Material.AIR) {
            return;
        }
        set.add(VirtualItem.toVirtualItemFilter(item));
    }

    private static ItemFrame itemFrame(Block atLocation, BlockFace blockFace, Collection<ItemFrame> itemFrames) {
        Location location = atLocation.getLocation();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        return itemFrames.stream().filter(itemFrame -> {
            return itemFrame.getAttachedFace().getOppositeFace() == blockFace;
        }).filter(a-> {
            Location iLoc = a.getLocation();
            return blockX == iLoc.getBlockX() && iLoc.getBlockY() == blockY && blockZ == iLoc.getBlockZ();
        }).findFirst().orElse(null);
    }
}
