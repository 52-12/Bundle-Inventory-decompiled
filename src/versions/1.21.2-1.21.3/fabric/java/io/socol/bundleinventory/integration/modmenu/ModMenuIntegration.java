package io.socol.bundleinventory.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.socol.bundleinventory.config.BundleInventoryConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return BundleInventoryConfigScreen::new;
   }
}
