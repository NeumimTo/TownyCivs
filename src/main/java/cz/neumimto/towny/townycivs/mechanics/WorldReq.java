package cz.neumimto.towny.townycivs.mechanics;

import cz.neumimto.towny.townycivs.mechanics.common.StringWrapper;

class WorldReq implements Mechanic<StringWrapper> {

    @Override
    public boolean check(TownContext townContext, StringWrapper configContext) {
        return townContext.structureCenterLocation.getWorld().getName().equalsIgnoreCase(configContext.value);
    }

    @Override
    public void nokmessage(TownContext townContext, StringWrapper configuration) {
        townContext.player.sendMessage(townContext.structure.name + " can be placed only in world " + configuration.value);
    }

    @Override
    public String id() {
        return Mechanics.WORLD;
    }

    @Override
    public StringWrapper getNew() {
        return new StringWrapper();
    }
}
