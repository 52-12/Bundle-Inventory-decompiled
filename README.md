# Bundle Inventory — 26.x port

Source port of **Bundle Inventory 1.7.3** (Fabric, by Socolio) to the Minecraft **26.x**
version line. Builds for **26.2** and **26.3** from one source tree (see the compat shims).

`main` holds the older 1.21.11 port (Yarn mappings). See the
[`decompiled`](../../tree/decompiled) branch for the decompiled original.

## Toolchain (26.x)

The `26.x` line is a major engine revision and Fabric changed its mapping strategy for it:

- **Mappings: Mojang official** — Yarn/intermediary stop at 1.21.11, so 26.x has no Yarn builds.
- **Loom** `net.fabricmc.fabric-loom` **1.18** (plugin id changed).
- **Java 25**, **Gradle 9.8**.
- Fabric API / Cloth Config / Mod Menu versions are per-version (see the table below).

> **Important:** the compiled jar only runs on the exact Minecraft version it was built
> against (Mojang-named mods are version-specific). A 26.2 jar will not run on 26.3 and vice
> versa. Build the one matching the game.

## Building

Requires JDK 25.

```bash
# default: 26.2
./gradlew build

# 26.3
./gradlew build \
  -Pminecraft_version=26.3 -Pmc_compat=26.3 \
  -Pfabric_api_version=0.162.0+26.3 \
  -Pcloth_config_version=26.3.159 -Pmodmenu_version=21.0.0
```

Output: `build/libs/BundleInventory-Fabric-1.7.3-port.jar`

| Target | `minecraft_version` | `mc_compat` | `fabric_api_version` | `cloth_config_version` | `modmenu_version` |
|---|---|---|---|---|---|
| 26.2 | `26.2` | `26.2` | `0.161.0+26.2` | `26.2.155` | `20.0.3` |
| 26.3 | `26.3` | `26.3` | `0.162.0+26.3` | `26.3.159` | `21.0.0` |

### Version-specific shims

Most of the port is identical across 26.x, but two calls differ between 26.2 and 26.3. They
are isolated behind `BundleCompat` (`src/compat/<version>/…`), and `build.gradle` adds the
matching `src/compat/<mc_compat>` directory to the source set. Adding a new 26.x version is:
add a `src/compat/<v>` folder + a CI matrix entry.

## CI

`.github/workflows/build.yml` builds a **matrix** of 26.2 and 26.3 on JDK 25 and uploads a jar
artifact per version.

## Port notes

The source was moved to Mojang names (remapped from the intermediary build via Loom, then
re-decompiled) and adapted to the rewritten 26.x APIs:

| Area | 1.21.11 | 26.x |
|---|---|---|
| GUI context | `GuiGraphics` | `GuiGraphicsExtractor` (`renderpearl` `RenderPipeline`) |
| Screen render | `render(...)` | `extractRenderState(...)` |
| Widget render | `renderWidget(...)` | `extractWidgetRenderState(...)` |
| Slots | `renderSlots` / `renderSlot` | `extractSlots` / `extractSlot` |
| Labels / bg | `renderLabels` / `renderBg` | `extractLabels` / `extractBackground` |
| Click type | `ClickType` | `ContainerInput` |
| Screen switch | `Minecraft.setScreen` | `Minecraft.setScreenAndShow` (`Minecraft.gui.screen()`) |
| Screen size | writable `imageWidth/Height` | **final** — passed via the `AbstractContainerScreen` constructor |
| Menu packet | `MultiPlayerGameMode.handleInventoryMouseClick` | `handleContainerInput` |
| Bundle contents | `List<ItemStack>`, `getItemUnsafe` | `List<ItemStackTemplate>`, `items().get(i).create()` |
| Bundle weight | `Fraction weight()` | `DataResult<Fraction> weight()` (`getOrThrow()`) |
| Bundle builder | `new Mutable(contents)` (26.2) | `contents.asMutable()` (26.3) — via `BundleCompat` |
| Key state | `InputConstants.isKeyDown(Window,int)` (26.2) | `isKeyDown(int)` (26.3) — via `BundleCompat` |
| Cloth Config screen | `AutoConfig.getConfigScreen` | `AutoConfigClient.getConfigScreen` |
| Mixin level | `JAVA_21` | `JAVA_25` |

Also fixed here (present since 1.21.9): the creative-inventory guard used the menu slot id
(`slotId < 9 || slotId >= 45`), which only matches the "Survival Inventory" tab; on other
creative tabs it let vanilla's `quickMove` delete the item. Now gated on
`slot.container == player inventory && slot.getContainerSlot() < 36`.

### Known behavior differences

- **Open-bundle highlight** (`indicateOpenBundle`): 26.x `BundleContents` stores
  `ItemStackTemplate`, which cannot represent an empty item, so the original "selected but
  empty" open state is not constructible. The port opens the bundle using its real first
  stored item instead; an empty bundle renders closed. The indicator is preserved; the exact
  pixels differ.
- The explicit `renderTooltip(...)` call was dropped because the new parent
  `extractRenderState` draws the hovered-slot tooltip itself (avoids a double tooltip).

## License / provenance

The original mod is **All Rights Reserved**, © Socolio. This is reverse-engineered source
produced for interoperability/study and is not affiliated with or endorsed by the original
author.
