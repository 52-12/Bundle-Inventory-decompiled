# Bundle Inventory 1.7.3 — decompiled sources

Reverse-engineered (`decompiled`) sources of **Bundle Inventory 1.7.3** (Fabric, by Socolio),
produced from `BundleInventory-Fabric-1.7.3.jar`.

The buildable/ported project for Minecraft 1.21.11 lives on the
[`main`](../../tree/main) branch.

## Layout

| Directory | Contents |
|---|---|
| `intermediary/` | Raw decompilation, names in Fabric **intermediary** (`class_1234`, `method_1234`, `field_1234`). |
| `yarn/` | Same sources remapped to **Yarn** names (`1.21.11+build.6`) for readability. |

## How it was produced

- **Decompiler**: [Vineflower](https://github.com/Vineflower/vineflower) **1.12.0**
  (`-dgs=1 -hdc=0 -rsy=1 -jrt=1 -win=1`), Java 21.
- **Yarn remap**: an intermediary→named (`tiny` v2) text remap of the decompiled sources
  (classes, methods, fields, record components, mixin descriptors, access widener).

The original jar declares `minecraft >= 1.21.9`; it was built with Fabric Loom 1.11.8.

## Notes

- `META-INF/jars/*.jar` (bundled Fabric API modules) and build outputs are intentionally
  omitted — this tree is source only.
- Decompiled code is not guaranteed to be faithful to the original source; renaming and
  control-flow reconstruction are approximate.

## License / provenance

The original mod is **All Rights Reserved**, © Socolio. This is reverse-engineered source
produced for interoperability/study and is not affiliated with or endorsed by the original
author.
