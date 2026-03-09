package com.ezinnovations.ezchat.utils;

import com.ezinnovations.ezchat.config.LogsConfig;
import com.ezinnovations.ezchat.model.CommunicationLogEntry;

public final class LogFormatUtil {

    private LogFormatUtil() {
    }

    public static String formatLine(final int displayIndex, final CommunicationLogEntry entry, final LogsConfig logsConfig) {
        final String time = logsConfig.formatTimestamp(entry.timestamp());
        final String type = entry.logType();

        return switch (type) {
            case "PUBLIC" -> "&7[" + displayIndex + "] " + time + " &8| &fPUBLIC &8| &f" + entry.senderName() + ": " + entry.message();
            case "MSG", "MAIL" -> "&7[" + displayIndex + "] " + time + " &8| &f" + type + " &8| &f"
                    + entry.senderName() + " &7-> &f" + entry.receiverName() + ": " + entry.message();
            default -> "&7[" + displayIndex + "] " + time + " &8| &f" + type + " &8| &f" + entry.senderName() + ": " + entry.message();
        };
    }
}
