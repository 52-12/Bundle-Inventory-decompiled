# Bundle Inventory — every version, every loader

The decompiled, buildable source of **Bundle Inventory** (Fabric mod by
Socolio, All Rights Reserved) for **every Minecraft version that has the
scrollable bundle item — 1.21.2 (Bundles of Bravery) through 26.3 — on both
Fabric and NeoForge**, rebuilt from the author's own published builds — plus
Forge from 1.21.3 and a **NilLoader build for 26.x** that runs the mod on
completely vanilla Minecraft with no mod loader at all.

One jar per **release group** per loader — the biggest Minecraft span each
loader can honestly run a single jar on:

**Fabric (9 jars)**

| Jar covers | Official build decompiled | Why this is the biggest range |
|---|---|---|
| 1.21.2 – 1.21.3 | 1.6.0 | The author's own jar covered both |
| 1.21.4 | 1.6.1 | Uses `skipNextRelease`, which 1.21.2 lacks |
| 1.21.5 | 1.7.1 | 1.21.5 rewrote the container-click packet; 1.21.6 the GUI API |
| 1.21.6 – 1.21.8 | 1.7.2 | The author's own jar covered all three |
| 1.21.9 – 1.21.10 | 1.7.3 | The author's own jar covered both |
| 1.21.11 | 1.7.3 | `Identifier` rename + mouse coords on `renderSlot` |
| 26.1 – 26.1.2 | (26.2 source, adapted) | javap diff of every API the mod touches is empty across the line |
| 26.2 | 1.7.3 | 26.3 moved `RenderPipeline` + changed `asMutable`/`isKeyDown` |
| 26.3 | 1.7.3 | — |

**NeoForge (9 jars)** and **Forge (9 jars)** — the same Minecraft walls apply.
NeoForge has no 1.21.2 line (two abandoned betas only) and Forge skipped 1.21.2
as well; Forge pins come from its own promotions file (53.1.16 … 66.1.9). All
Forge groups build with ForgeGradle 7: the official 1.21.6–1.21.10 MDKs still
pin ForgeGradle 6 on Gradle 8, which cannot run in this Gradle 9 build —
their userdev configurations are schema-identical to the FG7-supported
neighbours (verified against every pin's `-userdev.jar` config). One real
namespace difference survives from Forge's history: on 1.21.x, access
transformers key members by **SRG name** (Forge's own production ATs do;
the generated per-era ATs follow), while 26.x uses Mojang names. Quilt runs
the Fabric jars natively (they nest Fabric API's `fabric-api-base` and
`fabric-resource-loader` modules jar-in-jar to mount the mod's assets, which
Quilt Loader supports exactly like Fabric Loader, and the
`fabricloader >=0.16.x` floor is well under Quilt's reported 0.19.5), so no
separate Quilt build exists — verify with one playtest on Quilt. No other
loader has builds for any Minecraft version that has the bundle item.

**NilLoader (3 jars, 26.x only)** — the loader-less build: a standalone Java
agent that runs the mod on stock Minecraft. Only 26.x is viable because the
game is unobfuscated there (no mapping layer — and NilGradle's shaded ASM
cannot read the 26.x class format anyway) and the 26.x-era code draws vanilla
sprites only (a nilmod cannot mount a resource pack; the 1.21.2–1.21.4 eras
draw a mod-provided texture, so they are not portable). NilGradle itself
cannot run on Gradle 9 (`ConfigurationContainer.add` was removed), so the nil
build is self-managed: it resolves the Mojang client jar + library artifacts
from piston-meta, widens a copy of the client jar for javac, and packages
NilLoader embedded (`Premain-Class`/`Agent-Class` + `nilmod.css`). The three
mixins are rewritten as Mini transformers that delegate into the same
unchanged `Hooks` class, and the access widener is replaced by a runtime
access-flag transformer plus the build-time widened compile jar.

Each jar reports honest provenance, e.g. `BundleInventory-Fabric-1.7.2+mc1.21.6-1.21.8.jar`
is a recompile of official build **1.7.2** for **Minecraft 1.21.6–1.21.8**,
and declares exactly that range, so the loader never lets it run where it
would crash. Grouping evidence, in short: the author's own jars spanned
1.21.2–1.21.3 / 1.21.6–1.21.8 / 1.21.9–1.21.10; every member version
compiled green against its era source; and the 26.1–26.1.2 span is verified
by an empty API diff.

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
  neoforge/resources/   access transformer + pack.mcmeta
  forge/java/           Forge entrypoint (@Mod, mods.toml clientSideOnly)
  forge/templates/      mods.toml template
  forge/resources/      access transformer + pack.mcmeta
  nil/java/             NilLoader premain + Mini transformers (26.x eras)
  nil/resources/        bundleinventory.nilmod.css
  mixins/               the official mixin config
  packaging/            icon, language file, textures from the official jar
```

The build is driven by [Stonecutter](https://stonecutter.kikugie.dev/) with
[loom-back-compat](https://codeberg.org/KikuGie/loom-back-compat) (Fabric),
[ModDevGradle](https://github.com/neoforged/ModDevGradle) (NeoForge),
ForgeGradle 7 (Forge) and a self-managed agent build for NilLoader; per-node
coordinates live in `stonecutter.properties.toml`.

## Building

Requires **JDK 25 to run Gradle** (Stonecutter 0.10). The 1.21.x groups compile
with a Java 21 toolchain that Gradle auto-provisions.

```bash
# Everything (30 release groups: 9 Fabric + 9 NeoForge + 9 Forge + 3 NilLoader).
#
# One node at a time is the safe profile: ForgeGradle performs its Minecraft
# setup at CONFIGURATION time, so configure-on-demand is enabled in
# gradle.properties and org.gradle.workers.max=1 keeps memory flat.
# The FIRST build of a NeoForge group runs its NeoForm pipeline and the first
# build of a Forge group its ForgeGradle setup (Minecraft is decompiled for
# that version): ~2-3 minutes each. Everything is cached afterwards, and a
# single node then builds in 3-20 seconds.
./gradlew build --no-parallel

# One group:
./gradlew :1.21.4-fabric:build
./gradlew :26.3-neoforge:build
./gradlew :26.2-forge:build
./gradlew :26.3-nil:build

# Collect a group's jar into build/libs:
./gradlew :26.3-fabric:buildAndCollect
```

Jars land in `versions/<group>/build/libs/`, named
`BundleInventory-<Loader>-<official mod version>+mc<versions>.jar`.
Each group's jar is compiled against the group's **lowest** member and
declares the whole range, so `runClient` also runs that lowest version; to
dev-test another member, temporarily point the group at it in
`settings.gradle`.

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
- **NeoForge and Forge config screens.** The Cloth Config integration is
  Fabric-only, so the NeoForge and Forge builds run on the mod's built-in
  default config (6 rows, sounds on, open-bundle highlight on). Everything
  else works identically. Both declare the mod client-only (NeoForge
  `dist=CLIENT` entrypoint; Forge `clientSideOnly=true` in mods.toml), and
  their jars carry the `pack.mcmeta` those loaders require (copied from each
  line's official MDK).
- **NilLoader build.** The three SpongePowered mixins become Mini transformers
  that call the same `Hooks` methods (NilLoader does not support Mixin), and
  the access widener becomes a runtime access-flag transformer; the build
  additionally compiles against a widened copy of the client jar because
  javac enforces access against the raw jar. No config screen, and no dev
  environment: iterate by dropping the jar into `nilmods/` and restarting.
  With `standalone` enabled the jar embeds NilLoader, so it works directly as
  `-javaagent:`. Its metadata (`bundleinventory.nilmod.css`) carries no
  dependency or Minecraft-version fields — NilLoader has no version gating.
- **26.1 / 26.1.1 / 26.1.2.** The author never published a 26.1 build; these
  nodes compile the official 26.2 source with the one 26.1 difference applied
  (`Minecraft.screen` field instead of the 26.2 `Minecraft.gui.screen()`
  accessor).
- **Metadata.** `fabric.mod.json` declares the exact Minecraft range each jar
  covers (the official jars used broader ranges), and the NeoForge
  `neoforge.mods.toml` marks the mod client-only (`dist=CLIENT`,
  `displayTest="IGNORE_ALL_VERSION"`).
- **Creative-inventory fix, backported.** The official pre-1.21.11 jars gate the
  creative-inventory shift-right-click on the menu slot id
  (`slotId < 9 || slotId >= 45`). On the creative screen only the "Survival
  Inventory" tab wraps player slots with those ids; on other tabs the slots are
  appended after the picker, so the click fell through to vanilla and deleted
  the bundle. All eras now use the upstream fix
  (`52-12/Bundle-Inventory-decompiled@92d0dcbf`): only the container +
  `getContainerSlot() < 36` check.

Behavior otherwise matches each official build per version.

## License / provenance

The original mod is **All Rights Reserved**, © Socolio
([Modrinth](https://modrinth.com/mod/bundle-inventory)). This repository
contains reverse-engineered source produced for interoperability/study; it is
not affiliated with or endorsed by the original author. Do not redistribute the
original author's assets.
