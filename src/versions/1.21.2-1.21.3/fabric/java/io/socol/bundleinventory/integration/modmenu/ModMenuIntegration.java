package io.socol.bundleinventory.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.socol.bundleinventory.BundleInventoryMod;

public class ModMenuIntegration implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> BundleInventoryMod.config().getScreenFactory().apply(parent);
   }
}
