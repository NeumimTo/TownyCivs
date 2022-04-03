package cz.neumimto.towny.townycolonies.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.mechanics.MechanicService;
import cz.neumimto.towny.townycolonies.mechanics.RequirementMechanic;
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
    public String material;

    @Path("CustomModelData")
    public Integer customModelData;

    @Path("MaxCount")
    public Integer maxCount;

    @Path("AreaRadius")
    @Conversion(AreaConversion.class)
    public Area area;

    @Path("BuyRequirements")
    @Conversion(BuyReq.class)
    public List<LoadedPair<RequirementMechanic<?>,?>> buyRequirements;

    @Path("BuildRequirements")
    @Conversion(BuyReq.class)
    public List<LoadedPair<RequirementMechanic<?>,?>> buildRequirements;

    @Path("Upkeep")
    @Conversion(Upkeep.class)
    public List<LoadedPair<RequirementMechanic<?>,?>> upkeep;

    @Path("Blocks")
    @Conversion(Blocks.class)
    public Map<String, Integer> blocks;

    public static class Area {
        public final int x;
        public final int z;
        public final int y;

        public Area(int x,int z, int y) {
            this.x = x;
            this.z = z;
            this.y = y;
        }
    }

    public static class LoadedPair<M,C> {
        public final C configValue;
        public final M mechanic;

        public LoadedPair(C configValue, M mechanic) {
            this.configValue = configValue;
            this.mechanic = mechanic;
        }
    }

    public static class Upkeep extends LoadMechanic {
        @Override
        protected Optional<RequirementMechanic> mechanic(MechanicService service, String name) {
            return service.prodReq(name);
        }
    }

    public static class BuyReq extends LoadMechanic {
        @Override
        protected Optional<RequirementMechanic> mechanic(MechanicService service, String name) {
            return service.buyReq(name);
        }
    }

    public static class BuildReq extends LoadMechanic {
        @Override
        protected Optional<RequirementMechanic> mechanic(MechanicService service, String name) {
            return service.placeReq(name);
        }
    }

    public static abstract class LoadMechanic implements Converter<List<?>, List<Config>> {

        protected abstract Optional<RequirementMechanic> mechanic(MechanicService service, String name);

        @Override
        public List convertToField(List<Config> value) {
            List mechs = new ArrayList();
            if (value == null) {
                return mechs;
            }
            var registry = TownyColonies.injector.getInstance(MechanicService.class);

            for (Config config : value) {
                String mechanic = config.get("Mechanic");
                Optional<RequirementMechanic> requirementMechanic = mechanic(registry, mechanic);
                if (requirementMechanic.isPresent()) {
                    RequirementMechanic m = requirementMechanic.get();
                    Object aNew = m.getNew();
                    new ObjectConverter().toObject(config,aNew);
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

    public static class Blocks implements Converter<Map,Config> {

        @Override
        public Map convertToField(Config value) {
            Map map = new HashMap();



            return map;
        }

        @Override
        public Config convertFromField(Map value) {
            return null;
        }
    }
}

