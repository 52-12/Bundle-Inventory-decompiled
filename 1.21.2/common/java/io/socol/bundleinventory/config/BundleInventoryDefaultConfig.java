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
      return BundleInventorySettings.get().rows();
   }

   @Override
   public boolean shouldPlayBundleInventorySounds() {
      return BundleInventorySettings.get().sounds();
   }

   @Override
   public boolean indicateOpenBundle() {
      return BundleInventorySettings.get().indicateOpen();
   }

   @Override
   public boolean showEquipmentSlotsInBundleInventory() {
      return BundleInventorySettings.get().equipment();
   }

   @Override
   public boolean clientSideItemInsertionChecks() {
      return BundleInventorySettings.get().clientChecks();
   }

   @Override
   public boolean useBossBarFullness() {
      return BundleInventorySettings.get().bossBar();
   }
}
