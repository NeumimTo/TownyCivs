package cz.neumimto.towny.townycolonies.db;

import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import java.util.Collection;
import java.util.Objects;

public class Storage {

    private static IStorage impl;

    public Storage(IStorage impl) {
        Objects.requireNonNull(Storage.impl, "Storage already initialized");
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
        TownyColonies.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> impl.save(structure));
    }

    public static void scheduleRemove(LoadedStructure structure) {
        TownyColonies.MORE_PAPER_LIB.scheduling().asyncScheduler().run(() -> impl.remove(structure.uuid));
    }

}
