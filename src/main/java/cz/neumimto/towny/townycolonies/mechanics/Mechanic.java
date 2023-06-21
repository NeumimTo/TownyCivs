package cz.neumimto.towny.townycolonies.mechanics;

public interface Mechanic<C> {

    boolean check(TownContext townContext, C configContext);

    default void postAction(TownContext townContext, C configContext) {
    }

    default void nokmessage(TownContext townContext, C configuration) {
    }

    default void okmessage(TownContext townContext, C configuration) {
    }

    String id();

    C getNew();
}

