package cz.neumimto.towny.townycolonies.mechanics.common;

import cz.neumimto.towny.townycolonies.mechanics.RequirementMechanic;
import cz.neumimto.towny.townycolonies.mechanics.TownContext;

public class TownUpkeep implements RequirementMechanic<DoubleWrapper> {

    @Override
    public boolean check(TownContext townContext, DoubleWrapper configContext) {
        return townContext.town.getAccount().getHoldingBalance() >= configContext.value;
    }

    @Override
    public void postAction(TownContext townContext, DoubleWrapper configContext) {
        townContext.town.getAccount().withdraw(configContext.value, "townycolonies - upkeep " + townContext.structure.id);
    }

    @Override
    public DoubleWrapper getNew() {
        return new DoubleWrapper();
    }
}
