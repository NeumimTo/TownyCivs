package cz.neumimto.towny.townycolonies.config;

import com.electronwill.nightconfig.core.conversion.Path;

import java.time.Period;
import java.util.List;


public class Structure {

    @Path("Id")
    public String id;

    @Path("Name")
    public String name;

    @Path("Description")
    public List<String> description;

    @Path("Period")
    public long period;

}
