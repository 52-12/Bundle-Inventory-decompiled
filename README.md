# Bundle Inventory — every version, every loader

The decompiled, buildable source of **Bundle Inventory** (Fabric mod by
Socolio, All Rights Reserved) for **every Minecraft version that has the
scrollable bundle item — 1.21.2 (Bundles of Bravery) through 26.3 — on both
Fabric and NeoForge**, rebuilt from the author's own published builds.

One jar per Minecraft version per loader, produced from one repository:

| Minecraft | Fabric | NeoForge |
|---|---|---|
| 1.21.2 | ✅ | — (NeoForge only ever shipped two abandoned betas for this line) |
| 1.21.3 – 1.21.11 | ✅ | ✅ (21.6 / 21.7 / 21.9 lines are beta builds upstream too) |
| 26.1 – 26.3 | ✅ | ✅ |
| Forge | — | — (Forge's last target is 1.21.1, out of range) |
| Quilt | runs the Fabric jars natively, no separate build | |

Each jar reports honest provenance, e.g. `BundleInventory-Fabric-1.6.0+mc1.21.2.jar`
is a recompile of official build **1.6.0** for **Minecraft 1.21.2**.

## How this works

There is no version bridging, no reflection shims, no shared code forced across
API breaks. Instead, **the official Bundle Inventory build for each version line
was downloaded and decompiled**, and every Minecraft version compiles the
decompiled sources of the official build that targeted it:

| Era directory (`src/versions/…`) | Official build decompiled | Compiled for nodes |
|---|---|---|
| `1.21.2-1.21.3` | 1.6.0 (mc 1.21.2–1.21.3, Modrinth) | 1.21.2, 1.21.3 |
| `1.21.4` | 1.6.1 (mc 1.21.4, Modrinth) | 1.21.4 |
| `1.21.5` | 1.7.1 (mc 1.21.5, Modrinth) | 1.21.5 |
| `1.21.6-1.21.8` | 1.7.2 (mc 1.21.6–1.21.8, Modrinth) | 1.21.6, 1.21.7, 1.21.8 |
| `1.21.9-1.21.10` | 1.7.3 (mc 1.21.9–1.21.10, Modrinth) | 1.21.9, 1.21.10 |
| `1.21.11` | 1.7.3 (mc 1.21.11) | 1.21.11 |
| `26.1-26.1.2` | — (no official 26.1 build exists; the 26.2 source adapted to 26.1's pre-`Gui` screen access) | 26.1, 26.1.1, 26.1.2 |
| `26.2` | 1.7.3 (mc 26.2) | 26.2 |
| `26.3` | 1.7.3 (mc 26.3) | 26.3 |

The 1.21.x official jars ship in intermediary names, so each one was remapped
into Mojang official names first (Loom) and decompiled (Vineflower) — that gives
one readable, loader-agnostic `common` tree per era that both loaders compile:

```
src/versions/<era>/
  common/java/          game code decompiled from the official build
                        (loader annotations stripped, nothing else changed)
  fabric/java/          Fabric entrypoint + the Cloth Config / Mod Menu
                        integrations from the same official build
  fabric/resources/     fabric.mod.json + access widener (dev namespace;
                        Loom remaps it to intermediary in the jar)
  neoforge/java/        NeoForge entrypoint (@Mod, dist=CLIENT)
  neoforge/templates/   neoforge.mods.toml template
  neoforge/resources/   access transformer derived from the access widener
  mixins/               the official mixin config
  packaging/            icon, language file, textures from the official jar
```

The build is driven by [Stonecutter](https://stonecutter.kikugie.dev/) with
[loom-back-compat](https://codeberg.org/KikuGie/loom-back-compat) (Fabric) and
[ModDevGradle](https://github.com/neoforged/ModDevGradle) (NeoForge); per-node
coordinates live in `stonecutter.properties.toml`.

## Building

Requires **JDK 25 to run Gradle** (Stonecutter 0.10). The 1.21.x nodes compile
with a Java 21 toolchain that Gradle auto-provisions.

```bash
# Everything (29 nodes: 15 Fabric + 14 NeoForge).
#
# The FIRST build of a NeoForge node runs its NeoForm pipeline (Minecraft is
# decompiled for that version), which is heavy. Build sequentially to keep
# memory bounded; everything is cached from the second run on.
./gradlew build --no-parallel

# One node:
./gradlew :1.21.4-fabric:build
./gradlew :26.3-neoforge:build

# Collect a node's jar into build/libs:
./gradlew :26.3-fabric:buildAndCollect
```

Jars land in `versions/<node>/build/libs/`, named
`BundleInventory-<Loader>-<mod version>+mc<minecraft>.jar`.

## Deviations from the official builds

Kept deliberately minimal, and all mechanical:

- **Entrypoint split.** The official entrypoint class implemented Fabric's
  `ClientModInitializer` and resolved the config inline. The game logic now
  lives in `BundleInventoryMod` (unchanged) and each loader gets a thin
  entrypoint (`BundleInventoryFabricMod` / `BundleInventoryNeoForgeMod`)
  that calls it. Behavior is identical.
- **Loader annotations.** `@Environment(EnvType.CLIENT)` and the matching
  imports are stripped from the shared `common` classes (they do not exist on
  NeoForge); they are metadata only.
- **Decompiler artifacts.** Mixin classes cast themselves through
  `(Object)` (`(AbstractContainerScreen)(Object)this`) — the cast the
  decompiler dropped and Mixin needs to compile.
- **NeoForge config screen.** The Cloth Config integration is Fabric-only, so
  the NeoForge builds run on the mod's built-in default config (6 rows,
  sounds on, open-bundle highlight on). Everything else works identically.
- **26.1 / 26.1.1 / 26.1.2.** The author never published a 26.1 build; these
  nodes compile the official 26.2 source with the one 26.1 difference applied
  (`Minecraft.screen` field instead of the 26.2 `Minecraft.gui.screen()`
  accessor).
- **Metadata.** `fabric.mod.json` declares the exact Minecraft version each jar
  is built for (the official jars used broader ranges), and the NeoForge
  `neoforge.mods.toml` marks the mod client-only (`dist=CLIENT`,
  `displayTest="IGNORE_ALL_VERSION"`).

Behavior otherwise matches each official build per version — including the
pre-1.21.11 creative-inventory guard, which the author changed in the mc1.21.11
build (older versions keep the guard their official jars shipped with).

## License / provenance

The original mod is **All Rights Reserved**, © Socolio
([Modrinth](https://modrinth.com/mod/bundle-inventory)). This repository
contains reverse-engineered source produced for interoperability/study; it is
not affiliated with or endorsed by the original author. Do not redistribute the
original author's assets.
