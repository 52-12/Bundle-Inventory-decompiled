# Changelog

Every jar is a recompile of the author's official Bundle Inventory build for
that Minecraft line (1.6.0 through 1.7.3); the entries below are the port's own
fixes and additions.

## 1.7.3

### Fixed

- **Creative inventory**: shift-right-clicking a bundle on any tab other than
  `Survival Inventory` no longer moves or deletes the bundle.
- **Forge 26.1 / 26.2**: taking an item out of a bundle works again. Forge's
  `BundleItem` patch compared the *carried* stack's count instead of the
  bundle's, so a right-click with an empty cursor picked the bundle itself up.
  A scoped mixin restores the vanilla check on those two lines only.
- **Ghost cursor item**: container-click packets now report the real carried
  stack, so the server no longer resyncs a phantom item onto the cursor.
- **1.21.2 - 1.21.4**: a stack that only partly fits no longer leaves its
  remainder stranded on the cursor after a shift-click insert when client-side
  insertion checks are disabled.
- **26.x**: a bundle with malformed contents can no longer crash the client
  while computing its weight; it is treated as full like vanilla does.
- **Gray box** next to the boss-bar fullness indicator: the 1.21.2 - 1.21.4
  eras now draw the shared `UIDrawer` window instead of the old baked panel.
- **Smooth bar**: the frame is drawn with fills, so those jars no longer need
  the `bundle.png` GUI texture; the original unclamped overflow for illegal
  bundles is preserved.
- **Mod Menu**: Fabric falls back to the native config screen when Cloth Config
  is absent, and advertises `suggests` metadata for Mod Menu/Cloth.
- **NilLoader click stutter**: accessing a private field per click threw and
  was swallowed; the portable armor/offhand path fixed it.
- **NilLoader 1.21.6 - 1.21.8**: class-order-dependent mapping flattening
  crashed clicking a bundle; the two-pass hierarchy scan fixed it.
- **Equipment slots**: hit-testing outside the plate and the offhand slot now
  work with `showEquipmentSlotsInBundleInventory`.

### Changed

- **NilLoader 1.21.x jars are pinned per Minecraft version.** Obfuscated class
  names drift even between point releases (about 72% of classes changed
  1.21.2 -> 1.21.3, 28% 1.21.6 -> 1.21.8) and NilLoader silently skips a
  patch it cannot resolve, so a grouped jar was inert on all but the version
  it was built from. `1.21.3` and `1.21.8` now have their own jars;
  `1.21.6-1.21.7` and `1.21.9-1.21.10` stay grouped because their mappings are
  identical. 26.x is unobfuscated and unaffected.
- **Fabric loader floors** raised to what the nested Fabric API modules
  actually require: 0.17.0 (1.21.9 - 1.21.10), 0.17.3 (1.21.11), 0.18.4
  (26.1 / 26.2), 0.19.3 (26.3).
- **Forge 26.x packaging** uses Mixin `JAVA_21` (Forge's Mixin has no
  `JAVA_25` level) while the code still targets Java 25. Forge 26.1's
  `62.0.9` build is broken upstream, so that group targets 26.1.2 / 64.1.5.

## 1.7.2

- 1.21.2 - 1.21.3 gained the full 1.6.1 interaction layer (slot highlights,
  tooltips, Q-drop, click routing, nested bundles, stack copies).
- Config translations for `useBossBarFullness` and
  `clientSideItemInsertionChecks`; `minVersion` in the 26.x mixin configs.
- `1.21.2 - 1.21.4` draw the window with the shared `UIDrawer`; the old
  `bundle.png` panel is gone (the file is kept for provenance).

## 1.7.1

- Per-era access transformers, native config screens on Forge/NeoForge, and a
  properties-file config for NilLoader.
- Optional equipment slots (armor + offhand) in the bundle screen.
- Fullness bar can follow the boss-bar style or the faithful smooth bar.

## 1.7.0

- First multi-version port: Fabric, NeoForge, Forge and NilLoader builds from
  1.21.2 through 26.3, one jar per largest honest version range.
- Quilt runs the Fabric jars natively (no separate build).
