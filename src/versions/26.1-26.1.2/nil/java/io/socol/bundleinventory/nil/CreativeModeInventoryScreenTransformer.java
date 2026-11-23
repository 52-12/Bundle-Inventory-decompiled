package io.socol.bundleinventory.nil;

import io.socol.bundleinventory.Hooks;
import nilloader.api.ClassRetransformer;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

/** Replaces CreativeInventoryScreenMixin with a Mini patch on slotClicked. */
@Patch.Class("net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen")
public class CreativeModeInventoryScreenTransformer extends MiniTransformer implements ClassRetransformer {

   @Patch.Method("slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V")
   @Patch.Method.AffectsControlFlow
   public void patchSlotClicked(PatchContext ctx) {
      LabelNode Lcontinue = new LabelNode();
      ctx.jumpToStart();
      ctx.add(
         ALOAD(0), // this
         ALOAD(1), // Slot slot (may be a SlotWrapper on the creative tabs)
         ILOAD(2), // int slotId
         ILOAD(3), // int button
         ALOAD(4), // ContainerInput actionType
         INVOKESTATIC(hooks(), "onSlotClicked",
            "(Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)Z"),
         IFEQ(Lcontinue),
         RETURN(),
         Lcontinue);
   }

   private String hooks() {
      return getClass().getName().replace('.', '/') + "$Hooks";
   }

   public static class Hooks {
      public static boolean onSlotClicked(
            CreativeModeInventoryScreen self, Slot slot, int slotId, int button, ContainerInput actionType) {
         // The common Hooks already copes with a null player.
         return Hooks.onScreenSlotClick(self.getMenu(), slot, slotId, button, actionType);
      }
   }
}
