package cz.neumimto.towny.townycolonies.mechanics;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RegisterMechanicEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    public MechanicService mechanicService;

    public RegisterMechanicEvent(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
