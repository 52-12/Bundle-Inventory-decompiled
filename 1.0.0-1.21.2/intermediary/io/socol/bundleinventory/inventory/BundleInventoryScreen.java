package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1921;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_465;
import net.minecraft.class_746;
import net.minecraft.class_9276;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends class_465<BundleInventoryHandler> {
   private static final class_2960 TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");

   public BundleInventoryScreen(BundleInventoryHandler handler, class_1661 inventory) {
      super(handler, inventory, handler.getBundleTitle());
      this.field_2792 = 176;
      this.field_2779 = 258;
      this.field_25270 = this.field_2779 - 94;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      this.method_2380(context, mouseX, mouseY);
   }

   protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {
      int i = (this.field_22789 - this.field_2792) / 2;
      int j = (this.field_22790 - this.field_2779) / 2;
      context.method_25290(class_1921::method_62277, TEXTURE, i, j, 0.0F, 0.0F, this.field_2792, this.field_2779, 512, 512);
   }

   protected void method_2388(class_332 context, int mouseX, int mouseY) {
      super.method_2388(context, mouseX, mouseY);
      class_9276 content = ((BundleInventoryHandler)this.field_2797).getBundleInventory().getBundleContent();
      if (content != null) {
         int size = content.method_57428().getNumerator() * 64 / content.method_57428().getDenominator();
         int capacity = 64;
         class_2561 text = class_2561.method_30163(String.format("%d/%d", size, capacity));
         int k = this.field_2792 - 8 - this.field_22793.method_27525(text) - 2;
         context.method_25294(k - 2, this.field_25268 - 2, this.field_2792 - 8, this.field_25268 + 9, 1325400064);
         context.method_27535(this.field_22793, text, k, this.field_25268, -1);
      }
   }

   protected void method_2383(class_1735 slot, int slotId, int button, class_1713 actionType) {
      if (!(slot instanceof BundleInventoryHandler.BundleSlot)) {
         if (slotId >= 45) {
            int slotIndex = slotId - 45;
            ((BundleInventoryHandler)this.field_2797).handleBundleSlotClick(slot, slotIndex, button, actionType);
         } else {
            super.method_2383(slot, slotId, button, actionType);
         }
      }
   }

   protected void method_37432() {
      super.method_37432();
      class_746 player = class_310.method_1551().field_1724;
      if (player == null || !((BundleInventoryHandler)this.field_2797).method_7597(player)) {
         class_310.method_1551().method_1507(null);
      }
   }
}
