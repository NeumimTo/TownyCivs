package cz.neumimto.towny.townycivs.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.*;
import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.mechanics.Mechanic;
import cz.neumimto.towny.townycivs.mechanics.MechanicService;
import cz.neumimto.towny.townycivs.mechanics.common.Wrapper;
import org.bukkit.Material;

import java.util.*;


public class Structure {

    @Path("Id")
    public String id;

    @Path("Name")
    public String name;

    @Path("Description")
    public List<String> description;

    @Path("Period")
    public long period;

    @Path("Material")
    @Conversion(MaterialConversion.class)
    public Material material;

    @Path("CustomModelData")
    public int customModelData;

    @Path("MaxCount")
    public Integer maxCount;

    @Path("AreaRadius")
    @Conversion(AreaConversion.class)
    public Area area;

    @Path("BuyRequirements")
    @Conversion(BuyReq.class)
    public List<LoadedPair<Mechanic<?>, ?>> buyRequirements;

    @Path("PlaceRequirements")
    @Conversion(BuyReq.class)
    public List<LoadedPair<Mechanic<?>, ?>> placeRequirements;

    @Path("BuildRequirements")
    @Conversion(BuyReq.class)
    public List<LoadedPair<Mechanic<?>, ?>> buildRequirements;

    @Path("Upkeep")
    @Conversion(Upkeep.class)
    public List<LoadedPair<Mechanic<Object>, Object>> upkeep;

    @Path("Blocks")
    @Conversion(Blocks.class)
    public Map<String, Integer> blocks;

    @Path("SaveEachNTicks")
    public int saveEachNTicks;

    @Path("Production")
    @Conversion(Production.class)
    public List<LoadedPair<Mechanic<Object>, Object>> production;

    @Path("OnTick")
    public List<TMechanic> onTick;

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

    public static class LoadedPair<M, C> {
        public final C configValue;
        public final Mechanic<C> mechanic;

        public LoadedPair(C configValue, Mechanic<C> mechanic) {
            this.configValue = configValue;
            this.mechanic = mechanic;
        }
    }

    public static class Production extends ConfiguredMechanic {
        @Override
        protected Optional<Mechanic> mechanic(MechanicService service, String name) {
            return service.prodMech(name);
        }
    }


    public static class Upkeep extends ConfiguredMechanic {
        @Override
        protected Optional<Mechanic> mechanic(MechanicService service, String name) {
            return service.prodReq(name);
        }
    }

    public static class BuyReq extends ConfiguredMechanic {
        @Override
        protected Optional<Mechanic> mechanic(MechanicService service, String name) {
            return service.buyReq(name);
        }
    }

    public static class BuildReq extends ConfiguredMechanic {
        @Override
        protected Optional<Mechanic> mechanic(MechanicService service, String name) {
            return service.placeReq(name);
        }
    }

    public static abstract class ConfiguredMechanic implements Converter<List<?>, List<Config>> {

        protected abstract Optional<Mechanic> mechanic(MechanicService service, String name);

        @Override
        public List convertToField(List<Config> value) {
            List mechs = new ArrayList();
            if (value == null) {
                return mechs;
            }
            var registry = TownyCivs.injector.getInstance(MechanicService.class);

            for (Config config : value) {
                String mechanic = config.get("Mechanic");
                Optional<Mechanic> mech = mechanic(registry, mechanic);
                if (mech.isPresent()) {
                    Mechanic m = mech.get();
                    Object aNew = m.getNew();
                    if (aNew instanceof Wrapper w) {
                        if (w.isObject()) {
                            new ObjectConverter().toObject(config.get("Value"), aNew);
                            mechs.add(new LoadedPair<>(aNew, m));
                            continue;
                        }
                    }
                    new ObjectConverter().toObject(config, aNew);
                    mechs.add(new LoadedPair<>(aNew, m));
                }
            }

            return mechs;
        }

        @Override
        public List convertFromField(List value) {
            return null;
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

