# FIRED Client

FIRED Client is a Forge 1.8.9 client mod for Minecraft focused on Bedwars HUDs, quality-of-life toggles, and client-side visual customization. Fully vibe coded with Opus 4.6 and GPT5.4/5.

## Overview

- `39` modules
- `72` typed settings
- Minecraft `1.8.9`
- Java `8`
- Forge Gradle build

The mod stores its config in `.minecraft/firedclient/config.json` (existing AstroClient configs are migrated automatically). and supports draggable HUD overlays through the built-in HUD editor.

## Features

- Movement utilities: `ToggleSprint`, `ToggleSneak`, `Freelook`
- HUD modules: FPS, coordinates, CPS, keystrokes, ping, clock, memory, server address, day counter, direction, combo, reach, item count, resource pack, armor status, potion effects, Bedwars stats, Bedwars timers, cooldowns, and more
- Player rendering tools: `Hitboxes`, `FOVChanger`, `NoHurtCam`, `Nametags`, `Scoreboard`, `CustomCrosshair`
- World visual tweaks: `MotionBlur`, `MenuBlur`, `TimeChanger`, `WeatherChanger`, `BlockOutline`
- Misc tools: `AutoGG`, `ChatMod`

## Controls

| Action | Key |
|---|---|
| Open ClickGUI | `Right Shift` |
| Open HUD Editor | `H` |
| Zoom | `Q` held |
| Freelook | `Left Alt` held |

## Commands

- `/firedkey <key>` or `/setapikey` sets the Hypixel API key used by `Level Head`

## Build

```bash
./gradlew build
```

The compiled jar is written to `build/libs/`.

## Config

- Main config: `.minecraft/firedclient/config.json`
- API key file: `.minecraft/firedclient/apikey.txt`
- Forge splash config: `.minecraft/config/splash.properties`

## Notes

- `Zoom` and `Freelook` are hold-style modules.
- `HUDModule` provides draggable, scalable on-screen elements.
- `ConfigManager` persists enabled state, keybinds, visibility, HUD positions, typed settings, and a few legacy custom fields.
- FIRED user markers are automatic only: detected FIRED clients get an F marker in nametags and the tab list. The legacy detection channel is retained for compatibility.
