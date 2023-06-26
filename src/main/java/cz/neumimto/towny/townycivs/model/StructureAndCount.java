package cz.neumimto.towny.townycivs.model;

import cz.neumimto.towny.townycivs.config.Structure;

public class StructureAndCount {
    public int count;
    public Structure structure;

    public StructureAndCount(Structure key, Integer value) {
        this.structure = key;
        this.count = value;
    }
}
