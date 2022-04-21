package cz.neumimto.towny.townycolonies.model;

import java.util.Set;
import java.util.UUID;

public class VirtualContainer {
    public UUID id;
    public int x;
    public int y;
    public int z;
    public int space;
    public Set<String> inputFilter;

    protected VirtualContainer clone() {
        var v = new VirtualContainer();
        v.id = id;
        v.x = x;
        v.y = y;
        v.z = z;
        v.inputFilter.addAll(inputFilter);
        return v;
    }
}
