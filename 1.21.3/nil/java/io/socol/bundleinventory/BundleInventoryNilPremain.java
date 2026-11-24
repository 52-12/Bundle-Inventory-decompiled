package io.socol.bundleinventory;

import io.socol.bundleinventory.nil.AbstractContainerScreenTransformer;
import io.socol.bundleinventory.nil.CreativeModeInventoryScreenTransformer;
import io.socol.bundleinventory.nil.MultiPlayerGameModeTransformer;
import nilloader.api.ClassTransformer;
import nilloader.api.NilLogger;

/**
 * NilLoader entrypoint for the obfuscated 1.21.x line.
 *
 * Nothing reachable from here may touch a Minecraft type: premain runs before
 * NilLoader freezes its mapping set, and any class loaded before the freeze is
 * never remapped. The mod is initialised lazily from the patched call sites
 * instead (see NilBootstrap).
 */
public class BundleInventoryNilPremain implements Runnable {
   public static final NilLogger log = NilLogger.get("bundleinventory");

   public BundleInventoryNilPremain() {
   }

   @Override
   public void run() {
      ClassTransformer.register(new MultiPlayerGameModeTransformer());
      ClassTransformer.register(new AbstractContainerScreenTransformer());
      ClassTransformer.register(new CreativeModeInventoryScreenTransformer());
   }
}
