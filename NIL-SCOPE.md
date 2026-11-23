# NilLoader target scope

Question: can the mod be ported to earlier Minecraft versions under NilLoader,
and can those builds use the 1.7.0 (vanilla-sprite) window instead of the old
`bundle.png` art?

## Short answer

**The supported floor is 1.21.2, and that is a Minecraft decision, not a
NilLoader one.**

Bundles were added as an experimental item in 1.17 and stayed behind the bundle
experimental datapack through 1.21.1. They became a normal, non-experimental
item in **1.21.2** — the same release that added `BundleContents` selection and
`ServerboundSelectBundleItemPacket`. Upstream's first release (1.0.0) targets
1.21.2 for exactly this reason.

So there is no earlier version to port to: below 1.21.2 the item the mod exists
to open is not part of the game. Pre-1.20.5 bundles were also plain NBT, with
no components and no weight.

NilLoader itself is not a constraint. It is a JVM agent with Mini transformers
and has builds going back well before 1.21; what changes per target is the
method descriptors being patched and the Java bytecode level of our jar.

## What was verified (javap against the 1.21.1 merged jar)

Kept as evidence for why the experimental era is not a viable target even if
someone force-enabled bundles with the datapack:

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

With the datapack, the renderer would port down with two shims (hover
highlight drawn as fills, legacy boss-bar paths for 1.20.1-class targets), but
the handler could not: without the selection packet, clicking a content slot
can only ask vanilla to remove the last stack. A UI that lists N slots and
removes an arbitrary one is not the mod.

## Renderer decision (applies to the versions we do ship)

Use the 1.7.0 `UIDrawer` renderer everywhere — 1.21.2–1.21.4 already do. Do
**not** resurrect `bundle.png`:

- its recessed strip is what caused the gray box next to the boss bar,
- it ignores resource packs, which is the point of the 1.7.0 rewrite,
- the smooth bar's sunken frame sprite is the last remaining reference; it can
  become a fill-drawn frame so no era ships GUI art.

## Smooth bar

The smooth fullness bar is an addition of this port, not upstream. It is only
offered where it can be drawn faithfully with the era's assets (1.21.2+). There
is no older NilLoader node, so there is no node on which it needs to be hidden.

## Recommendation

- No pre-1.21.2 build. The item does not officially exist there.
- Keep the current range: 1.21.2 → 26.3 across Fabric, Quilt, NeoForge, Forge
  and NilLoader.
- Cheap follow-up: replace the smooth bar's `bundle.png` frame with fills, so
  the 1.21.2–1.21.4 jars contain no GUI textures of their own.
