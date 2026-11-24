package io.socol.bundleinventory.mixin.client;

import io.socol.bundleinventory.BundleInventoryMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BundleItem;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
   public ClientPlayerInteractionManagerMixin() {
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"},
      cancellable = true
   )
   private void onInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (!player.isSneaking()) {
         if (itemStack.getItem() instanceof BundleItem) {
            int bundleSlot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
            BundleInventoryMod.openBundleInventory(bundleSlot);
            info.setReturnValue(ActionResult.SUCCESS);
         }
      }
   }
}
