# NilLoader support

NilLoader now covers every era the mod supports: **1.21.2 → 26.3**, eleven
jars (eight for 1.21.x, three for 26.x). The floor is Minecraft's: bundles only became
a normal item in 1.21.2, so there is nothing below it to load.

## Why 26.x was easy and 1.21.x was not

26.x ships **unobfuscated**. The Mini transformers name classes and methods
directly and nothing else is needed.

1.21.x ships **obfuscated**, so a nilmod needs two extra things that have
nothing to do with the mod itself:

1. **A named jar to compile against.** javac must see
   `net.minecraft.world.item.ItemStack`, not `cwq`.
2. **`META-INF/nil/mappings.json`.** NilLoader uses it to rewrite this mod's
   own classes and Mini patch strings from the dev names into runtime names
   while they load.

Both are generated in `nil.build.gradle` from Mojang's published client
mappings; nothing is committed.

## Why 1.21.x jars are pinned per version

NilLoader cannot gate on the Minecraft version (its metadata has no such field)
and it catches and logs a failed Mini patch instead of failing the launch.
Obfuscated class names, however, change even between point releases: about 72%
of classes were renamed from 1.21.2 to 1.21.3 and 28% from 1.21.6 to 1.21.8,
including this mod's three patch targets. A jar whose mappings were generated
from a different version therefore remaps nothing, and the mod is silently
inert. Each obfuscated jar is pinned to exactly one version, or to a pair whose
mappings are byte-identical (1.21.6-1.21.7 and 1.21.9-1.21.10).

## The flattening step

NilLoader's remapper (Lorenz) resolves inherited members through a runtime
inheritance provider, which only sees **obfuscated** classes. With a
named→obf mapping set it therefore cannot follow `Player extends Entity` to
find `isShiftKeyDown`. The build sidesteps that by flattening:

- every Minecraft member referenced by our compiled classes (call sites,
  field accesses, **and declared overrides**) is resolved against the named
  hierarchy *at build time*;
- the mapping is then emitted a second time under the **call-site owner**
  (`Player.isShiftKeyDown`, `BundleInventoryHandler.addSlot`, …).

This is why the emitted file is ~10 MB: it carries explicit entries for the
references the mod actually uses rather than relying on runtime inheritance.

Two related details:

- **Interfaces** are walked as well. `ItemStack.get(DataComponentType)` is
  declared on `DataComponentHolder`, and that is how it gets mapped.
- **Access widening** rides in the same file (`widen.fields`), e.g.
  `ItemRenderer.itemModelShaper` for 1.21.2-1.21.3. No Fabric access widener
  is involved.

## Deferred initialisation

Premain runs **before** NilLoader freezes its mapping set, and classes loaded
before the freeze are never remapped. `BundleInventoryMod` therefore must not
be touched from premain; `NilBootstrap.ensure()` initialises it from the first
patched call site instead.

## Running one

NilLoader instances are stock Minecraft plus the jar as a Java agent:

```
JvmArgs=-javaagent:BundleInventory-Nil-<version>+mc<label>.jar
```

The jar embeds NilLoader, so no loader has to be installed.

## Notes

- No config screen: NilLoader has no config API, so these builds read
  `config/bundleinventory.properties` only.
- The spawn of each build is `nil.build.gradle`; 26.x skips the mapping half
  entirely.
