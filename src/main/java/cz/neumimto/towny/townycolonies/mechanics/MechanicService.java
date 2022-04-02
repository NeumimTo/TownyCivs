package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.Permission;
import cz.neumimto.towny.townycolonies.mechanics.common.Price;
import org.bukkit.Bukkit;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MechanicService {

    private Map<String, RequirementMechanic> productionReq = new HashMap<>();
    private Map<String, RequirementMechanic> buyReqs = new HashMap<>();


    public void buyReq(String name, RequirementMechanic mech) {
        buyReqs.put(name.toLowerCase(Locale.ROOT), mech);
    }

    public void prodReq(String name, RequirementMechanic mech) {
        productionReq.put(name.toLowerCase(Locale.ROOT), mech);
    }

    public void registerDefaults() {
        buyReq("permission", new Permission());
        buyReq("price", new Price());
        buyReq("town_upkeep", new Price());

        Bukkit.getPluginManager().callEvent(new RegisterMechanicEvent(this));;
    }

    public Optional<RequirementMechanic> buyReq(String mechanic) {
        return Optional.ofNullable(buyReqs.get(mechanic.toLowerCase(Locale.ROOT)));
    }

    public Optional<RequirementMechanic> prodReq(String mechanic) {
        return Optional.ofNullable(productionReq.get(mechanic.toLowerCase(Locale.ROOT)));
    }
}
