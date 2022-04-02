package cz.neumimto.towny.townycolonies.mechanics;

public interface RequirementMechanic<C> {

    boolean check(TownContext townContext, C configContext);

    default void postAction(TownContext townContext, C configContext) {}

    default void nokmessage(TownContext townContext, C configuration) {}

    default void okmessage(TownContext townContext, C configuration) {}

    C getNew();
}

