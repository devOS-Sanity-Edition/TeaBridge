# TeaBridge

<p align="center">
  <img title="modicon" height="512" src="src/main/resources/assets/teabridge/icon.png">
</p>

<p align="center">
  <a href="https://modrinth.com/mod/teabridge" target="_blank">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/modrinth/art/blob/main/Branding/Badge/badge-dark__184x72.png?raw=true">
      <img title="modrinth" height="50" src="https://github.com/modrinth/art/blob/main/Branding/Badge/badge-light__184x72.png?raw=true">
    </picture>
  </a>
</p>

TeaBridge is another Fabric/Quilt mod that bridges your Discord and Minecraft chat together. Originally made only for the devOS Seasons server to fix some issues we've had with other chat bridge mods, this mod is now available to all.

## Features

- Send and view messages between Discord and Minecraft
- Server Crash detection [uploads to mclo.gs]
- Markdown support [provided by Placeholder API]
- Shows roles of a Discord member if you hover over their Discord name in Minecraft chat
- Shows in Minecraft chat if a message sent in Discord chat was an attachment
- Mention filtering [good luck pinging `@everyone`/`@here` in Minecraft chat]
- Death and Advencement messages

[screenshots in Gallery]

### Discord Permissions Required
- View Channel
- Send Messages
- Read Message History

## Config

```json5
{
  "discord": {
    "token": "Nope",
    "webhook": "Nope",
    "pkMessageDelay": 0,
    "pkMessageDelayMilliseconds": true
  },
  "avatars": {
    "avatarUrl": "https://api.nucleoid.xyz/skin/face/256/%s",
    "useTextureId": false
  },
  "game": {
    "serverStartingMessage": "Server is starting...",
    "serverStartMessage": "Server has started!",
    "serverStopMessage": "Server has stopped!",
    "serverCrashMessage": "Server has crashed!",
    "mirrorJoin": true,
    "mirrorLeave": true,
    "mirrorDeath": true,
    "mirrorAdvancements": true,
    "mirrorCommandMessages": true
  },
  "crashes": {
    "uploadToMclogs": true
  },
  "debug": false
}
```

## Valid Config

```json5
{
  "discord": { // dont worry, the token and webhook used in this example are expired.
    "token": "NzQ5MjM1NTk4NzMyMjk2MjA0.GnI_ax.ThhYNDCRQoqc6wRScUIomB-jq3ZMbv_IZwGOmw",
    "webhook": "https://discord.com/api/webhooks/1145637647440687184/VdqcLdm7kFYrRNUfQT2X6Kmy6bACyZp9MnKCTwH0o0V79lg9CIsjd9rXOAjt0JwIZmTd",
    "pkMessageDelay": 0,
    "pkMessageDelayMilliseconds": true
  },
  "avatars": {
    "avatarUrl": "https://api.nucleoid.xyz/skin/face/256/%s",
    "useTextureId": false
  },
  "game": {
    "serverStartingMessage": "Server is starting...",
    "serverStartMessage": "Server has started!",
    "serverStopMessage": "Server has stopped!",
    "serverCrashMessage": "Server has crashed!",
    "mirrorJoin": true,
    "mirrorLeave": true,
    "mirrorDeath": true,
    "mirrorAdvancements": true,
    "mirrorCommandMessages": true
  },
  "crashes": {
    "uploadToMclogs": true
  },
  "debug": false
}
```

## Development
TeaBridge is on devOS' Maven

```groovy
maven { url = "https://mvn.devos.one/snapshots/" }
```

```groovy
modImplementation("one.devos.nautical:TeaBridge:<version>")
```
