package com.ezinnovations.ezchat.moderation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class PlayerFloodState {

    private long lastMessageTimestamp;
    private final Deque<Long> messageTimestamps = new ArrayDeque<>();
    private final Map<String, Deque<Long>> duplicateTimestamps = new HashMap<>();

    public synchronized FloodDetectionResult checkAndRecord(final long now,
                                                            final String normalizedMessage,
                                                            final boolean checkCooldown,
                                                            final long cooldownMilliseconds,
                                                            final boolean checkDuplicate,
                                                            final long duplicateWindowMilliseconds,
                                                            final int maxDuplicates,
                                                            final boolean checkBurst,
                                                            final long burstWindowMilliseconds,
                                                            final int maxMessages) {
        if (checkCooldown && lastMessageTimestamp > 0L && now - lastMessageTimestamp < cooldownMilliseconds) {
            return FloodDetectionResult.blocked(FloodType.COOLDOWN);
        }

        if (checkDuplicate) {
            pruneDuplicateEntries(now, duplicateWindowMilliseconds);
            final Deque<Long> duplicateHistory = duplicateTimestamps.computeIfAbsent(normalizedMessage, key -> new ArrayDeque<>());
            if (duplicateHistory.size() >= maxDuplicates) {
                return FloodDetectionResult.blocked(FloodType.DUPLICATE);
            }
        }

        if (checkBurst) {
            pruneMessageTimestamps(now, burstWindowMilliseconds);
            if (messageTimestamps.size() >= maxMessages) {
                return FloodDetectionResult.blocked(FloodType.BURST);
            }
        }

        lastMessageTimestamp = now;

        if (checkBurst) {
            messageTimestamps.addLast(now);
        }

        if (checkDuplicate) {
            duplicateTimestamps.computeIfAbsent(normalizedMessage, key -> new ArrayDeque<>()).addLast(now);
        }

        return FloodDetectionResult.allowed();
    }

    private void pruneMessageTimestamps(final long now, final long windowMilliseconds) {
        while (!messageTimestamps.isEmpty() && now - messageTimestamps.peekFirst() > windowMilliseconds) {
            messageTimestamps.removeFirst();
        }
    }

    private void pruneDuplicateEntries(final long now, final long windowMilliseconds) {
        final Iterator<Map.Entry<String, Deque<Long>>> iterator = duplicateTimestamps.entrySet().iterator();
        while (iterator.hasNext()) {
            final Deque<Long> timestamps = iterator.next().getValue();
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMilliseconds) {
                timestamps.removeFirst();
            }
            if (timestamps.isEmpty()) {
                iterator.remove();
            }
        }
    }
}
