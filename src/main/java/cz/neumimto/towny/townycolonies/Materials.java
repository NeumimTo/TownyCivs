package cz.neumimto.towny.townycolonies;


import com.electronwill.nightconfig.core.Config;
import cz.neumimto.towny.townycolonies.utils.Wildcards;
import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Materials {

    private static Map<Material, Set<String>> blockRegistry = new HashMap<>();
    private static Map<String, Set<Material>> materialsByTag = new HashMap<>();

    public static Collection<String> getTags(Material material) {
        return blockRegistry.getOrDefault(material, Collections.emptySet());
    }

    public static Collection<Material> getMaterials(String tag) {
        return materialsByTag.getOrDefault(tag, Collections.emptySet());
    }

    public static void init(Config config) {
        blockRegistry.clear();
        for (Config.Entry entry : config.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            parse(key, value);
        }
    }

    private static void parse(String tag, String blocks) {
        String[] s = blocks.split(" ");
        List<String> collect = Stream.of(Material.values())
                .filter(a -> !a.isLegacy())
                .map(a -> a.name().toLowerCase())
                .collect(Collectors.toList());
        for (String s1 : s) {
            Set<String> substract = Wildcards.substract(s1.toLowerCase(), collect);
            for (String s2 : substract) {
                Material material = Material.matchMaterial(s2);
                if (material != null) {
                    String group = "tc:" + tag;
                    Set<String> set = new HashSet<>();
                    set.add(group);
                    blockRegistry.merge(material, set, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
                    Set<Material> m = new HashSet<>();
                    m.add(material);
                    materialsByTag.merge(group, m, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
                }
            }
        }
    }

}
