package io.socol.bundleinventory.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;

@Environment(EnvType.CLIENT)
public class UIDrawer {
   private static final int DEMO_BG_WIDTH = 248;
   private static final int DEMO_BG_HEIGHT = 166;
   private static final Identifier DEMO_BG = Identifier.withDefaultNamespace("textures/gui/demo_background.png");
   private static final int SLOT_SIZE = 18;
   private static final Identifier CHEST = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   private static final Identifier SCROLLER_ENABLED = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
   private static final Identifier SCROLLER_DISABLED = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");
   private static final Identifier CREATIVE_INV_TAB_ITEMS = Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png");
   private static final Identifier[] BOSSBAR_BACKGROUND = new Identifier[]{
      Identifier.withDefaultNamespace("boss_bar/pink_background"),
      Identifier.withDefaultNamespace("boss_bar/blue_background"),
      Identifier.withDefaultNamespace("boss_bar/red_background"),
      Identifier.withDefaultNamespace("boss_bar/green_background"),
      Identifier.withDefaultNamespace("boss_bar/yellow_background"),
      Identifier.withDefaultNamespace("boss_bar/purple_background"),
      Identifier.withDefaultNamespace("boss_bar/white_background")
   };
   private static final Identifier[] BOSSBAR_PROGRESS = new Identifier[]{
      Identifier.withDefaultNamespace("boss_bar/pink_progress"),
      Identifier.withDefaultNamespace("boss_bar/blue_progress"),
      Identifier.withDefaultNamespace("boss_bar/red_progress"),
      Identifier.withDefaultNamespace("boss_bar/green_progress"),
      Identifier.withDefaultNamespace("boss_bar/yellow_progress"),
      Identifier.withDefaultNamespace("boss_bar/purple_progress"),
      Identifier.withDefaultNamespace("boss_bar/white_progress")
   };
   private static final Identifier[] BOSSBAR_NOTCHED_BACKGROUND = new Identifier[]{
      Identifier.withDefaultNamespace("boss_bar/notched_6_background"),
      Identifier.withDefaultNamespace("boss_bar/notched_10_background"),
      Identifier.withDefaultNamespace("boss_bar/notched_12_background"),
      Identifier.withDefaultNamespace("boss_bar/notched_20_background")
   };
   private static final Identifier[] BOSSBAR_NOTCHED_PROGRESS = new Identifier[]{
      Identifier.withDefaultNamespace("boss_bar/notched_6_progress"),
      Identifier.withDefaultNamespace("boss_bar/notched_10_progress"),
      Identifier.withDefaultNamespace("boss_bar/notched_12_progress"),
      Identifier.withDefaultNamespace("boss_bar/notched_20_progress")
   };
   private static final Identifier RECIPE_BOOK_BG = Identifier.withDefaultNamespace("recipe_book/overlay_recipe");
   private final GuiGraphicsExtractor ctx;
   private final int x0;
   private final int y0;

   private UIDrawer(GuiGraphicsExtractor ctx, int x0, int y0) {
      this.ctx = ctx;
      this.x0 = x0;
      this.y0 = y0;
   }

   public UIDrawer(GuiGraphicsExtractor ctx) {
      this(ctx, 0, 0);
   }

   public UIDrawer at(int x, int y) {
      return new UIDrawer(this.ctx, x, y);
   }

   public UIDrawer move(int x, int y) {
      return this.at(this.x0 + x, this.y0 + y);
   }

   public UIDrawer drawResizablePlate(int width, int height) {
      this.ctx.blitSprite(RenderPipelines.GUI_TEXTURED, RECIPE_BOOK_BG, this.x0, this.y0, width, height);
      return this;
   }

   public UIDrawer drawPlate(int width, int height) {
      int w1 = width / 2;
      int h1 = height / 2;
      int w2 = width - w1;
      int h2 = height - h1;
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, DEMO_BG, this.x0, this.y0, 0.0F, 0.0F, w1, h1, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, DEMO_BG, this.x0 + w1, this.y0, 248 - w2, 0.0F, w2, h1, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, DEMO_BG, this.x0, this.y0 + h1, 0.0F, 166 - h2, w1, h2, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, DEMO_BG, this.x0 + w1, this.y0 + h1, 248 - w2, 166 - h2, w2, h2, 256, 256);
      return this;
   }

   private void drawSlotAt(int dx, int dy) {
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CHEST, this.x0 + dx, this.y0 + dy, 79.0F, 157.0F, 18, 18, 256, 256);
   }

   public UIDrawer drawSlot() {
      this.drawSlotAt(0, 0);
      return this;
   }

   public UIDrawer drawSlots(int columns, int rows) {
      for (int r = 0; r < rows; r++) {
         for (int c = 0; c < columns; c++) {
            this.drawSlotAt(c * 18, r * 18);
         }
      }

      return this;
   }

   public UIDrawer drawScroller(boolean enabled) {
      Identifier sprite = enabled ? SCROLLER_ENABLED : SCROLLER_DISABLED;
      this.ctx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.x0, this.y0, 12, 15);
      return this;
   }

   public UIDrawer drawScrollerGroove(int height) {
      int h1 = height / 2;
      int h2 = height - h1;
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0, this.y0, 174.0F, 17.0F, 14, h1, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0, this.y0 + h1, 174.0F, 129 - h2, 14, h2, 256, 256);
      return this;
   }

   public UIDrawer fill(int width, int height, int color) {
      this.ctx.fill(this.x0, this.y0, this.x0 + width, this.y0 + height, color);
      return this;
   }

   private void drawShading(int width, int height, boolean groove) {
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0 + width - 1, this.y0, 187.0F, 17.0F, 1, 1, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0, this.y0 + height - 1, 187.0F, 17.0F, 1, 1, 256, 256);
      int u1 = groove ? 174 : 187;
      int u2 = groove ? 187 : 174;
      int v = 79;
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0, this.y0, u1, v, width - 1, 1, 1, 1, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0, this.y0, u1, v, 1, height - 1, 1, 1, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0 + 1, this.y0 + height - 1, u2, v, width - 1, 1, 1, 1, 256, 256);
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, CREATIVE_INV_TAB_ITEMS, this.x0 + width - 1, this.y0 + 1, u2, v, 1, height - 1, 1, 1, 256, 256);
   }

   public UIDrawer drawGrooveShading(int width, int height) {
      this.drawShading(width, height, true);
      return this;
   }

   public UIDrawer drawBumpShading(int width, int height) {
      this.drawShading(width, height, false);
      return this;
   }

   public UIDrawer drawBossBarBackground(int width, BossBarColor color, BossBarOverlay style) {
      this.renderBossBarBackground(width, width, color, style, BOSSBAR_BACKGROUND, BOSSBAR_NOTCHED_BACKGROUND);
      return this;
   }

   public UIDrawer drawBossBarProgress(int width, int progress, BossBarColor color, BossBarOverlay style) {
      this.renderBossBarBackground(width, progress, color, style, BOSSBAR_PROGRESS, BOSSBAR_NOTCHED_PROGRESS);
      return this;
   }

   public UIDrawer drawBossBar(int width, int progress, BossBarColor color, BossBarOverlay style) {
      this.drawBossBarBackground(width, color, style);
      this.drawBossBarProgress(width, progress, color, style);
      return this;
   }

   private void renderBossBarBackground(int width, int progress, BossBarColor color, BossBarOverlay style, Identifier[] textures, Identifier[] notchedTextures) {
      this.renderBossBar(width, progress, textures[color.ordinal()]);
      if (style != BossBarOverlay.PROGRESS) {
         this.renderBossBar(width, progress, notchedTextures[style.ordinal() - 1]);
      }
   }

   private void renderBossBar(int width, int progress, Identifier sprite) {
      int w1 = Math.min(width - 5, progress);
      if (w1 > 0) {
         this.ctx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 182, 5, 0, 0, this.x0, this.y0, w1, 5);
      }

      int w2 = progress - (width - 5);
      if (w2 > 0) {
         this.ctx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 182, 5, 177, 0, this.x0 + w1, this.y0, w2, 5);
      }
   }
}
