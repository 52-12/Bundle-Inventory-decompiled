package io.socol.bundleinventory;

import io.socol.bundleinventory.inventory.BundleInventory;
import io.socol.bundleinventory.inventory.BundleInventoryHandler;
import io.socol.bundleinventory.inventory.BundleInventoryScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
public class BundleInventoryMod implements ClientModInitializer {
   public static final String MOD_ID = "bundleinventory";

   public BundleInventoryMod() {
   }

   public static Identifier id(String path) {
      return Identifier.of("bundleinventory", path);
   }

   public static String land(String path) {
      return "bundleinventory." + path;
   }

   public void onInitializeClient() {
   }

   public static boolean openBundleInventory(int playerInventoryBundleSlot, boolean openInventoryOnClose) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return false;
      } else if (!(player.currentScreenHandler instanceof PlayerScreenHandler)) {
         return false;
      } else {
         PlayerInventory playerInventory = player.getInventory();
         BundleInventory bundleInventory = new BundleInventory(player, playerInventoryBundleSlot);
         if (bundleInventory.getBundleContent() == null) {
            return false;
         } else {
            BundleInventoryHandler handler = new BundleInventoryHandler(playerInventory, bundleInventory, player.playerScreenHandler.syncId);
            BundleInventoryScreen screen = new BundleInventoryScreen(handler, playerInventory, openInventoryOnClose);
            MinecraftClient.getInstance().setScreen(screen);
            return true;
         }
      }
   }
}
