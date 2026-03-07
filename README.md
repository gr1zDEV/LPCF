# EzChat

EzChat is a lightweight, LuckPerms-powered chat formatter for **Paper/Folia 1.21+**.

It gives you a configurable chat pipeline with group-aware formats, optional PlaceholderAPI expansion, message color permissions, and per-player chat visibility toggles.

---

## What’s New / Current Feature Set

- LuckPerms metadata formatting (`prefix`, `suffix`, stacked prefixes/suffixes, meta colors).
- Group-specific format overrides (`group-formats.<primary-group>`).
- Placeholder-ready formats (native EzChat placeholders + optional PlaceholderAPI placeholders).
- Fine-grained player color permissions:
  - `ezchat.colorcodes` for legacy `&` colors.
  - `ezchat.rgbcodes` for hex/RGB colors.
- `/chattoggle` with persistent toggle state in `plugins/EzChat/toggles.yml`.
- `/ezchat debug <player>` output for fast troubleshooting.
- Floodgate hook for Bedrock detection (graceful fallback if Floodgate is absent).
- Paper `AsyncChatEvent` integration and Folia support.

---

## Compatibility

- **Platform:** Paper / Folia
- **API version:** 1.21
- **Java:** 21
- **Required dependency:** LuckPerms
- **Optional dependencies:** PlaceholderAPI, floodgate, Geyser-Spigot

---

## Installation

1. Download the latest `EzChat-<version>.jar` from Releases.
2. Put it in your server `plugins/` folder.
3. Ensure **LuckPerms** is installed.
4. *(Optional)* Install **PlaceholderAPI** for PAPI format placeholders.
5. Start or restart the server.
6. Edit `plugins/EzChat/config.yml`.
7. Run `/ezchat reload` to apply config changes.

---

## Commands

### `/ezchat`

- `/ezchat reload` — reloads `config.yml`.
- `/ezchat clear` — clears chat and broadcasts `clear-chat-message`.
- `/ezchat debug <player>` — displays resolved LuckPerms metadata and relevant plugin state.

### `/chattoggle`

- Toggles whether that player receives public chat messages.

---

## Permissions

- `ezchat.reload` — use `/ezchat reload` *(default: op)*
- `ezchat.clearchat` — use `/ezchat clear` *(default: op)*
- `ezchat.debug` — use `/ezchat debug` *(default: op)*
- `ezchat.chattoggle` — use `/chattoggle` *(default: true)*
- `ezchat.colorcodes` — allow legacy `&` color codes in player message input *(default: false)*
- `ezchat.rgbcodes` — allow hex/RGB color codes in player message input *(default: false)*

---

## Configuration

Default:

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

If PlaceholderAPI is installed, PAPI placeholders inside format strings are also resolved.

### Group-specific formats

```yml
group-formats:
  default: "{prefix}{name}&r: {message}"
  admin: "&c[Admin] {prefix}{name}&r: {message}"
```

### Example formats

```yml
chat-format: "[{world}] {prefix}{name}&r: {message}"
chat-format: "{prefix}{name}{suffix}&r: {message}"
chat-format: "{prefix}{username-color}{name}&r: {message-color}{message}"
```

### Player message color rules

- Has **both** `ezchat.colorcodes` + `ezchat.rgbcodes` → legacy + hex accepted.
- Has **only** `ezchat.colorcodes` → legacy accepted, hex removed.
- Has **only** `ezchat.rgbcodes` → hex accepted, legacy removed.
- Has **neither** → all color codes removed.

---

## Troubleshooting

- **Prefixes/suffixes missing**
  - Verify LuckPerms metadata exists for the player/group.
  - Run `/ezchat debug <player>` and inspect resolved values.

- **Placeholders not expanding**
  - Verify PlaceholderAPI is installed and expansions are available.

- **Duplicate formatting/messages**
  - Disable chat formatting in other chat plugins (EssentialsChat, VentureChat, etc.).

- **Color permissions not behaving as expected**
  - Confirm the player’s effective permissions for `ezchat.colorcodes` and `ezchat.rgbcodes`.

---

## Build from Source

```bash
mvn clean package
```

Output JAR:

```text
target/EzChat-<version>.jar
```

---

## Releases

This repository includes a GitHub Actions release workflow.

Supported tag patterns:
- `v*` (example: `v3.7.1`)
- `x.y.z` (example: `3.7.1`)
