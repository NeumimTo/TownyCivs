package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.config.Structure;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class LoadedStructure {
    public UUID uuid;

    public UUID town;

    public String strucutureId;
    public Location center;

    public List<VirtualContainer> containers;

    public List<VirtualInventory> storage;
    public long lastTickTime;

    public boolean editMode;

    public transient Structure structure;

    public int x;
    public int y;
    public int z;

    public LoadedStructure clone() {
        var l = new LoadedStructure();
        l.uuid = uuid;
        l.town = town;
        l.center = center;
        if (containers != null) {
            for (VirtualContainer c : containers) {
                l.containers.add(c.clone());
            }
        }
        if (storage != null) {
            for (VirtualInventory c : storage) {
                l.storage.add(c.clone());
            }
        }
        l.lastTickTime = lastTickTime;
        l.editMode = editMode;
        l.x = x;
        l.y = y;
        l.z = z;
        return l;
    }
}
