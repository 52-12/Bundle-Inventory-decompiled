package io.socol.bundleinventory.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.socol.bundleinventory.BundleInventoryMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
   public ModMenuIntegration() {
   }

   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> BundleInventoryMod.config().getScreenFactory().apply(parent);
   }
}
