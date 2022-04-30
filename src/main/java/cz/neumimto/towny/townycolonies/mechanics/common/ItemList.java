package cz.neumimto.towny.townycolonies.mechanics.common;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;

import java.util.ArrayList;
import java.util.List;

public class ItemList implements Wrapper {

    @Path("Items")
    public List<ConfigItem> configItems = new ArrayList<>();

    public static class ConfigItem {

        @Path("Material")
        public String material;

        @Path("CustomModelData")
        @Optional
        public Integer customModelData;

        @Path("Amount")
        @Optional
        public Integer amount;

        @Path("Fuel")
        @Optional
        public Integer fuel;
    }
}
