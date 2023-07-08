package cz.neumimto.towny.townycivs.model;

import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;

public record ActionResult(List<Component> msg) {
    public static final ActionResult OK = new ActionResult(Collections.emptyList());

    public static ActionResult of(List<Component> msg) {
        return new ActionResult(msg);
    }

    public boolean isOk() {
        return msg.isEmpty();
    }
}
