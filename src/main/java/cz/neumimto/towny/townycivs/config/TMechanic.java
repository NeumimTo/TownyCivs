package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;

import java.util.ArrayList;
import java.util.List;

public class TMechanic {

    @Path("RequiredStructures")
    @Optional
    public List<String> requiredStructures = new ArrayList<>();
    @Path("Reagent")
    @Optional
    public List<ConfigItem> reagent = new ArrayList<>();

    @Path("Price")
    @Optional
    public double reagentPrice;

    @Path("ItemInput")
    @Optional
    public List<ConfigItem> input = new ArrayList<>();

    @Path("ItemOutput")
    @Optional
    public List<ConfigItem> output = new ArrayList<>();

    @Path("CommandOutput")
    @Optional
    public List<String> commands = new ArrayList<>();

    @Path("GiveMoney")
    @Optional
    public double giveMoney;
}
