package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;

/**
 * Deferred mod init for NilLoader on obfuscated versions. Called from the
 * patched call sites, which only run after NilLoader has frozen and started
 * remapping mod classes; touching BundleInventoryMod any earlier would load it
 * unremapped and break every Minecraft reference inside it.
 */
public final class NilBootstrap {
   private static boolean ready;

   private NilBootstrap() {
   }

   public static void ensure() {
      if (!ready) {
         ready = true;
         BundleInventoryMod.init(BundleInventoryDefaultConfig.INSTANCE);
      }
   }
}
