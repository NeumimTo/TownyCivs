package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.config.Structure;
import org.bukkit.Location;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.UUID;

@NotThreadSafe
public class LoadedStructure {

    public UUID uuid; //threadsafe

    public UUID town; //threadsafe

    public String structureId; //threadsafe
    public Location center; //threadsafe

    public List<VirtualContainer> containers;

    public List<VirtualInventory> storage;
    public long lastTickTime;

    public transient long nextTickTime;

    public boolean editMode;

    public transient Structure structureDef; //threadsafe

    public transient int unsavedTickCount;


    public LoadedStructure clone() {
        var l = new LoadedStructure();
        l.uuid = uuid;
        l.town = town;
        l.center = center;
        l.structureId = structureId;
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
        return l;
    }
}
