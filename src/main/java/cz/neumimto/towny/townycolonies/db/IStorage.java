package cz.neumimto.towny.townycolonies.db;

import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import java.util.Collection;
import java.util.UUID;

public sealed interface IStorage permits Flatfile, Database {
    void init();

    void save(LoadedStructure structure);

    void remove(UUID uuid);

    Collection<LoadedStructure> allStructures();

}
