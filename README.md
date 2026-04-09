# EzChat

> A production-ready communication suite for **Paper/Folia 1.21+** that combines modern chat formatting, direct messaging, offline mail, moderation, audit logging, staff workflows, and optional Discord webhooks in one plugin.

EzChat is built for servers that want a single, cohesive communication stack instead of a patchwork of small plugins. It uses **LuckPerms** for metadata-driven formatting, stores persistent player state in **SQLite**, supports **PlaceholderAPI** expansions, and includes the day-to-day controls staff teams expect for live operations.

---

## Why EzChat

- **One plugin, broad coverage**: public chat, direct messages, offline mail, ignore controls, moderation, staff chat, alerts, logging, and message visibility toggles.
- **Operationally safe**: persistent storage for mutes, toggles, ignores, mail, and communication history.
- **Configurable without code changes**: dedicated YAML files for each major subsystem.
- **Modern server support**: targets **Paper/Folia 1.21** and **Java 21**.
- **Optional ecosystem integrations**: LuckPerms, PlaceholderAPI, Floodgate/Geyser, and Discord webhooks.

---

## Table of Contents

- [Compatibility](#compatibility)
- [Feature Overview](#feature-overview)
- [Chat Formatting Placeholders](#chat-formatting-placeholders)
- [PlaceholderAPI Expansion](#placeholderapi-expansion)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration Files](#configuration-files)
- [Discord Webhook Events](#discord-webhook-events)
- [Storage and Persistence](#storage-and-persistence)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Operational Notes](#operational-notes)
- [Documentation](#documentation)

---

## Compatibility

| Item | Requirement / Support |
| --- | --- |
| Server software | Paper / Folia |
| Minecraft API | 1.21 |
| Java | 21 |
| Required dependency | LuckPerms |
| Optional dependencies | PlaceholderAPI, floodgate, Geyser-Spigot |
| Persistent storage | SQLite |

---

## Feature Overview

### 1. Public chat formatting

EzChat formats chat through Paper's modern chat pipeline and resolves **LuckPerms metadata**, built-in chat variables, and optional PlaceholderAPI placeholders.

Highlights:

- Global `chat-format` plus per-group overrides via `group-formats.<group>`.
- LuckPerms-aware formatting for prefixes, suffixes, and metadata colors.
- Permission-aware player color support for both legacy and RGB color input.
- Optional PlaceholderAPI resolution inside chat formats when PlaceholderAPI is installed.
- Staff chat mode interception so staff can speak privately without changing commands.

### 2. Player formatting permissions

EzChat gives you granular control over message styling:

- `ezchat.colorcodes` allows legacy codes such as `&a`, `&l`, and `&7`.
- `ezchat.rgbcodes` allows hex input such as `&#12abef` and Bukkit-style `&x&1&2&a&b&e&f`.

Behavior matrix:

| Permission set | Result |
| --- | --- |
| `ezchat.colorcodes` + `ezchat.rgbcodes` | Legacy and hex colors are allowed |
| `ezchat.colorcodes` only | Legacy colors stay, hex codes are stripped |
| `ezchat.rgbcodes` only | Hex colors stay, legacy codes are stripped |
| Neither | Formatting codes are removed |

### 3. Direct messaging

Private messaging is built in and backed by moderation and visibility controls.

- `/msg <player> <message>` for direct messages.
- `/reply <message>` to answer the latest DM target.
- `/togglemsg [on|off]` so players can opt out of DMs.
- Messages can be blocked by mute state, ignore state, advertising checks, and profanity checks.
- Social spy can mirror PM activity to authorized staff.
- Direct messages can be routed to Discord webhooks when enabled.

### 4. Ignore controls

Players can independently block communication from specific users.

- Ignore scopes: `ALL`, `CHAT`, `MSG`, and `MAIL`.
- `/ignorelist` shows active ignore rules.
- `/unignore` removes specific rules, including `ALL_TYPES` cleanup.
- Ignore logic is persisted, so settings survive restarts.

### 5. Persistent offline mail

EzChat includes a mailbox system for asynchronous communication.

- `/mail <player> <message>` sends mail even when the target is offline.
- Inbox and history views for received, unread, and sent mail.
- Read and delete workflows for individual mail entries.
- Optional unread-mail login notifications.
- Mail spy support for staff oversight.
- Bedrock-aware behavior for players where Java-only interactive chat patterns are not suitable.

### 6. Moderation and communication safety

#### Mute system

The mute system supports both permanent and temporary actions.

- Permanent mute: `/ezchat mute <player> <reason...>`
- Temporary mute: `/ezchat mutetemp <player> <duration> <reason...>`
- Unmute: `/ezchat unmute <player> [reason]`
- Inspection: `/ezchat muteinfo <player>`

Mutes can independently block:

- public chat
- private messages
- mail

#### Anti-advertising

Advertising detection is configurable per channel.

- Channel coverage: public chat, private messages, and optional mail scanning.
- Pattern coverage: IPv4 addresses, domains, Discord invites, and custom expressions.
- Domain and exact-value whitelist support.
- Configurable staff/audit/Discord actions.
- Bypass permission: `ezchat.bypass.advertising`

#### Profanity filtering

Profanity filtering is separated into its own feature set.

- Channel coverage: public chat, private messages, mail, and optional staff chat.
- Supports both blocked-word lists and regex patterns.
- Configurable sender notification, audit logging, and Discord logging.
- Bypass permission: `ezchat.bypass.profanity`

### 7. Logging and audit trail

EzChat is designed for moderation review as well as live operations.

Communication logging can store:

- public chat
- private messages
- mail

Audit logging can track:

- moderation actions
- toggle changes
- staff/system actions emitted by EzChat features

Log review subcommands include:

- `/ezchat logs player <player> [page]`
- `/ezchat logs between <player1> <player2> [page]`
- `/ezchat logs public <player> [page]`
- `/ezchat logs msg <player> [page]`
- `/ezchat logs mail <player> [page]`
- `/ezchat logs search <keyword> [page]`

### 8. Staff communication tools

EzChat includes dedicated tools for internal staff communication.

- `/staffchat <message>` and `/sc <message>` for staff-only chat.
- `/togglestaffchat` to route normal chat into staff chat mode.
- `/ezchat staffalert <message>` to send staff alerts.
- `/togglesocialspy` for PM monitoring.
- `/togglemailspy` for mail monitoring.

### 9. Server, death, and join/leave messaging

Server-wide and event-driven messaging can be customized and individually hidden by players.

- `/ezchat broadcast <message>` for server broadcasts.
- `/toggleservermsg [on|off]` to hide broadcasts.
- `/toggledeathmsg [on|off]` to hide death messages.
- `/togglejoinleavemsg [on|off]` to hide join/leave messages.
- Configurable formatting, optional sound playback, toggle persistence, and optional logging.

### 10. Discord webhook integration

Discord routing is optional and can be enabled globally or per event type.

Available webhook channels include:

- public chat
- private messages
- mail
- mute actions
- audit actions
- server broadcasts
- death messages
- join messages
- leave messages

Webhook behavior supports:

- per-event webhook URLs with default fallback
- embed or plain content delivery
- configurable display name strategy
- configurable avatar strategy
- event-specific message formats

### 11. Persistent player settings

EzChat stores critical communication state in SQLite so server restarts do not wipe player preferences or moderation records.

Persisted data includes:

- chat/mail/visibility toggles
- staff mode and spy toggles
- ignore rules
- mail entries
- communication logs
- audit logs
- mute history and active mutes

---

## Chat Formatting Placeholders

The main chat format in `config.yml` supports the following placeholders:

| Placeholder | Description |
| --- | --- |
| `{message}` | The player's chat message |
| `{name}` | The player's Minecraft username |
| `{displayname}` | The player's display name |
| `{world}` | The player's current world |
| `{prefix}` | Highest-priority LuckPerms prefix |
| `{suffix}` | Highest-priority LuckPerms suffix |
| `{prefixes}` | All LuckPerms prefixes ordered by weight |
| `{suffixes}` | All LuckPerms suffixes ordered by weight |
| `{username-color}` | LuckPerms metadata value for username color |
| `{message-color}` | LuckPerms metadata value for message color |

Example formats:

```yaml
chat-format: "{prefix}{name}&r: {message}"
chat-format: "[{world}] {prefix}{name}&r: {message}"
chat-format: "{prefix}{username-color}{name}&r: {message-color}{message}"
```

Additional template support:

- `join-leave.yml` join/leave formats support `{player}`, `{displayname}`, and `{message}`.
- Discord event templates support event-specific placeholders such as `{sender}`, `{receiver}`, `{target}`, `{actor}`, `{duration}`, and `{message}` depending on the event type.

---

## PlaceholderAPI Expansion

If **PlaceholderAPI** is installed and EzChat placeholders are enabled in `placeholders.yml`, EzChat registers the `ezchat` expansion for player communication settings.

### Available EzChat placeholders

| Placeholder | Returns |
| --- | --- |
| `%ezchat_togglechat%` | Chat visibility state |
| `%ezchat_togglemsg%` | Private-message toggle state |
| `%ezchat_togglemail%` | Mail toggle state |
| `%ezchat_toggleservermsg%` | Server-message visibility state |
| `%ezchat_toggledeathmsg%` | Death-message visibility state |
| `%ezchat_togglejoinleavemsg%` | Join/leave-message visibility state |
| `%ezchat_togglestaffchat%` | Staff chat mode state |
| `%ezchat_togglesocialspy%` | Social spy state |
| `%ezchat_togglemailspy%` | Mail spy state |

### Formatted variants

Append `_formatted` to any placeholder to use the configured on/off text from `placeholders.yml`.

Examples:

- `%ezchat_togglechat%`
- `%ezchat_togglechat_formatted%`
- `%ezchat_togglemsg_formatted%`
- `%ezchat_togglesocialspy_formatted%`

### Output mode

`placeholders.yml` controls raw output mode:

- `true-false` → raw placeholders return `true` / `false`
- `on-off` → raw placeholders return `on` / `off`

Formatted output uses the configured `true-text` and `false-text` values.

---

## Commands

### `/ezchat` subcommands

| Command | Description |
| --- | --- |
| `/ezchat reload` | Reloads EzChat configuration and feature state. |
| `/ezchat clear` | Clears visible chat and broadcasts the configured clear message. |
| `/ezchat debug <player>` | Shows LuckPerms metadata, format source, PlaceholderAPI hook status, and color permissions for a player. |
| `/ezchat logs player <player> [page]` | View all communication logs involving a player. |
| `/ezchat logs between <player1> <player2> [page]` | View logs between two players. |
| `/ezchat logs public <player> [page]` | View public chat logs for a player. |
| `/ezchat logs msg <player> [page]` | View private-message logs for a player. |
| `/ezchat logs mail <player> [page]` | View mail logs for a player. |
| `/ezchat logs search <keyword> [page]` | Search communication logs by keyword. |
| `/ezchat mute <player> <reason...>` | Permanently mute a player. |
| `/ezchat mutetemp <player> <duration> <reason...>` | Temporarily mute a player. |
| `/ezchat unmute <player> [reason]` | Remove an active mute. |
| `/ezchat muteinfo <player>` | Inspect a player's mute status. |
| `/ezchat staffalert <message>` | Send a staff alert. |
| `/ezchat broadcast <message>` | Broadcast a server message. |

### Player communication commands

| Command | Description |
| --- | --- |
| `/togglechat [on|off]` | Toggle public chat visibility for yourself. |
| `/msg <player> <message>` | Send a private message. |
| `/reply <message>` | Reply to your most recent DM target. |
| `/togglemsg [on|off]` | Enable or disable private messages. |
| `/ignore <player> <ALL\|CHAT\|MSG\|MAIL>` | Add or toggle an ignore rule. |
| `/ignorelist` | View your active ignore rules. |
| `/unignore <player> <ALL\|CHAT\|MSG\|MAIL\|ALL_TYPES>` | Remove an ignore rule. |
| `/mail <player> <message>` | Send persistent mail to a player. |
| `/mail inbox [page]` | View your inbox. |
| `/mail unread [page]` | View unread mail. |
| `/mail received <player> [page]` | View mail received from a specific player. |
| `/mail sent [page]` | List players you have sent mail to. |
| `/mail sent <player> [page]` | View mail you have sent to a specific player. |
| `/mail read <id>` | Open a single mail entry. |
| `/mail delete <id>` | Delete a mail entry from your inbox. |
| `/togglemail [on|off]` | Enable or disable incoming mail. |

### Staff utility commands

| Command | Description |
| --- | --- |
| `/staffchat <message>` | Send a staff-chat message. |
| `/sc <message>` | Alias for `/staffchat`. |
| `/togglestaffchat` | Toggle staff-chat mode for normal chat input. |
| `/togglesocialspy` | Toggle private-message spy mode. |
| `/togglemailspy` | Toggle mail spy mode. |
| `/toggleservermsg [on|off]` | Toggle receipt of server broadcasts. |
| `/toggledeathmsg [on|off]` | Toggle receipt of death messages. |
| `/togglejoinleavemsg [on|off]` | Toggle receipt of join/leave messages. |

---

## Permissions

### Core and player permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.chattoggle` | `true` | Use `/togglechat`. |
| `ezchat.msg` | `true` | Use `/msg`. |
| `ezchat.reply` | `true` | Use `/reply`. |
| `ezchat.togglemsg` | `true` | Use `/togglemsg`. |
| `ezchat.ignore` | `true` | Use ignore commands. |
| `ezchat.mail` | `true` | Send mail with `/mail`. |
| `ezchat.mail.inbox` | `true` | View `/mail inbox`. |
| `ezchat.mail.sent` | `true` | View `/mail sent`. |
| `ezchat.mail.received` | `true` | View `/mail received`. |
| `ezchat.mail.unread` | `true` | View `/mail unread`. |
| `ezchat.mail.read` | `true` | Use `/mail read`. |
| `ezchat.mail.delete` | `true` | Use `/mail delete`. |
| `ezchat.togglemail` | `true` | Use `/togglemail`. |
| `ezchat.servermsg.toggle` | `true` | Use `/toggleservermsg`. |
| `ezchat.deathmsg.toggle` | `true` | Use `/toggledeathmsg`. |
| `ezchat.joinleavemsg.toggle` | `true` | Use `/togglejoinleavemsg`. |
| `ezchat.colorcodes` | `false` | Use legacy chat color codes. |
| `ezchat.rgbcodes` | `false` | Use RGB / hex chat colors. |

### Staff and administrative permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.reload` | `op` | Use `/ezchat reload`. |
| `ezchat.clearchat` | `op` | Use `/ezchat clear`. |
| `ezchat.debug` | `op` | Use `/ezchat debug`. |
| `ezchat.logs` | `op` | Use `/ezchat logs ...`. |
| `ezchat.mute` | `op` | Use `/ezchat mute`. |
| `ezchat.mutetemp` | `op` | Use `/ezchat mutetemp`. |
| `ezchat.unmute` | `op` | Use `/ezchat unmute`. |
| `ezchat.muteinfo` | `op` | Use `/ezchat muteinfo`. |
| `ezchat.broadcast` | `op` | Use `/ezchat broadcast` as a player. |
| `ezchat.staffchat` | `op` | Send and receive staff chat. |
| `ezchat.staffchat.toggle` | `op` | Use `/togglestaffchat`. |
| `ezchat.socialspy` | `op` | Use `/togglesocialspy` and receive PM spy. |
| `ezchat.mailspy` | `op` | Use `/togglemailspy` and receive mail spy. |
| `ezchat.staffalerts` | `op` | Receive staff alerts. |
| `ezchat.staffalerts.send` | `op` | Send `/ezchat staffalert` as a player. |

### Moderation bypass permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.bypass.advertising` | `op` | Bypass anti-advertising checks. |
| `ezchat.bypass.profanity` | `op` | Bypass profanity checks. |
| `ezchat.bypass.flood` | `op` | Bypass anti-flood checks. |

---

## Configuration Files

EzChat splits its configuration into focused files so production changes stay readable and low-risk.

| File | Purpose |
| --- | --- |
| `config.yml` | Core chat formatting, public chat feature flags, ignore messages, clear-chat behavior, and group format overrides. |
| `private-message.yml` | Private message formats and PM toggle messaging. |
| `mail.yml` | Mail behavior, inbox messaging, and unread login notifications. |
| `logs.yml` | Communication and audit logging settings, page size, and timestamp formatting. |
| `mute.yml` | Mute feature toggles, channel blocks, and mute command messaging. |
| `anti-spam.yml` | Advertising detection rules, channel coverage, actions, and whitelist settings. |
| `anti-flood.yml` | Anti-flood thresholds, cooldown windows, and action routing for repetitive messages. |
| `profanity.yml` | Profanity checks, regex settings, actions, and channel coverage. |
| `blocked-words.yml` | Word-list entries used by the profanity system. |
| `staff.yml` | Staff chat, alerts, social spy, and mail spy formatting/settings. |
| `server-message.yml` | Broadcast formatting, broadcast logging, and server-message toggles. |
| `death-message.yml` | Death-message formatting, sounds, logging, and player visibility defaults. |
| `join-leave.yml` | Join/leave formatting, sounds, logging, and player visibility defaults. |
| `discord.yml` | Discord webhook routing, identity settings, avatars, event toggles, and templates. |
| `placeholders.yml` | PlaceholderAPI feature enablement and raw/formatted EzChat placeholder output. |

Recommended production workflow:

1. Start the server once to generate all configuration files.
2. Stop the server before major bulk edits.
3. Edit YAML carefully using spaces, not tabs.
4. Use `/ezchat reload` for normal iterative changes.
5. Rejoin with a test account and validate formatting, toggles, mail, and moderation behavior.

---

## Discord Webhook Events

When `discord.yml` enables webhooks, EzChat can send these event types:

| Event key | Default state |
| --- | --- |
| `public-chat` | `true` |
| `private-messages` | `false` |
| `mail` | `false` |
| `mute-actions` | `true` |
| `audit-actions` | `true` |
| `server-broadcasts` | `false` |
| `death-messages` | `false` |
| `join-messages` | `false` |
| `leave-messages` | `false` |

You can point each event to its own webhook URL or let it fall back to the default webhook.

---

## Storage and Persistence

EzChat initializes an SQLite-backed database layer for persistent communication data.

Persisted records include:

- player toggle states
- ignore entries
- mail entries
- communication logs
- audit logs
- mute records

This makes the plugin suitable for production servers that need restart-safe settings and moderation history.

---

## Installation

1. Install **LuckPerms** on your Paper or Folia server.
2. Drop the EzChat jar into your `plugins/` folder.
3. Optionally install:
   - **PlaceholderAPI** for chat placeholders and EzChat toggle placeholders
   - **floodgate** / **Geyser-Spigot** for improved Bedrock-aware behavior
4. Start the server to generate configuration files.
5. Review all YAML files and tune the systems you plan to use.
6. Assign permissions with LuckPerms.
7. Use `/ezchat reload` after configuration updates.

---

## Building from Source

### Maven

```bash
mvn clean package
```

Build output:

- shaded jar in `target/`
- final artifact name: `EzChat-<version>.jar`

### Requirements

- Java 21
- Maven 3.9+
- Network access to the configured Paper, Spigot, and PlaceholderAPI repositories

---

### Previewing the documentation site

If you maintain the `docs/` site locally:

```bash
pip install mkdocs-material
mkdocs serve
```

Then open the local URL shown by MkDocs (usually `http://127.0.0.1:8000`).

## Operational Notes

- **LuckPerms is required**. EzChat disables itself if LuckPerms is unavailable.
- **PlaceholderAPI is optional**. If it is missing, chat formatting still works, but PlaceholderAPI placeholders and EzChat's `%ezchat_*%` expansion remain unavailable.
- **Chat-plugin overlap**: EzChat warns if common chat-formatting plugins are also installed, because dual formatting can duplicate or conflict with output.
- **Discord is opt-in** and disabled by default.
- **Feature flags are granular**: most systems can be enabled or disabled independently through config files.
- **SQLite is included** so no external database is required for production use.

---

## Documentation

Additional project documentation is available in `docs/`:

- `docs/index.md`
- `docs/installation.md`
- `docs/configuration.md`
- `docs/commands.md`
- `docs/permissions.md`
- `docs/logging.md`
- `docs/mute-system.md`
- `docs/anti-flood.md` (if anti-flood feature is enabled in your deployment)

If you are preparing a public release page, this README is intended to be the high-level overview while the `docs/` directory serves as the deeper admin reference.
