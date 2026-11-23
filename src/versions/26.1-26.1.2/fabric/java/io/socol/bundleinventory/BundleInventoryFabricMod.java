package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BundleInventoryFabricMod implements ClientModInitializer {
   public BundleInventoryFabricMod() {
   }

   public void onInitializeClient() {
      BundleInventoryMod.init(BundleInventoryDefaultConfig.INSTANCE);
   }
}
