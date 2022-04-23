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

    @Path("blockdb")
    @Conversion(MapSLM.class)
    public Map<String, List<Material>> blockdb = new HashMap<>();

    @Path("fontdb")
    @Conversion(MapSS.class)
    public Map<String, String> fontdb = new HashMap<>();

    @Path("structure_lore_template")
    public List<String> structureLoreTemplate;

    @Path("structure_lore_desc_templatc")
    public String structureLoreDescTemplate;

    @Path("scheduler_tick_rate")
    public int schedulerTickRate;

    @Path("copy_defaults")
    public boolean copyDefaults;

    @Path("blueprint_lore_desc_template")
    public String blueprintLoreDescTemplate;

    @Path("blueprint_lore_template")
    public List<String> blueprintLoreTemplate;

    public static class MapSLM implements Converter<Map<String, List<Material>>, Config> {

        @Override
        public Map<String, List<Material>> convertToField(Config value) {
            Map<String, List<Material>> map = new HashMap<>();

            Set<String> allNames = Stream.of(Material.values()).map(Material::name).collect(Collectors.toSet());

            if (value != null) {
                for (Config.Entry entry : value.entrySet()) {
                    String key = entry.getKey();
                    String v = entry.getValue();
                    String[] split = v.split(" ");
                    List<Material> materials = new ArrayList<>();
                    for (String s : split) {
                        s = s.trim();
                        Material material = Material.matchMaterial(s);
                        if (material == null) {
                            Set<String> mats = Wildcards.substract(s.toUpperCase(Locale.ENGLISH), allNames);
                            materials.addAll(mats.stream().map(Material::matchMaterial).toList());
                        } else {
                            materials.add(material);
                        }
                    }
                    map.put(key, materials);
                }
            }

            return map;
        }

        @Override
        public Config convertFromField(Map<String, List<Material>> value) {
            return null;
        }
    }

    public static class MapSS implements Converter<Map<String, String>, Config> {

        @Override
        public Map convertToField(Config value) {
            Map<String, String> map = new HashMap();
            if (value != null) {
                for (Map.Entry<String, Object> entry : value.valueMap().entrySet()) {
                    map.put(entry.getKey(), entry.getValue().toString());
                }
            }
            return map;
        }

        @Override
        public Config convertFromField(Map<String, String> value) {
            Config config = Config.inMemory();
            new ObjectConverter().toConfig(value, config);
            return config;
        }
    }

}
