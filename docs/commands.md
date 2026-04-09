# Commands

EzChat commands are grouped below by role.

## Core command

| Command | Description |
| --- | --- |
| `/ezchat reload` | Reload plugin configuration and feature state. |
| `/ezchat clear` | Clear visible chat and send configured clear notice. |
| `/ezchat debug <player>` | Show format/debug metadata for a player. |
| `/ezchat logs player <player> [page]` | Show all communication logs involving a player. |
| `/ezchat logs between <player1> <player2> [page]` | Show logs between two players. |
| `/ezchat logs public <player> [page]` | Show public-chat logs for a player. |
| `/ezchat logs msg <player> [page]` | Show private-message logs for a player. |
| `/ezchat logs mail <player> [page]` | Show mail logs for a player. |
| `/ezchat logs search <keyword> [page]` | Search logs by keyword. |
| `/ezchat mute <player> <reason...>` | Permanently mute a player. |
| `/ezchat mutetemp <player> <duration> <reason...>` | Temporarily mute a player. |
| `/ezchat unmute <player> [reason]` | Remove active mute. |
| `/ezchat muteinfo <player>` | Inspect mute details. |
| `/ezchat staffalert <message>` | Send an internal staff alert. |
| `/ezchat broadcast <message>` | Send server-wide broadcast. |

## Player commands

| Command | Description |
| --- | --- |
| `/togglechat [on|off]` | Toggle your public chat visibility. |
| `/msg <player> <message>` | Send a private message. |
| `/reply <message>` | Reply to your most recent DM partner. |
| `/togglemsg [on|off]` | Enable/disable receiving private messages. |
| `/ignore <player> <ALL\|CHAT\|MSG\|MAIL>` | Add/toggle ignore rule. |
| `/ignorelist` | Show your active ignore entries. |
| `/unignore <player> <ALL\|CHAT\|MSG\|MAIL\|ALL_TYPES>` | Remove ignore rule. |
| `/mail <player> <message>` | Send offline mail. |
| `/mail inbox [page]` | View inbox. |
| `/mail unread [page]` | View unread messages. |
| `/mail received <player> [page]` | View received mail from player. |
| `/mail sent [page]` | View players you mailed. |
| `/mail sent <player> [page]` | View sent mail to player. |
| `/mail read <id>` | Read a specific mail item. |
| `/mail delete <id>` | Delete a specific inbox mail item. |
| `/togglemail [on|off]` | Enable/disable receiving mail. |

## Staff utility commands

| Command | Description |
| --- | --- |
| `/staffchat <message>` | Send staff-only chat message. |
| `/sc <message>` | Alias of `/staffchat`. |
| `/togglestaffchat` | Route normal chat input to staff chat. |
| `/togglesocialspy` | Toggle PM spy feed. |
| `/togglemailspy` | Toggle mail spy feed. |
| `/toggleservermsg [on|off]` | Toggle server broadcast visibility. |
| `/toggledeathmsg [on|off]` | Toggle death message visibility. |
| `/togglejoinleavemsg [on|off]` | Toggle join/leave message visibility. |

## Duration format (`/ezchat mutetemp`)

Durations use suffix notation, for example:

- `30m` (30 minutes)
- `12h` (12 hours)
- `7d` (7 days)

