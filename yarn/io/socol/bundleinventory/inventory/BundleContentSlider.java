package io.socol.bundleinventory.inventory;

import io.socol.bundleinventory.BundleInventoryMod;
import io.socol.bundleinventory.util.UIDrawer;
import java.util.function.DoubleConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

@Environment(EnvType.CLIENT)
public class BundleContentSlider extends ClickableWidget {
   private static final Identifier TEXTURE = BundleInventoryMod.id("textures/gui/bundle.png");
   private final DoubleConsumer valueConsumer;
   private double value;

   public BundleContentSlider(int x, int y, int width, int height, DoubleConsumer valueConsumer) {
      super(x, y, width, height, Text.empty());
      this.valueConsumer = valueConsumer;
   }

   protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      int dy = 1;
      if (this.value >= 0.0) {
         dy += (int)Math.round(this.value * (this.height - 17));
      }

      new UIDrawer(context).at(this.getX() + 1, this.getY() + dy).drawScroller(this.value >= 0.0);
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
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

   public void onClick(Click click, boolean doubled) {
      this.setValueFromMouse(click.y());
   }

   protected void onDrag(Click click, double offsetX, double offsetY) {
      this.setValueFromMouse(click.y());
      super.onDrag(click, offsetX, offsetY);
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public void onRelease(Click click) {
      if (this.value >= 0.0) {
         super.playDownSound(MinecraftClient.getInstance().getSoundManager());
      }
   }
}
