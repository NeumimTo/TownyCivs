package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;

import java.util.ArrayList;
import java.util.List;

public class BuyRequirements {

    @Path("Permission")
    @Optional
    public List<String> permission = new ArrayList<>();

    @Path("Price")
    @Optional
    public double price;

}
