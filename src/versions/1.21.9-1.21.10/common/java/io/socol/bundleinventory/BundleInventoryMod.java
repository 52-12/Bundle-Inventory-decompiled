package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryConfig;
import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import io.socol.bundleinventory.inventory.BundleInventory;
import io.socol.bundleinventory.inventory.BundleInventoryHandler;
import io.socol.bundleinventory.inventory.BundleInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

public class BundleInventoryMod {
   public static final String MOD_ID = "bundleinventory";
   private static BundleInventoryConfig config = BundleInventoryDefaultConfig.INSTANCE;

   public static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("bundleinventory", path);
   }

   public static String land(String path) {
      return "bundleinventory." + path;
   }

   // Called by each loader's entrypoint with the config it resolved
   // (Cloth Config's when present, the defaults otherwise).
   public static void init(BundleInventoryConfig loadedConfig) {
      config = loadedConfig;
   }

   public static boolean openBundleInventory(int playerInventoryBundleSlot, boolean openInventoryOnClose) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return false;
      }

      if (!(player.containerMenu instanceof InventoryMenu)) {
         return false;
      }

      Inventory playerInventory = player.getInventory();
      BundleInventory bundleInventory = new BundleInventory(player, playerInventoryBundleSlot);
      if (bundleInventory.getBundleContent() == null) {
         return false;
      }

      BundleInventoryHandler handler = new BundleInventoryHandler(
         playerInventory, bundleInventory, player.inventoryMenu.containerId, config.bundleInventoryVisibleRows(), openInventoryOnClose
      );
      BundleInventoryScreen screen = new BundleInventoryScreen(handler, playerInventory, openInventoryOnClose);
      Minecraft.getInstance().setScreen(screen);
      return true;
   }

   public static BundleInventoryConfig config() {
      return config;
   }
}
