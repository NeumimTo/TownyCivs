package cz.neumimto.towny.townycolonies.config;

import com.electronwill.nightconfig.core.conversion.Path;

import java.util.List;

public class PluginConfig {

    @Path("copy_defaults")
    public boolean copyDefaults;

    @Path("blueprint_lore_desc_template")
    public String blueprintLoreDescTemplate;

    @Path("blueprint_lore_template")
    public List<String> blueprintLoreTemplate;

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
