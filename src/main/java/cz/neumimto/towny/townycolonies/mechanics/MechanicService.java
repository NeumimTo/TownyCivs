package cz.neumimto.towny.townycolonies.mechanics;

import com.google.inject.Injector;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MechanicService {

    @Inject
    private Injector injector;

    private Map<String, Mechanic> productionReq = new HashMap<>();
    private Map<String, Mechanic> buyReqs = new HashMap<>();
    private Map<String, Mechanic> placeReq = new HashMap<>();

    private Map<String, Mechanic> prodMech = new HashMap<>();

    public void buyReq(String name, Mechanic mech) {
        buyReqs.put(name.toLowerCase(Locale.ROOT), mech);
    }

    public void prodReq(String name, Mechanic mech) {
        productionReq.put(name.toLowerCase(Locale.ROOT), mech);
    }

    public void registerDefaults() {
        productionReq.clear();
        buyReqs.clear();
        placeReq.clear();
        prodMech.clear();

        buyReq("permission", new Permission());
        buyReq("price", new Price());
        buyReq("town_level", new TownRank());
        buyReq("structure", injector.getInstance(MStructure.class));

        prodReq("town_upkeep", new Price());

        placeReq("y_bellow", new YBellow());
        placeReq("y_above", new YBellow());
        placeReq("world", new WorldReq());
        placeReq("town_level", new TownRank());
        placeReq("structure", injector.getInstance(MStructure.class));



        Bukkit.getPluginManager().callEvent(new RegisterMechanicEvent(this));
    }

    public Optional<Mechanic> buyReq(String mechanic) {
        return Optional.ofNullable(buyReqs.get(mechanic.toLowerCase(Locale.ROOT)));
    }

    public Optional<Mechanic> prodReq(String mechanic) {
        return Optional.ofNullable(productionReq.get(mechanic.toLowerCase(Locale.ROOT)));
    }

    public Optional<Mechanic> placeReq(String mechanic) {
        return Optional.ofNullable(placeReq.get(mechanic.toLowerCase(Locale.ROOT)));
    }

    public void placeReq(String name, Mechanic mech) {
        placeReq.put(name.toLowerCase(Locale.ROOT), mech);
    }

    public Optional<Mechanic> prodMech(String name) {
        return Optional.ofNullable(prodMech.get(name.toLowerCase(Locale.ROOT)));
    }
}
