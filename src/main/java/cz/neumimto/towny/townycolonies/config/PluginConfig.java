package cz.neumimto.towny.townycolonies.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import cz.neumimto.towny.townycolonies.utils.Wildcards;
import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginConfig {

    @Path("structure_lore_template")
    public List<String> structureLoreTemplate;

    @Path("structure_lore_desc_templatc")
    public String structureLoreDescTemplate;

    @Path("copy_defaults")
    public boolean copyDefaults;

    @Path("blueprint_lore_desc_template")
    public String blueprintLoreDescTemplate;

    @Path("blueprint_lore_template")
    public List<String> blueprintLoreTemplate;

}
