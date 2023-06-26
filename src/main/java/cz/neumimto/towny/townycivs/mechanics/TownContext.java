package cz.neumimto.towny.townycivs.mechanics;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.config.Structure;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.CountDownLatch;

public class TownContext {
    public Town town;
    public Resident resident;

    public Player player;

    public Structure structure;
    public Location structureCenterLocation;

    public LoadedStructure loadedStructure;
    public CountDownLatch cdl;
    public boolean cdlResult;
}
