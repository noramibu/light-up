# Light Up (Fabric, Quilt, NeoForge)

Light Up places light sources automatically around players to brighten caves or surfaces, with per-tick pacing and undo support. This repository targets Fabric, Quilt, and NeoForge for Minecraft 26.1.x (26.1, 26.1.1, 26.1.2), built against 26.1.2.

Demo video: https://youtu.be/0liju6_XNwA

[![Light Up Demo](https://img.youtube.com/vi/0liju6_XNwA/hqdefault.jpg)](https://youtu.be/0liju6_XNwA "Light Up demo")

## Features
- Server-side only logic (no client-side features required)
- Command-driven lighting with range, min light, skylight toggle, and type (surface/cave/all)
- Per-player undo of the last lighting task
- Action-bar style progress (toggle/format configurable)
- JSON configuration with live reload
- Multi-loader 26.1.x compatible build (Fabric, Quilt, NeoForge)

## Commands
- `/lightup reload` - reloads `config/light-up.json`
- `/lightup cancel` - cancels your active lighting task
- `/lightup undo` - undoes your last lighting task
- `/lightup <block> <min_light_level 0-15> <range> <include_skylight true|false> <lightup_type surface|cave|all>` - starts a new lighting task
- Alias: `/lu`

## Configuration (JSON)
A JSON file is created on first run:

```
config/light-up.json
```

## Acknowledgements
Inspired by the Spigot/PaperMC plugin LightUp by @LOOHP. See [LOOHP/LightUp on GitHub](https://github.com/LOOHP/LightUp).
