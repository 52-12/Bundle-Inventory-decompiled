package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import io.socol.bundleinventory.nil.AbstractContainerScreenTransformer;
import io.socol.bundleinventory.nil.AccessWidenerTransformer;
import io.socol.bundleinventory.nil.CreativeModeInventoryScreenTransformer;
import io.socol.bundleinventory.nil.MultiPlayerGameModeTransformer;
import nilloader.api.ClassTransformer;
import nilloader.api.NilLogger;

/**
 * NilLoader entrypoint (the only one the loader defines).
 *
 * Everything the mod needs from a "loader" on 26.x is supplied here: a startup
 * hook, a transformer registry, and access widening. There is no obfuscation
 * to undo and the UI uses vanilla sprites only, so no mapping or resource
 * machinery is involved.
 */
public class BundleInventoryNilPremain implements Runnable {
   public static final NilLogger log = NilLogger.get("bundleinventory");

   public BundleInventoryNilPremain() {
   }

   @Override
   public void run() {
      // No ModRemapper.setTargetMapping call: this build has no mappings block,
      // and the game is unobfuscated anyway.

      BundleInventoryMod.init(BundleInventoryDefaultConfig.INSTANCE);

      // Widener first, then the three patches (order is not strictly required,
      // but every transformer sees the same ClassNode in one pass).
      ClassTransformer.register(new AccessWidenerTransformer());
      ClassTransformer.register(new MultiPlayerGameModeTransformer());
      ClassTransformer.register(new AbstractContainerScreenTransformer());
      ClassTransformer.register(new CreativeModeInventoryScreenTransformer());
   }
}
