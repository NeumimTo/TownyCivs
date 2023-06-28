package cz.neumimto.towny.townycivs;


import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.config.Structure;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TownService implements Listener {

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private StructureService structureService;

    private static Map<UUID, Set<UUID>> onlineStateCache = new ConcurrentHashMap<>();


    public void resetPlayerActivity() {
        onlineStateCache.clear();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Resident resident = TownyUniverse.getInstance().getResident(onlinePlayer.getUniqueId());
            if (resident == null || !resident.hasTown()) {
                continue;
            }
            Town town = resident.getTownOrNull();
            onlineStateCache.compute(town.getUUID(), (uuid, uuids) -> {
                if (uuids == null) {
                    uuids = new HashSet<>();
                }
                uuids.add(onlinePlayer.getUniqueId());
                return uuids;
            });
        }
    }

    public int getTownPointsMax(Town town) {
        Map<Integer, Integer> townpoints = configurationService.config.townpoints;
        int level = town.getLevel();
        Integer integer = townpoints.get(level);
        return integer == null ? 0 : integer;
    }

    public int getTownPointsSpent(Town town) {
        return structureService.getAllStructures(town).stream()
                .filter(s->!s.editMode.get())
                .mapToInt(s->s.structureDef.townPointPrice)
                .sum();
    }

    public boolean hasPointsForStructure(Town town, Structure structure) {
        return getTownPointsMax(town) + structure.townPointPrice >= getTownPointsSpent(town);
    }
}
