# Mute System

The mute system allows staff to block one or more communication channels for rule enforcement.

## Commands

| Command | Description |
| --- | --- |
| `/ezchat mute <player> <reason...>` | Apply a permanent mute. |
| `/ezchat mutetemp <player> <duration> <reason...>` | Apply a temporary mute with duration token. |
| `/ezchat unmute <player> [reason]` | Remove active mute. |
| `/ezchat muteinfo <player>` | Inspect current and recent mute state. |

## Channel blocking behavior

Depending on `mute.yml`, mutes can block:

- Public chat
- Private messages
- Mail

Muted players receive configurable feedback when blocked actions are attempted.

## Temporary mute duration format

Use duration suffixes when applying temporary mutes:

- `m` = minutes
- `h` = hours
- `d` = days

Examples: `45m`, `6h`, `3d`.

## Operational recommendations

- Always include clear, staff-readable reasons.
- Review `muteinfo` before reapplying mutes to avoid overlap.
- Keep mute actions in audit logs for moderator accountability.
