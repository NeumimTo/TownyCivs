package cz.neumimto.towny.townycolonies.schedulers;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.StructureMetadata;
import cz.neumimto.towny.townycolonies.StructureMetadata.LoadedStructure;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.config.ConfigurationService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Level;

public class StructureScheduler implements Runnable {

    @Inject
    private ConfigurationService configurationService;

    @Override
    public void run() {
        Collection<Town> towns = TownyUniverse.getInstance().getTowns();
        long tick = System.currentTimeMillis();
        for (Town town : towns) {
            if (town.hasMeta(TownyColonies.METADATA_KEY)) {
                StructureMetadata metadata = town.getMetadata(TownyColonies.METADATA_KEY, StructureMetadata.class);
                if (metadata == null) {
                    continue;
                }
                StructureMetadata.Data value = metadata.getValue();
                if (value.structures == null) {
                    continue;
                }
                for (LoadedStructure structure : value.structures) {
                    configurationService.findStructureById(structure.id).ifPresent(d -> {
                        if (structure.lastTickTime + d.period * 1000 < tick) {
                            handleTick(structure);
                            structure.lastTickTime = tick;
                        }
                    });
                }
            }
        }
    }

    private void handleTick(LoadedStructure structure) {
        TownyColonies.logger.log(Level.INFO, "Ticking region " + structure.id);
    }
}
