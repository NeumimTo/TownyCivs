package cz.neumimto.towny.townycolonies.utils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class Wildcards {

    public static boolean matches(String text, String pattern) {
        return text.matches(pattern.replace("?", ".?").replace("*", ".*?"));
    }

    public static Set<String> substract(String expr, Collection<String> set) {
        return set.stream()
                .filter(a -> matches(a, expr))
                .collect(Collectors.toSet());
    }
}