package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryConfigScreen;
import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "bundleinventory", dist = Dist.CLIENT)
public class BundleInventoryNeoForgeMod {
   public BundleInventoryNeoForgeMod(ModContainer container) {
      BundleInventoryMod.init(BundleInventoryDefaultConfig.INSTANCE);
      container.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> new BundleInventoryConfigScreen(parent));
   }
}
