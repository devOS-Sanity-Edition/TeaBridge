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
- Markdown support [provided by Styled Chat API]
    - Optional but we recommend you have Styled Chat installed for this to work
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
    "token": "",
    "guild": "",
    "channel": "",
    "webhook": ""
  },
  "game": {
    "serverStartingMessage": "Server is starting...",
    "serverStartMessage": "Server has started!",
    "serverStopMessage": "Server has stopped!",
    "serverCrashMessage": "Server has crashed!",
    "mirrorDeath": true,
    "mirrorAdvancements": true
  },
  "crashes": {
    "uploadToMclogs": true
  }
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