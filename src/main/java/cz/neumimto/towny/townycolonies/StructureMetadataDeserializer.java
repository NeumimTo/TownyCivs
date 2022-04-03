package cz.neumimto.towny.townycolonies;

import com.palmergames.bukkit.towny.object.metadata.DataFieldDeserializer;
import cz.neumimto.towny.townycolonies.StructureMetadata.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StructureMetadataDeserializer implements DataFieldDeserializer<StructureMetadata> {
    @Override
    public @Nullable StructureMetadata deserialize(@NotNull String key, @Nullable String value) {
        if (value == null) {
            return new StructureMetadata(new Data());
        }
        Data data = StructureMetadata.gson.fromJson(value, Data.class);
        return new StructureMetadata(data);
    }
}
