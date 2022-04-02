package cz.neumimto.towny.townycolonies.mechanics.common;

import com.electronwill.nightconfig.core.conversion.Path;

public class StringWrapper implements Wrapper {

    @Path("Value")
    public String value;

    @Override
    public Object value() {
        return value;
    }
}
