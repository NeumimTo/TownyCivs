package cz.neumimto.towny.townycivs.mechanics;

import cz.neumimto.towny.townycivs.mechanics.common.DoubleWrapper;

class YAbove implements Mechanic<DoubleWrapper> {

    @Override
    public boolean check(TownContext townContext, DoubleWrapper configContext) {
        return townContext.structureCenterLocation.getBlockY() < configContext.value;
    }

    @Override
    public void nokmessage(TownContext townContext, DoubleWrapper configuration) {
        townContext.player.sendMessage(townContext.structure + " must be placed above y " + configuration.value);
    }

    @Override
    public String id() {
        return Mechanics.Y_ABOVE;
    }

    @Override
    public DoubleWrapper getNew() {
        return new DoubleWrapper();
    }
}
