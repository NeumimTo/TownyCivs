package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.db.Database;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import java.util.UUID;

public class SetEditModeCommand extends ScheduledCommand {
    private final boolean edit;

    public SetEditModeCommand(UUID uuid, boolean edit) {
        super(uuid);
        this.edit = edit;
    }

    @Override
    public void executeFromAsyncThread(LoadedStructure loadedStructure) {
        loadedStructure.editMode = edit;
        Database.scheduleSave(loadedStructure);
    }
}
