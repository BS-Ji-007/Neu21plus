# Contributing to Neu21Plus

Thank you for your interest in contributing to Neu21Plus! This guide will help you get started with developing the Fabric 26.1 port of NotEnoughUpdates.

## Quick Note

Neu21Plus is a community-driven port of [NotEnoughUpdates](https://github.com/NotEnoughUpdates/NotEnoughUpdates) from Minecraft 1.8.9 Forge to Minecraft 26.1 Fabric. Contributions are welcome via pull requests to [BS-Ji-007/Neu21plus](https://github.com/BS-Ji-007/Neu21plus).

## Before You Contribute

- Check if your feature or bug fix has already been addressed in open [pull requests](https://github.com/BS-Ji-007/Neu21plus/pulls) or [issues](https://github.com/BS-Ji-007/Neu21plus/issues).
- Make sure your feature idea complies with the [Hypixel Rules](https://hypixel.net/rules). Refer to these posts for guidance: [Mods in SkyBlock](https://hypixel.net/threads/regarding-the-recent-announcement-with-mods-in-skyblock.4045481/), [QoL Modifications](https://hypixel.net/threads/update-to-disallowed-modifications-qol-modifications.4043482/), [Invalid Clicks](https://hypixel.net/threads/update-regarding-modifications-sending-invalid-clicks.5130489/).
- Verify that your feature is not already implemented in another free mod.
- All contributions must follow the project coding standards (see below).

## Development Environment Setup

### Prerequisites

- **Java 25** (Eclipse Temurin recommended) — [Download](https://adoptium.net/temurin/releases)
- **Git** — [Download](https://git-scm.com/downloads)
- **IntelliJ IDEA** (recommended) — [Download](https://www.jetbrains.com/idea/download)
- **Minecraft 26.1** with Fabric Loader 0.19.2+

### Setting Up

1. Fork the repository at [BS-Ji-007/Neu21plus](https://github.com/BS-Ji-007/Neu21plus)
2. Clone your fork: `git clone https://github.com/<YourUserName>/Neu21plus`
3. Switch to the development branch: `git checkout port-26.1`
4. Create a feature branch: `git checkout -b feature/your-feature-name`
5. Open the project in IntelliJ IDEA and import it as a Gradle project
6. Set your project SDK to **Java 25** (File > Project Structure > Project SDK)
7. Set your Gradle JVM to **Java 25** (Settings > Build > Gradle > Gradle JVM)
8. Wait for Gradle to sync and download dependencies
9. Run the `runClient` Gradle task to verify everything works

### Build Commands

```bash
# Clean and build
./gradlew clean build

# Run the client
./gradlew runClient

# Generate IDE run configurations
./gradlew ide
```

## Coding Standards

These rules are mandatory for all contributions:

1. **No `@Suppress` annotations** — Fix the underlying issue instead.
2. **No comments** — Code should be self-documenting. Use clear variable and method names.
3. **No compiler warnings** — All code must compile cleanly.
4. **Mojang official mappings** — Always use official (Mojang) mappings, never SRG or MCP.
5. **Data Components over NBT** — Use `DataComponents` API instead of NBT for item data on MC 26.1.
6. **Human-like code** — Write clean, readable code as if a human wrote it from scratch.
7. **MC 26.1 API only** — Do not use deprecated or legacy APIs from older MC versions.
8. **`@Override` where appropriate** — Always use `@Override` on methods that override parent class or interface methods.
9. **Package structure** — All code goes under `io.github.legentpc.neu21plus`.

## Porting from Original NEU

When porting features from the original NEU (1.8.9 Forge), keep these API changes in mind:

| Original NEU (1.8.9 Forge) | Neu21Plus (26.1 Fabric) |
|---|---|
| NBT (`ItemStack.getTag()`) | Data Components (`stack.get(DataComponents.XXX)`) |
| `new ClickEvent(Action, String)` | `ClickEvent.runCommand(cmd)` / `ClickEvent.openUrl(url)` |
| SRG/MCP mappings | Mojang official mappings |
| `ItemStack.getTooltipLines(...)` | `getTooltipLines(Item.TooltipContext, Player, TooltipFlag)` |
| `player.getArmorSlots()` | `player.getItemBySlot(EquipmentSlot)` |
| Forge events | Fabric API events |
| `ResourceLocation(String, String)` | `ResourceLocation.withDefaultNamespace(String)` / constructor |
| Custom NBT component | `CustomData` component |

## Pull Request Process

1. Ensure your code compiles with `./gradlew clean build` (no errors, no warnings).
2. Test your changes in-game on MC 26.1 Fabric.
3. Follow the PR template when creating your pull request.
4. Use a descriptive PR title following naming conventions: `Add`, `Fix`, `Remove`, `Port`, `Improve`, `Refactor`, `Update`, or `meta:`.
5. Request a review from the appropriate code owner.
6. Address all review feedback before merge.

## Reporting Bugs

Please use the [Bug Report](https://github.com/BS-Ji-007/Neu21plus/issues/new?template=bug_report.md) template when filing issues. Include your MC version, Fabric Loader version, and crash logs if applicable.

## Security

If you find a security vulnerability, please follow our [security policy](.github/SECURITY.md). Do NOT open public issues for security vulnerabilities.
