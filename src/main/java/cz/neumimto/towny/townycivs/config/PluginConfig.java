package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;

import java.util.HashMap;
import org.bukkit.Material;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class PluginConfig {

    @Path("copy_defaults")
    public boolean copyDefaults;

    @Path("inventory_blocker_material")
    @Conversion(Structure.MaterialConversion.class)
    public Material inventoryBlockerMaterial;

    @Path("inventory_blocker_custom_model_data")
    public Integer inventoryBlockerCustomModelData;

    @Path("town_points")
    @Conversion(TownPointsConvertor.class)
    public Map<Integer, Integer> townpoints;

    @Path("storage")
    public String storage;

    @Path("db_url")
    public String dbUrl;

    @Path("db_password")
    public String dbPassword;

    @Path("db_user")
    public String dbUser;

    @Path("db_database")
    public String dbDatabase;

    static class TownPointsConvertor implements Converter<Map, Config> {

        @Override
        public Map convertToField(Config value) {
            Map map = new HashMap();

            if (value != null) {
                SortedMap<Integer, Map<TownySettings.TownLevel, Object>> configTownLevel = TownySettings.getConfigTownLevel();
                Map<String, Object> stringObjectMap = value.valueMap();
                for (Map.Entry<String, Object> e : stringObjectMap.entrySet()) {
                    configTownLevel.entrySet().stream()
                            .filter(a-> matches(e.getKey(), a.getValue().get(TownySettings.TownLevel.NAME_POSTFIX), a.getValue().get(TownySettings.TownLevel.NAME_PREFIX)))
                            .findFirst()
                            .ifPresent(a->{
                                map.put(a.getKey(), Integer.parseInt(e.getValue().toString()));
                            });

                }
            }

            return map;
        }

        private boolean matches(String key, Object postfixO, Object prefixO) {
            String postfix = String.valueOf(postfixO);
            String prefix = String.valueOf(prefixO);

            postfix = postfix.trim().replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_]", "");
            prefix = prefix.trim().replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_]", "");
            return key.equalsIgnoreCase(prefix) || key.equalsIgnoreCase(postfix);
        }

        @Override
        public Config convertFromField(Map value) {
            return null;
        }
    }

}
