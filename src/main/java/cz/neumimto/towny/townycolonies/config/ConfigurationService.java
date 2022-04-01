package cz.neumimto.towny.townycolonies.config;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Singleton
public class ConfigurationService {

    private Map<String, Structure> structures = new HashMap<>();

    public Optional<Structure> findStructureById(String id) {
        return Optional.ofNullable(structures.get(id.toLowerCase(Locale.ROOT)));
    }

    public long smallestPeriod() {
        return structures.values().stream().mapToLong(value -> value.period).min().orElse(Long.MAX_VALUE);
    }

    public void load(Path path) throws IOException {
        structures.clear();
        Path structures = path.resolve("structures");
        if (!Files.exists(structures)) {
            Files.createDirectory(structures);
        }

    }
}
