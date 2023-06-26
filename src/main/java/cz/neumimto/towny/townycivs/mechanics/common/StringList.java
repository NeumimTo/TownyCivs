package cz.neumimto.towny.townycivs.mechanics.common;

import com.electronwill.nightconfig.core.conversion.Path;

import java.util.ArrayList;
import java.util.List;

public class StringList implements Wrapper {

    @Path("List")
    public List<String> configItems = new ArrayList<>();

    @Override
    public boolean isObject() {
        return true;
    }
}
