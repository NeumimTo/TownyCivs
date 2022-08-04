package cz.neumimto.towny.townycolonies.schedulers;

import cz.neumimto.towny.townycolonies.mechanics.VirtualItem;
import cz.neumimto.towny.townycolonies.model.LoadedStructure;
import cz.neumimto.towny.townycolonies.model.VirtualContainer;
import cz.neumimto.towny.townycolonies.model.VirtualContent;

import java.util.List;
import java.util.UUID;

public class VirtualInventoryUpdateCommand extends ScheduledCommand {

    private LoadedStructure loadedStructure;
    private UUID containerUUID;
    private List<VirtualItem> newContent;

    public VirtualInventoryUpdateCommand(LoadedStructure loadedStructure, UUID containerUUID, List<VirtualItem> newContent) {
        super(null);
        this.loadedStructure = loadedStructure;
        this.containerUUID = containerUUID;
        this.newContent = newContent;
    }

    @Override
    public void executeFromAsyncThread(LoadedStructure loadedStructure) {
        for (VirtualContent virtualContent : loadedStructure.storage) {
            if (virtualContent.containerUUID.equals(containerUUID)) {
                virtualContent.content = newContent;
                break;
            }
        }
    }
}
