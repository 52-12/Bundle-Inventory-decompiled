package io.socol.bundleinventory.nil;

import nilloader.api.ClassRetransformer;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;

/** Replaces ClientPlayerInteractionManagerMixin for 1.21.x (ClickType era). */
@Patch.Class("net.minecraft.client.multiplayer.MultiPlayerGameMode")
public class MultiPlayerGameModeTransformer extends MiniTransformer implements ClassRetransformer {

   @Patch.Method("useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")
   @Patch.Method.AffectsControlFlow
   public void patchUseItem(PatchContext ctx) {
      LabelNode Lcontinue = new LabelNode();
      ctx.jumpToStart();
      ctx.add(
         ALOAD(1),
         ALOAD(2),
         INVOKESTATIC(hooks(), "onUseItem",
            "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"),
         DUP(),
         IFNULL(Lcontinue),
         ARETURN(),
         Lcontinue,
         POP());
   }

   @Patch.Method("handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V")
   @Patch.Method.AffectsControlFlow
   public void patchHandleInventoryMouseClick(PatchContext ctx) {
      LabelNode Lcontinue = new LabelNode();
      ctx.jumpToStart();
      ctx.add(
         ILOAD(1),
         ILOAD(2),
         ILOAD(3),
         ALOAD(4),
         ALOAD(5),
         INVOKESTATIC(hooks(), "onClickSlot",
            "(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)Z"),
         IFEQ(Lcontinue),
         RETURN(),
         Lcontinue);
   }

   private String hooks() {
      return getClass().getName().replace('.', '/') + "$Hooks";
   }

   public static class Hooks {
      public static InteractionResult onUseItem(Player player, InteractionHand hand) {
         io.socol.bundleinventory.NilBootstrap.ensure();
         return io.socol.bundleinventory.Hooks.onItemInteract(player, hand);
      }

      public static boolean onClickSlot(
            int syncId, int slotId, int button, ClickType actionType, Player player) {
         io.socol.bundleinventory.NilBootstrap.ensure();
         return io.socol.bundleinventory.Hooks.onClickSlot(syncId, slotId, button, actionType, player);
      }
   }
}
