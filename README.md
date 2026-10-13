# Bundle Inventory — 1.21.11 port

A source port of **Bundle Inventory 1.7.3** (Fabric, by Socolio) to Minecraft **1.21.11**.

The original 1.7.3 jar targets `minecraft >= 1.21.9`. This branch is the ported, buildable
Fabric project (Yarn mappings) that compiles and runs on 1.21.11.

See the [`decompiled`](../../tree/decompiled) branch for the decompiled original.

## Building

Requires JDK 21.

```
./gradlew build
```

Output: `build/libs/BundleInventory-Fabric-1.7.3-port.jar`

## What was changed for 1.21.11

Only three API breaks surfaced when recompiling against 1.21.11:

| File | Change |
|---|---|
| `BundleInventoryScreen` | `HandledScreen.drawSlot` gained mouse coords → `drawSlot(DrawContext, Slot, int, int)`; `drawSlots` → `drawSlots(DrawContext, int, int)`. Updated the overrides, `super` calls, and the internal call. |
| `HandledScreenMixin`, `CreativeInventoryScreenMixin` | Restored an intermediate `(Object)` cast the decompiler had dropped: `(HandledScreen)this` → `(HandledScreen)(Object)this`. |
| `bundleinventory.accesswidener` | Authored in the `named` namespace (Loom emits it as `intermediary` in the jar). |
| `Hooks.onScreenSlotClick` | Dropped the `slotId < 9 || slotId >= 45` menu-slot-id guard (kept `slot.getIndex() < 36`). In the creative screen the player-inventory slots only have menu ids 0..45 on the "Survival Inventory" tab; on other tabs they are plain slots appended after the 45 picker slots (menu ids >= 45). The old guard rejected those, so the shift-right-click wasn't intercepted and vanilla's `CreativeScreenHandler.quickMove` deleted the item. The container/index check now covers every creative tab. |

Mixins are remapped statically by Loom (no refmap), so the `@Inject(method = "...")`
targets are written in Yarn names and rewritten to intermediary at build time.

## Dependencies at runtime

- Fabric Loader >= 0.16.14
- Minecraft >= 1.21.9 (built/tested on 1.21.11)
- Fabric API
- Cloth Config + Mod Menu (optional — for the in-game config screen)

## License / provenance

The original mod is **All Rights Reserved**, © Socolio. This repository contains
reverse-engineered source produced for interoperability/study; it is not affiliated with
or endorsed by the original author. Do not redistribute the original author's assets.
