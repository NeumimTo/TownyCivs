package cz.neumimto.towny.townycolonies.model;

import java.util.*;

public class VirtualInventory {
    public UUID containerUUID;
    public Map<String, Integer> content = new HashMap<>();

    protected VirtualInventory clone() {
        VirtualInventory virtualInventory = new VirtualInventory();
        virtualInventory.containerUUID = this.containerUUID;
        virtualInventory.content.putAll(content);
        return virtualInventory;
    }
}
