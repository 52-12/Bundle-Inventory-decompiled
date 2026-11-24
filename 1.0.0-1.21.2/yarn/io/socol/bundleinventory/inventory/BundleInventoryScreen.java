package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.BundleContentsComponent;

@Environment(EnvType.CLIENT)
public class BundleInventoryScreen extends HandledScreen<BundleInventoryHandler> {
   private static final Identifier TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");

   public BundleInventoryScreen(BundleInventoryHandler handler, PlayerInventory inventory) {
      super(handler, inventory, handler.getBundleTitle());
      this.backgroundWidth = 176;
      this.backgroundHeight = 258;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int i = (this.width - this.backgroundWidth) / 2;
      int j = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 512, 512);
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      super.drawForeground(context, mouseX, mouseY);
      BundleContentsComponent content = ((BundleInventoryHandler)this.handler).getBundleInventory().getBundleContent();
      if (content != null) {
         int size = content.getOccupancy().getNumerator() * 64 / content.getOccupancy().getDenominator();
         int capacity = 64;
         Text text = Text.of(String.format("%d/%d", size, capacity));
         int k = this.backgroundWidth - 8 - this.textRenderer.getWidth(text) - 2;
         context.fill(k - 2, this.titleY - 2, this.backgroundWidth - 8, this.titleY + 9, 1325400064);
         context.drawTextWithShadow(this.textRenderer, text, k, this.titleY, -1);
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
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null || !((BundleInventoryHandler)this.handler).canUse(player)) {
         MinecraftClient.getInstance().setScreen(null);
      }
   }
}
