package cz.neumimto.towny.townycivs.mechanics;

import com.palmergames.bukkit.towny.TownyMessaging;
import cz.neumimto.towny.townycivs.mechanics.common.DoubleWrapper;

class Price implements Mechanic<DoubleWrapper> {


    @Override
    public boolean check(TownContext townContext, DoubleWrapper configContext) {
        return townContext.town.getAccount().getHoldingBalance() >= configContext.value;
    }

    @Override
    public void postAction(TownContext townContext, DoubleWrapper configContext) {
        townContext.town.getAccount().withdraw(configContext.value, "townycivs - bought " + townContext.structure.id);
    }

    @Override
    public void nokmessage(TownContext townContext, DoubleWrapper configuration) {
        townContext.player.sendMessage("Not enough funds in town bank - " + configuration.value);
    }

    @Override
    public void okmessage(TownContext townContext, DoubleWrapper configuration) {
        TownyMessaging.sendPrefixedTownMessage(townContext.town, townContext.player.getName() + " bought " + townContext.structure.name);
    }

    @Override
    public String id() {
        return Mechanics.PRICE;
    }

    @Override
    public DoubleWrapper getNew() {
        return new DoubleWrapper();
    }
}
