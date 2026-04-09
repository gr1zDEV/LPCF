# EzChat Documentation

EzChat is a communication and moderation suite for **Paper/Folia 1.21+** servers. It combines chat formatting, private messaging, offline mail, moderation checks, logging, and staff communication in one plugin.

## What you can configure

- Public chat formatting and per-group formats (LuckPerms metadata aware)
- Private messages (`/msg`, `/reply`) and player-side toggles
- Persistent offline mail with inbox/sent/unread views
- Ignore rules (`ALL`, `CHAT`, `MSG`, `MAIL`)
- Moderation controls (mute, anti-advertising, profanity, anti-flood)
- Communication + audit logging for staff review
- Staff utilities (staff chat, social spy, mail spy, alerts)
- Optional Discord webhook routing for selected event types

## Quick start checklist

1. Install **LuckPerms** (required).
2. Drop EzChat into `plugins/` and start the server once.
3. Stop the server and edit generated YAML files.
4. Assign player/staff permissions in LuckPerms.
5. Start the server and validate features in-game.
6. Use `/ezchat reload` for normal config iteration.

## Documentation map

- [Installation](installation.md): requirements, first boot, update flow
- [Commands](commands.md): command reference by role
- [Permissions](permissions.md): complete permission list
- [Configuration](configuration.md): file-by-file configuration guide
- [Logging](logging.md): communication and audit log behavior
- [Mute System](mute-system.md): mute command flows and behavior
- [Anti-Flood](anti-flood.md): repetitive message protection settings

## Operational notes

- EzChat requires LuckPerms and disables itself if LuckPerms is unavailable.
- PlaceholderAPI, floodgate, and Geyser are optional integrations.
- SQLite is bundled and used for persistent toggles, mail, logs, ignore entries, and mutes.
