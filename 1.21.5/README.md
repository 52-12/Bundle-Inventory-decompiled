# Minecraft 1.21.5

Decompiled Bundle Inventory **1.7.1** source for Minecraft 1.21.5, adapted
for the loaders that ship a jar for this version (Fabric, NeoForge, Forge and
NilLoader; 1.21.2 has Fabric and NilLoader only, NeoForge starts at 1.21.3).

- common/ - loader-agnostic decompiled code (screen, menu, click handling)
- abric/, 
eoforge/, orge/, 
il/ - loader glue and entry points
- mixins/ - client mixin config and classes
- packaging/ - per-loader metadata and resources
- aw/intermediary/, aw/yarn/ - (1.21.11 only) the straight Vineflower
  decompilation of the official jar, before the port's adaptations

Versions sharing one source era (1.21.6-1.21.8, 1.21.9-1.21.10, 26.1-26.1.2)
contain the same tree, because the author's build covered them with one source
set.

The buildable multi-version project is on the main branch; jars are attached
to the [releases](https://github.com/52-12/Bundle-Inventory-decompiled/releases).