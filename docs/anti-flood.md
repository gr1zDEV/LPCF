# Anti-Flood

EzChat's anti-flood module helps prevent repeated or spam-like message bursts.

## What it checks

Depending on `anti-flood.yml`, checks can apply to:

- Public chat
- Private messages
- Repeated/similar content within a short window

## Typical actions

Configured actions usually include one or more of:

- Canceling the message
- Notifying the sender
- Logging to audit/communication logs
- Sending alerts to staff or Discord (if enabled)

## Permissions

- `ezchat.bypass.flood` lets trusted users bypass anti-flood checks.

## Tuning tips

- Start with conservative thresholds and adjust based on real chat traffic.
- Keep stricter flood limits for public channels than staff channels.
- Pair anti-flood with anti-advertising and profanity checks for better coverage.
