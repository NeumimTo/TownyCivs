package cz.neumimto.towny.townycivs.mechanics;

import cz.neumimto.towny.townycivs.mechanics.common.DoubleWrapper;

class TownUpkeep implements Mechanic<DoubleWrapper> {

    @Override
    public boolean check(TownContext townContext, DoubleWrapper configContext) {
        return townContext.town.getAccount().getHoldingBalance() >= configContext.value;
    }

    @Override
    public void postAction(TownContext townContext, DoubleWrapper configContext) {
        townContext.town.getAccount().withdraw(configContext.value, "townycivs - upkeep " + townContext.structure.id);
    }

    @Override
    public String id() {
        return Mechanics.TOWN_UPKEEP;
    }

    @Override
    public DoubleWrapper getNew() {
        return new DoubleWrapper();
    }
}
