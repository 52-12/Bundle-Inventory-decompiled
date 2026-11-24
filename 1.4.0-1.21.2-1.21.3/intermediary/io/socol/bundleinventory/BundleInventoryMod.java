package io.socol.bundleinventory;

import io.socol.bundleinventory.inventory.BundleInventory;
import io.socol.bundleinventory.inventory.BundleInventoryHandler;
import io.socol.bundleinventory.inventory.BundleInventoryScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_1723;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public class BundleInventoryMod implements ClientModInitializer {
   public static final String MOD_ID = "bundleinventory";

   public BundleInventoryMod() {
   }

   public static class_2960 id(String path) {
      return class_2960.method_60655("bundleinventory", path);
   }

   public static String land(String path) {
      return "bundleinventory." + path;
   }

   public void onInitializeClient() {
   }

   public static boolean openBundleInventory(int playerInventoryBundleSlot, boolean openInventoryOnClose) {
      class_746 player = class_310.method_1551().field_1724;
      if (player == null) {
         return false;
      } else if (!(player.field_7512 instanceof class_1723)) {
         return false;
      } else {
         class_1661 playerInventory = player.method_31548();
         BundleInventory bundleInventory = new BundleInventory(player, playerInventoryBundleSlot);
         if (bundleInventory.getBundleContent() == null) {
            return false;
         } else {
            BundleInventoryHandler handler = new BundleInventoryHandler(playerInventory, bundleInventory, player.field_7512.field_7763);
            BundleInventoryScreen screen = new BundleInventoryScreen(handler, playerInventory, openInventoryOnClose);
            class_310.method_1551().method_1507(screen);
            return true;
         }
      }
   }
}
