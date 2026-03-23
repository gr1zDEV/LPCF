package com.ezinnovations.ezchat.moderation;

public record FloodDetectionResult(boolean blocked, FloodType floodType) {

    public static FloodDetectionResult allowed() {
        return new FloodDetectionResult(false, null);
    }

    public static FloodDetectionResult blocked(final FloodType floodType) {
        return new FloodDetectionResult(true, floodType);
    }
}
