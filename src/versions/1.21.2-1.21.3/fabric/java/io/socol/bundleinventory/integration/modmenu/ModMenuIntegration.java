package io.socol.bundleinventory.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.config.BundleInventoryConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {
   public ModMenuIntegration() {
   }

   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> {
         Screen screen = BundleInventoryMod.config().getScreenFactory().apply(parent);
         return screen != null ? screen : new BundleInventoryConfigScreen(parent);
      };
   }
}
