package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryConfig;
import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import io.socol.bundleinventory.integration.cloth.BundleInventoryClothConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class BundleInventoryFabricMod implements ClientModInitializer {
   public BundleInventoryFabricMod() {
   }

   public void onInitializeClient() {
      BundleInventoryConfig loadedConfig = FabricLoader.getInstance().isModLoaded("cloth-config")
         ? BundleInventoryClothConfig.create()
         : BundleInventoryDefaultConfig.INSTANCE;
      BundleInventoryMod.init(loadedConfig);
   }
}
