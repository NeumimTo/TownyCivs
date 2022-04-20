package cz.neumimto.towny.townycolonies.model;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class Region {

    public final String structureId;
    public final BoundingBox boundingBox;
    public final String world;
    public boolean editingAllowed;

    public Region(String structureId, BoundingBox boundingBox, String world) {
        this.structureId = structureId;
        this.boundingBox = boundingBox;
        this.world = world;
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
            return world.equals(location.getWorld());
        }
        return false;
    }
}

