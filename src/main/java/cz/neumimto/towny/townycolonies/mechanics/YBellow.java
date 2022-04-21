package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.DoubleWrapper;

class YBellow implements RequirementMechanic<DoubleWrapper> {

    @Override
    public boolean check(TownContext townContext, DoubleWrapper configContext) {
        return townContext.structureCenterLocation.getBlockY() > configContext.value;
    }

    @Override
    public void nokmessage(TownContext townContext, DoubleWrapper configuration) {
        townContext.player.sendMessage(townContext.structure + " must be placed bellow y " + configuration.value);
    }

    @Override
    public DoubleWrapper getNew() {
        return new DoubleWrapper();
    }
}