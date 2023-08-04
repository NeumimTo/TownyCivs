package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.*;
import com.typesafe.config.Optional;
import org.bukkit.Material;

import java.util.*;


public class Structure {

    @Path("Id")
    public String id;

    @Path("Name")
    public String name;

    @Path("Period")
    public long period;

    @Path("Material")
    @Conversion(MaterialConversion.class)
    @Optional
    public Material material = Material.STONE;

    @Path("Description")
    @Optional
    public List<String> description = new ArrayList<>();

    @Path("CustomModelData")
    public int customModelData;

    @Path("MaxCount")
    public Integer maxCount;

    @Path("AreaRadius")
    @Conversion(AreaConversion.class)
    public Area area;

    @Path("BuyRequirements")
    @Optional
    public BuyRequirements buyRequirements = new BuyRequirements();

    @Path("PlaceRequirements")
    @Optional
    public PlaceRequirements placeRequirements = new PlaceRequirements();

    @Path("Blocks")
    @Conversion(Blocks.class)
    public Map<String, Integer> blocks = new HashMap<>();

    @Path("SaveEachNTicks")
    public int saveEachNTicks;

    @Path("OnTick")
    @Optional
    public List<TMechanic> onTick = new ArrayList<>();

    @Path("InventorySize")
    public int inventorySize;

    @Path("TownPointPrice")
    public int townPointPrice;

    public static class Area {
        public final int x;
        public final int z;
        public final int y;

        public Area(int x, int z, int y) {
            this.x = x;
            this.z = z;
            this.y = y;
        }
    }

    public static class AreaConversion implements Converter<Area, String> {
        @Override
        public Area convertToField(String value) {
            String[] a = value.split("x");
            return new Area(Integer.parseInt(a[0]), Integer.parseInt(a[1]), Integer.parseInt(a[2]));
        }

        @Override
        public String convertFromField(Area value) {
            return null;
        }
    }

    public static class Blocks implements Converter<Map, Config> {

        @Override
        public Map convertToField(Config value) {
            Map map = new HashMap();

            if (value != null) {
                Map<String, Object> stringObjectMap = value.valueMap();
                for (Map.Entry<String, Object> e : stringObjectMap.entrySet()) {
                    map.put(e.getKey(), Integer.parseInt(e.getValue().toString()));
                }
            }

            return map;
        }

        @Override
        public Config convertFromField(Map value) {
            return null;
        }
    }

    public static class MaterialConversion implements Converter<Material, String> {
        @Override
        public Material convertToField(String value) {
            return Material.matchMaterial(value);
        }

        @Override
        public String convertFromField(Material value) {
            return value.name();
        }
    }

}

