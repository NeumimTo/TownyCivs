package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.conversion.Path;
import cz.neumimto.towny.townycivs.mechanics.common.ItemList;

import java.util.List;

public class TMechanic {

    @Path("Reagent")
    public List<ItemList.ConfigItem> reagent;

    @Path("ItemInput")
    public List<ItemList.ConfigItem> input;

    @Path("ItemOutput")
    public List<ItemList.ConfigItem> output;

    @Path("Commandoutput")
    public List<String> commands;
}
