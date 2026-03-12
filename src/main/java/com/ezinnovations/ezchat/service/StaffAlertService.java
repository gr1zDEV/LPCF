package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.StaffConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class StaffAlertService {

    private final EzChat plugin;
    private final StaffConfig staffConfig;
    private final CommunicationLogService communicationLogService;
    private final AuditLogService auditLogService;

    public StaffAlertService(final EzChat plugin,
                             final StaffConfig staffConfig,
                             final CommunicationLogService communicationLogService,
                             final AuditLogService auditLogService) {
        this.plugin = plugin;
        this.staffConfig = staffConfig;
        this.communicationLogService = communicationLogService;
        this.auditLogService = auditLogService;
    }

    public boolean isAlertsEnabled() {
        return staffConfig.isStaffAlertsEnabled();
    }

    public boolean isConsoleAlertCommandEnabled() {
        return staffConfig.isConsoleStaffAlertCommandEnabled();
    }

    public void sendStaffAlert(final String message) {
        sendStaffAlert(null, null, message, false);
    }

    public void sendStaffAlert(final CommandSender sender, final String message) {
        final boolean fromConsole = !(sender instanceof Player);
        final UUID actorUuid = sender instanceof Player player ? player.getUniqueId() : null;
        final String actorName = sender != null ? sender.getName() : "EzChat";
        sendStaffAlert(actorUuid, actorName, message, fromConsole);
    }

    private void sendStaffAlert(final UUID actorUuid,
                                final String actorName,
                                final String message,
                                final boolean fromConsole) {
        final String formatKey = fromConsole ? "console-staff-alert" : "staff-alert";
        final String fallback = fromConsole ? "&8[&cAlert&8] &7[Console]&f {message}" : "&8[&cAlert&8] &f{message}";
        final String rendered = plugin.colorize(staffConfig.getFormat(formatKey, fallback).replace("{message}", message));

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (final Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.hasPermission("ezchat.staffalerts")) {
                    online.sendMessage(rendered);
                }
            }
        });

        if (staffConfig.shouldLogStaffAlerts()) {
            communicationLogService.logStaffAlert(actorUuid, actorName, message);
            final String source = fromConsole ? "CONSOLE" : (actorName != null ? actorName : "SYSTEM");
            auditLogService.log(actorUuid, actorName, "STAFF_ALERT", "source=" + source + " message=" + message);
        }
    }
}
