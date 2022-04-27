package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.StringWrapper;

class Permission implements Mechanic<StringWrapper> {

    @Override
    public boolean check(TownContext townContext, StringWrapper configContext) {
        return townContext.player.hasPermission(configContext.value);
    }

    @Override
    public void nokmessage(TownContext townContext, StringWrapper configuration) {
        townContext.resident.getPlayer().sendMessage("You dont have permission to use " + townContext.structure.name);
    }

    @Override
    public StringWrapper getNew() {
        return new StringWrapper();
    }

}
