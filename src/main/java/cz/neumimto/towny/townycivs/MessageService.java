package cz.neumimto.towny.townycivs;

import com.palmergames.bukkit.towny.object.Translatable;
import cz.neumimto.towny.townycivs.config.Structure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class MessageService {

    private MiniMessage mm() {
        return MiniMessage.miniMessage();
    }

    private TagResolver placeholder(String k, Object v) {
        return Placeholder.component(k, Component.text(String.valueOf(v)));
    }

    private String t(String msg, CommandSender commandSender) {
        return Translatable.of(msg).forLocale(commandSender);
    }

    public Component errReqAboveY(Player player, Structure structure) {
        return mm().deserialize(
                t("toco_error_aboveY", player),
                placeholder("structure", structure.name),
                placeholder("Y", structure.placeRequirements.aboveY)
        );
    }

    public Component errReqBellowY(Player player, Structure structure) {
        return mm().deserialize(
                t("toco_error_bellowY", player),
                placeholder("structure", structure.name),
                placeholder("Y", structure.placeRequirements.bellowY)
        );
    }

    public Component notOnWhitelistedBiome(Player player, Structure structure, String biome) {
        return mm().deserialize(
                t("toco_error_blacklisted_biome", player),
                placeholder("structure", structure.name),
                placeholder("biome", biome)
        );
    }

    public Component notOnWhitelistedBiomes(Player player, Structure structure, List<String> biomeWhitelist) {
        return mm().deserialize(
                t("toco_error_whitelisted_biome", player),
                placeholder("structure", structure.name),
                placeholder("biomes", String.join(", ", biomeWhitelist))
        );
    }

    public Component missingPermission(Player player) {
        return mm().deserialize(
                t("toco_error_no_permission", player)
        );
    }

    public Component townBankNotEnoughFunds(Player player, Structure structure, double amountReq) {
        return mm().deserialize(
                t("toco_error_not_enough_funds", player),
                placeholder("structure", structure.name),
                placeholder("amount", amountReq)
        );
    }
}
