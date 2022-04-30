package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.mechanics.VirtualItem;

import java.util.*;
import java.util.stream.IntStream;

public class VirtualContent {
    public UUID containerUUID;
    public Map<String, Integer> content = new LinkedHashMap<>();

    public static VirtualContent empty(int i, UUID id) {
        var vi  = new VirtualContent();
        vi.containerUUID = id;
        IntStream.of(i).forEach(a-> vi.content.put(VirtualItem.empty_slot, 0));
        return vi;
    }

    protected VirtualContent clone() {
        VirtualContent virtualContent = new VirtualContent();
        virtualContent.containerUUID = this.containerUUID;
        virtualContent.content.putAll(content);
        return virtualContent;
    }
}
