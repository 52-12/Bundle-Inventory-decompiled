package io.socol.bundleinventory.mixin.client;

import io.socol.bundleinventory.Hooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_636.class)
public class ClientPlayerInteractionManagerMixin {
   public ClientPlayerInteractionManagerMixin() {
   }

   @Inject(at = @At("HEAD"), method = "method_2919(Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;", cancellable = true)
   private void interactItemHook(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> info) {
      class_1269 result = Hooks.onItemInteract(player, hand);
      if (result != null) {
         info.setReturnValue(result);
      }
   }

   @Inject(at = @At("HEAD"), method = "method_2906(IIILnet/minecraft/class_1713;Lnet/minecraft/class_1657;)V", cancellable = true)
   private void clickSlot(int syncId, int slotId, int button, class_1713 actionType, class_1657 player, CallbackInfo info) {
      boolean cancel = Hooks.onClickSlot(syncId, slotId, button, actionType, player);
      if (cancel) {
         info.cancel();
      }
   }
}
