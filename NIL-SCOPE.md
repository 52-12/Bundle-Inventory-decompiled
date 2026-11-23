# NilLoader target scope (pre-1.21.2)

Question: can the mod be ported to earlier Minecraft versions under NilLoader,
and can those builds use the 1.7.0 (vanilla-sprite) window instead of the old
`bundle.png` art?

## Short answer

NilLoader is not the blocker. The bundle **API** is.

- **1.21.2 and newer** — already covered (Fabric/Quilt/Forge/NeoForge/Nil).
- **1.21 / 1.21.1** — renderer portable, but removal of a *chosen* item is not
  possible: 1.21.1 has no `ServerboundSelectBundleItemPacket` and
  `BundleItem.overrideStackedOnOther` calls `BundleContents.Mutable.removeOne()`,
  which always takes the last stack. A UI that lists N slots and removes the
  last one is not the mod.
- **1.20.5 / 1.20.6** — same selection problem, plus `overrideStackedOnOther`
  support has to be re-verified for that exact version.
- **1.20.4 and older** — bundles are plain NBT (`Items`), there is no
  `BundleContents`, no weight, no components. Full rewrite; not a port.

Upstream started at 1.21.2 for the same reason: the selection packet is what
makes a per-slot UI possible.

## What was verified (javap against the 1.21.1 merged jar)

| API | 1.21.1 |
|---|---|
| `BundleContents` (`weight`, `size`, `getItemUnsafe`, `items`) | yes |
| `BundleContents.Mutable` (`tryInsert`, `removeOne`, `toImmutable`) | yes |
| `Mutable.toggleSelectedItem` | **no** |
| `ServerboundSelectBundleItemPacket` | **no** |
| `BundleItem.overrideStackedOnOther` / `overrideOtherStackedOnMe` | yes |
| removal target | `removeOne()` = last item only |
| `recipe_book/overlay_recipe` nine-slice | yes |
| `boss_bar/*` sprites | yes |
| `container/slot_highlight_back/front` sprites | **no** (added 1.21.2) |
| Java level | 21 |

So the renderer (`UIDrawer`) ports down with two shims: hover highlight drawn
as fills, and (only for 1.20.1-class targets) legacy boss-bar texture paths.

## Renderer decision

Use the 1.7.0 `UIDrawer` renderer for any older node — same as 1.21.2–1.21.4
already do. Do **not** resurrect `bundle.png`:

- it is what caused the gray box (recess baked into the panel),
- it ignores resource packs, which is the point of the 1.7.0 rewrite,
- the smooth bar's sunken frame sprite is the last remaining reference; it can
  become a fill-drawn frame so no era ships GUI art.

## Smooth bar on older nodes

Per request: the smooth fullness bar is **not offered** on a pre-1.21.2 node.
That era's `BundleInventoryConfig` omits `useBossBarFullness`, the screen always
draws the boss-bar style, and `config/bundleinventory.properties` ignores the
key. 26.x keeps the option.

## Effort, if 1.21.1 is wanted anyway

Rough shape (one era node, ~1.5 days):

1. New stonecutter node `1.21-1.21.1` (Java 21), Nil + Fabric loaders.
2. Handler variant: drop `selectBundleSlot`, `toggleSelectedItem`, the selection
   index arithmetic; keep insert, view, weight bar, quick-move.
3. Removal semantics: either remove the last item (label it honestly) or hide
   removal. Do not pretend a clicked slot is the one removed.
4. Screen: `UIDrawer` + fill-based hover highlight; boss bar only.
5. Nil transformers: same targets, 1.21.1 descriptors
   (`AbstractContainerScreen.slotClicked(Slot,int,int,ClickType)`,
   `MultiPlayerGameMode.handleInventoryMouseClick`).
6. Playtest: insert, remove-last, quick-move, creative open, weight bar.

## Recommendation

Do **not** ship a 1.21.1 build that silently removes the wrong item. If older
coverage matters, the honest options are:

- ship it as **view + insert + "remove last"**, clearly labelled, or
- skip it; 1.21.2+ already spans 13 Minecraft versions across five loaders.

## Follow-ups that are cheap and worthwhile regardless

- Replace the smooth bar's `bundle.png` frame with fills (removes the last GUI
  texture from 1.21.2–1.21.4 jars).
- Keep documentation of the version gate so this is not re-litigated.
