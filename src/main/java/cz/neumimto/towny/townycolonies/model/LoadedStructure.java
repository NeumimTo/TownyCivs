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

    public final UUID uuid; //threadsafe

    public final UUID town; //threadsafe

    public final String structureId; //threadsafe
    public final Location center; //threadsafe

    public final transient Structure structureDef; //threadsafe

    public final AtomicBoolean editMode;

    public transient int unsavedTickCount;

    public long lastTickTime;

    public transient long nextTickTime;

    public LoadedStructure(UUID uuid, UUID town, String structureId, Location center, Structure structureDef) {
        this.uuid = uuid;
        this.town = town;
        this.structureId = structureId;
        this.center = center;
        this.structureDef = structureDef;
        this.editMode = new AtomicBoolean(false);
    }
}
