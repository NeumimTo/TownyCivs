package cz.neumimto.towny.townycolonies.mechanics;

import cz.neumimto.towny.townycolonies.StructureService;
import cz.neumimto.towny.townycolonies.mechanics.common.StringWrapper;

import javax.inject.Inject;

class MStructure implements Mechanic<StringWrapper> {

    @Inject
    private StructureService structureService;

    @Override
    public boolean check(TownContext townContext, StringWrapper configContext) {
        return structureService.getAllStructuresByTown()
                .values()
                .stream()
                .anyMatch(a -> a.stream().anyMatch(l -> l.structureId.contains(configContext.value)));
    }

    @Override
    public void nokmessage(TownContext townContext, StringWrapper configuration) {
        townContext.resident.getPlayer().sendMessage("You dont have permission to use " + townContext.structure.name);
    }

    @Override
    public String id() {
        return Mechanics.STRUCTURE;
    }

    @Override
    public StringWrapper getNew() {
        return new StringWrapper();
    }

}
