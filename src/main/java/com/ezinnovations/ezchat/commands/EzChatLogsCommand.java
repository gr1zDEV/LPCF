package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.LogsConfig;
import com.ezinnovations.ezchat.model.CommunicationLogEntry;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.CommunicationLogService;
import com.ezinnovations.ezchat.utils.LogFormatUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class EzChatLogsCommand {

    private final EzChat plugin;
    private final LogsConfig logsConfig;
    private final CommunicationLogService communicationLogService;
    private final AuditLogService auditLogService;

    public EzChatLogsCommand(final EzChat plugin,
                             final LogsConfig logsConfig,
                             final CommunicationLogService communicationLogService,
                             final AuditLogService auditLogService) {
        this.plugin = plugin;
        this.logsConfig = logsConfig;
        this.communicationLogService = communicationLogService;
        this.auditLogService = auditLogService;
    }

    public boolean execute(final CommandSender sender, final String[] args) {
        if (!logsConfig.isFeatureEnabled()) {
            sender.sendMessage(plugin.colorize(logsConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (!sender.hasPermission("ezchat.logs")) {
            sender.sendMessage(plugin.colorize(logsConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.colorize(logsConfig.getMessage("invalid-usage", "&cInvalid usage.")));
            return true;
        }

        final String mode = args[1].toLowerCase();
        final int page;
        final String details;
        final CommunicationLogService.QueryResult result;

        switch (mode) {
            case "player" -> {
                final String player = args[2];
                page = parsePage(args, 3);
                result = communicationLogService.findByPlayer(player, page);
                details = "queried logs player " + player + " page " + page;
            }
            case "between" -> {
                if (args.length < 4) {
                    sender.sendMessage(plugin.colorize(logsConfig.getMessage("invalid-usage", "&cInvalid usage.")));
                    return true;
                }
                final String playerOne = args[2];
                final String playerTwo = args[3];
                page = parsePage(args, 4);
                result = communicationLogService.findBetween(playerOne, playerTwo, page);
                details = "queried logs between " + playerOne + " and " + playerTwo + " page " + page;
            }
            case "public" -> {
                final String player = args[2];
                page = parsePage(args, 3);
                result = communicationLogService.findPublicByPlayer(player, page);
                details = "queried logs public " + player + " page " + page;
            }
            case "msg" -> {
                final String player = args[2];
                page = parsePage(args, 3);
                result = communicationLogService.findMsgByPlayer(player, page);
                details = "queried logs msg " + player + " page " + page;
            }
            case "mail" -> {
                final String player = args[2];
                page = parsePage(args, 3);
                result = communicationLogService.findMailByPlayer(player, page);
                details = "queried logs mail " + player + " page " + page;
            }
            case "search" -> {
                final String keyword = args[2];
                page = parsePage(args, 3);
                result = communicationLogService.search(keyword, page);
                details = "queried logs search \"" + keyword + "\" page " + page;
            }
            default -> {
                sender.sendMessage(plugin.colorize(logsConfig.getMessage("invalid-usage", "&cInvalid usage.")));
                return true;
            }
        }

        final List<CommunicationLogEntry> entries = result.entries();
        if (entries.isEmpty()) {
            sender.sendMessage(plugin.colorize(logsConfig.getMessage("no-results", "&7No matching logs found.")));
            auditLookup(sender, details);
            return true;
        }

        sender.sendMessage(plugin.colorize(logsConfig.getMessage("header", "&8&m----------------")));
        int index = 1 + (Math.max(1, page) - 1) * logsConfig.getPageSize();
        for (final CommunicationLogEntry entry : entries) {
            sender.sendMessage(plugin.colorize(LogFormatUtil.formatLine(index, entry, logsConfig)));
            index++;
        }
        sender.sendMessage(plugin.colorize("&7Page " + Math.max(1, page) + "/" + result.totalPages()));
        sender.sendMessage(plugin.colorize("&7Type /ezchat logs " + mode + " " + buildTail(args, mode) + " " + (Math.max(1, page) + 1) + " for next page."));
        sender.sendMessage(plugin.colorize(logsConfig.getMessage("footer", "&8&m----------------")));

        auditLookup(sender, details);
        return true;
    }

    private void auditLookup(final CommandSender sender, final String details) {
        if (sender instanceof org.bukkit.entity.Player player) {
            auditLogService.log(player, "LOGS_QUERY", details);
        } else {
            auditLogService.log(null, "CONSOLE", "LOGS_QUERY", details);
        }
    }

    private String buildTail(final String[] args, final String mode) {
        return switch (mode) {
            case "between" -> args[2] + " " + args[3];
            default -> args[2];
        };
    }

    private int parsePage(final String[] args, final int index) {
        if (args.length <= index) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(args[index]));
        } catch (final NumberFormatException exception) {
            return 1;
        }
    }
}
