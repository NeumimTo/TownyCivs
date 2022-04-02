package cz.neumimto.towny.townycolonies.resourcepack;

import cz.neumimto.towny.townycolonies.config.ConfigurationService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

@Singleton
public class ResourcePackGenerator {

    @Inject
    private ConfigurationService configurationService;

    public void generatePack(Path configDir) throws IOException {
        Path resourcepack = configDir.resolve("resourcepack");

        try {
            Files.delete(resourcepack);
            Files.createDirectory(resourcepack);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Files.write(resourcepack.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":8,\"description\":\"§9§lTownycolonies gui\"}}".getBytes(), StandardOpenOption.CREATE_NEW);
        Path fontDir = Files.createDirectories(resourcepack.resolve("assets/minecraft/font/"));
        Path gifs = Files.createDirectories(resourcepack.resolve("assets/townycolonies/gifs"));

        Set<Map.Entry<String, String>> entries = configurationService.config.fontdb.entrySet();


    }
}
