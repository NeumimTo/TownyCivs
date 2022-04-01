package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import cz.neumimto.towny.townycolonies.config.Structure;
import org.bukkit.Location;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@Singleton
public class StructureService {

    public List<StructureMetadata.LoadedStructure> getAllStructures(Town town) {
        CustomDataField<?> metadata = town.getMetadata(TownyColonies.METADATA_KEY);
        if (metadata != null) {
            return ((StructureMetadata)metadata).getValue().structures;
        }
        return Collections.emptyList();
    }

    public void isValidLocation(Structure structure, Location location) {

    }

    public void addToTown(Structure structure, Town town,  Location location) {

    }

}
