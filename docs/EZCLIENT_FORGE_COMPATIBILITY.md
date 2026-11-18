# ezclient → Forge 1.8.9 compatibility work

Status: **analysis / scaffolding only**. This branch is isolated from `main`; no Fabric code has been removed and no game compatibility is claimed yet.

## Goal

Make ezclient the primary FIRED game client on Minecraft 1.8.9, running on Forge 11.15.1.2318 with OptiFine, without OptiFabric. Keep the original ezclient project/source intact and port only through this experimental branch. Do not merge into `main` until the game launches and the feature checklist passes.

## What the upstream 1.8.9 implementation currently depends on

The upstream `mod-legacy` module is built with Fabric Loom / Legacy Looming and depends on:

- Fabric Loader (`fabricloader`) and Legacy Fabric API.
- Fabric client entrypoint `eu.ezclient.EzClient implements ClientModInitializer`.
- Legacy Fabric keybinding, tick/lifecycle, HUD-render, and entity-renderer registration callbacks.
- Fabric Loader metadata lookup in `EzClient.VERSION`.
- Mixin configuration `ezclient.mixins.json`, referenced by `fabric.mod.json`.
- Mixins under `eu.ezclient.mixin` that inject into Minecraft client, HUD, input, renderers, resources, and title screen.

This is not a drop-in Forge mod. In particular, the upstream sources use Yarn/Legacy Fabric names such as `MinecraftClient`, `setScreen`, and Fabric callback types; Forge 1.8.9 uses MCP names and Forge event registration. The source must be ported/mapped, not merely have its metadata file renamed.

## Existing FIRED Forge infrastructure that can be reused

This repository already has a Forge 1.8.9 event bridge:

- `dev.astro.event.ForgeEventBridge`: client tick, overlay render, keyboard/mouse input, GUI-open, FOV and camera events.
- `dev.astro.event.EventBus`: internal event dispatch.
- `dev.astro.AstroClient`: Forge mod initialization and key registration.

These are candidate integration points for the ezclient feature code. They are not yet an adapter for ezclient and do not prove that its modules work on Forge.

## Porting plan

1. **Preserve source and isolate work** — do all experiments on this branch; do not modify the upstream ezclient repository or the stable FIRED branch.
2. **Build a compatibility layer** — provide Forge-backed equivalents for initialization, tick/start/stop events, keybindings, HUD rendering, and player cosmetic renderer registration.
3. **Port mixin-dependent behavior** — replace each required mixin with a Forge event, an access transformer/coremod only where unavoidable, or a narrowly scoped Forge-compatible hook. OptiFine conflicts must be tested individually.
4. **Port Minecraft API usage** — translate Legacy Fabric/Yarn names and APIs to Minecraft 1.8.9 MCP/Forge names. Keep feature logic separate from game hooks where possible.
5. **Integrate features in small groups** — menu and keybindings; HUD modules; zoom/input; cosmetics/player rendering; Discord and persistence.
6. **Add OptiFine only after Forge baseline works** — use the official OptiFine 1.8.9 installer/mod distribution appropriate to Forge. Do not package the preview jar as a production dependency.
7. **Validate** — clean Gradle build, launch to title screen, enter a world/server, open Mods and Cosmetics screens, test each HUD/module, test cosmetic rendering, test OptiFine video settings and shader path if supported, then inspect logs for mixin/coremod conflicts.

## Acceptance checklist

- [ ] Forge 1.8.9 starts without Fabric Loader or OptiFabric.
- [ ] OptiFine is loaded and its video settings are available.
- [ ] ezclient main menu and Mods screen open.
- [ ] HUD editor and HUD modules render and save settings.
- [ ] Zoom and keybindings work.
- [ ] Cosmetics render on the local player and other players.
- [ ] No regression in game startup, world join, or shutdown.
- [ ] FIRED Launcher launches this exact tested installation.

## Important

This branch currently contains this migration plan only. No ezclient modules have been ported in this commit, and no successful Forge + OptiFine build or in-game test is being claimed.
