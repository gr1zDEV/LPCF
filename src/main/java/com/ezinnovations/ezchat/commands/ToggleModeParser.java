package com.ezinnovations.ezchat.commands;

public final class ToggleModeParser {

    private ToggleModeParser() {
    }

    public static ToggleMode parse(final String[] args) {
        if (args.length == 0) {
            return ToggleMode.TOGGLE;
        }

        if (args.length != 1) {
            return null;
        }

        if ("on".equalsIgnoreCase(args[0])) {
            return ToggleMode.ON;
        }

        if ("off".equalsIgnoreCase(args[0])) {
            return ToggleMode.OFF;
        }

        return null;
    }
}
