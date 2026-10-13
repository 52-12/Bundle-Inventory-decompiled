package io.socol.bundleinventory.config;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_437;

@Environment(EnvType.CLIENT)
public class BundleInventoryDefaultConfig implements BundleInventoryConfig {
   public static final BundleInventoryConfig INSTANCE = new BundleInventoryDefaultConfig();

   public BundleInventoryDefaultConfig() {
   }

   @Override
   public Function<class_437, class_437> getScreenFactory() {
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
