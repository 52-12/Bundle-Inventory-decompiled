package io.socol.bundleinventory.mixin.client;

import io.socol.bundleinventory.Hooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(CreativeModeInventoryScreen.class)
public class CreativeInventoryScreenMixin {
   public CreativeInventoryScreenMixin() {
   }

   @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
   private void onMouseClickHook(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo info) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         AbstractContainerScreen screen = (AbstractContainerScreen)(Object)this;
         if (Hooks.onScreenSlotClick(screen.getMenu(), slot, slotId, button, actionType)) {
            info.cancel();
         }
      }
   }
}
