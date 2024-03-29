package cz.neumimto.towny.townycivs.model;

import cz.neumimto.towny.townycivs.config.Structure;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EditSession {

    public final UUID uuid;

    public Structure structure;
    public Location center;
    public Set<Location> currentStructureBorder = new HashSet<>();
    public Set<Location> overlappintStructureBorder = new HashSet<>();

    public EditSession(Structure structure, Location location) {
        this.uuid = UUID.randomUUID();
        this.center = location;
        this.structure = structure;
    }
}
