package cz.neumimto.towny.townycivs.mechanics;

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

    public void buyReq(Mechanic mech) {
        buyReqs.put(mech.id().toLowerCase(Locale.ROOT), mech);
    }

    public void prodReq(Mechanic mech) {
        productionReq.put(mech.id().toLowerCase(Locale.ROOT), mech);
    }

    public void registerDefaults() {
        productionReq.clear();
        buyReqs.clear();
        placeReq.clear();
        prodMech.clear();

        buyReq(new Permission());
        buyReq(new Price());
        buyReq(new TownRank());
        buyReq(injector.getInstance(MStructure.class));

        prodReq(new Price());
        prodReq(new ItemUpkeep());

        placeReq(new Permission());
        placeReq(new YBellow());
        placeReq(new YAbove());
        placeReq(new WorldReq());
        placeReq(new TownRank());
        placeReq(injector.getInstance(MStructure.class));

        prodMech(new ItemProduction());


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

    public void placeReq(Mechanic mech) {
        placeReq.put(mech.id().toLowerCase(Locale.ROOT), mech);
    }

    public void prodMech(Mechanic mech) {
        prodMech.put(mech.id().toLowerCase(Locale.ROOT), mech);
    }

    public Optional<Mechanic> prodMech(String name) {
        return Optional.ofNullable(prodMech.get(name.toLowerCase(Locale.ROOT)));
    }
}
