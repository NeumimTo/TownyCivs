package cz.neumimto.towny.townycivs.model;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.UUID;

public class Region {

    public final UUID uuid;
    public final String structureId;
    public final BoundingBox boundingBox;
    public final String world;
    public LoadedStructure loadedStructure;

    public Region(String structureId, BoundingBox boundingBox, String world) {
        this.structureId = structureId;
        this.boundingBox = boundingBox;
        this.world = world;
        this.uuid = UUID.randomUUID();
    }

    public boolean overlaps(BoundingBox boundingBox) {
        return this.boundingBox.overlaps(boundingBox);
    }

    public boolean overlaps(Region region) {
        return this.boundingBox.overlaps(region.boundingBox);
    }

    public boolean overlaps(Location location) {
        boolean contains = this.boundingBox.contains(location.getX(), location.getY(), location.getZ());
        if (contains) {
            return world.equals(location.getWorld().getName());
        }
        return false;
    }
}

