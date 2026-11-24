package io.socol.bundleinventory.nil;

import nilloader.api.ClassRetransformer;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/** Replaces HandledScreenMixin for 1.21.x. */
@Patch.Class("net.minecraft.client.gui.screens.inventory.AbstractContainerScreen")
public class AbstractContainerScreenTransformer extends MiniTransformer implements ClassRetransformer {

   @Patch.Method("slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V")
   @Patch.Method.AffectsControlFlow
   public void patchSlotClicked(PatchContext ctx) {
      LabelNode Lcontinue = new LabelNode();
      ctx.jumpToStart();
      ctx.add(
         ALOAD(0),
         ALOAD(1),
         ILOAD(2),
         ILOAD(3),
         ALOAD(4),
         INVOKESTATIC(hooks(), "onSlotClicked",
            "(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)Z"),
         IFEQ(Lcontinue),
         RETURN(),
         Lcontinue);
   }

   private String hooks() {
      return getClass().getName().replace('.', '/') + "$Hooks";
   }

   public static class Hooks {
      public static boolean onSlotClicked(
            AbstractContainerScreen self, Slot slot, int slotId, int button, ClickType actionType) {
         io.socol.bundleinventory.NilBootstrap.ensure();
         return io.socol.bundleinventory.Hooks.onScreenSlotClick(self.getMenu(), slot, slotId, button, actionType);
      }
   }
}
