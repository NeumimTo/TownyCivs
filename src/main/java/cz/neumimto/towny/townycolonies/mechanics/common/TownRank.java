package cz.neumimto.towny.townycolonies.mechanics.common;

import com.palmergames.bukkit.towny.TownyAPI;
import cz.neumimto.towny.townycolonies.mechanics.RequirementMechanic;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;

public class TownRank implements RequirementMechanic<DoubleWrapper> {

    @Override
    public boolean check(TownContext townContext, DoubleWrapper configContext) {
        return townContext.town.getLevel() >= configContext.value;

    }

    @Override
    public void nokmessage(TownContext townContext, DoubleWrapper configuration) {
        townContext.player.sendMessage("Town level is not high enough to build " + townContext.structure.name);
    }

    @Override
    public DoubleWrapper getNew() {
        return new DoubleWrapper();
    }
}
