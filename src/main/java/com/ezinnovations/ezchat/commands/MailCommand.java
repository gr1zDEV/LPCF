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
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.CommunicationLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import com.ezinnovations.ezchat.service.MuteService;
import com.ezinnovations.ezchat.service.SpyService;
import com.ezinnovations.ezchat.utils.FloodgateHook;
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
import java.util.Optional;
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
    private final SpyService spyService;

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
                       final ProfanityCheckService profanityCheckService,
                       final SpyService spyService) {
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
        this.spyService = spyService;
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
            case "inbox" -> handleInbox(player, args);
            case "unread" -> handleUnread(player, args);
            case "sent" -> handleSent(player, args);
            case "received" -> handleReceived(player, args);
            case "read" -> handleRead(player, args);
            case "delete" -> handleDelete(player, args);
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

        if (featureManager.isIgnoreEnabled() && ignoreManager.isIgnoring(target.getUniqueId(), sender.getUniqueId(), IgnoreManager.IgnoreType.MAIL)) {
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
        final MailEntry entry = mailManager.sendMail(sender.getUniqueId(), sender.getName(), target.getUniqueId(), receiverName, message);
        sender.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.sent-confirmation", "&aMail sent to {player}.")
                .replace("{player}", receiverName)));

        final Player onlineTarget = plugin.getServer().getPlayer(target.getUniqueId());
        if (onlineTarget != null) {
            onlineTarget.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.received-notify", "&eYou received new mail from {player}. Use /mail inbox")
                    .replace("{player}", sender.getName())));
        }

        communicationLogService.logMail(sender.getUniqueId(), sender.getName(), target.getUniqueId(), receiverName, message);
        auditLogService.log(sender, "MAIL_SEND", "sent mail to " + receiverName + " (id=" + entry.getId() + ")");
        discordNotificationService.sendMail(sender.getUniqueId(), sender.getName(), target.getUniqueId(), receiverName, message);
        spyService.dispatchMailSpy(sender, receiverName, message);
    }

    private void handleInbox(final Player player, final String[] args) {
        final int page = readPage(args, 1);
        final List<MailEntry> inbox = mailManager.getInbox(player.getUniqueId());
        if (inbox.isEmpty()) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.inbox-empty", "&7Your inbox is empty.")));
            return;
        }

        sendMailPage(player, "&6&lInbox", inbox, page, true, false);
        auditLogService.log(player, "MAIL_INBOX_VIEW", "viewed inbox");
    }

    private void handleUnread(final Player player, final String[] args) {
        final int page = readPage(args, 1);
        final List<MailEntry> unread = mailManager.getUnreadInbox(player.getUniqueId());
        if (unread.isEmpty()) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.unread-empty", "&7You have no unread mail.")));
            return;
        }

        sendMailPage(player, "&6&lUnread Mail", unread, page, true, true);
        auditLogService.log(player, "MAIL_UNREAD_VIEW", "viewed unread mail");
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

        final OfflinePlayer sender = resolveOfflinePlayer(args[1]);
        if (sender == null || sender.getUniqueId() == null) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.player-not-found", "&cPlayer not found.")));
            return;
        }

        final int page = readPage(args, 2);
        final List<MailEntry> received = mailManager.getInboxFromSender(player.getUniqueId(), sender.getUniqueId());
        if (received.isEmpty()) {
            final String senderName = sender.getName() != null ? sender.getName() : args[1];
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-mail-from-player", "&7No received mail from {player}.")
                    .replace("{player}", senderName)));
            return;
        }

        sendMailPage(player, "&6&lReceived from " + (sender.getName() == null ? args[1] : sender.getName()), received, page, true, true);
        auditLogService.log(player, "MAIL_RECEIVED_VIEW", "viewed received mail from " + (sender.getName() == null ? args[1] : sender.getName()));
    }

    private void handleSent(final Player player, final String[] args) {
        if (!player.hasPermission("ezchat.mail.sent")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return;
        }

        if (args.length == 1 || isNumber(args[1])) {
            final int page = readPage(args, 1);
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
        auditLogService.log(player, "MAIL_SENT_VIEW", "viewed sent mail to " + targetName);
    }

    private void handleRead(final Player player, final String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.read-invalid-usage", "&cUsage: /mail read <id>")));
            return;
        }
        final Optional<MailEntry> mailEntry = mailManager.getInboxById(player.getUniqueId(), args[1]);
        if (mailEntry.isEmpty()) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-not-found", "&cMail entry not found.")));
            return;
        }
        final MailEntry entry = mailEntry.get();
        if (!entry.isRead()) {
            mailManager.markAsRead(player.getUniqueId(), entry.getId());
        }

        player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-read-header", "&8&m----------------")));
        player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-read-from", "&eFrom: &f{player}").replace("{player}", entry.getSenderName())));
        player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-read-to", "&eTo: &f{player}").replace("{player}", entry.getReceiverName())));
        player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-read-time", "&eTime: &f{time}").replace("{time}", TimeFormatUtil.formatTimestamp(entry.getTimestamp()))));
        player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-read-message", "&eMessage: &f{message}").replace("{message}", entry.getMessage())));
        player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-read-header", "&8&m----------------")));
        auditLogService.log(player, "MAIL_READ", "read mail id=" + entry.getId());
    }

    private void handleDelete(final Player player, final String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.delete-invalid-usage", "&cUsage: /mail delete <id>")));
            return;
        }

        if (!mailManager.deleteInboxById(player.getUniqueId(), args[1])) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-not-found", "&cMail entry not found.")));
            return;
        }

        player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.mail-deleted", "&aMail deleted.")));
        auditLogService.log(player, "MAIL_DELETE", "deleted mail id=" + args[1]);
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
            player.sendMessage(plugin.colorize("&8#" + entry.getId() + " &7[" + status + "&7] &f" + person + " &8| &7" + time));
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
        player.sendMessage(plugin.colorize("&e/mail read <id> &7- Read full mail"));
        player.sendMessage(plugin.colorize("&e/mail delete <id> &7- Delete mail from your inbox"));
        player.sendMessage(plugin.colorize("&e/mail received <player> [page] &7- View mail from player"));
        player.sendMessage(plugin.colorize("&e/mail sent [page] &7- List players you've mailed"));
        player.sendMessage(plugin.colorize("&e/mail sent <player> [page] &7- View mail sent to player"));
    }

    private record PageWindow<T>(List<T> items, int page, int totalPages) {
    }
}
