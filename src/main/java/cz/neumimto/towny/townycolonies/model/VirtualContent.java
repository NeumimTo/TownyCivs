package cz.neumimto.towny.townycolonies.model;

import cz.neumimto.towny.townycolonies.mechanics.VirtualItem;

import java.util.*;
import java.util.stream.IntStream;

public class VirtualContent {
    public UUID containerUUID;
    public List<VirtualItem> content = new ArrayList<>();

    public static VirtualContent empty(int i, UUID id) {
        var vi  = new VirtualContent();
        vi.containerUUID = id;
        IntStream.of(i).forEach(a-> vi.content.add(VirtualItem.empty_slot));
        return vi;
    }

    protected VirtualContent clone() {
        VirtualContent virtualContent = new VirtualContent();
        virtualContent.containerUUID = this.containerUUID;
        virtualContent.content.addAll(content);
        return virtualContent;
    }
}
