package io.socol.bundleinventory.mixin.client;

import io.socol.bundleinventory.BundleInventoryMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_5537;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_636.class})
public class ClientPlayerInteractionManagerMixin {
   public ClientPlayerInteractionManagerMixin() {
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_2919(Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"},
      cancellable = true
   )
   private void onInteractItem(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> info) {
      class_1799 itemStack = player.method_5998(hand);
      if (!player.method_5715()) {
         if (itemStack.method_7909() instanceof class_5537) {
            int bundleSlot = hand == class_1268.field_5808 ? player.method_31548().field_7545 : 40;
            BundleInventoryMod.openBundleInventory(bundleSlot);
            info.setReturnValue(class_1269.field_5812);
         }
      }
   }
}
