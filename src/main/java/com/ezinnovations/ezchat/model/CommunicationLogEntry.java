package com.ezinnovations.ezchat.model;

public record CommunicationLogEntry(long id,
                                    String logType,
                                    String senderUuid,
                                    String senderName,
                                    String receiverUuid,
                                    String receiverName,
                                    String message,
                                    long timestamp) {
}
