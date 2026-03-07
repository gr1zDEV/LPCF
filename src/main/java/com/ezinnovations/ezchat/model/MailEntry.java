package com.ezinnovations.ezchat.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MailEntry {

    private final String id;
    private final UUID sender;
    private final String senderName;
    private final UUID receiver;
    private final String receiverName;
    private final String message;
    private final long timestamp;
    private boolean read;

    public MailEntry(final String id,
                     final UUID sender,
                     final String senderName,
                     final UUID receiver,
                     final String receiverName,
                     final String message,
                     final long timestamp,
                     final boolean read) {
        this.id = id;
        this.sender = sender;
        this.senderName = senderName;
        this.receiver = receiver;
        this.receiverName = receiverName;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public UUID getSender() {
        return sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(final boolean read) {
        this.read = read;
    }

    public Map<String, Object> toInboxMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("sender", sender.toString());
        map.put("sender-name", senderName);
        map.put("message", message);
        map.put("timestamp", timestamp);
        map.put("read", read);
        return map;
    }

    public Map<String, Object> toSentMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("receiver", receiver.toString());
        map.put("receiver-name", receiverName);
        map.put("message", message);
        map.put("timestamp", timestamp);
        return map;
    }
}
