package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.config.Structure;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EditSession {
    public Structure structure;
    public Location center;

    public Set<Location> currentStructureBorder = new HashSet<>();
    public Set<Location> overlappintStructureBorder = new HashSet<>();

    public final UUID uuid;

    public EditSession(Structure structure, Location location) {
        this.uuid = UUID.randomUUID();
        this.center = location;
    }
}
