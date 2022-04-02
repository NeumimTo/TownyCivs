package cz.neumimto.towny.townycolonies.gui;

import com.palmergames.bukkit.towny.TownyAPI;
import cz.neumimto.towny.townycolonies.gui.api.ConfigurableGui;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class TCGui extends ConfigurableGui {

    public TCGui(String fileName, Path workingDir) {
        super(fileName, workingDir);
    }

    @Override
    protected String getAssetAsString(String guiFileName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(guiFileName)) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String translateKey(CommandSender commandSender, String translationKey) {
        return translationKey;
    }
}
