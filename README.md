# Light Up (Fabric server-side)

Light Up places light sources automatically around players to brighten caves or surfaces, with per-tick pacing and undo support. This repository includes a MultiMCGradle setup to build multiple Minecraft versions from one codebase.

Demo video: https://youtu.be/YJgNZasIbfk

[![Light Up Demo](https://img.youtube.com/vi/YJgNZasIbfk/hqdefault.jpg)](https://youtu.be/YJgNZasIbfk "Light Up demo")

## Features
- Server-side only (no client mod required)
- Command-driven lighting with range, min light, skylight toggle, and type (surface/cave/all)
- Per-player undo of the last lighting task
- Action-bar style progress (toggle/format configurable)
- JSON configuration with live reload
- Multi-version builds (1.19.x → 1.21.x)

## Commands
- `/lightup reload` – reloads `config/light-up.json`
- `/lightup cancel` – cancels your active lighting task
- `/lightup undo` – undoes your last lighting task
- `/lightup <block> <min_light_level 0–15> <range> <include_skylight true|false> <lightup_type surface|cave|all>` – starts a new lighting task
- Alias: `/lu`

## Configuration (JSON)
A JSON file is created on first run:

```
config/light-up.json
```

## Version support
- 1.19.x, 1.20.x, 1.21.x

## Acknowledgements
Inspired by the Spigot/PaperMC plugin LightUp by @LOOHP. See [LOOHP/LightUp on GitHub](https://github.com/LOOHP/LightUp).
