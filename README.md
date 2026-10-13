# Bundle Inventory — 26.x port

Source port of **Bundle Inventory 1.7.3** (Fabric, by Socolio) to the Minecraft **26.x**
version line (built and tested against **26.2**).

`main` holds the older 1.21.11 port (Yarn mappings). See the
[`decompiled`](../../tree/decompiled) branch for the decompiled original.

## Toolchain (26.x)

The `26.x` line is a major engine revision and Fabric changed its mapping strategy for it:

- **Mappings: Mojang official** — Yarn/intermediary stop at 1.21.11, so 26.x has no Yarn builds.
- **Loom** `net.fabricmc.fabric-loom` **1.18** (plugin id changed).
- **Java 25**, **Gradle 9.8**.
- Minecraft `26.2`, Fabric API `0.161.0+26.2`, Cloth Config `26.2.155`, Mod Menu `20.0.3`.

All versions live in `gradle.properties` — bumping to a newer 26.x build is a config change
(e.g. `minecraft_version=26.3`, `fabric_api_version=0.162.0+26.3`,
`cloth_config_version=26.3.159`, `modmenu_version=21.0.0`).

> **Important:** the compiled jar only runs on the exact Minecraft version it was built
> against (Mojang-named mods are version-specific). Building for 26.2 and running on 26.3
> (or vice-versa) fails with `NoSuchFieldError`/`NoSuchMethodError`.

## Building

Requires JDK 25.

```
./gradlew build
```

Output: `build/libs/BundleInventory-Fabric-1.7.3-port.jar`

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
| Bundle builder | `new Mutable(contents)` (26.2) / `contents.asMutable()` (26.3) | upto `new Mutable(contents)` |
| Cloth Config screen | `AutoConfig.getConfigScreen` | `AutoConfigClient.getConfigScreen` |
| Mixin level | `JAVA_21` | `JAVA_25` |

Small 26.2-vs-26.3 deltas handled for 26.2:
- `InputConstants.isKeyDown(Window, int)` (26.2) vs `isKeyDown(int)` (26.3).
- `BundleContents.Mutable` is constructed directly (`new Mutable(content)`) in 26.2;
  26.3 adds the `BundleContents.asMutable()` factory.

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
