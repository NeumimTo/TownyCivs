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

    private Map<String, RequirementMechanic> productionReq = new HashMap<>();
    private Map<String, RequirementMechanic> buyReqs = new HashMap<>();
    private Map<String, RequirementMechanic> placeReq = new HashMap<>();

    public void buyReq(String name, RequirementMechanic mech) {
        buyReqs.put(name.toLowerCase(Locale.ROOT), mech);
    }

    public void prodReq(String name, RequirementMechanic mech) {
        productionReq.put(name.toLowerCase(Locale.ROOT), mech);
    }

    public void registerDefaults() {
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

    public Optional<RequirementMechanic> buyReq(String mechanic) {
        return Optional.ofNullable(buyReqs.get(mechanic.toLowerCase(Locale.ROOT)));
    }

    public Optional<RequirementMechanic> prodReq(String mechanic) {
        return Optional.ofNullable(productionReq.get(mechanic.toLowerCase(Locale.ROOT)));
    }

    public Optional<RequirementMechanic> placeReq(String mechanic) {
        return Optional.ofNullable(placeReq.get(mechanic.toLowerCase(Locale.ROOT)));
    }

    public void placeReq(String name, RequirementMechanic mech) {
        placeReq.put(name.toLowerCase(Locale.ROOT), mech);
    }
}
