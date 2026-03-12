package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.StaffConfig;
import com.ezinnovations.ezchat.service.StaffAlertService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class StaffAlertSubcommand {

    private final EzChat plugin;
    private final StaffConfig staffConfig;
    private final StaffAlertService staffAlertService;

    public StaffAlertSubcommand(final EzChat plugin, final StaffConfig staffConfig, final StaffAlertService staffAlertService) {
        this.plugin = plugin;
        this.staffConfig = staffConfig;
        this.staffAlertService = staffAlertService;
    }

    public boolean execute(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player) {
                sender.sendMessage(plugin.colorize(staffConfig.getMessage("invalid-usage-staffalert", "&cUsage: /ezchat staffalert <message>")));
            }
            return true;
        }

        if (!staffAlertService.isAlertsEnabled() || !staffAlertService.isConsoleAlertCommandEnabled()) {
            if (sender instanceof Player) {
                sender.sendMessage(plugin.colorize(staffConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            }
            return true;
        }

        if (sender instanceof Player && !sender.hasPermission("ezchat.staffalerts.send")) {
            sender.sendMessage(plugin.colorize(staffConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        staffAlertService.sendStaffAlert(sender, message);
        return true;
    }

    public List<String> tabComplete(final CommandSender sender, final String[] args) {
        if (args.length == 1 && sender.hasPermission("ezchat.staffalerts.send")) {
            return List.of("staffalert").stream().filter(v -> v.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
