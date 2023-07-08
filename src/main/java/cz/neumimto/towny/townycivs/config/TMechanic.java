package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;

import java.util.List;

public class TMechanic {

    @Path("Reagent")
    @Optional
    public List<ConfigItem> reagent;

    @Path("ItemInput")
    @Optional
    public List<ConfigItem> input;

    @Path("ItemOutput")
    @Optional
    public List<ConfigItem> output;

    @Path("CommandOutput")
    @Optional
    public List<String> commands;
}
