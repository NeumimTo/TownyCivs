package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.config.Structure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@NotThreadSafe
public class LoadedStructure {

    public UUID uuid; //threadsafe

    public UUID town; //threadsafe

    public String structureId; //threadsafe
    public Location center; //threadsafe

    public AtomicBoolean editMode = new AtomicBoolean(false);

    public transient Structure structureDef; //threadsafe

    public transient int unsavedTickCount;

    public long lastTickTime;

    public transient long nextTickTime;

    public LoadedStructure clone() {
        var l = new LoadedStructure();
        l.uuid = uuid;
        l.town = town;
        l.center = center;
        l.structureId = structureId;
        l.lastTickTime = lastTickTime;
        l.editMode = new AtomicBoolean(editMode.get());
        return l;
    }
}
