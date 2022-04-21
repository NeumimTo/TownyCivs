package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.StringWrapper;

class WorldReq implements RequirementMechanic<StringWrapper> {

    @Override
    public boolean check(TownContext townContext, StringWrapper configContext) {
        return townContext.structureCenterLocation.getWorld().getName().equalsIgnoreCase(configContext.value);
    }

    @Override
    public void nokmessage(TownContext townContext, StringWrapper configuration) {
        townContext.player.sendMessage(townContext.structure.name + " can be placed only in world " + configuration.value);
    }

    @Override
    public StringWrapper getNew() {
        return new StringWrapper();
    }
}
