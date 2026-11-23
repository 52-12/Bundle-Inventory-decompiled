package io.socol.bundleinventory.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** Loader-agnostic settings, stored in config/bundleinventory.properties. */
public final class BundleInventorySettings {
   private static final BundleInventorySettings INSTANCE = new BundleInventorySettings();
   private final Properties values = new Properties();
   private final Path file;

   private BundleInventorySettings() {
      Path gameDir = Paths.get(System.getProperty("user.dir", "."));
      this.file = gameDir.resolve("config").resolve("bundleinventory.properties");
      if (Files.isRegularFile(this.file)) {
         try (InputStream in = Files.newInputStream(this.file)) {
            this.values.load(in);
         } catch (IOException ignored) {
         }
      }
   }

   public static BundleInventorySettings get() {
      return INSTANCE;
   }

   private boolean flag(String key, boolean fallback) {
      String value = this.values.getProperty(key);
      return value == null ? fallback : Boolean.parseBoolean(value.trim());
   }

   private int number(String key, int fallback) {
      String value = this.values.getProperty(key);
      if (value == null) {
         return fallback;
      }

      try {
         return Integer.parseInt(value.trim());
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   public int rows() {
      return Math.max(3, Math.min(8, this.number("bundleInventoryVisibleRows", 6)));
   }

   public boolean sounds() {
      return this.flag("shouldPlayBundleInventorySounds", true);
   }

   public boolean indicateOpen() {
      return this.flag("indicateOpenBundle", true);
   }

   public boolean equipment() {
      return this.flag("showEquipmentSlotsInBundleInventory", false);
   }

   public boolean clientChecks() {
      return this.flag("clientSideItemInsertionChecks", true);
   }

   public boolean bossBar() {
      return this.flag("useBossBarFullness", true);
   }

   public void setRows(int value) {
      this.values.setProperty("bundleInventoryVisibleRows", Integer.toString(Math.max(3, Math.min(8, value))));
   }

   public void setSounds(boolean value) {
      this.values.setProperty("shouldPlayBundleInventorySounds", Boolean.toString(value));
   }

   public void setIndicateOpen(boolean value) {
      this.values.setProperty("indicateOpenBundle", Boolean.toString(value));
   }

   public void setEquipment(boolean value) {
      this.values.setProperty("showEquipmentSlotsInBundleInventory", Boolean.toString(value));
   }

   public void setClientChecks(boolean value) {
      this.values.setProperty("clientSideItemInsertionChecks", Boolean.toString(value));
   }

   public void setBossBar(boolean value) {
      this.values.setProperty("useBossBarFullness", Boolean.toString(value));
   }

   public void save() {
      try {
         Files.createDirectories(this.file.getParent());
         try (OutputStream out = Files.newOutputStream(this.file)) {
            this.values.store(out, "Bundle Inventory settings");
         }
      } catch (IOException ignored) {
      }
   }
}
