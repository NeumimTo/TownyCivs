package cz.neumimto.towny.townycolonies.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualInventory {
    public UUID containerUUID;
    public Map<String, Integer> map = new HashMap<>();

    protected VirtualInventory clone() {
        VirtualInventory virtualInventory = new VirtualInventory();
        virtualInventory.containerUUID = this.containerUUID;
        virtualInventory.map.putAll(map);
        return virtualInventory;
    }
}
