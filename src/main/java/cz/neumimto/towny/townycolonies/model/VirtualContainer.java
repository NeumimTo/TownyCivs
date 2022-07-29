package cz.neumimto.towny.townycolonies.model;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VirtualContainer {
    public UUID id;
    public String world;
    public int x;
    public int y;
    public int z;
    public Set<String> inputFilter;
    public boolean full;

    public static VirtualContainer from(Block block, UUID uuid, Collection<String> filter) {
        var c = new VirtualContainer();
        c.id = uuid;
        Location location = block.getLocation();
        c.world = location.getWorld().getName();
        c.x = location.getBlockX();
        c.y = location.getBlockY();
        c.z = location.getBlockZ();
        c.inputFilter = filter == null ? new HashSet<>() : new HashSet<>(filter);
        return c;
    }

    protected VirtualContainer clone() {
        var v = new VirtualContainer();
        v.id = id;
        v.x = x;
        v.y = y;
        v.z = z;
        v.full = full;
        v.world = world;
        v.inputFilter = new HashSet<>();
        if (inputFilter != null) {
            v.inputFilter.addAll(inputFilter);
        }
        return v;
    }
}
