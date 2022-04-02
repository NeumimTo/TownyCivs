package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.config.Structure;

public class StructureAndCount {
    public int count;
    public Structure structure;

    public StructureAndCount(Structure key, Integer value) {
        this.structure = key;
        this.count = value;
    }
}
