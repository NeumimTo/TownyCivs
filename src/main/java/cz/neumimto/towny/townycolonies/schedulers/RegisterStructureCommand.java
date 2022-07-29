package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import java.util.HashSet;
import java.util.Set;

public class RegisterStructureCommand extends ScheduledCommand {

    private LoadedStructure loadedStructure;

    public RegisterStructureCommand(LoadedStructure loadedStructure) {
        super(null);
        this.loadedStructure = loadedStructure;
    }

    @Override
    public void executeFromAsyncThread(LoadedStructure loadedStructure) {
        Set<LoadedStructure> set = new HashSet<>();
        set.add(this.loadedStructure);
        TownyColonies.injector.getInstance(StructureScheduler.class).structuresByTown.merge(this.loadedStructure.town, set, (a,b) ->{
            a.addAll(b);
            return a;
        });
    }
}
