package io.socol.bundleinventory.config;

import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;

public interface BundleInventoryConfig {
   String BUNDLE_INVENTORY_ROWS_DESC = "How many rows will be visible in Bundle inventory";
   int BUNDLE_INVENTORY_ROWS_MIN = 3;
   int BUNDLE_INVENTORY_ROWS_MAX = 8;
   String BUNDLE_INVENTORY_SOUNDS_DESC = "Should play sounds when player is interacting with Bundle via inventory";
   String INDICATE_OPEN_BUNDLE = "Should highlight open Bundle by rendering it differently";
   String SHOW_EQUIPMENT_SLOTS = "Show armor & second hand slots in Bundle inventory";
   String CLIENT_SIDE_ITEM_INSERTION_CHECKS = "Do client-side checks of bundle occupancy on item insertion";

   String USE_BOSS_BAR_FULLNESS = "Use the boss-bar style fullness indicator instead of the smooth bar";

   Function<Screen, Screen> getScreenFactory();

   int bundleInventoryVisibleRows();

   boolean shouldPlayBundleInventorySounds();

   boolean indicateOpenBundle();

   boolean showEquipmentSlotsInBundleInventory();

   boolean clientSideItemInsertionChecks();

   boolean useBossBarFullness();
}
