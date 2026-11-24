package io.socol.bundleinventory.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1263;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_5537;
import net.minecraft.class_9276;
import net.minecraft.class_9334;

@Environment(EnvType.CLIENT)
public class BundleInventory implements class_1263 {
   private final class_1657 player;
   private final int playerInventoryBundleSlot;

   public BundleInventory(class_1657 player, int playerInventoryBundleSlot) {
      this.player = player;
      this.playerInventoryBundleSlot = playerInventoryBundleSlot;
   }

   public int getPlayerInventoryBundleSlot() {
      return this.playerInventoryBundleSlot;
   }

   public class_1799 getBundleStack() {
      class_1799 stack = this.player.method_31548().method_5438(this.playerInventoryBundleSlot);
      if (stack.method_7960()) {
         return class_1799.field_8037;
      } else {
         return stack.method_7909() instanceof class_5537 ? stack : class_1799.field_8037;
      }
   }

   public class_9276 getBundleContent() {
      class_1799 stack = this.getBundleStack();
      return stack.method_7960() ? null : (class_9276)stack.method_58694(class_9334.field_49650);
   }

   public int method_5439() {
      class_9276 content = this.getBundleContent();
      return content != null ? content.method_57426() : 0;
   }

   public boolean method_5442() {
      class_9276 content = this.getBundleContent();
      return content == null || content.method_57429();
   }

   public class_1799 method_5438(int slot) {
      class_9276 content = this.getBundleContent();
      return content != null && slot < content.method_57426() ? content.method_57422(slot) : class_1799.field_8037;
   }

   public class_1799 method_5434(int slot, int amount) {
      return class_1799.field_8037;
   }

   public class_1799 method_5441(int slot) {
      return class_1799.field_8037;
   }

   public void method_5447(int slot, class_1799 stack) {
   }

   public void method_5431() {
   }

   public boolean method_5443(class_1657 player) {
      return !this.getBundleStack().method_7960();
   }

   public void method_5448() {
   }
}
