# FAQ for Neu21Plus

## What is Neu21Plus?

Neu21Plus is a community-driven port of the popular NotEnoughUpdates (NEU) mod from Minecraft 1.8.9 Forge to Minecraft 26.1 Fabric. It aims to bring the same SkyBlock quality-of-life features to the latest Minecraft version.

## What features are currently available?

See the [Features section in README.md](README.md#features) for a complete list of implemented and planned features. Not all original NEU features have been ported yet — this is a work in progress.

## Missing Repo Data

If you get a "Missing Repo Data" popup:
1. Run `/neu resetrepo` to re-download the item repository
2. Check your internet connection
3. The default repo URL is `https://github.com/NotEnoughUpdates/NotEnoughUpdates-repo`
4. If the issue persists, open a [GitHub Issue](https://github.com/BS-Ji-007/Neu21plus/issues)

## Equipment Overlay

The equipment overlay shows the current equipment of the player. If it doesn't fit with your texture pack, go to `/neu` settings and change the overlay style.

## How do I turn on fairy soul waypoints?

Run `/neu` and navigate to the relevant settings page, or use the keybinding configured in your controls menu.

## How do I move a GUI element?

Go to the second tab of `/neu` settings where you can find all the movable GUI elements.

## How do I set my Hypixel API key?

1. Join Hypixel and type `/api new` in chat
2. Copy the API key
3. In Neu21Plus, run `/neu api <your-key>`
4. Price data and profile viewer will now work

## Profile Viewer isn't loading

- Make sure your Hypixel API key is set (`/neu api <key>`)
- The player must be online on Hypixel SkyBlock
- API data is cached for 60 seconds — wait and try again
- Check that the player name is spelled correctly

## The mod crashes on startup

- Verify you are using Minecraft 26.1 (not a different version)
- Ensure Fabric Loader 0.19.2+ and Fabric API are installed
- Check that you are using Java 25
- Look at the crash log for specific errors and open a [Bug Report](https://github.com/BS-Ji-007/Neu21plus/issues/new?template=bug_report.md)

## How is this different from the original NEU?

| Feature | Original NEU | Neu21Plus |
|---------|-------------|-----------|
| Minecraft | 1.8.9 | 26.1 |
| Mod Loader | Forge | Fabric |
| Java | 8 / 17 | 25 |
| Mappings | MCP / SRG | Mojang Official |
| Item Data | NBT | Data Components |
| Mixin System | Forge Mixin | Fabric Mixin |
| Config | MoulConfig (Forge) | MoulConfig (Fabric modern) |

## Where can I get help?

- [GitHub Discussions](https://github.com/BS-Ji-007/Neu21plus/discussions)
- [GitHub Issues](https://github.com/BS-Ji-007/Neu21plus/issues)
- [Original NEU Discord](https://discord.gg/moulberry) (for original NEU questions only)
