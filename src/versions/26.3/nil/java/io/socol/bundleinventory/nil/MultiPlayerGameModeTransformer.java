package io.socol.bundleinventory.nil;

import io.socol.bundleinventory.Hooks;
import nilloader.api.ClassRetransformer;
import nilloader.api.lib.asm.tree.LabelNode;
import nilloader.api.lib.mini.MiniTransformer;
import nilloader.api.lib.mini.PatchContext;
import nilloader.api.lib.mini.annotation.Patch;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;

/**
 * Replaces ClientPlayerInteractionManagerMixin (two HEAD cancellable injects)
 * with Mini patches that delegate to the unchanged common Hooks class.
 */
@Patch.Class("net.minecraft.client.multiplayer.MultiPlayerGameMode")
public class MultiPlayerGameModeTransformer extends MiniTransformer implements ClassRetransformer {

   // @Inject(at = HEAD, cancellable) on useItem: return the hook's result when
   // it is non-null, otherwise let vanilla run.
   @Patch.Method("useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")
   @Patch.Method.AffectsControlFlow
   public void patchUseItem(PatchContext ctx) {
      LabelNode Lcontinue = new LabelNode();
      ctx.jumpToStart();
      ctx.add(
         ALOAD(1), // Player player (slot 0 is `this`)
         ALOAD(2), // InteractionHand hand
         INVOKESTATIC(hooks(), "onUseItem",
            "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"),
         DUP(),             // keep a copy for ARETURN
         IFNULL(Lcontinue), // consume one; decline -> continue
         ARETURN(),         // accept -> return the hook's result
         Lcontinue,         // the null path still has one null on the stack
         POP());            // ...which must be discarded before vanilla code
   }

   // @Inject(at = HEAD, cancellable) on handleContainerInput: cancel when the
   // hook returns true.
   @Patch.Method("handleContainerInput(IIILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V")
   @Patch.Method.AffectsControlFlow
   public void patchHandleContainerInput(PatchContext ctx) {
      LabelNode Lcontinue = new LabelNode();
      ctx.jumpToStart();
      ctx.add(
         ILOAD(1), // int syncId
         ILOAD(2), // int slotId
         ILOAD(3), // int button
         ALOAD(4), // ContainerInput actionType
         ALOAD(5), // Player player
         INVOKESTATIC(hooks(), "onClickSlot",
            "(IIILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)Z"),
         IFEQ(Lcontinue),
         RETURN(),
         Lcontinue);
   }

   private String hooks() {
      return getClass().getName().replace('.', '/') + "$Hooks";
   }

   public static class Hooks {
      public static InteractionResult onUseItem(Player player, InteractionHand hand) {
         return Hooks.onItemInteract(player, hand);
      }

      public static boolean onClickSlot(
            int syncId, int slotId, int button, ContainerInput actionType, Player player) {
         return Hooks.onClickSlot(syncId, slotId, button, actionType, player);
      }
   }
}
