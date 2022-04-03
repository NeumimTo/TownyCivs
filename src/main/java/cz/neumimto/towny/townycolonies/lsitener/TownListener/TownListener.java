package cz.neumimto.towny.townycolonies.lsitener.TownListener;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.TownyLoadedDatabaseEvent;
import com.palmergames.bukkit.towny.listeners.TownyPlayerListener;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycolonies.StructureMetadata;
import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.SubclaimService;
import cz.neumimto.towny.townycolonies.TownyColonies;
import cz.neumimto.towny.townycolonies.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class TownListener implements Listener {

    @Inject
    private SubclaimService subclaimService;

    @Inject
    private StructureService structureService;

    @EventHandler
    public void onTownLoad(TownyLoadedDatabaseEvent event) {
        Collection<Town> towns = TownyUniverse.getInstance().getTowns();
        for (Town town : towns) {
            StructureMetadata metadata = structureService.getMetadata(town);
            if (metadata != null) {
                for (StructureMetadata.LoadedStructure structure : metadata.getValue().structures) {
                    Optional<Region> region = subclaimService.createRegion(structure);
                    region.ifPresent(value -> subclaimService.registerRegion(value));
                }
            }
        }
    }
}
