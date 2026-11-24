package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryConfigScreen;
import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("bundleinventory")
public class BundleInventoryForgeMod {
   public BundleInventoryForgeMod(FMLJavaModLoadingContext context) {
      BundleInventoryMod.init(BundleInventoryDefaultConfig.INSTANCE);
      ModLoadingContext.get().registerExtensionPoint(
         ConfigScreenHandler.ConfigScreenFactory.class,
         () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new BundleInventoryConfigScreen(parent))
      );
   }
}
