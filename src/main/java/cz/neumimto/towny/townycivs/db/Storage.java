package cz.neumimto.towny.townycivs.db;

import cz.neumimto.towny.townycivs.TownyCivs;
import cz.neumimto.towny.townycivs.model.LoadedStructure;

import java.util.Collection;

public class Storage {

    private static IStorage impl;

    public Storage(IStorage impl) {
        Storage.impl = impl;
        Storage.impl.init();
    }

    public static void saveAll(Collection<LoadedStructure> values) {
        for (LoadedStructure structure : values) {
            impl.save(structure);
        }
    }

    public static Collection<LoadedStructure> allStructures() {
        return impl.allStructures();
    }

    public static void scheduleSave(LoadedStructure structure) {
        TownyCivs.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> {
            impl.save(structure);
        });
    }

    public static void scheduleRemove(LoadedStructure structure) {
        TownyCivs.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> impl.remove(structure.uuid));
    }

}
