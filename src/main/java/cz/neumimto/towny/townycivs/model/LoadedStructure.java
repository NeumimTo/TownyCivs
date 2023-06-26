package cz.neumimto.towny.townycivs.model;

import cz.neumimto.towny.townycivs.config.Structure;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@NotThreadSafe
public class LoadedStructure {

    public final UUID uuid;

    public final UUID town;

    public final String structureId;
    public final Location center;

    public final transient Structure structureDef;

    public final AtomicBoolean editMode;
    public final Map<Location, Inventory> inventory;
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
        this.inventory = new HashMap<>();
    }
}
