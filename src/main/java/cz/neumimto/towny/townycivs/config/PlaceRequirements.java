package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;

import java.util.ArrayList;
import java.util.List;

public class PlaceRequirements {

    @Path("BiomeWhitelist")
    @Optional
    public List<String> biomeWhitelist = new ArrayList<>();

    @Path("BiomeBlacklist")
    @Optional
    public List<String> biomeBlacklist = new ArrayList<>();

    @Path("Above-Y")
    @Optional
    public Integer aboveY;

    @Path("Bellow-Y")
    @Optional
    public Integer bellowY;

    @Path("Permission")
    @Optional
    public List<String> permission = new ArrayList<>();

    @Path("Structures")
    @Optional
    public List<String> structures = new ArrayList<>();

}
