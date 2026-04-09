# Permissions

Use your permissions plugin (typically LuckPerms) to assign EzChat capabilities.

## Player permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.chattoggle` | true | Use `/togglechat`. |
| `ezchat.msg` | true | Use `/msg`. |
| `ezchat.reply` | true | Use `/reply`. |
| `ezchat.togglemsg` | true | Use `/togglemsg`. |
| `ezchat.ignore` | true | Use ignore commands. |
| `ezchat.mail` | true | Send mail with `/mail`. |
| `ezchat.mail.inbox` | true | View `/mail inbox`. |
| `ezchat.mail.sent` | true | View `/mail sent`. |
| `ezchat.mail.received` | true | View `/mail received`. |
| `ezchat.mail.unread` | true | View `/mail unread`. |
| `ezchat.mail.read` | true | Use `/mail read`. |
| `ezchat.mail.delete` | true | Use `/mail delete`. |
| `ezchat.togglemail` | true | Use `/togglemail`. |
| `ezchat.servermsg.toggle` | true | Use `/toggleservermsg`. |
| `ezchat.deathmsg.toggle` | true | Use `/toggledeathmsg`. |
| `ezchat.joinleavemsg.toggle` | true | Use `/togglejoinleavemsg`. |
| `ezchat.colorcodes` | false | Allow legacy color codes. |
| `ezchat.rgbcodes` | false | Allow RGB/hex color codes. |

## Staff/admin permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.reload` | op | Use `/ezchat reload`. |
| `ezchat.clearchat` | op | Use `/ezchat clear`. |
| `ezchat.debug` | op | Use `/ezchat debug`. |
| `ezchat.logs` | op | Use `/ezchat logs ...`. |
| `ezchat.mute` | op | Use `/ezchat mute`. |
| `ezchat.mutetemp` | op | Use `/ezchat mutetemp`. |
| `ezchat.unmute` | op | Use `/ezchat unmute`. |
| `ezchat.muteinfo` | op | Use `/ezchat muteinfo`. |
| `ezchat.broadcast` | op | Use `/ezchat broadcast`. |
| `ezchat.staffchat` | op | Send/receive staff chat. |
| `ezchat.staffchat.toggle` | op | Use `/togglestaffchat`. |
| `ezchat.socialspy` | op | Use `/togglesocialspy` and receive PM spy. |
| `ezchat.mailspy` | op | Use `/togglemailspy` and receive mail spy. |
| `ezchat.staffalerts` | op | Receive staff alerts. |
| `ezchat.staffalerts.send` | op | Send `/ezchat staffalert`. |

## Moderation bypass permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ezchat.bypass.advertising` | op | Bypass anti-advertising checks. |
| `ezchat.bypass.profanity` | op | Bypass profanity checks. |
| `ezchat.bypass.flood` | op | Bypass anti-flood checks. |
