# Installation

## Requirements

- **Minecraft server software**: Paper or Folia
- **Minecraft API target**: 1.21
- **Java**: 21+
- **Required plugin**: LuckPerms
- **Optional plugins**: PlaceholderAPI, floodgate, Geyser-Spigot

## Install from release jar

1. Stop your server.
2. Place the EzChat JAR in `plugins/`.
3. Ensure LuckPerms is installed and enabled.
4. Start the server and wait for first-boot file generation.
5. Stop the server and review generated `plugins/EzChat/*.yml` files.
6. Start the server and run `/ezchat reload` after future config changes.

## First-boot validation

After startup, verify:

- No startup errors related to dependency hooks.
- EzChat reports successful enable in console.
- `plugins/EzChat/` contains expected config files.
- Basic commands work (`/togglechat`, `/msg`, `/mail`).

## Updating EzChat

1. Back up `plugins/EzChat/` and your server world.
2. Stop the server.
3. Replace the old EzChat JAR with the new one.
4. Start the server and inspect console output.
5. Compare new default configs against existing configs before merging changes.

## Build from source

```bash
mvn clean package
```

Output jar: `target/EzChat-<version>.jar`
