package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import java.util.UUID;

public abstract class ScheduledCommand {

    protected final UUID uuid;

    public ScheduledCommand(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getStructureUUID() {
        return uuid;
    }

    public abstract void executeFromAsyncThread(LoadedStructure loadedStructure);
}
