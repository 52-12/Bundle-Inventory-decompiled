package io.socol.bundleinventory.nil;

import io.socol.bundleinventory.Hooks;
import nilloader.api.ClassRetransformer;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

/** Replaces HandledScreenMixin with a Mini patch on slotClicked. */
@Patch.Class("net.minecraft.client.gui.screens.inventory.AbstractContainerScreen")
public class AbstractContainerScreenTransformer extends MiniTransformer implements ClassRetransformer {

   // Target by full descriptor: 26.x also has a private
   // slotClicked(Slot,int,MouseButtonEvent,ContainerInput)V overload.
   @Patch.Method("slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V")
   @Patch.Method.AffectsControlFlow
   public void patchSlotClicked(PatchContext ctx) {
      LabelNode Lcontinue = new LabelNode();
      ctx.jumpToStart();
      ctx.add(
         ALOAD(0), // this
         ALOAD(1), // Slot slot
         ILOAD(2), // int slotId
         ILOAD(3), // int button
         ALOAD(4), // ContainerInput actionType
         INVOKESTATIC(hooks(), "onSlotClicked",
            "(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)Z"),
         IFEQ(Lcontinue),
         RETURN(),
         Lcontinue);
   }

   private String hooks() {
      return getClass().getName().replace('.', '/') + "$Hooks";
   }

   public static class Hooks {
      public static boolean onSlotClicked(
            AbstractContainerScreen self, Slot slot, int slotId, int button, ContainerInput actionType) {
         return io.socol.bundleinventory.Hooks.onScreenSlotClick(self.getMenu(), slot, slotId, button, actionType);
      }
   }
}
