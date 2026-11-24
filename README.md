# Bundle Inventory - decompiled sources by Minecraft version

One folder per supported Minecraft version. Each folder holds the decompiled
Bundle Inventory source for that version, adapted for the loaders that ship a
jar, plus a per-version note.

| Folder | Official build | Extra |
|---|---|---|
| 1.21.2 | 1.6.0 | same source as 1.21.3 (author's build covered both) |
| 1.21.3 | 1.6.0 | |
| 1.21.4 | 1.6.1 | |
| 1.21.5 | 1.7.1 | |
| 1.21.6 | 1.7.2 | same source as 1.21.7 / 1.21.8 |
| 1.21.7 | 1.7.2 | |
| 1.21.8 | 1.7.2 | |
| 1.21.9 | 1.7.3 | same source as 1.21.10 |
| 1.21.10 | 1.7.3 | |
| 1.21.11 | 1.7.3 | includes aw/intermediary + aw/yarn (straight Vineflower) |
| 26.1 | 1.7.3 | same source as 26.1.1 / 26.1.2 |
| 26.1.1 | 1.7.3 | |
| 26.1.2 | 1.7.3 | |
| 26.2 | 1.7.3 | |
| 26.3 | 1.7.3 | |

## Provenance

The sources are reverse-engineered from the author's official Bundle Inventory
jars (Socolio, All Rights Reserved) and then adapted for the multi-version port:
Mojang-named code, loader entry points, and the fixes listed in the
[changelog](../../blob/main/CHANGELOG.md). Decompiled code is approximate;
renaming and control-flow reconstruction are not guaranteed to match the
author's original source.

The buildable Stonecutter project (38 jars across Fabric, NeoForge, Forge and
NilLoader) lives on the [main](../../tree/main) branch. Jars are attached to
[releases](../../releases).