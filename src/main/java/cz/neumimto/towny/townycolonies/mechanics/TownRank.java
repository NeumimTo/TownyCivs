package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.DoubleWrapper;

class TownRank implements Mechanic<DoubleWrapper> {

    @Override
    public boolean check(TownContext townContext, DoubleWrapper configContext) {
        return townContext.town.getLevel() >= configContext.value;

    }

    @Override
    public void nokmessage(TownContext townContext, DoubleWrapper configuration) {
        townContext.player.sendMessage("Town level is not high enough to build " + townContext.structure.name);
    }

    @Override
    public String id() {
        return Mechanics.TOWN_RANK;
    }

    @Override
    public DoubleWrapper getNew() {
        return new DoubleWrapper();
    }
}
