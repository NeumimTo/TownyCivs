package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.mechanics.common.StringList;

public class Biome implements Mechanic<StringList> {

    @Override
    public boolean check(TownContext townContext, StringList configContext) {

        org.bukkit.block.Biome computedBiome = townContext.structureCenterLocation.getBlock().getComputedBiome();
        return configContext.configItems.contains(computedBiome.getKey().asString());
    }

    @Override
    public String id() {
        return Mechanics.BIOME;
    }


    @Override
    public StringList getNew() {
        return new StringList();
    }
}
