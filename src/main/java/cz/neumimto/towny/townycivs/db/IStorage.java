package cz.neumimto.towny.townycivs.db;

import cz.neumimto.towny.townycivs.model.LoadedStructure;

import java.util.Collection;
import java.util.UUID;

public sealed interface IStorage permits Flatfile, Database {
    void init();

    void save(LoadedStructure structure);

    void remove(UUID uuid);

    Collection<LoadedStructure> allStructures();

}
