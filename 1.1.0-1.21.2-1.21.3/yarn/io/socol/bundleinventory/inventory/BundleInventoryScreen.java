package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.StepColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.BundleContentsComponent;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends HandledScreen<BundleInventoryHandler> {
   private static final Identifier TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");
   private final StepColor barColor;
   private float currentBarOccupancy;
   private float prevBarOccupancy;

   public BundleInventoryScreen(BundleInventoryHandler handler, PlayerInventory inventory) {
      super(handler, inventory, handler.getBundleTitle());
      this.backgroundWidth = 176;
      this.backgroundHeight = 258;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
      this.barColor = new StepColor(new float[]{0.75F, 0.9F, 1.0F, 2.0F}, new int[]{-13710223, -2379249, -1671646, -1618884});
      this.currentBarOccupancy = handler.getBundleInventory().getBundleContent().getOccupancy().floatValue();
      this.prevBarOccupancy = this.currentBarOccupancy;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int i = (this.width - this.backgroundWidth) / 2;
      int j = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 512, 512);
      BundleContentsComponent content = ((BundleInventoryHandler)this.handler).getBundleInventory().getBundleContent();
      if (content != null) {
         float barOccupancy = MathHelper.lerp(delta, this.prevBarOccupancy, this.currentBarOccupancy);
         int barWidth = MathHelper.ceil(barOccupancy * 38.0F);
         int barColor = this.barColor.get(content.getOccupancy().floatValue(), delta);
         context.fill(i + this.backgroundWidth - 44, j + 6, i + this.backgroundWidth - 6, j + 14, 1325400064);
         context.fill(i + this.backgroundWidth - 44, j + 6, i + this.backgroundWidth - 44 + barWidth, j + 14, barColor);
         context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, i + this.backgroundWidth - 45, j + 5, 176.0F, 0.0F, 40, 10, 512, 512);
      }
   }

   protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
      if (!(slot instanceof BundleInventoryHandler.BundleSlot)) {
         if (slotId >= 45) {
            int slotIndex = slotId - 45;
            ((BundleInventoryHandler)this.handler).handleBundleSlotClick(slot, slotIndex, button, actionType);
         } else {
            super.onMouseClick(slot, slotId, button, actionType);
         }
      }
   }

   protected void handledScreenTick() {
      super.handledScreenTick();
      this.prevBarOccupancy = this.currentBarOccupancy;
      BundleContentsComponent content = ((BundleInventoryHandler)this.handler).getBundleInventory().getBundleContent();
      if (content != null) {
         if (content.isEmpty()) {
            this.barColor.reset();
         } else {
            this.barColor.update(content.getOccupancy().floatValue());
         }

         float targetBarValue = content.getOccupancy().floatValue();
         this.currentBarOccupancy = MathHelper.lerp(0.25F, this.currentBarOccupancy, targetBarValue);
         if (targetBarValue <= 0.0F && this.currentBarOccupancy <= 0.01F) {
            this.currentBarOccupancy = 0.0F;
         }
      }

      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null || !((BundleInventoryHandler)this.handler).canUse(player)) {
         MinecraftClient.getInstance().setScreen(null);
      }
   }
}
