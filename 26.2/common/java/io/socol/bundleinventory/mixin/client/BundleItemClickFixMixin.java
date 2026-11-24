package io.socol.bundleinventory.mixin.client;

import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Forge 26.1/26.2 ships a {@code BundleItem} patch that opens
 * {@code overrideOtherStackedOnMe} with {@code if (other.getCount() != 1) return false;}
 * where upstream (and Forge 26.3) checks {@code self.getCount()}. Because the carried
 * stack is empty when you right-click a bundle to take its selected item out (count 0,
 * and usually far more than 1 when inserting a stack), every such click bailed out
 * early: extraction instead picked the bundle itself up, and stack insertion did nothing.
 *
 * <p>This redirect touches only that first {@code getCount()} call. On the broken Forge
 * builds the receiver is the carried stack, so a non-bundle stack (including an empty one)
 * is reported as a single item and the vanilla branch runs; a carried bundle keeps its real
 * count. On builds where the receiver is the slot's bundle (vanilla, NeoForge, Forge 26.3)
 * the real count is returned and Forge's stacked-bundle guard is preserved. Where the
 * instruction does not exist at all, the redirect simply does not apply.
 */
@Mixin(BundleItem.class)
public class BundleItemClickFixMixin {
   @Redirect(
      method = "overrideOtherStackedOnMe",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", ordinal = 0),
      require = 0
   )
   private int bundleInventory$repairCarriedCountCheck(ItemStack stack) {
      return stack.getItem() instanceof BundleItem ? stack.getCount() : 1;
   }
}
