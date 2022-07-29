package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.db.Database;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RemoveStructureCommand extends ScheduledCommand {

    private LoadedStructure loadedStructure;

    public RemoveStructureCommand(LoadedStructure loadedStructure) {
        super(null);
        this.loadedStructure = loadedStructure;
    }

    @Override
    public void executeFromAsyncThread(LoadedStructure loadedStructure) {
        Map<UUID, Set<LoadedStructure>> map = TownyColonies.injector.getInstance(StructureScheduler.class).structuresByTown;
        Set<LoadedStructure> loadedStructures = map.get(this.loadedStructure.town);
        if (loadedStructures == null) {
            return;
        }
        Iterator<LoadedStructure> iterator = loadedStructures.iterator();
        while (iterator.hasNext()) {
            LoadedStructure next = iterator.next();
            if (next == this.loadedStructure) {
                iterator.remove();
                Database.scheduleRemove(this.loadedStructure);
                break;
            }
        }
    }
}
