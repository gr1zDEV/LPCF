# EzChat Wiki

> A lightweight, LuckPerms-powered chat system for **Paper/Folia 1.21+**.

EzChat handles **global chat formatting**, **private messaging**, **ignore controls**, and **player-side visibility toggles** in one plugin.

---

## 📚 Table of Contents

- [Overview](#-overview)
- [Feature Index](#-feature-index)
- [Compatibility & Dependencies](#-compatibility--dependencies)
- [Installation](#-installation)
- [Commands](#-commands)
- [Permissions](#-permissions)
- [Configuration Guide](#-configuration-guide)
- [Storage Files](#-storage-files)
- [Troubleshooting](#-troubleshooting)
- [Build from Source](#-build-from-source)
- [Releases](#-releases)

---

## 🔎 Overview

EzChat is designed to replace fragmented chat tooling with a single, focused plugin that supports:

- LuckPerms-aware chat metadata (prefix/suffix + meta colors).
- Group format overrides.
- Permission-based player message coloring.
- Per-player chat visibility toggles.
- Built-in private messaging (`/msg`, `/reply`, `/togglemsg`).
- Ignore modes for chat and/or private messages (`/ignore`).

---

## ✅ Feature Index

### Global Chat Formatting

- Uses `AsyncChatEvent` on Paper for modern chat handling.
- Supports LuckPerms metadata:
  - `{prefix}` / `{suffix}`
  - `{prefixes}` / `{suffixes}` (stacked values)
  - `{username-color}` / `{message-color}` meta
- Supports global `chat-format` with placeholders.
- Supports per-group overrides via `group-formats.<primary-group>`.
- Optional PlaceholderAPI parsing in format strings when PlaceholderAPI is installed.

### Message Color Permission Rules

- `ezchat.colorcodes` enables legacy `&` color/style codes.
- `ezchat.rgbcodes` enables hex/RGB color codes (`&#rrggbb` and `&x&f&f...`).
- If players have neither permission, color codes are stripped.

### Player Chat Visibility

- `/togglechat` (alias `/chattoggle`) lets players hide/show global chat.
- State persists in `plugins/EzChat/database.db` (SQLite).

### Private Messaging Suite

- `/msg <player> <message>` for direct messages.
- `/reply <message>` uses conversation tracking to reply quickly.
- `/togglemsg` lets players opt out of private messages.
- Customizable sent/received formats and response messages in config.
- Rejects self-messaging and blocked messaging scenarios.

### Ignore System

- `/ignore <player> <ALL|CHAT|MSG>` toggles ignore mode per target.
- `ALL`: block both public chat and private messages.
- `CHAT`: block public chat only.
- `MSG`: block private messages only.
- State persists in `plugins/EzChat/database.db` (SQLite).

### Admin & Diagnostics

- `/ezchat reload` reloads configuration.
- `/ezchat clear` clears chat and broadcasts a configurable clear message.
- `/ezchat debug <player>` displays resolved metadata and processing context.
- Warns in console when common chat-formatting plugins are detected to help prevent duplicate formatting.

### Bedrock Awareness (Optional)

- Floodgate hook is auto-detected when available.
- If Floodgate is missing/incompatible, EzChat falls back safely without crashing.

---

## 🧩 Compatibility & Dependencies

- **Platform:** Paper / Folia
- **API Version:** 1.21
- **Java:** 21
- **Required:** LuckPerms
- **Optional:** PlaceholderAPI, floodgate, Geyser-Spigot

---

## 🚀 Installation

1. Build or download `EzChat-<version>.jar`.
2. Place it in your server `plugins/` directory.
3. Install **LuckPerms** (required).
4. (Optional) Install **PlaceholderAPI**.
5. (Optional) Install **floodgate/Geyser** for Bedrock detection support.
6. Start/restart server.
7. Edit `plugins/EzChat/config.yml`.
8. Run `/ezchat reload`.

---

## ⌨️ Commands

| Command | Description |
|---|---|
| `/ezchat reload` | Reload `config.yml`. |
| `/ezchat clear` | Clears chat and broadcasts `clear-chat-message`. |
| `/ezchat debug <player>` | Shows resolved LuckPerms metadata and EzChat context. |
| `/togglechat` (`/chattoggle`) | Toggle receiving global chat messages. |
| `/msg <player> <message>` | Send a private message. |
| `/reply <message>` | Reply to your last DM contact. |
| `/togglemsg` (`/msgtoggle`) | Toggle whether you can receive DMs. |
| `/ignore <player> <ALL|CHAT|MSG>` | Toggle ignore mode for a target player. |

---

## 🔐 Permissions

| Permission | Default | Description |
|---|---:|---|
| `ezchat.reload` | `op` | Use `/ezchat reload`. |
| `ezchat.clearchat` | `op` | Use `/ezchat clear`. |
| `ezchat.debug` | `op` | Use `/ezchat debug`. |
| `ezchat.chattoggle` | `true` | Use `/togglechat` / `/chattoggle`. |
| `ezchat.msg` | `true` | Use `/msg`. |
| `ezchat.reply` | `true` | Use `/reply`. |
| `ezchat.togglemsg` | `true` | Use `/togglemsg` / `/msgtoggle`. |
| `ezchat.ignore` | `true` | Use `/ignore`. |
| `ezchat.colorcodes` | `false` | Allow legacy `&` color/style codes in chat input. |
| `ezchat.rgbcodes` | `false` | Allow RGB/hex colors in chat input. |

---

## 🛠 Configuration Guide

### Core format

```yml
chat-format: "{prefix}{name}&r: {message}"
```

### Built-in placeholders

- `{message}`
- `{name}`
- `{displayname}`
- `{world}`
- `{prefix}`
- `{suffix}`
- `{prefixes}`
- `{suffixes}`
- `{username-color}`
- `{message-color}`

If PlaceholderAPI is installed, PAPI placeholders in format strings are also resolved.

### Group-specific format overrides

```yml
group-formats:
  default: "{prefix}{name}&r: {message}"
  admin: "&c[Admin] {prefix}{name}&r: {message}"
```

### Format examples

```yml
chat-format: "[{world}] {prefix}{name}&r: {message}"
chat-format: "{prefix}{name}{suffix}&r: {message}"
chat-format: "{prefix}{username-color}{name}&r: {message-color}{message}"
```

### Private message settings

```yml
private-messages:
  sent-format: "&8[&aTo {receiver}&8] &f{message}"
  received-format: "&8[&6From {sender}&8] &f{message}"
```

### Ignore message settings

```yml
ignore:
  enabled-all: "&cYou are now ignoring {player}."
  enabled-chat: "&cYou will no longer see chat messages from {player}."
  enabled-msg: "&cYou will no longer receive private messages from {player}."
  disabled: "&aYou are no longer ignoring {player}."
```

### Player message color behavior

- Has **both** `ezchat.colorcodes` + `ezchat.rgbcodes` → legacy + hex accepted.
- Has **only** `ezchat.colorcodes` → legacy accepted, hex removed.
- Has **only** `ezchat.rgbcodes` → hex accepted, legacy removed.
- Has **neither** permission → all color codes removed.

---

## 💾 Storage Files

- `plugins/EzChat/config.yml` — main plugin behavior/messages.
- `plugins/EzChat/database.db` — SQLite storage for togglechat/togglemsg/togglemail state, ignore relationships, mail, and unread tracking.

---

## 🧯 Troubleshooting

- **Prefixes/suffixes missing**
  - Confirm LuckPerms metadata exists.
  - Use `/ezchat debug <player>` to inspect resolved values.

- **Placeholders not expanding**
  - Install PlaceholderAPI and the needed expansions.

- **Players report duplicated chat format**
  - Disable format features in other chat plugins (EssentialsChat, VentureChat, etc.).

- **DMs fail unexpectedly**
  - Check whether target has `/togglemsg` enabled.
  - Check ignore mode (`ALL` or `MSG`).

- **Color permissions not applying**
  - Verify effective permissions for `ezchat.colorcodes` and `ezchat.rgbcodes`.

---

## 🏗 Build from Source

```bash
mvn clean package
```

Output artifact:

```text
target/EzChat-<version>.jar
```

---

## 📦 Releases

GitHub Actions release workflow supports tags:

- `v*` (example: `v3.7.1`)
- `x.y.z` (example: `3.7.1`)
