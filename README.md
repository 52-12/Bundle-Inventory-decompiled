# Bundle Inventory - decompiled sources by Minecraft version

One folder per Minecraft version covered by the author's official builds on
[Modrinth](https://modrinth.com/mod/bundle-inventory/versions). Each folder
holds the decompiled source of the official build that covers that version,
adapted for the multi-version port, plus a per-version note.

| Folder | Official build | Notes |
|---|---|---|
| 1.21.2 | 1.6.0 | the 1.6.0 upload covered 1.21.2 - 1.21.3 |
| 1.21.3 | 1.6.0 | |
| 1.21.4 | 1.6.1 | 1.6.0 and 1.6.1 were uploaded for 1.21.4; 1.6.1 is the latest |
| 1.21.5 | 1.7.1 | 1.6.1, 1.7.0 and 1.7.1 were uploaded for 1.21.5; 1.7.1 is the latest |
| 1.21.6 | 1.7.2 | |
| 1.21.7 | 1.7.2 | 1.7.1 and 1.7.2 were uploaded for 1.21.7; 1.7.2 is the latest |
| 1.21.8 | 1.7.2 | |
| 1.21.9 | 1.7.3 | |
| 1.21.10 | 1.7.3 | |

Older Modrinth uploads (1.0.0 through 1.5.0) target 1.21.2 / 1.21.3; their
source is represented by the latest build for those versions (1.6.0) in the
folders above.

**Not in this branch:** the port also ships jars for **1.21.11** and
**26.1 - 26.3**, but those are not Modrinth uploads (as of this writing no
official build covers them). They are adaptations of the 1.7.3 source, so they
do not have a decompiled folder here. See the [main branch](../../tree/main)
for the port's full support matrix.

## Provenance

The sources are reverse-engineered from the author's official Bundle
Inventory jars (Socolio, All Rights Reserved) and adapted for the port:
Mojang-named code, loader entry points, and the fixes listed in the
[changelog](../../blob/main/CHANGELOG.md). Decompiled code is approximate;
renaming and control-flow reconstruction are not guaranteed to match the
author's original source.

The buildable Stonecutter project (38 jars across Fabric, NeoForge, Forge and
NilLoader) and the release jars live on the [main branch](../../tree/main) and
[releases](../../releases).