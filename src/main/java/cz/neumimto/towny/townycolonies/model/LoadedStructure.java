package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.config.Structure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@NotThreadSafe
public class LoadedStructure {

    public final UUID uuid; 

    public final UUID town; 

    public final String structureId; 
    public final Location center; 

    public final transient Structure structureDef; 

    public final AtomicBoolean editMode;

    public transient int unsavedTickCount;

    public long lastTickTime;

    public transient long nextTickTime;

    public final Map<Location, Inventory> inventory;

    public LoadedStructure(UUID uuid, UUID town, String structureId, Location center, Structure structureDef) {
        this.uuid = uuid;
        this.town = town;
        this.structureId = structureId;
        this.center = center;
        this.structureDef = structureDef;
        this.editMode = new AtomicBoolean(false);
        this.inventory = new HashMap<>();
    }
}
