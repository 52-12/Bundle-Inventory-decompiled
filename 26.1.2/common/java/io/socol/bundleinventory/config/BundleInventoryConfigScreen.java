package io.socol.bundleinventory.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Vanilla-widget settings screen; used by Mod Menu, Forge and NeoForge. */
public class BundleInventoryConfigScreen extends Screen {
   private final Screen parent;
   private final BundleInventorySettings settings = BundleInventorySettings.get();
   private int rows;
   private boolean sounds;
   private boolean indicateOpen;
   private boolean equipment;
   private boolean clientChecks;
   private boolean bossBar;

   public BundleInventoryConfigScreen(Screen parent) {
      super(Component.literal("Bundle Inventory"));
      this.parent = parent;
      this.rows = this.settings.rows();
      this.sounds = this.settings.sounds();
      this.indicateOpen = this.settings.indicateOpen();
      this.equipment = this.settings.equipment();
      this.clientChecks = this.settings.clientChecks();
      this.bossBar = this.settings.bossBar();
   }

   private Component rowsLabel() {
      return Component.literal("Visible rows: " + this.rows);
   }

   private Component soundsLabel() {
      return Component.literal("Sounds: " + (this.sounds ? "ON" : "OFF"));
   }

   private Component indicateLabel() {
      return Component.literal("Open highlight: " + (this.indicateOpen ? "ON" : "OFF"));
   }

   private Component equipmentLabel() {
      return Component.literal("Equipment slots: " + (this.equipment ? "ON" : "OFF"));
   }

   private Component checksLabel() {
      return Component.literal("Client-side checks: " + (this.clientChecks ? "ON" : "OFF"));
   }

   private Component barLabel() {
      return Component.literal("Fullness bar: " + (this.bossBar ? "BOSS BAR" : "SMOOTH"));
   }

   @Override
   protected void init() {
      int left = this.width / 2 - 100;
      int y = Math.max(30, this.height / 2 - 84);
      this.addRenderableWidget(
         Button.builder(this.rowsLabel(), button -> {
            this.rows = this.rows >= 8 ? 3 : this.rows + 1;
            button.setMessage(this.rowsLabel());
         }).bounds(left, y, 200, 20).build()
      );
      y += 24;
      this.addRenderableWidget(
         Button.builder(this.soundsLabel(), button -> {
            this.sounds = !this.sounds;
            button.setMessage(this.soundsLabel());
         }).bounds(left, y, 200, 20).build()
      );
      y += 24;
      this.addRenderableWidget(
         Button.builder(this.indicateLabel(), button -> {
            this.indicateOpen = !this.indicateOpen;
            button.setMessage(this.indicateLabel());
         }).bounds(left, y, 200, 20).build()
      );
      y += 24;
      this.addRenderableWidget(
         Button.builder(this.equipmentLabel(), button -> {
            this.equipment = !this.equipment;
            button.setMessage(this.equipmentLabel());
         }).bounds(left, y, 200, 20).build()
      );
      y += 24;
      this.addRenderableWidget(
         Button.builder(this.checksLabel(), button -> {
            this.clientChecks = !this.clientChecks;
            button.setMessage(this.checksLabel());
         }).bounds(left, y, 200, 20).build()
      );
      y += 24;
      this.addRenderableWidget(
         Button.builder(this.barLabel(), button -> {
            this.bossBar = !this.bossBar;
            button.setMessage(this.barLabel());
         }).bounds(left, y, 200, 20).build()
      );
      y += 32;
      this.addRenderableWidget(
         Button.builder(Component.literal("Done"), button -> this.saveAndClose()).bounds(left, y, 200, 20).build()
      );
   }

   private void saveAndClose() {
      this.settings.setRows(this.rows);
      this.settings.setSounds(this.sounds);
      this.settings.setIndicateOpen(this.indicateOpen);
      this.settings.setEquipment(this.equipment);
      this.settings.setClientChecks(this.clientChecks);
      this.settings.setBossBar(this.bossBar);
      this.settings.save();
      Minecraft.getInstance().setScreenAndShow(this.parent);
   }
}
