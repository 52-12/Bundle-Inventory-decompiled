package io.socol.bundleinventory.mixin.client;

import io.socol.bundleinventory.Hooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
   public ClientPlayerInteractionManagerMixin() {
   }

   @Inject(
      at = @At("HEAD"),
      method = "useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
      cancellable = true
   )
   private void interactItemHook(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
      InteractionResult result = Hooks.onItemInteract(player, hand);
      if (result != null) {
         info.setReturnValue(result);
      }
   }

   @Inject(
      at = @At("HEAD"),
      method = "handleContainerInput(IIILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V",
      cancellable = true
   )
   private void clickSlot(int syncId, int slotId, int button, ContainerInput actionType, Player player, CallbackInfo info) {
      boolean cancel = Hooks.onClickSlot(syncId, slotId, button, actionType, player);
      if (cancel) {
         info.cancel();
      }
   }
}
