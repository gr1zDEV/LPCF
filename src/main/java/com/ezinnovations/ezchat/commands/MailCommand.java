package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.managers.IgnoreManager;
import com.ezinnovations.ezchat.managers.MailManager;
import com.ezinnovations.ezchat.model.MailEntry;
import com.ezinnovations.ezchat.moderation.AdvertisingCheckService;
import com.ezinnovations.ezchat.moderation.AdvertisingDetectionResult;
import com.ezinnovations.ezchat.moderation.ProfanityCheckService;
import com.ezinnovations.ezchat.moderation.ProfanityDetectionResult;
import com.ezinnovations.ezchat.utils.FloodgateHook;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.CommunicationLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import com.ezinnovations.ezchat.service.MuteService;
import com.ezinnovations.ezchat.utils.TimeFormatUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MailCommand implements CommandExecutor {

    private static final int PAGE_SIZE = 6;

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final MailManager mailManager;
    private final ChatToggleManager chatToggleManager;
    private final IgnoreManager ignoreManager;
    private final FloodgateHook floodgateHook;
    private final CommunicationLogService communicationLogService;
    private final AuditLogService auditLogService;
    private final MuteService muteService;
    private final DiscordNotificationService discordNotificationService;
    private final AdvertisingCheckService advertisingCheckService;
    private final ProfanityCheckService profanityCheckService;

    public MailCommand(final EzChat plugin,
                       final FeatureManager featureManager,
                       final MailManager mailManager,
                       final ChatToggleManager chatToggleManager,
                       final IgnoreManager ignoreManager,
                       final FloodgateHook floodgateHook,
                       final CommunicationLogService communicationLogService,
                       final AuditLogService auditLogService,
                       final MuteService muteService,
                       final DiscordNotificationService discordNotificationService,
                       final AdvertisingCheckService advertisingCheckService,
                       final ProfanityCheckService profanityCheckService) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.mailManager = mailManager;
        this.chatToggleManager = chatToggleManager;
        this.ignoreManager = ignoreManager;
        this.floodgateHook = floodgateHook;
        this.communicationLogService = communicationLogService;
        this.auditLogService = auditLogService;
        this.muteService = muteService;
        this.discordNotificationService = discordNotificationService;
        this.advertisingCheckService = advertisingCheckService;
        this.profanityCheckService = profanityCheckService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!featureManager.isMailEnabled()) {
            player.sendMessage(plugin.colorize(featureManager.getFeatureDisabledMessage()));
            return true;
        }

        if (!player.hasPermission("ezchat.mail")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        final String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "inbox" -> { handleInbox(player, args); auditLogService.log(player, "MAIL_INBOX_VIEW", "viewed inbox"); discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "viewed inbox"); }
            case "unread" -> { handleUnread(player, args); auditLogService.log(player, "MAIL_UNREAD_VIEW", "viewed unread mail"); discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "viewed unread mail"); }
            case "sent" -> { handleSent(player, args); auditLogService.log(player, "MAIL_SENT_VIEW", "viewed sent mail"); discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "viewed sent mail"); }
            case "received" -> { handleReceived(player, args); auditLogService.log(player, "MAIL_RECEIVED_VIEW", "viewed received mail"); discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "viewed received mail"); }
            default -> handleSend(player, args);
        }
        return true;
    }

    private void handleSend(final Player sender, final String[] args) {
        if (muteService.isFeatureEnabled() && muteService.blockMail() && muteService.isMuted(sender.getUniqueId())) {
            muteService.sendMuteBlockedMessage(sender, "muted-mail", "&cYou are muted and cannot send mail.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.colorize("&cUsage: /mail <player> <message>"));
            return;
        }

        final OfflinePlayer target = resolveOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.player-not-found", "&cPlayer not found.")));
            return;
        }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.cannot-mail-self", "&cYou cannot mail yourself.")));
            return;
        }

        if (featureManager.isMailToggleEnabled() && chatToggleManager.isMailDisabled(target.getUniqueId())) {
            sender.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.target-mail-disabled", "&cThat player has mail disabled.")));
            return;
        }

        if (featureManager.isIgnoreEnabled()
                && ignoreManager.isIgnoring(target.getUniqueId(), sender.getUniqueId(), IgnoreManager.IgnoreType.MAIL)) {
            sender.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.target-ignoring-mail", "&cThat player is ignoring your mail.")));
            return;
        }

        final String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
        if (advertisingCheckService.shouldScanMail() && !advertisingCheckService.shouldBypass(sender)) {
            final AdvertisingDetectionResult detectionResult = advertisingCheckService.checkAdvertising(message);
            if (advertisingCheckService.handleBlockedMessage(sender, AdvertisingCheckService.CommunicationType.MAIL, detectionResult, message)) {
                return;
            }
        }
        if (profanityCheckService.shouldScanMail() && !profanityCheckService.shouldBypass(sender)) {
            final ProfanityDetectionResult detectionResult = profanityCheckService.checkProfanity(message);
            if (profanityCheckService.handleBlockedMessage(sender, ProfanityCheckService.CommunicationType.MAIL, detectionResult, message)) {
                return;
            }
        }
        final String receiverName = target.getName() != null ? target.getName() : args[0];

        mailManager.sendMail(sender.getUniqueId(), sender.getName(), target.getUniqueId(), receiverName, message);
        communicationLogService.logMail(sender.getUniqueId(), sender.getName(), target.getUniqueId(), receiverName, message);
        discordNotificationService.sendMail(sender.getUniqueId(), sender.getName(), target.getUniqueId(), receiverName, message);

        sender.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.sent-confirmation", "&aMail sent to {player}.")
                .replace("{player}", receiverName)));

        auditLogService.log(sender, "MAIL_SEND", "sent mail to " + receiverName);
        discordNotificationService.sendAuditAction(sender.getUniqueId(), sender.getName(), "sent mail to " + receiverName);

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.received-notify", "&eYou received new mail from {player}. Use /mail inbox")
                    .replace("{player}", sender.getName())));
        }
    }

    private void handleInbox(final Player player, final String[] args) {
        if (!player.hasPermission("ezchat.mail.inbox")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return;
        }

        final int page = readPage(args, 1);
        final List<MailEntry> inbox = mailManager.getInbox(player.getUniqueId());
        if (inbox.isEmpty()) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.inbox-empty", "&7Your inbox is empty.")));
            return;
        }

        sendMailPage(player, "&6&lMail Inbox", inbox, page, true, true);
    }

    private void handleUnread(final Player player, final String[] args) {
        if (!player.hasPermission("ezchat.mail.unread")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return;
        }

        final int page = readPage(args, 1);
        final List<MailEntry> unread = mailManager.getUnreadInbox(player.getUniqueId());
        if (unread.isEmpty()) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.unread-empty", "&7You have no unread mail.")));
            return;
        }

        sendMailPage(player, "&6&lUnread Mail", unread, page, true, true);
    }

    private void handleReceived(final Player player, final String[] args) {
        if (!player.hasPermission("ezchat.mail.received")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /mail received <player> [page]"));
            return;
        }

        final OfflinePlayer source = resolveOfflinePlayer(args[1]);
        if (source == null || source.getUniqueId() == null) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.player-not-found", "&cPlayer not found.")));
            return;
        }

        final int page = readPage(args, 2);
        final List<MailEntry> received = mailManager.getInboxFromSender(player.getUniqueId(), source.getUniqueId());
        if (received.isEmpty()) {
            final String sourceName = source.getName() != null ? source.getName() : args[1];
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-mail-from-player", "&7No received mail from {player}.")
                    .replace("{player}", sourceName)));
            return;
        }

        final String sourceName = source.getName() != null ? source.getName() : args[1];
        sendMailPage(player, "&6&lReceived from " + sourceName, received, page, true, true);
    }

    private void handleSent(final Player player, final String[] args) {
        if (!player.hasPermission("ezchat.mail.sent")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return;
        }

        if (args.length == 1 || isNumber(args[1])) {
            final int page = args.length > 1 ? readPage(args, 1) : 1;
            final Set<String> targets = mailManager.getUniqueSentTargetNames(player.getUniqueId());
            if (targets.isEmpty()) {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.sent-empty", "&7You have not sent any mail.")));
                return;
            }
            sendSentTargets(player, new ArrayList<>(targets), page);
            return;
        }

        final OfflinePlayer target = resolveOfflinePlayer(args[1]);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.player-not-found", "&cPlayer not found.")));
            return;
        }

        final int page = readPage(args, 2);
        final List<MailEntry> sentTo = mailManager.getSentToReceiver(player.getUniqueId(), target.getUniqueId());
        if (sentTo.isEmpty()) {
            final String targetName = target.getName() != null ? target.getName() : args[1];
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-mail-to-player", "&7No sent mail to {player}.")
                    .replace("{player}", targetName)));
            return;
        }

        final String targetName = target.getName() != null ? target.getName() : args[1];
        sendMailPage(player, "&6&lSent to " + targetName, sentTo, page, false, false);
    }

    private void sendSentTargets(final Player player, final List<String> targets, final int page) {
        final PageWindow<String> window = paginate(targets, page);
        player.sendMessage(plugin.colorize("&6&lSent Targets &7(Page " + window.page + "/" + window.totalPages + ")"));

        final boolean bedrock = floodgateHook.isBedrockPlayer(player);
        for (final String name : window.items) {
            if (bedrock) {
                player.sendMessage(plugin.colorize("&e- " + name + " &7(Type: /mail sent " + name + ")"));
            } else {
                final Component line = LegacyComponentSerializer.legacySection().deserialize("&e- " + name)
                        .clickEvent(ClickEvent.suggestCommand("/mail sent " + name));
                player.sendMessage(line);
            }
        }

        player.sendMessage(plugin.colorize("&7Type /mail sent <player> to view messages sent to that player."));
    }

    private void sendMailPage(final Player player,
                              final String title,
                              final List<MailEntry> entries,
                              final int page,
                              final boolean showSender,
                              final boolean markRead) {
        final PageWindow<MailEntry> window = paginate(entries, page);
        player.sendMessage(plugin.colorize(title + " &7(Page " + window.page + "/" + window.totalPages + ")"));

        for (final MailEntry entry : window.items) {
            final String person = showSender ? entry.getSenderName() : entry.getReceiverName();
            final String status = entry.isRead() ? "&aREAD" : "&cUNREAD";
            final String preview = shorten(entry.getMessage(), 48);
            final String time = TimeFormatUtil.formatTimestamp(entry.getTimestamp());
            player.sendMessage(plugin.colorize("&8#" + entry.getId().substring(0, 8) + " &7[" + status + "&7] &f" + person + " &8| &7" + time));
            player.sendMessage(plugin.colorize("&7" + preview));
        }

        if (markRead) {
            mailManager.markAsRead(player.getUniqueId(), window.items);
        }
    }

    private OfflinePlayer resolveOfflinePlayer(final String name) {
        final Player online = plugin.getServer().getPlayerExact(name);
        if (online != null) {
            return online;
        }

        final OfflinePlayer cached = plugin.getServer().getOfflinePlayerIfCached(name);
        if (cached != null && (cached.hasPlayedBefore() || cached.isOnline())) {
            return cached;
        }

        for (final OfflinePlayer offline : plugin.getServer().getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(name)
                    && (offline.hasPlayedBefore() || offline.isOnline())) {
                return offline;
            }
        }

        return null;
    }

    private int readPage(final String[] args, final int index) {
        if (args.length <= index || !isNumber(args[index])) {
            return 1;
        }
        return Math.max(1, Integer.parseInt(args[index]));
    }

    private boolean isNumber(final String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    private String shorten(final String text, final int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private <T> PageWindow<T> paginate(final List<T> list, final int requestedPage) {
        final int totalPages = Math.max(1, (int) Math.ceil((double) list.size() / PAGE_SIZE));
        final int page = Math.min(Math.max(1, requestedPage), totalPages);
        final int from = (page - 1) * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, list.size());
        return new PageWindow<>(list.subList(from, to), page, totalPages);
    }

    private void sendUsage(final Player player) {
        player.sendMessage(plugin.colorize("&6&lMail Commands"));
        player.sendMessage(plugin.colorize("&e/mail <player> <message> &7- Send persistent mail"));
        player.sendMessage(plugin.colorize("&e/mail inbox [page] &7- View inbox"));
        player.sendMessage(plugin.colorize("&e/mail unread [page] &7- View unread mail"));
        player.sendMessage(plugin.colorize("&e/mail received <player> [page] &7- View mail from player"));
        player.sendMessage(plugin.colorize("&e/mail sent [page] &7- List players you've mailed"));
        player.sendMessage(plugin.colorize("&e/mail sent <player> [page] &7- View mail sent to player"));
    }

    private record PageWindow<T>(List<T> items, int page, int totalPages) {
    }
}
