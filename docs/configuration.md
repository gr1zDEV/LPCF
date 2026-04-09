# Configuration

EzChat uses multiple focused YAML files so each subsystem can be tuned independently.

## File reference

| File | Purpose |
| --- | --- |
| `config.yml` | Core chat formatting, group format overrides, ignore and clear-chat messaging. |
| `private-message.yml` | PM format templates and toggle messaging. |
| `mail.yml` | Mail command behavior, inbox templates, unread notices. |
| `logs.yml` | Communication and audit logging settings, page size, timestamp format. |
| `mute.yml` | Mute system behavior and command feedback templates. |
| `anti-spam.yml` | Anti-advertising rules (domains, IPs, invite patterns), actions and whitelist controls. |
| `anti-flood.yml` | Flood detection windows, thresholds, and moderation actions. |
| `profanity.yml` | Profanity detection settings and action routing. |
| `blocked-words.yml` | Blocked word lists used by profanity checks. |
| `staff.yml` | Staff chat, social spy, mail spy, and alert formatting/settings. |
| `server-message.yml` | Broadcast formatting, visibility defaults, and logging toggles. |
| `death-message.yml` | Death message templates, sound settings, visibility defaults. |
| `join-leave.yml` | Join/leave templates, optional sounds, visibility defaults. |
| `discord.yml` | Webhook URLs, event toggles, identity/avatar and message templates. |
| `placeholders.yml` | PlaceholderAPI expansion behavior and formatted true/false text. |

## Safe editing workflow

1. Start server once to generate default files.
2. Stop server before large edits.
3. Edit YAML using spaces (no tabs).
4. Run `/ezchat reload` for normal iterative changes.
5. Validate in-game with a staff account and a player account.

## High-impact settings to review first

- Chat format strings in `config.yml`
- Permission and toggle messaging in `private-message.yml` and `mail.yml`
- Moderation actions in `anti-spam.yml`, `anti-flood.yml`, and `profanity.yml`
- Log retention/detail settings in `logs.yml`
- Webhook routing defaults in `discord.yml`
