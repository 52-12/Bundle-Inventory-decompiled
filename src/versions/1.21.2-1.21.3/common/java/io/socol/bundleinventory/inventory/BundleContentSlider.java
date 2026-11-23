package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.util.UIDrawer;
import java.util.function.DoubleConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class BundleContentSlider extends AbstractWidget {
   private final DoubleConsumer valueConsumer;
   private double value;

   public BundleContentSlider(int x, int y, int width, int height, DoubleConsumer valueConsumer) {
      super(x, y, width, height, Component.empty());
      this.valueConsumer = valueConsumer;
   }

   protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
      int dy = 1;
      if (this.value >= 0.0) {
         dy += (int)Math.round(this.value * (this.height - 17));
      }

      new UIDrawer(context).at(this.getX() + 1, this.getY() + dy).drawScroller(this.value >= 0.0);
   }

   protected void updateWidgetNarration(NarrationElementOutput builder) {
   }

   public void setValue(double value) {
      this.value = value;
   }

   private void setValueFromMouse(double mouseY) {
      if (!(this.value < 0.0)) {
         double l = mouseY - (this.getY() + 9);
         this.value = l / (this.height - 17);
         this.value = Math.clamp(this.value, 0.0, 1.0);
         this.valueConsumer.accept(this.value);
      }
   }

   public void onClick(double mouseX, double mouseY) {
      this.setValueFromMouse(mouseY);
   }

   protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
      this.setValueFromMouse(mouseY);
      super.onDrag(mouseX, mouseY, deltaX, deltaY);
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public void onRelease(double mouseX, double mouseY) {
      if (this.value >= 0.0) {
         super.playDownSound(Minecraft.getInstance().getSoundManager());
      }
   }
}
