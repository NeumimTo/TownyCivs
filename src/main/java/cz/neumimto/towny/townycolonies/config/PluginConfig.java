package cz.neumimto.towny.townycolonies.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import org.bukkit.Material;

import java.util.List;

public class PluginConfig {

    @Path("copy_defaults")
    public boolean copyDefaults;

    @Path("inventory_blocker_material")
    @Conversion(Structure.MaterialConversion.class)
    public Material inventoryBlockerMaterial;

    @Path("inventory_blocker_custom_model_data")
    public Integer inventoryBlockerCustomModelData;

    @Path("storage")
    public String storage;

    @Path("db_url")
    public String dbUrl;

    @Path("db_password")
    public String dbPassword;

    @Path("db_user")
    public String dbUser;

    @Path("db_database")
    public String dbDatabase;

}
