package io.socol.bundleinventory.config;

import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;

public class BundleInventoryDefaultConfig implements BundleInventoryConfig {
   public static final BundleInventoryConfig INSTANCE = new BundleInventoryDefaultConfig();

   @Override
   public Function<Screen, Screen> getScreenFactory() {
      return parent -> null;
   }

   @Override
   public int bundleInventoryVisibleRows() {
      return 6;
   }

   @Override
   public boolean shouldPlayBundleInventorySounds() {
      return true;
   }

   @Override
   public boolean indicateOpenBundle() {
      return true;
   }

   @Override
   public boolean showEquipmentSlotsInBundleInventory() {
      return false;
   }

   @Override
   public boolean clientSideItemInsertionChecks() {
      return true;
   }
}
