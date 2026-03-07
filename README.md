# EzChat

A modern LuckPerms-powered chat formatting plugin for **Paper/Folia 1.21.11** servers.

EzChat lets you build chat formats from LuckPerms prefixes/suffixes/meta, optionally expand PlaceholderAPI placeholders, and control who receives chat with a per-player `/chattoggle` setting.

---

## Features

- **LuckPerms-based chat formatting**
  - Uses primary prefix/suffix and full prefix/suffix stacks.
  - Supports per-group format overrides.
- **Rich placeholders in formats**
  - Built-in tokens like `{prefix}`, `{suffix}`, `{name}`, `{displayname}`, `{world}`, `{message}`, `{username-color}`, and `{message-color}`.
- **Color controls by permission**
  - Standard color codes (`&a`, `&7`, etc.) via `lpc.colorcodes`.
  - RGB/hex (`&#rrggbb` and Bukkit `&x&...`) via `lpc.rgbcodes`.
- **Optional PlaceholderAPI integration**
  - If PlaceholderAPI is installed, placeholders in formats are expanded automatically.
- **Chat visibility toggle**
  - `/chattoggle` lets players hide/show public chat.
  - State is persisted in `plugins/EzChat/toggles.yml`.
- **Admin utilities**
  - `/lpc reload` to reload config.
  - `/lpc clear` to clear chat and broadcast a configurable message.
  - `/lpc debug <player>` to inspect resolved LuckPerms formatting values.
- **Compatibility-minded behavior**
  - Native Paper `AsyncChatEvent` pipeline.
  - Folia supported.
  - Optional Floodgate/Geyser Bedrock detection hook.
  - Warns when known chat-formatting plugins are also installed to help avoid duplicate formatting.

---

## Compatibility

- **Minecraft/Paper API:** `1.21.11`
- **Folia:** supported (`folia-supported: true`)
- **Java:** 21
- **Required dependency:** LuckPerms
- **Optional dependencies:** PlaceholderAPI, Floodgate, Geyser-Spigot

---

## Installation

1. Download the latest `EzChat-<version>.jar` from Releases.
2. Place it in your server's `plugins/` folder.
3. Make sure **LuckPerms** is installed.
4. (Optional) Install **PlaceholderAPI** if you want PAPI placeholders in format strings.
5. Start/restart the server.
6. Edit `plugins/EzChat/config.yml` to customize your chat format.
7. Run `/lpc reload` after config changes.

---

## Commands

### `/lpc`

- `/lpc reload` — reloads `config.yml`.
- `/lpc clear` — clears chat and broadcasts `clear-chat-message`.
- `/lpc debug <player>` — prints resolved LuckPerms prefix/suffix/meta details for troubleshooting.

### `/chattoggle`

- Toggles whether the player receives public chat messages.

---

## Permissions

- `lpc.reload` — allows `/lpc reload` (default: op)
- `lpc.clearchat` — allows `/lpc clear` (default: op)
- `lpc.debug` — allows `/lpc debug` (default: op)
- `lpc.colorcodes` — allows legacy color codes in user messages (default: false)
- `lpc.rgbcodes` — allows RGB/hex codes in user messages (default: false)
- `lpc.chattoggle` — allows `/chattoggle` (default: true)

---

## Configuration

Default format:

```yml
chat-format: "{prefix}{name}&r: {message}"
```

### Supported placeholders

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

### Group-specific formats

You can override `chat-format` by LuckPerms primary group:

```yml
group-formats:
  default: "{prefix}{name}&r: {message}"
  admin: "&c[Admin] {prefix}{name}&r: {message}"
```

### Color input rules for player messages

- Has both `lpc.colorcodes` + `lpc.rgbcodes`: can use legacy + hex.
- Has only `lpc.colorcodes`: legacy colors allowed, hex stripped.
- Has only `lpc.rgbcodes`: hex allowed, legacy stripped.
- Has neither: all color codes stripped.

---

## Example formats

```yml
chat-format: "[{world}] {prefix}{name}&r: {message}"
chat-format: "{prefix}{name}{suffix}&r: {message}"
chat-format: "{prefix}{username-color}{name}&r: {message-color}{message}"
```

---

## Troubleshooting

- **No prefixes/suffixes showing?**
  - Verify LuckPerms is installed and user/group metadata is configured.
  - Run `/lpc debug <player>` and check reported values.
- **PAPI placeholders not resolving?**
  - Confirm PlaceholderAPI is installed and required expansions are present.
- **Duplicate chat messages/formats?**
  - Disable formatting in other chat plugins (EssentialsChat, VentureChat, etc.) so EzChat is the only formatter.
- **Color codes not working for players?**
  - Check `lpc.colorcodes` and/or `lpc.rgbcodes` permissions.

---

## Building from source

```bash
mvn clean package
```

The shaded output jar is generated as:

```text
target/EzChat-<version>.jar
```

---

## Releases

GitHub Actions automatically builds and publishes release artifacts when a version tag is pushed.

Supported tag patterns:
- `v*` (example: `v3.7.1`)
- `x.y.z` (example: `3.7.1`)
