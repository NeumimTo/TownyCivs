package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.config.Structure;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.Region;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class SubclaimService {

    @Inject
    private ConfigurationService configurationService;

    private Set<Region> subclaims = new HashSet<>();

    public Optional<Region> createRegion(LoadedStructure structure) {
        Optional<Structure> structureById = configurationService.findStructureById(structure.structureId);
        if (structureById.isEmpty()) {
            return Optional.empty();
        }
        Region region = createRegion(structureById.get(), structure.center);
        region.loadedStructure = structure;
        return Optional.of(region);
    }

    public Region createRegion(Structure def, Location center) {
        BoundingBox of = BoundingBox.of(center, def.area.x, def.area.y, def.area.z);
        return new Region(def.id, of, center.getWorld().getName());
    }

    public void registerRegion(Region value, LoadedStructure loadedStructure) {
        value.loadedStructure = loadedStructure;
        subclaims.add(value);
    }

    public Optional<Region> regionAt(Location location) {
        for (Region subclaim : subclaims) {
            if (subclaim.overlaps(location)) {
                return Optional.of(subclaim);
            }
        }
        return Optional.empty();
    }

    public Optional<Region> overlaps(Region region) {
        for (Region subclaim : subclaims) {
            if (subclaim.overlaps(region)) {
                return Optional.of(subclaim);
            }
        }
        return Optional.empty();
    }

    public boolean isOutsideTownClaim(Region region, Town town) {

        BoundingBox bb = region.boundingBox;
        double minX = bb.getMinX();
        double maxX = bb.getMaxX();

        double minZ = bb.getMinZ();
        double maxZ = bb.getMaxZ();

        World world = Bukkit.getWorld(region.world);
        Location location = new Location(world, minX, 0, minZ);

        Location location2 = new Location(world, minX, 0, maxZ);

        Location location3 = new Location(world, maxX, 0, minZ);

        Location location4 = new Location(world, maxX, 0, maxZ);

        return isOutsideTownClaim(location, town) ||
                isOutsideTownClaim(location2, town) ||
                isOutsideTownClaim(location3, town) ||
                isOutsideTownClaim(location4, town);
    }

    public boolean isOutsideTownClaim(Location location, Town town) {
        WorldCoord worldCoord = new WorldCoord(location.getWorld().getName(), Coord.parseCoord(location));

        TownBlock townBlock = worldCoord.getTownBlockOrNull();
        if (townBlock == null) {
            return true;
        }
        if (!townBlock.hasTown()) {
            return true;
        }
        Town townAtLoc = townBlock.getTownOrNull();
        if (town != townAtLoc) {
            return true;
        }
        return false;
    }

    public Optional<Structure> getStructureAt(Location location) {
        for (Region subclaim : subclaims) {
            if (subclaim.boundingBox.contains(location.getX(), location.getY(), location.getZ())) {
                return configurationService.findStructureById(subclaim.structureId);
            }
        }
        return Optional.empty();
    }

    public Region getRegion(UUID fromString) {
        for (Region subclaim : subclaims) {
            if (subclaim.uuid.equals(fromString)) {
                return subclaim;
            }
        }
        return null;
    }


    public Region getRegion(LoadedStructure loadedStructure) {
        for (Region subclaim : subclaims) {
            if (subclaim.loadedStructure == loadedStructure) {
                return subclaim;
            }
        }
        return null;
    }

    public void delete(Region region) {
        Iterator<Region> iterator = subclaims.iterator();
        while (iterator.hasNext()) {
            Region next = iterator.next();
            if (next == region) {
                iterator.remove();
                break;
            }
        }
    }

    public Map<Material, Integer> blocksWithinRegion(Region region) {
        Location center = region.loadedStructure.center.clone();
        BoundingBox bb = region.boundingBox;
        World world = center.getWorld();
        Map<Material, Integer> map = new HashMap<>();
        for (int x = (int) bb.getMinX(); x < bb.getMaxX(); x++) {
            for (int z = (int) bb.getMinZ(); z < bb.getMaxZ(); z++) {
                for (int y = (int) bb.getMinY(); y < bb.getMaxY(); y++) {
                    Block blockAt = world.getBlockAt(x, y, z);
                    map.merge(blockAt.getType(),1, Integer::sum);
                }
            }
        }
        return map;
    }

    public Collection<Block> blocksWithinRegion(Collection<Material> filter, Region region) {
        Location center = region.loadedStructure.center.clone();
        BoundingBox bb = region.boundingBox;
        World world = center.getWorld();
        Set<Block> blocks = new HashSet<>();

        for (int x = (int) bb.getMinX(); x < bb.getMaxX(); x++) {
            for (int z = (int) bb.getMinZ(); z < bb.getMaxZ(); z++) {
                for (int y = (int) bb.getMinY(); y < bb.getMaxY(); y++) {
                    Block blockAt = world.getBlockAt(x, y, z);
                    if (filter.contains(blockAt.getType())) {
                        blocks.add(blockAt);
                    }
                }
            }
        }

        return blocks;
    }

    public Map<String, Integer> remainingBlocks(Region region) {
        Map<Material, Integer> placedBlocks = blocksWithinRegion(region);
        Map<String, Integer> requirements = clone(region.loadedStructure.structureDef.blocks);

        for (Map.Entry<Material, Integer> entry : placedBlocks.entrySet()) {
            String mcKey = entry.getKey().getKey().toString();
            Integer reqCount = requirements.get(mcKey);
            if (reqCount != null) {
                requirements.put(mcKey, reqCount - entry.getValue());
                continue;
            }

            mcKey = "!" + mcKey;
            reqCount = requirements.get(mcKey);
            if (reqCount != null) {
                requirements.put(mcKey, reqCount - entry.getValue());
                continue;
            }

            Collection<String> tags = Materials.getTags(entry.getKey());
            for (String tag : tags) {
                reqCount = requirements.get(tag);
                if (reqCount != null) {
                    requirements.put(tag, reqCount - entry.getValue());
                }
                tag = "!" + tag;
                reqCount = requirements.get(tag);
                if (reqCount != null) {
                    requirements.put(tag, reqCount - entry.getValue());
                }
            }
        }
        return requirements;
    }

    private Map<String, Integer> clone(Map<String, Integer> blocks) {
        Map<String, Integer> map = new HashMap<>();
        map.putAll(blocks);
        return map;
    }

    public boolean noRemainingBlocks(Map<String, Integer> remainingBlocks, LoadedStructure loadedStructure) {
        for (Map.Entry<String, Integer> e : remainingBlocks.entrySet()) {
            if (e.getKey().startsWith("!") && e.getValue() != 0) {
                return false;
            }
            if (e.getValue() > 0) {
                return false;
            }
        }
        return true;
    }
}
