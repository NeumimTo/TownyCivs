package cz.neumimto.towny.townycolonies.schedulers;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.StructureMetadata;
import cz.neumimto.towny.townycolonies.StructureMetadata.LoadedStructure;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Level;

public class StructureScheduler implements Runnable {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureService structureService;

    @Override
    public void run() {
        for (LoadedStructure structure : structureService.getAllStructures()) {
            if (structure.lastTickTime <= System.currentTimeMillis()) {
                configurationService.findStructureById(structure.id).ifPresent(a->{
                    structure.lastTickTime = System.currentTimeMillis();
                    handleTick(structure);
                });
            }
        }
    }

    private void handleTick(LoadedStructure structure) {
        TownyColonies.logger.log(Level.INFO, "Ticking region " + structure.id);
    }
}
