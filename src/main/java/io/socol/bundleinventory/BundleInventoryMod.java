package io.socol.bundleinventory;

import io.socol.bundleinventory.config.BundleInventoryConfig;
import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import io.socol.bundleinventory.integration.cloth.BundleInventoryClothConfig;
import io.socol.bundleinventory.inventory.BundleInventory;
import io.socol.bundleinventory.inventory.BundleInventoryHandler;
import io.socol.bundleinventory.inventory.BundleInventoryScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

@Environment(EnvType.CLIENT)
public class BundleInventoryMod implements ClientModInitializer {
   public static final String MOD_ID = "bundleinventory";
   private static BundleInventoryConfig config = BundleInventoryDefaultConfig.INSTANCE;

   public BundleInventoryMod() {
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("bundleinventory", path);
   }

   public static String land(String path) {
      return "bundleinventory." + path;
   }

   public void onInitializeClient() {
      if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
         config = BundleInventoryClothConfig.create();
      }
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
      Minecraft.getInstance().setScreenAndShow(screen);
      return true;
   }

   public static BundleInventoryConfig config() {
      return config;
   }
}
