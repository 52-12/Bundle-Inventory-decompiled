package io.socol.bundleinventory.mixin.client;

import io.socol.bundleinventory.Hooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
   public ClientPlayerInteractionManagerMixin() {
   }

   @Inject(at = @At("HEAD"), method = "interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", cancellable = true)
   private void interactItemHook(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
      ActionResult result = Hooks.onItemInteract(player, hand);
      if (result != null) {
         info.setReturnValue(result);
      }
   }

   @Inject(at = @At("HEAD"), method = "clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V", cancellable = true)
   private void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo info) {
      boolean cancel = Hooks.onClickSlot(syncId, slotId, button, actionType, player);
      if (cancel) {
         info.cancel();
      }
   }
}
