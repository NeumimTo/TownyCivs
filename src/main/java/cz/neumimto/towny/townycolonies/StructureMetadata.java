package cz.neumimto.towny.townycolonies;

import com.google.gson.*;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import it.unimi.dsi.fastutil.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StructureMetadata extends CustomDataField<StructureMetadata.Data> {

    public static Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, (JsonSerializer<Location>) (src, typeOfSrc, context) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("w",src.getWorld().getName());
                    jsonObject.addProperty("x",src.getBlockX());
                    jsonObject.addProperty("y",src.getBlockY());
                    jsonObject.addProperty("z",src.getBlockZ());
                    return jsonObject;
                })
                .registerTypeAdapter(Location.class, (JsonDeserializer<Location>) (json, typeOfSrc, context) -> {
                    JsonObject asJsonObject = json.getAsJsonObject();
                    String world = asJsonObject.get("w").getAsString();
                    int x = asJsonObject.get("x").getAsInt();
                    int y = asJsonObject.get("y").getAsInt();
                    int z = asJsonObject.get("z").getAsInt();
                    return new Location(Bukkit.getWorld(world),x,y,z);
                })
                .create();
    }

    public StructureMetadata(Data data) {
        super(TownyColonies.METADATA_KEY,data);
    }

    public static String typeID() {
        return "TownyColonies-Structure";
    }

    @Override
    public @NotNull String getTypeID() {
        return typeID();
    }

    @Override
    public void setValueFromString(String strValue) {
        setValue(gson.fromJson(strValue, Data.class));
    }

    public String serializeValueToString() {
        return gson.toJson(getValue()).replaceAll("\"","\\\\\"");
    }

    @Override
    public boolean shouldDisplayInStatus() {
        return false;
    }

    @Override
    protected String displayFormattedValue() {
        return "";
    }

    @Override
    public @NotNull CustomDataField<Data> clone() {
        return new StructureMetadata(getValue());
    }

    public static class Data {
        public List<LoadedStructure> structures;
        public Map<String, Integer> blueprints;

        public Data() {
            this.structures = new ArrayList<>();
            this.blueprints = new HashMap<>();
        }
    }

    public static class LoadedStructure {
        public UUID uuid;
        public String id;
        public Location pos1;
        public Location pos2;
        public Location container;
        public long lastTickTime;
    }
}
