<h1 align="center">Neu21Plus</h1>

<div align="center">
    <!-- release -->
    <a href="https://github.com/BS-Ji-007/Neu21plus/releases/latest" target="_blank">
        <img src="https://img.shields.io/github/v/release/BS-Ji-007/Neu21plus?color=informational&include_prereleases&label=release&logo=github&logoColor=white" alt="release">
    </a>
    <!-- license -->
    <a href="./COPYING.LESSER" target="_blank">
        <img src="https://img.shields.io/github/license/BS-Ji-007/Neu21plus?color=informational" alt="license">
    </a>
    <!-- contributors -->
    <a href="https://github.com/BS-Ji-007/Neu21plus/graphs/contributors" target="_blank">
        <img src="https://img.shields.io/github/contributors/BS-Ji-007/Neu21plus?color=informational&logo=GitHub" alt="contributors">
    </a>
    <!-- mc version -->
    <a href="https://fabricmc.net/develop/" target="_blank">
        <img src="https://img.shields.io/badge/minecraft-26.1-informational?logo=minecraft&logoColor=white" alt="mc-version">
    </a>
    <!-- fabric -->
    <a href="https://fabricmc.net/" target="_blank">
        <img src="https://img.shields.io/badge/modloader-fabric-1976d2?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0xMiAyTDIgN2wxMCA1IDEwLTV6Ii8+PC9zdmc+" alt="fabric">
    </a>
</div>

**Neu21Plus** is a community-driven port of [NotEnoughUpdates (NEU)](https://github.com/NotEnoughUpdates/NotEnoughUpdates) from Minecraft 1.8.9 Forge to **Minecraft 26.1 Fabric**. It brings the essential SkyBlock quality-of-life features to the latest Minecraft version.

> ⚠️ **This is a work-in-progress.** Not all features from the original NEU have been ported yet. See [Features](#features) for current status.

## Installation

### Prerequisites

- **Minecraft 26.1**
- **Fabric Loader** 0.19.2 or later — [Install Guide](https://fabricmc.net/use/installer/)
- **Fabric API** — [Download](https://modrinth.com/mod/fabric-api)

### Steps

1. Install Fabric Loader for Minecraft 26.1
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your `.minecraft/mods/` folder
3. Download the latest Neu21Plus release from [GitHub Releases](https://github.com/BS-Ji-007/Neu21plus/releases)
4. Place the Neu21Plus JAR in your `.minecraft/mods/` folder
5. Launch Minecraft with the Fabric profile
6. Join Hypixel SkyBlock and type `/neu` to configure the mod

## Features

### ✅ Implemented

- **Item Repository** — Browse every SkyBlock item with recipes and information (data from [NEU Repo](https://github.com/NotEnoughUpdates/NotEnoughUpdates-repo))
- **NEU Overlay** — In-game item browser overlay with search
- **Recipe Viewer** — Crafting, Forge, Trade, Shop, Mob Loot, and Kat recipes
- **Tooltip Modifications** — Custom item tooltips with price information
- **Price Data** — Bazaar and lowest BIN prices from the Hypixel API
- **Dungeon Features** — Minimap, room detection, puzzle solvers, score calculation, win messages
- **Mining Features** — Dwarven Mines overlay, commission tracker, Crystal Hollows map, fossil/metal detector solvers
- **Profile Viewer** — In-game player profile viewer accessed with `/pv [player]`
- **Slayer Tracker** — Track slayer boss kills and drops
- **Farming Overlay** — Farming contest tracking and crop displays
- **Pet Info** — Pet display and information overlay
- **Auction House Helper** — Enhanced auction house browsing
- **Wardrobe Stats** — View wardrobe equipment stats
- **Notification System** — Toast notifications for important events
- **Custom Keybinds** — Configurable keybindings for all features
- **Chat Processing** — Custom chat handler with feature integration
- **Collection Display** — SkyBlock collection progress tracking
- **Accessory Helper** — Accessory bag management
- **Equipment Comparison** — Compare equipment stats side by side
- **Fairy Soul Waypoints** — Track and display fairy soul locations
- **Mayor Display** — Current mayor and special mayor information
- **Storage Viewer** — Enhanced storage GUI
- **Tab Overlay** — Improved tab list for SkyBlock
- **Action Bar Display** — Custom action bar information
- **Bazaar Helper** — Enhanced bazaar browsing interface
- **Inventory Buttons** — Custom clickable buttons in inventories
- **Crafting Overlay** — Recipe crafting guidance overlay
- **Recipe History** — Navigate through recently viewed recipes
- **Item Price Information** — Show item prices in tooltips

### 🔄 In Progress / Planned

- Custom Storage GUI
- Custom Enchanting Table GUI
- Enchant Color Customization (`/neuec`)
- Slot Locking
- Fishing Particle Alerts
- Block Zapper / Builder's Wand Overlays
- Full dungeon solver suite
- Custom AH search GUI

## Commands

| Command | Description |
|---------|-------------|
| `/neu` | Open the main NEU settings menu |
| `/neu help` | Show available commands |
| `/neu reload` | Reload config and repo data |
| `/neu resetrepo` | Re-download the item repository |
| `/neu api <key>` | Set your Hypixel API key |
| `/neu pv [player]` | Open the Profile Viewer |
| `/neu profile` | Show your current profile info |
| `/neu toggle <feature>` | Toggle a feature on/off |

## Configuration

Neu21Plus uses [MoulConfig](https://github.com/romangraef/MoulConfig) for its configuration GUI. Access it via `/neu` in-game. Configuration categories include:

- **Item Browser** — Overlay position, search settings, display options
- **Price Data** — API settings, bazaar/BIN display, refresh interval
- **Dungeons** — Minimap, solvers, score display, win messages
- **Mining** — Commission overlay, Crystal Hollows map, drill fuel
- **Profile Viewer** — PV display settings, player info options
- **Misc** — Slayer tracker, farming overlay, pet display, auction helper, wardrobe stats, cookie buff timer

The item repository is loaded from `https://github.com/NotEnoughUpdates/NotEnoughUpdates-repo` by default.

## Development

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup and coding standards.

### Tech Stack

| Component | Version |
|-----------|---------|
| Minecraft | 26.1 |
| Fabric Loader | 0.19.2 |
| Fabric API | 0.149.1+26.1.2 |
| Java | 25 |
| Gradle | 9.5.1 |
| Fabric Loom | 1.16-SNAPSHOT |
| MoulConfig | 4.6.0 (modern-26.1) |
| Shadow Plugin | 9.0.0-beta4 |

## Credits

- **Moulberry** and the original [NotEnoughUpdates](https://github.com/NotEnoughUpdates/NotEnoughUpdates) contributors — The original mod this is ported from
- **Neu21Plus contributors** — Everyone who helps with the Fabric 26.1 port

## Support

- [GitHub Discussions](https://github.com/BS-Ji-007/Neu21plus/discussions) — Ask questions and get help
- [GitHub Issues](https://github.com/BS-Ji-007/Neu21plus/issues) — Report bugs or request features
- [Original NEU Discord](https://discord.gg/moulberry) — For questions about the original 1.8.9 mod

## Security

If you have found a vulnerability, please follow our [security policy](.github/SECURITY.md).

## License

This project is licensed under LGPL-3.0-or-later, see [COPYING](COPYING) and [COPYING.LESSER](COPYING.LESSER) for more details.

This is a port of NotEnoughUpdates, which is also licensed under LGPL-3.0-or-later.
