package cz.neumimto.towny.townycivs;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Town;
import cz.neumimto.towny.townycivs.config.BuyRequirements;
import cz.neumimto.towny.townycivs.config.ConfigurationService;
import cz.neumimto.towny.townycivs.config.PlaceRequirements;
import cz.neumimto.towny.townycivs.config.Structure;
import cz.neumimto.towny.townycivs.db.Flatfile;
import cz.neumimto.towny.townycivs.db.Storage;
import cz.neumimto.towny.townycivs.mechanics.TownContext;
import cz.neumimto.towny.townycivs.model.ActionResult;
import cz.neumimto.towny.townycivs.model.LoadedStructure;
import cz.neumimto.towny.townycivs.model.Region;
import cz.neumimto.towny.townycivs.model.StructureAndCount;
import cz.neumimto.towny.townycivs.schedulers.FolliaScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class StructureService {

    private Map<UUID, LoadedStructure> structures = new ConcurrentHashMap<>();
    private Map<UUID, Set<LoadedStructure>> structuresByTown = new ConcurrentHashMap<>();

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SubclaimService subclaimService;

    @Inject
    private FolliaScheduler structureScheduler;

    @Inject
    private ManagementService managementService;

    @Inject
    private MessageService messageService;

    public Map<UUID, Set<LoadedStructure>> getAllStructuresByTown() {
        return structuresByTown;
    }


    public Collection<LoadedStructure> getAllStructures(Town town) {
        return structures.values().stream().filter(a -> a.town.equals(town.getUUID())).collect(Collectors.toSet());
    }

    public ItemStack toItemStack(Structure structure, int count) {
        ItemStack itemStack = new ItemStack(structure.material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        var mm = MiniMessage.miniMessage();
        itemMeta.displayName(mm.deserialize(structure.name));
        itemMeta.setCustomModelData(structure.customModelData);

        List<Component> lore = configurationService.buildStructureLore(structure, count, structure.maxCount);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<StructureAndCount> findTownStructures(Town town) {
        Collection<Structure> allStructures = configurationService.getAll();
        Collection<LoadedStructure> townStructures = getAllStructures(town);

        Map<Structure, Integer> alreadyBuilt = new HashMap<>();
        List<Structure> avalaible = new ArrayList<>();

        for (Structure structure : allStructures) {
            boolean found = false;
            for (LoadedStructure townStructure : townStructures) {
                if (townStructure.structureDef == structure) {
                    alreadyBuilt.merge(structure, 1, Integer::sum);
                    found = true;
                }
            }

            if (!found) {
                avalaible.add(structure);
            }
        }

        List<StructureAndCount> merged = new ArrayList<>();
        for (Map.Entry<Structure, Integer> entry : alreadyBuilt.entrySet()) {
            merged.add(new StructureAndCount(entry.getKey(), entry.getValue()));
        }
        avalaible.sort(Comparator.comparing(o -> o.name));

        for (Structure structure : avalaible) {
            merged.add(new StructureAndCount(structure, 0));
        }

        return merged;
    }

    public StructureAndCount findTownStructureById(Town town, Structure structure) {
        Collection<LoadedStructure> townStructures = getAllStructures(town);
        int count = 0;
        for (LoadedStructure townStructure : townStructures) {
            if (townStructure.structureDef == structure) {
                count++;
            }
        }
        return new StructureAndCount(structure, count);
    }

    public ItemStack buyBlueprint(TownContext townContext) {

        if (townContext.structure.buyRequirements != null) {
            townContext.town.getAccount().withdraw(townContext.structure.buyRequirements.price,
                    "townycivs - bought " + townContext.structure.id);
        }
        TownyMessaging.sendPrefixedTownMessage(townContext.town, townContext.player.getName() + " bought " + townContext.structure.name);
        return toBlueprintItemStack(townContext.structure);
    }

    private ItemStack toBlueprintItemStack(Structure structure) {
        ItemStack itemStack = new ItemStack(structure.material);
        itemStack.editMeta(itemMeta -> {
            itemMeta.displayName(Component.text("Blueprint - " + structure.name));
            itemMeta.setCustomModelData(structure.customModelData);
        });
        return itemStack;
    }

    public void addToTown(Town town, LoadedStructure loadedStructure) {
        structures.put(loadedStructure.uuid, loadedStructure);

        Set<LoadedStructure> set = ConcurrentHashMap.newKeySet();
        set.add(loadedStructure);

        structuresByTown.merge(loadedStructure.town, set, (a, b) -> {
            a.addAll(b);
            return a;
        });

    }

    public void loadAll() {
        structures.clear();
        new Storage(TownyCivs.injector.getInstance(Flatfile.class));
        Collection<LoadedStructure> loaded = Storage.allStructures();
        Collection<UUID> towns = TownyAPI.getInstance().getTowns().stream().map(Town::getUUID).collect(Collectors.toSet());
        loaded.stream()
                .filter(a -> a.structureDef != null)
                .filter(a -> towns.contains(a.town))
                .peek(a -> {
                    if (a.editMode.get()) {
                        managementService.structuresBeingEdited.add(a.uuid);
                    }
                })
                .peek(a -> subclaimService.createRegion(a).ifPresent(b -> subclaimService.registerRegion(b, a)))
                .forEach(a -> {
                    Town town = TownyAPI.getInstance().getTown(a.town);
                    addToTown(town, a);
                });
    }

    public void delete(Region region, Player player) {
        subclaimService.delete(region);
        LoadedStructure l = region.loadedStructure;
        structures.remove(l.uuid);
        Set<LoadedStructure> loadedStructures = structuresByTown.getOrDefault(l.town, Collections.emptySet());
        loadedStructures.remove(l);
        Town town = TownyAPI.getInstance().getTown(l.town);

        TownyMessaging.sendPrefixedTownMessage(town, player.getName() + " deleted structure " + l.structureDef.name);

        Storage.scheduleRemove(l);
    }

    public Optional<LoadedStructure> findStructureByUUID(UUID uuid) {
        return Optional.ofNullable(structures.get(uuid));
    }

    public ActionResult checkBuyRequirements(Player player, Town town, Structure structure) {
        BuyRequirements buyRequirements = structure.buyRequirements;
        if (buyRequirements == null) {
            return ActionResult.OK;
        }

        List<Component> errorMessages = new ArrayList<>();

        for (String perm : buyRequirements.permission) {
            if (!player.hasPermission(perm)) {
                errorMessages.add(messageService.missingPermission(player));
                break;
            }
        }

        if (town.getAccount().getHoldingBalance() < buyRequirements.price) {
            errorMessages.add(messageService.townBankNotEnoughFunds(player, structure, buyRequirements.price));
        }

        return ActionResult.of(errorMessages);
    }

    public ActionResult checkPlaceRequirements(Player player, Town town, Location location, Structure structure) {
        PlaceRequirements placeRequirements = structure.placeRequirements;
        if (placeRequirements == null) {
            return ActionResult.OK;
        }

        List<Component> errorMessages = new ArrayList<>();
        if (placeRequirements.aboveY != null && placeRequirements.aboveY < location.getBlockY()) {
           errorMessages.add(messageService.errReqAboveY(player, structure));
        }

        if (placeRequirements.bellowY != null && placeRequirements.bellowY > location.getBlockY()) {
            errorMessages.add(messageService.errReqBellowY(player, structure));
        }

        if (placeRequirements.biomeBlacklist != null) {
            NamespacedKey biomeKey = Bukkit.getUnsafe().getBiomeKey(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (placeRequirements.biomeBlacklist.contains(biomeKey.asString())) {
                errorMessages.add(messageService.notOnWhitelistedBiome(player, structure, biomeKey.asString()));
            }
        }

        if (placeRequirements.biomeWhitelist != null) {
            NamespacedKey biomeKey = Bukkit.getUnsafe().getBiomeKey(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (!placeRequirements.biomeWhitelist.contains(biomeKey.asString())) {
                errorMessages.add(messageService.notOnWhitelistedBiomes(player, structure, placeRequirements.biomeWhitelist));
            }
        }

        for (String perm : placeRequirements.permission) {
            if (!player.hasPermission(perm)) {
                errorMessages.add(messageService.missingPermission(player));
                break;
            }
        }

        return ActionResult.of(errorMessages);
    }
}
