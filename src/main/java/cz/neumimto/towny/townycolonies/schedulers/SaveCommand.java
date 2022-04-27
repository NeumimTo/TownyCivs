package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.db.Database;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import java.util.UUID;

public class SaveCommand extends ScheduledCommand {

    public SaveCommand(UUID uuid) {
        super(uuid);
    }

    @Override
    public void executeFromAsyncThread(LoadedStructure loadedStructure) {
        Database.scheduleSave(loadedStructure);
    }
}
