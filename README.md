# EzChat

> A LuckPerms-powered communication suite for **Paper/Folia 1.21+** with chat formatting, private messaging, mail, moderation, logging, staff tools, and per-player visibility toggles.

EzChat is no longer just a chat formatter. The current plugin build includes:

- Public chat formatting with LuckPerms metadata and PlaceholderAPI support.
- Private messages, replies, ignore controls, and persistent mail.
- Moderation tools such as mutes, anti-advertising, and profanity filtering.
- Staff chat, staff alerts, social spy, and mail spy.
- Server broadcasts plus player-controlled toggles for chat, mail, death, join/leave, and server messages.
- SQLite-backed persistence for toggles, ignores, mail, communication logs, audit logs, and mutes.
- Optional Discord webhook routing for multiple event types.

---

## Table of Contents

- [Compatibility](#compatibility)
- [Core Features](#core-features)
- [Commands](#commands)
- [Permissions](#permissions)
- [Feature Toggles](#feature-toggles)
- [Configuration Files](#configuration-files)
- [Storage](#storage)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Notes and Gotchas](#notes-and-gotchas)

---

## Compatibility

- **Server software:** Paper / Folia
- **API version:** 1.21
- **Java:** 21
- **Required dependency:** LuckPerms
- **Optional integrations:** PlaceholderAPI, floodgate, Geyser-Spigot
- **Database:** SQLite

---

## Core Features

### 1. Public chat formatting

- Uses Paper's modern chat pipeline.
- Supports a global `chat-format` string.
- Supports per-group overrides with `group-formats.<primary-group>`.
- Resolves LuckPerms metadata placeholders:
  - `{prefix}` / `{suffix}`
  - `{prefixes}` / `{suffixes}`
  - `{username-color}` / `{message-color}`
- Also supports:
  - `{message}`
  - `{name}`
  - `{displayname}`
  - `{world}`
- PlaceholderAPI placeholders are resolved when PlaceholderAPI is installed.
- Legacy `&` colors and hex colors are handled according to permissions.

### 2. Player chat color permissions

- `ezchat.colorcodes` allows legacy codes like `&a` and `&l`.
- `ezchat.rgbcodes` allows hex input like `&#12abef` and Bukkit-style `&x&1&2...`.
- Behavior by permission combination:
  - both permissions: legacy + hex allowed
  - only `ezchat.colorcodes`: legacy allowed, hex removed
  - only `ezchat.rgbcodes`: hex allowed, legacy removed
  - neither: all formatting codes stripped

### 3. Private messaging

- `/msg <player> <message>` sends direct messages.
- `/reply <message>` replies to the most recent DM target.
- `/togglemsg [on|off]` lets players opt in or out of DMs.
- Messages can be blocked by mute state, ignore state, anti-advertising, or profanity rules.
- Private message activity can be mirrored to Discord and to social spy when enabled.

### 4. Ignore system

- `/ignore <player> <ALL|CHAT|MSG|MAIL>` toggles ignore entries.
- `/ignorelist` shows your current ignore entries.
- `/unignore <player> <ALL|CHAT|MSG|MAIL|ALL_TYPES>` removes a specific ignore rule.
- Ignore rules can independently affect public chat, private messages, and mail.

### 5. Persistent mail

- `/mail <player> <message>` sends offline-safe mail.
- Inbox browsing is built in:
  - `/mail inbox [page]`
  - `/mail unread [page]`
  - `/mail received <player> [page]`
  - `/mail sent [page]`
  - `/mail sent <player> [page]`
  - `/mail read <id>`
  - `/mail delete <id>`
- Players can disable incoming mail with `/togglemail [on|off]`.
- Optional unread mail login notifications are supported.
- Mail can be monitored with mail spy for staff.

### 6. Moderation and safety

#### Mutes

- `/ezchat mute <player> <reason...>` sets a permanent mute.
- `/ezchat mutetemp <player> <duration> <reason...>` sets a temporary mute.
- `/ezchat unmute <player> [reason]` removes a mute.
- `/ezchat muteinfo <player>` shows current mute details.
- Muted players can be blocked from:
  - public chat
  - private messages
  - mail

#### Anti-advertising

- Separate config file for advertising checks.
- Can scan:
  - public chat
  - private messages
  - mail
- Can block:
  - IPv4 addresses
  - domains
  - Discord invites
  - custom patterns
- Includes whitelist support and bypass permission.

#### Profanity filter

- Separate config file for profanity checks.
- Can scan:
  - public chat
  - private messages
  - mail
  - optionally staff chat
- Supports blocked words and regex patterns.
- Includes bypass permission.

### 7. Logging and audit trail

- Communication logging supports:
  - public chat
  - private messages
  - mail
- Audit logging tracks administrative and toggle actions.
- `/ezchat logs` supports these modes:
  - `/ezchat logs player <player> [page]`
  - `/ezchat logs between <player1> <player2> [page]`
  - `/ezchat logs public <player> [page]`
  - `/ezchat logs msg <player> [page]`
  - `/ezchat logs mail <player> [page]`
  - `/ezchat logs search <keyword> [page]`

### 8. Staff tools

- `/staffchat <message>` and `/sc <message>` send staff chat messages.
- `/togglestaffchat` enables staff chat mode for normal chat input.
- `/ezchat staffalert <message>` sends a staff alert.
- `/togglesocialspy` toggles live spying for private messages.
- `/togglemailspy` toggles live spying for mail.

### 9. Server, death, and join/leave messaging

- `/ezchat broadcast <message>` sends a configurable server broadcast.
- `/toggleservermsg [on|off]` toggles receipt of server broadcasts.
- `/toggledeathmsg [on|off]` toggles death messages.
- `/togglejoinleavemsg [on|off]` toggles join/leave messages.
- Death messages and join/leave messages support custom formatting, optional sounds, and optional logging.

### 10. Discord webhook integration

Discord webhook support is optional and configurable per event type. Current event routing includes:

- public chat
- private messages
- mail
- mute actions
- audit actions
- server broadcasts
- death messages
- join messages
- leave messages

Webhook identity, avatar handling, embed usage, and per-event formatting are configurable in `discord.yml`.

### 11. Persistence and Bedrock awareness

- Toggle states, ignores, mail, logs, and mutes are persisted in SQLite.
- Floodgate is auto-detected when present.
- Bedrock players receive safe fallback behavior where clickable Java chat interactions are not appropriate.

---

## Commands

### Main `/ezchat` subcommands

| Command | Description |
| --- | --- |
| `/ezchat reload` | Reloads config and feature state. |
| `/ezchat clear` | Clears chat and broadcasts the configured clear message. |
| `/ezchat debug <player>` | Shows resolved LuckPerms metadata, group format source, PAPI status, and chat color permissions. |
| `/ezchat logs player <player> [page]` | Shows all logged communication for a player. |
| `/ezchat logs between <player1> <player2> [page]` | Shows communication between two players. |
| `/ezchat logs public <player> [page]` | Shows public chat entries for a player. |
| `/ezchat logs msg <player> [page]` | Shows private message entries for a player. |
| `/ezchat logs mail <player> [page]` | Shows mail entries for a player. |
| `/ezchat logs search <keyword> [page]` | Searches communication logs by keyword. |
| `/ezchat mute <player> <reason...>` | Permanently mutes a player. |
| `/ezchat mutetemp <player> <duration> <reason...>` | Temporarily mutes a player. |
| `/ezchat unmute <player> [reason]` | Removes a mute. |
| `/ezchat muteinfo <player>` | Displays active mute details. |
| `/ezchat staffalert <message>` | Sends a staff alert. |
| `/ezchat broadcast <message>` | Sends a server broadcast. |

### Player communication commands

| Command | Description |
| --- | --- |
| `/togglechat [on|off]` | Show or hide public chat for yourself. |
| `/msg <player> <message>` | Send a private message. |
| `/reply <message>` | Reply to your last DM target. |
| `/togglemsg [on|off]` | Enable or disable private messages. |
| `/ignore <player> <ALL|CHAT|MSG|MAIL>` | Toggle an ignore entry. |
| `/ignorelist` | View your ignore entries. |
| `/unignore <player> <ALL|CHAT|MSG|MAIL|ALL_TYPES>` | Remove a specific ignore entry. |
| `/mail <player> <message>` | Send persistent mail. |
| `/mail inbox [page]` | View your inbox. |
| `/mail unread [page]` | View unread mail. |
| `/mail received <player> [page]` | View received mail from one player. |
| `/mail sent [page]` | List players you have sent mail to. |
| `/mail sent <player> [page]` | View mail you sent to a player. |
| `/mail read <id>` | Read a mail entry in full. |
| `/mail delete <id>` | Delete a mail entry from your inbox. |
| `/togglemail [on|off]` | Enable or disable mail delivery. |

### Staff visibility and utility commands

| Command | Description |
| --- | --- |
| `/togglesocialspy` | Toggle live private-message spy. |
| `/togglemailspy` | Toggle live mail spy. |
| `/toggleservermsg [on|off]` | Toggle server broadcast visibility. |
| `/toggledeathmsg [on|off]` | Toggle death message visibility. |
| `/togglejoinleavemsg [on|off]` | Toggle join/leave message visibility. |
| `/staffchat <message>` | Send a staff chat message. |
| `/sc <message>` | Alias for `/staffchat`. |
| `/togglestaffchat` | Toggle staff chat mode. |

---

## Permissions

### Core and player permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.chattoggle` | `true` | Use `/togglechat`. |
| `ezchat.msg` | `true` | Use `/msg`. |
| `ezchat.reply` | `true` | Use `/reply`. |
| `ezchat.togglemsg` | `true` | Use `/togglemsg`. |
| `ezchat.ignore` | `true` | Use `/ignore`, `/ignorelist`, and `/unignore`. |
| `ezchat.mail` | `true` | Use `/mail`. |
| `ezchat.mail.inbox` | `true` | View inbox-style mail pages. |
| `ezchat.mail.sent` | `true` | Use `/mail sent ...`. |
| `ezchat.mail.received` | `true` | Use `/mail received ...`. |
| `ezchat.mail.unread` | `true` | Use `/mail unread`. |
| `ezchat.mail.read` | `true` | Use `/mail read <id>`. |
| `ezchat.mail.delete` | `true` | Use `/mail delete <id>`. |
| `ezchat.togglemail` | `true` | Use `/togglemail`. |
| `ezchat.servermsg.toggle` | `true` | Use `/toggleservermsg`. |
| `ezchat.deathmsg.toggle` | `true` | Use `/toggledeathmsg`. |
| `ezchat.joinleavemsg.toggle` | `true` | Use `/togglejoinleavemsg`. |
| `ezchat.colorcodes` | `false` | Allow legacy color/style codes in chat input. |
| `ezchat.rgbcodes` | `false` | Allow hex color codes in chat input. |

### Staff and admin permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.reload` | `op` | Use `/ezchat reload`. |
| `ezchat.clearchat` | `op` | Use `/ezchat clear`. |
| `ezchat.debug` | `op` | Use `/ezchat debug <player>`. |
| `ezchat.logs` | `op` | Use `/ezchat logs ...`. |
| `ezchat.mute` | `op` | Use `/ezchat mute`. |
| `ezchat.mutetemp` | `op` | Use `/ezchat mutetemp`. |
| `ezchat.unmute` | `op` | Use `/ezchat unmute`. |
| `ezchat.muteinfo` | `op` | Use `/ezchat muteinfo`. |
| `ezchat.socialspy` | `op` | Use `/togglesocialspy` and receive PM spy output. |
| `ezchat.mailspy` | `op` | Use `/togglemailspy` and receive mail spy output. |
| `ezchat.broadcast` | `op` | Use `/ezchat broadcast` as a player. |
| `ezchat.staffchat` | `op` | Send and receive staff chat. |
| `ezchat.staffchat.toggle` | `op` | Use `/togglestaffchat`. |
| `ezchat.staffalerts` | `op` | Receive staff alerts. |
| `ezchat.staffalerts.send` | `op` | Send `/ezchat staffalert` as a player. |

### Moderation bypass permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.bypass.advertising` | `op` | Bypass anti-advertising checks. |
| `ezchat.bypass.profanity` | `op` | Bypass profanity checks. |

---

## Feature Toggles

These are the main enable/disable switches shipped in the current config set.

### `config.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.public-chat.enabled` | `true` | Enables formatted public chat handling. |
| `features.chat-toggle.enabled` | `true` | Enables `/togglechat`. |
| `features.private-messages.enabled` | `true` | Enables `/msg` and `/reply`. |
| `features.private-message-toggle.enabled` | `true` | Enables `/togglemsg`. |
| `features.ignore.enabled` | `true` | Enables ignore checks and commands. |
| `features.mail.enabled` | `true` | Enables the mail system. |
| `features.mail-toggle.enabled` | `true` | Enables `/togglemail`. |

### `mail.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `settings.unread-login-notify.enabled` | `true` | Sends unread mail notifications on login. |

### `logs.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `true` | Enables communication/audit logging features. |
| `settings.logging-enabled` | `true` | Enables writing log data. |
| `settings.public-chat` | `true` | Log public chat. |
| `settings.private-messages` | `true` | Log private messages. |
| `settings.mail` | `true` | Log mail. |
| `settings.audit` | `true` | Log audit actions. |

### `mute.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `true` | Enables mute commands and mute enforcement. |
| `settings.block-public-chat` | `true` | Prevent muted players from public chat. |
| `settings.block-private-messages` | `true` | Prevent muted players from private messaging. |
| `settings.block-mail` | `true` | Prevent muted players from using mail. |

### `anti-spam.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `true` | Enables anti-advertising support. |
| `features.anti-advertising.enabled` | `true` | Enables the advertising detector. |
| `checks.public-chat` | `true` | Scan public chat. |
| `checks.private-messages` | `true` | Scan private messages. |
| `checks.mail` | `false` | Scan mail. |
| `patterns.block-ipv4` | `true` | Block IPv4 addresses. |
| `patterns.block-domains` | `true` | Block domains. |
| `patterns.block-discord-invites` | `true` | Block Discord invites. |
| `actions.block-message` | `true` | Cancel blocked messages. |
| `actions.notify-sender` | `true` | Notify the sender when blocked. |
| `actions.audit-log` | `true` | Write audit logs for blocks. |
| `actions.discord-log` | `false` | Forward blocked events to Discord. |

### `profanity.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `true` | Enables profanity filtering. |
| `checks.public-chat` | `true` | Scan public chat. |
| `checks.private-messages` | `true` | Scan private messages. |
| `checks.mail` | `true` | Scan mail. |
| `checks.staff-chat` | `false` | Scan staff chat. |
| `actions.block-message` | `true` | Cancel blocked messages. |
| `actions.notify-sender` | `true` | Notify the sender when blocked. |
| `actions.audit-log` | `true` | Write audit logs for blocks. |
| `actions.discord-log` | `false` | Forward blocked events to Discord. |
| `word-list.enabled` | `true` | Use blocked words list. |
| `regex.enabled` | `true` | Use regex patterns. |

### `discord.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `false` | Enables Discord webhook support. |
| `webhook.use-embeds` | `true` | Uses embed payloads when supported. |
| `events.public-chat` | `true` | Route public chat events. |
| `events.private-messages` | `false` | Route private messages. |
| `events.mail` | `false` | Route mail. |
| `events.mute-actions` | `true` | Route mute actions. |
| `events.audit-actions` | `true` | Route audit actions. |
| `events.server-broadcasts` | `false` | Route broadcasts. |
| `events.death-messages` | `false` | Route death messages. |
| `events.join-messages` | `false` | Route joins. |
| `events.leave-messages` | `false` | Route leaves. |

### `server-message.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `true` | Enables server broadcast messaging. |
| `settings.log-broadcasts` | `true` | Logs broadcasts. |
| `settings.log-toggle-actions` | `true` | Logs broadcast toggle actions. |

### `death-message.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `true` | Enables custom death message handling. |
| `settings.log-death-messages` | `true` | Logs death messages. |
| `settings.use-vanilla-death-message` | `true` | Reuses vanilla death text before formatting. |
| `settings.default-enabled` | `true` | New players receive death messages by default. |
| `sound.enabled` | `true` | Plays the configured death-message sound. |

### `join-leave.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.enabled` | `true` | Enables join/leave messaging. |
| `settings.log-join-leave-messages` | `true` | Logs join/leave messages. |
| `settings.default-enabled` | `true` | New players receive join/leave messages by default. |
| `settings.use-vanilla-join-message` | `true` | Reuses vanilla join text before formatting. |
| `settings.use-vanilla-leave-message` | `true` | Reuses vanilla leave text before formatting. |
| `sounds.join.enabled` | `true` | Plays the configured join sound. |
| `sounds.leave.enabled` | `false` | Plays the configured leave sound. |

### `staff.yml`

| Path | Default | Effect |
| --- | --- | --- |
| `features.staff-chat.enabled` | `true` | Enables staff chat commands and mode. |
| `features.staff-alerts.enabled` | `true` | Enables staff alerts. |
| `features.console-staff-alert-command.enabled` | `true` | Allows console staff alert command support. |
| `settings.log-staff-chat` | `true` | Logs staff chat. |
| `settings.log-staff-alerts` | `true` | Logs staff alerts. |

---

## Configuration Files

EzChat currently ships with these resource files:

| File | Purpose |
| --- | --- |
| `config.yml` | Core public chat, private messaging, ignore, and mail feature flags plus main chat format. |
| `private-message.yml` | Direct message formats and DM toggle messages. |
| `mail.yml` | Mail notifications, inbox strings, read/delete messages, and login unread notices. |
| `logs.yml` | Communication log and audit log settings. |
| `mute.yml` | Mute enforcement behavior and mute command messages. |
| `anti-spam.yml` | Anti-advertising checks, patterns, whitelist, and actions. |
| `profanity.yml` | Profanity scanning rules, word-list behavior, and regex patterns. |
| `blocked-words.yml` | Blocked word entries for the profanity system. |
| `staff.yml` | Staff chat, staff alerts, and spy message formats. |
| `server-message.yml` | Broadcast format, toggle behavior, and broadcast messages. |
| `death-message.yml` | Death message formatting, logging, and sounds. |
| `join-leave.yml` | Join/leave formatting, defaults, and sounds. |
| `discord.yml` | Discord webhook routing, identity, avatar, and format options. |
| `plugin.yml` | Bukkit command and permission registration metadata. |

---

## Storage

EzChat stores runtime data in SQLite. The current README-relevant storage includes:

- player toggle states
- ignore relationships
- persistent mail
- communication logs
- audit logs
- mute records

The default database file is `plugins/EzChat/database.db`.

---

## Installation

1. Build or download `EzChat-<version>.jar`.
2. Install **LuckPerms** on your Paper/Folia server.
3. Optionally install **PlaceholderAPI** if you want PAPI support in formats.
4. Optionally install **floodgate** and/or **Geyser-Spigot** for Bedrock-aware behavior.
5. Put the jar in `plugins/`.
6. Start the server once to generate config files.
7. Edit the YAML files in `plugins/EzChat/`.
8. Run `/ezchat reload` or restart the server.

---

## Building from Source

### Requirements

- Java 21
- Maven 3.9+

### Build command

```bash
mvn package
```

This produces a versioned jar named like `EzChat-1.0.0.jar`.

---

## Notes and Gotchas

- **LuckPerms is required.** EzChat disables itself if LuckPerms is missing.
- **No PlaceholderAPI installed?** PAPI placeholders are simply skipped.
- **Chat formatting duplication can happen** if another chat plugin is also formatting messages. Disable overlapping format features in plugins such as EssentialsChat, VentureChat, DeluxeChat, and similar tools.
- **`/ezchat debug <player>` is your best first diagnostic tool** for prefix, suffix, meta colors, group format selection, and color permission checks.
- **Toggle state is persistent.** Players keep their visibility/toggle choices across restarts.
- **Only `/staffchat` has an explicit alias in the current plugin metadata** via `/sc`.

