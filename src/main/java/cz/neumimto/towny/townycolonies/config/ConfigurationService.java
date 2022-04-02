package cz.neumimto.towny.townycolonies.config;


import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.TownyColonies;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;

@Singleton
public class ConfigurationService {

    private Map<String, Structure> structures = new HashMap<>();
    public PluginConfig config;

    public Optional<Structure> findStructureById(String id) {
        return Optional.ofNullable(structures.get(id.toLowerCase(Locale.ROOT)));
    }

    public Collection<Structure> getAll() {
        return structures.values();
    }

    public long smallestPeriod() {
        return structures.values().stream().mapToLong(value -> value.period).min().orElse(Long.MAX_VALUE);
    }

    public void load(Path path) throws IOException {
        structures.clear();
        Path structures = path.resolve("structures");
        if (!Files.exists(structures)) {
            Files.createDirectories(structures);
        }

        Path settingsPath = path.resolve("settings.conf");
        if (!settingsPath.toFile().exists()) {
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("settings.conf")) {
                Files.write(settingsPath, resourceAsStream.readAllBytes(), StandardOpenOption.CREATE_NEW);
            }
        }

        try (var f = FileConfig.of(path.resolve("settings.conf"))) {
            f.load();

            PluginConfig config = new PluginConfig();
            new ObjectConverter().toObject(f, config);
            this.config = config;
        }

        if (config.copyDefaults) {
            copy(structures, "cactus-farm.conf");
        }

        try (var paths = Files.newDirectoryStream(structures)){
            paths.forEach(this::loadStructure);
        }

    }

    private void loadStructure(Path path) {
        Structure structure;
        try (var f = FileConfig.of(path)){
            f.load();
            structure = new Structure();
            new ObjectConverter().toObject(f, structure);

        } catch (Throwable t) {
            t.printStackTrace();
            TownyColonies.logger.log(Level.SEVERE, "Unable to read structure file " + path.getFileName());
            return ;
        }
        structures.put(structure.id, structure);
        TownyColonies.logger.info("Loaded structure " + path.getFileName());
    }

    public void copy(Path structures, String file) {
        Path resolve = structures.resolve(file);
        if (!Files.exists(resolve)) {
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("structures/" + file)) {
                Files.write(resolve, resourceAsStream.readAllBytes(), StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Component> buildStructureLore(Structure structure,
                                              int townCount,
                                              int maxCount,
                                              Town town) {
        var mm = MiniMessage.miniMessage();

        List<Component> list = new ArrayList<>();
        List<Component> descL = structure.description.stream()
                .map(a -> config.structureLoreDescTemplate.replaceFirst("\\{line}", a))
                .map(mm::deserialize)
                .toList();

        for (String s : config.structureLoreTemplate) {
            if (s.contains("{desc}")) {
                list.addAll(descL);
            } else {
                s = s.replaceFirst("\\{towncount}", String.valueOf(townCount))
                    .replaceFirst("\\{maxcount}", String.valueOf(maxCount));

                list.add(mm.deserialize(s));
            }
        }
        return list;
    }
}
