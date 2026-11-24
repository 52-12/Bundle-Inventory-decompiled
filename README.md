# Bundle Inventory — every version, every loader

The decompiled, buildable source of **Bundle Inventory** (Fabric mod by
Socolio, All Rights Reserved) for **every Minecraft version that has the
scrollable bundle item — 1.21.2 (Bundles of Bravery) through 26.3 — on both
Fabric and NeoForge**, rebuilt from the author's own published builds — plus
Forge from 1.21.3 and a **NilLoader build** that runs the mod on
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

**NilLoader (11 jars)** - the loader-less build: a standalone Java agent that
runs the mod on stock Minecraft. 26.x is unobfuscated, so those three jars are
ordinary builds; the eight 1.21.x jars carry a mapping layer. NilLoader has no
version gating of its own and swallows a failed Mini patch, and obfuscated
class names drift even between point releases (about 72% of classes changed
1.21.2 -> 1.21.3 and 28% 1.21.6 -> 1.21.8), so a jar built from the wrong
version's mappings is silently inert. Each obfuscated jar is therefore pinned
to one Minecraft version, or to a pair whose mappings are byte-identical
(1.21.6-1.21.7 and 1.21.9-1.21.10). NilGradle itself cannot run on Gradle 9
(`ConfigurationContainer.add` was removed), so the nil build is self-managed:
it resolves the Mojang client jar + library artifacts from piston-meta, widens
a copy of the client jar for javac, and packages NilLoader embedded
(`Premain-Class`/`Agent-Class` + `nilmod.css`). The three mixins are rewritten
as Mini transformers that delegate into the same unchanged `Hooks` class, and
the access widener is replaced by a runtime access-flag transformer plus the
build-time widened compile jar.

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
- **Config screens.** Settings live in `config/bundleinventory.properties` on
  every loader, with one exception: Fabric/Quilt **with Cloth Config
  installed** read `config/bundleinventory.json5` instead, because the
  original Cloth AutoConfig screen is what edits them there. Fabric exposes
  its settings through Mod Menu (Cloth screen when Cloth is present, the
  built-in vanilla-widget screen otherwise), Forge through
  `ConfigScreenHandler.ConfigScreenFactory` (Config button in the Mods list)
  and NeoForge through `IConfigScreenFactory` (Config button). The built-in
  screen writes the properties file, so Cloth is optional, and `fabric.mod.json`
  lists `modmenu` and `cloth-config` under `suggests`. NilLoader has no config
  API, so it reads the properties file only. Both non-Fabric entrypoints
  declare the mod client-only (NeoForge `dist=CLIENT`; Forge
  `clientSideOnly=true`), and their jars carry the `pack.mcmeta` those loaders
  require (copied from each line's official MDK).
- **NeoForge 1.21.9 caveat.** NeoForge's last 1.21.9 build (21.9.16-beta)
  crashes its own Mods screen: `ModListScreen.reloadMods` stores
  `stream().toList()` and `tick` then calls `.sort()` on it
  (`UnsupportedOperationException` in `ImmutableCollections`). That is
  upstream and happens with or without this mod. The 1.21.9-1.21.10 node still
  compiles against 1.21.9 (the oldest supported version) so the jar stays safe
  on both, but its playtest instance runs Minecraft 1.21.10 with NeoForge
  21.10.64, where the list is collected mutably and the screen opens. Verified
  there: join, Mods screen, and bundle insert/take-out.
- **Forge 26.x caveats.** Forge 62.0.9 (the 26.1 line) crashes in its own
  `FieldToMethodTransformer` (`Field fluid is not private`) even with no mods
  installed, so the 26.1-26.1.2 node and its instance target 26.1.2 with Forge
  64.1.5. Forge 26.x also ships upstream Mixin 0.8.7, which has no `JAVA_25`
  level, so those jars package the mixin config as `JAVA_21` while the code
  still targets Java 25 (ForgeGradle declares the artifacts JVM-25-only and
  refuses a 21 target). Verified joining on 26.1.2/64.1.5, 26.2 and 26.3.
  Both lines also ship a buggy `BundleItem` patch: `overrideOtherStackedOnMe`
  starts with `if (other.getCount() != 1) return false` where 26.3 checks
  `self.getCount()`. Because the cursor is empty (or holds a whole stack) in
  normal bundle clicks, right-clicking a bundle to take an item out picked the
  bundle itself up instead, and stack insertion silently failed - in vanilla
  screens too. `BundleItemClickFixMixin` redirects just that first
  `getCount()` call for the 26.1-26.1.2 and 26.2 jars (a no-op where the
  instruction is absent, such as Fabric/NeoForge/26.3) and returns the bundle
  count where the receiver really is the bundle. Verified on Forge 26.1.2 and
  26.2: insert a stack, then take it back out. On a remote Forge 26.1/26.2
  server without this mod the upstream bug still applies.
- **NilLoader build.** The three SpongePowered mixins become Mini transformers
  that call the same `Hooks` methods (NilLoader does not support Mixin).
  Eleven jars: eight cover 1.21.2-1.21.11 (pinned per version where the
  obfuscated names drift) and three cover 26.x; the jar embeds NilLoader so
  it runs directly as `-javaagent:` with no loader installed. Two modes:
  - **26.x** is unobfuscated, so no mapping layer is involved.
  - **1.21.x is obfuscated.** Those nodes download Mojang's client mappings
    and use them twice: to rewrite the client jar to named classes for javac,
    and to emit `META-INF/nil/mappings.json` so NilLoader remaps this mod's
    classes and Mini patch strings when they load. Lorenz's runtime
    inheritance provider only sees obfuscated classes, so the mappings are
    **flattened**: every Minecraft member our compiled classes reference -
    including inherited calls such as `this.addSlot(...)` and interface
    methods like `ItemStack.get(...)` - gets a direct entry under the
    call-site owner. Access-widened members ride along in the same file's
    `widen` block, so no Fabric access widener is involved.
  - Premain runs before NilLoader freezes its mappings and classes loaded
    before the freeze are never remapped, so the mod initialises lazily from
    the patched call sites (`NilBootstrap`) rather than at premain.

  No config screen (properties file only) and no dev environment: iterate by
  dropping the jar into the working directory and restarting. Its metadata
  (`bundleinventory.nilmod.css`) carries no dependency or Minecraft-version
  fields - NilLoader has no version gating.
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
- **Fullness-bar style option (new).** `useBossBarFullness` (default on) is
  exposed in the Cloth config on Fabric and defaults to on everywhere else. The
  boss-bar style uses the vanilla `boss_bar/*` sprites on every era. The smooth
  style reproduces the original 1.21.2–1.21.4 bar exactly: the same `StepColor`
  steps (green → gold → orange → red, white at 2.0), a 38x8 recess, and no
  clamping, so an over-full or illegal bundle paints past the frame and across
  the window. 1.21.2–1.21.4 draw the original sunken frame from `bundle.png`;
  later eras outline it with fills because they ship no such asset. This is an
  addition, not a backport of an upstream option.
- **Client-side insertion checks, backported.** 1.21.5 added the
  `clientSideItemInsertionChecks` config (default true). The check methods
  already existed in 1.21.2–1.21.4; they are now gated by the same config there.
- **Click resync fix.** Bundle clicks now send the real container state id
  (`containerMenu.getStateId()`) instead of `-1`. With `-1` the server answered
  every insert/remove with a full container broadcast (including the bundle
  contents) instead of an incremental update.
- **Mixin metadata.** Every mixin config declares `minVersion: "0.8"`; Forge's
  Mixin logged an error without it.

The 1.21.2–1.21.4 screen art still uses the official `bundle.png` layout; the
vanilla-style `UIDrawer` plate introduced in 1.21.5 was **not** backported
(cosmetic only).

Behavior otherwise matches each official build per version.

## License / provenance

The original mod is **All Rights Reserved**, © Socolio
([Modrinth](https://modrinth.com/mod/bundle-inventory)). This repository
contains reverse-engineered source produced for interoperability/study; it is
not affiliated with or endorsed by the original author. Do not redistribute the
original author's assets.
