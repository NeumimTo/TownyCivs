package cz.neumimto.towny.townycolonies;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MaterialsTest {

    //@Test
    public void test() throws Exception {
        File p = new File(getClass().getClassLoader().getResource("settings.conf").getFile());
        try (var c = FileConfig.of(p)){
            c.load();
            Config a = c.get("blockdb");
            Materials.init(a);
        }
    }
}
