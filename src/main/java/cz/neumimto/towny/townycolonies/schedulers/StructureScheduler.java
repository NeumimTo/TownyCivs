package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;

import javax.inject.Inject;
import java.util.logging.Level;

public class StructureScheduler implements Runnable {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureService structureService;

    @Override
    public void run() {
        for (LoadedStructure structure : structureService.getAllStructures()) {
            if (structure.lastTickTime <= System.currentTimeMillis() && !structure.editMode && structure.structureDef != null) {
                structure.lastTickTime = System.currentTimeMillis();
                handleTick(structure);
            }
        }
    }

    private void handleTick(LoadedStructure structure) {
        TownyColonies.logger.log(Level.INFO, "Ticking region " + structure.structureDef.id);
    }
}
