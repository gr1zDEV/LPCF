# Logging

EzChat provides two persistent log streams backed by SQLite.

## Communication logs

Communication logs can track:

- Public chat
- Private messages
- Mail events

Use these for player-to-player history review.

## Audit logs

Audit logs track internal administrative events such as:

- Moderation actions (mute/unmute)
- Staff actions triggered by EzChat features
- Toggle and system-level state changes

## Log commands

| Command | Purpose |
| --- | --- |
| `/ezchat logs player <player> [page]` | All communication involving a player |
| `/ezchat logs between <player1> <player2> [page]` | Conversation logs between two players |
| `/ezchat logs public <player> [page]` | Public chat logs for one player |
| `/ezchat logs msg <player> [page]` | Private message logs for one player |
| `/ezchat logs mail <player> [page]` | Mail logs for one player |
| `/ezchat logs search <keyword> [page]` | Keyword search across communication logs |

## Tuning logging behavior

Review `logs.yml` for:

- Page size and output readability
- Timestamp format and timezone assumptions
- Which categories are enabled
- Message templates for staff-facing output

## Best practices

- Restrict `ezchat.logs` to trusted moderators.
- Keep logs enabled for at least chat + PM on public servers.
- Pair log review with audit logs for moderation context.
