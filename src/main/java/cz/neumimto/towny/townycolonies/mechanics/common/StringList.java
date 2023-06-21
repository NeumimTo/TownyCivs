package cz.neumimto.towny.townycolonies.mechanics.common;

import com.electronwill.nightconfig.core.conversion.Path;
import com.typesafe.config.Optional;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StringList implements Wrapper {

    @Path("List")
    public List<String> configItems = new ArrayList<>();

    @Override
    public boolean isObject() {
        return true;
    }
}
