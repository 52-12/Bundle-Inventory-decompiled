package io.socol.bundleinventory.mixin.client;

import io.socol.bundleinventory.Hooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_465.class)
public class HandledScreenMixin {
   public HandledScreenMixin() {
   }

   @Inject(method = "method_2383(Lnet/minecraft/class_1735;IILnet/minecraft/class_1713;)V", at = @At("HEAD"), cancellable = true)
   private void onMouseClickHook(class_1735 slot, int slotId, int button, class_1713 actionType, CallbackInfo info) {
      class_465 screen = (class_465)this;
      if (Hooks.onScreenSlotClick(screen.method_17577(), slot, slotId, button, actionType)) {
         info.cancel();
      }
   }
}
