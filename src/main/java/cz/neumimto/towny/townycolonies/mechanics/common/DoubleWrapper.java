package cz.neumimto.towny.townycolonies.mechanics.common;

import com.electronwill.nightconfig.core.conversion.Path;

public class DoubleWrapper implements Wrapper {

    @Path("Value")
    public double value;

    @Override
    public Object value() {
        return Double.valueOf(value);
    }
}
