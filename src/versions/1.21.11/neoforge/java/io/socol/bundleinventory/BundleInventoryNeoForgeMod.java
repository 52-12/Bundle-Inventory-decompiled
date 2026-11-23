package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge entrypoint for Bundle Inventory.
 *
 * The Fabric build reads its config through Cloth Config when that mod is
 * present; the NeoForge build ships with the mod's built-in defaults.
 * Like the original, everything is client-only: this class is dist-gated and
 * never constructed on a dedicated server.
 */
@Mod(value = "bundleinventory", dist = Dist.CLIENT)
public class BundleInventoryNeoForgeMod {
   public BundleInventoryNeoForgeMod(ModContainer container) {
      BundleInventoryMod.init(BundleInventoryDefaultConfig.INSTANCE);
   }
}
