package cz.neumimto.towny.townycivs.model;

import cz.neumimto.towny.townycivs.config.Structure;

public class BlueprintItem {
    public final int customModelData;
    public final Structure structure;

    public BlueprintItem(int customModelData, Structure structure) {
        this.customModelData = customModelData;
        this.structure = structure;
    }
}
