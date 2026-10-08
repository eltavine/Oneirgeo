# Oneirgeo

**English** | [简体中文](README.zh-CN.md)

*The clocks stopped at 3:17. The lights are still on.*

Oneirgeo is a Fabric mod that turns Minecraft into a vast, surreal dreamscape. It replaces the Overworld, Nether, and End, and adds the Mirror Sea, Poolrooms, and Backrooms. Across six dimensions, familiar places become strange, space folds back on itself, and fragments of memory tell a story.

Dreamcore, weirdcore, liminal spaces, and psychological horror shape the experience. Explore, find your way between worlds, and remember what these places mean.

Current prerelease: **1.0.1**. Downloads are available on the [GitHub Releases page](https://github.com/eltavine/Oneirgeo/releases).

## Six worlds

Every Oneirgeo dimension spans **4,064 blocks vertically**, from Y = -2032 to Y = 2031.

| World | What you will find |
| --- | --- |
| Overworld | A vast neural web suspended in fog, with tissue-like structures, synapses, and traces of home. |
| Nether | Boiler corridors, ash plains, lava seas, an immense hearth, and a hanging city. |
| End | A night sea, star cemeteries, impossible geometry, and an observatory. |
| Mirror Sea | A still, reflective expanse, standing mirrors, and another side beneath the surface. |
| Poolrooms | Endless tiled pools, quiet water, enclosed rooms, and doors that may lead elsewhere. |
| Backrooms | Yellow wallpaper, damp carpet, fluorescent lights, and seemingly endless office rooms. |

## Features

- **Folded space and unusual gravity.** Spatial seams, inverted regions, and enclosed traps make navigation part of the mystery.
- **Dreaming and waking.** Sleeping in the Overworld takes you into the Poolrooms. Wake doors return you, and sufficiently low lucidity in the Poolrooms ends the dream.
- **Hidden lucidity.** Darkness, dream dimensions, and spatial anomalies wear down your awareness. Light, food, sleep, and lucid tea can restore it; low lucidity changes the atmosphere and encounters.
- **A story told through exploration.** Collect memory fragments, read your dream journal, and recover VHS recordings across six chapters.
- **Unsettling encounters.** Faceless figures, stalkers, and mimics share these spaces with lifeguards and night nurses.
- **Ways through the dream.** Mirror portals, hidden doors, elevators, and updrafts connect places and heights.
- **Survival supplies.** Periodic deliveries provide items according to your dimension. The interval is configurable; trapped rooms interrupt automatic deliveries.
- **Nothing left behind.** New worlds keep your inventory when you die, and the dream journal, VHS tapes, and other story items cannot be dropped or thrown away.
- **Dreamlike presentation.** Custom ambience, reverb, distant silhouettes, altered skies, post-processing, and a camcorder-style view. Client settings let you adjust the effects.

In-game text is available in **English and Simplified Chinese**.

## Requirements and installation

The current source configuration targets:

| Component | Version |
| --- | --- |
| Minecraft | `26.3` |
| Fabric Loader | `0.19.5` or newer |
| Fabric API | `0.162.0+26.3` |
| Java | `25` |

1. Set up a Fabric instance for the target Minecraft version.
2. Download the mod JAR from [GitHub Releases](https://github.com/eltavine/Oneirgeo/releases), or build it from source using the instructions below.
3. Place the mod JAR and the matching Fabric API JAR in the instance's `mods` directory. Use the mod JAR, not the `-sources.jar`.
4. Launch the game and create a **new world with the default world preset** to experience Oneirgeo's replacement world generation.

For multiplayer, install Oneirgeo and Fabric API on both the server and every client. Oneirgeo changes world generation and includes client visuals and networking; a new world provides the intended experience.

## Getting started

You receive a dream journal when you first join. Explore and use memory fragments to fill it; completing a chapter gives you a VHS tape.

- Sleep in an Overworld bed to enter the Poolrooms. Look for a wake door to return.
- Build a vertical rectangular frame from **Mirror Frame** blocks, leave the interior empty, and use a **Mirror Shard** on a frame face pointing into the opening to create a portal to the Mirror Sea. A 2-block-wide, 3-block-high opening works.
- Stand on an **Elevator** block and press jump to reach the next elevator directly above, or sneak to reach one below.
- Pay attention to doors, phones, televisions, clocks, and objects that seem out of place.

## Client settings

Settings are stored in `config/oneirgeo-client.json`. Use `/oneirgeofx` to view them, or change them in-game:

| Command | Effect |
| --- | --- |
| `/oneirgeofx safe_mode true` | Suppress flickering effects and slow remaining transitions. |
| `/oneirgeofx effects false` | Disable the dream post-processing effects. |
| `/oneirgeofx intensity 0.5` | Set the master effect intensity; range: `0`–`2`. |
| `/oneirgeofx camcorder false` | Disable the camcorder presentation. |
| `/oneirgeofx shake 0` | Set camera shake intensity; range: `0`–`2`. |
| `/oneirgeofx reverb false` | Disable audio reverb. |
| `/oneirgeofx reload` | Reload the configuration file. |

Additional toggles include `screen_text`, `far_silhouettes`, and `wrong_sky`, each accepting `true` or `false`.

## Building and development

Install JDK 25 and make sure Gradle uses it. The repository includes the Gradle Wrapper.

```sh
git clone https://github.com/eltavine/Oneirgeo.git
cd Oneirgeo
./gradlew build
```

Build artifacts are written to `build/libs/`. On Windows, replace `./gradlew` with `gradlew.bat`.

| Command | Purpose |
| --- | --- |
| `./gradlew runClient` | Launch the development client. |
| `./gradlew runServer` | Launch the development dedicated server. |
| `./gradlew runDatagen` | Regenerate resources under `src/main/generated/`. |
| `./gradlew runSelftest` | Run the headless generation smoke test across all dimensions. |
| `./gradlew runClientGameTest` | Run the visual smoke test and capture screenshots under `run/gametest/screenshots/`. |

The development client defaults to a 4 GB heap; the headless self-test defaults to 3 GB. Override the configured heap when needed:

```sh
./gradlew runClient -Poneirgeo.heap=6G
```

Generated assets and world data are versioned; data generation caches and local runtime files are ignored.

### Operator commands

These commands require game-master permissions, such as an operator account or cheats enabled in singleplayer.

| Command | Purpose |
| --- | --- |
| `/oneirgeo where` | Describe the current layer, scene, biome, and spatial conditions. |
| `/oneirgeo dim <dimension>` | Travel to a dimension, e.g. `oneirgeo:poolrooms`. |
| `/oneirgeo layer <name>` | Travel to a named layer in the current dimension; use tab completion for names. |
| `/oneirgeo stats` | Show chunk generation timings. |
| `/oneirgeo seams` | Inspect nearby spatial seams. |
| `/oneirgeo lucidity [value]` | Read lucidity or set it to a value from `0` to `1`. |
| `/oneirgeo supply` | Deliver supplies immediately. |
| `/oneirgeo story` | Inspect story progress. |

Game rules: `oneirgeo:lucidity` controls whether lucidity changes; `oneirgeo:supply_interval` sets automatic supply intervals in ticks (default `6000`, or five minutes at 20 ticks per second; `0` disables deliveries). Oneirgeo also turns the vanilla `keep_inventory` rule on by default, so new worlds keep inventories through death; use `/gamerule keep_inventory false` to turn it off. Existing worlds keep their current setting.

## License

Created by **eltavine**. Licensed under **Apache-2.0**; see [LICENSE.txt](LICENSE.txt).
