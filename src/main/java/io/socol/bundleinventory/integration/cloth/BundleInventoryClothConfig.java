package io.socol.bundleinventory.integration.cloth;

import io.socol.bundleinventory.config.BundleInventoryConfig;
import io.socol.bundleinventory.config.BundleInventoryDefaultConfig;
import java.util.function.Function;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Config(name = "bundleinventory")
@Environment(EnvType.CLIENT)
public class BundleInventoryClothConfig implements ConfigData, BundleInventoryConfig {
   @Comment("How many rows will be visible in Bundle inventory")
   @BoundedDiscrete(min = 3L, max = 8L)
   int bundleInventoryVisibleRows = BundleInventoryDefaultConfig.INSTANCE.bundleInventoryVisibleRows();
   @Comment("Should play sounds when player is interacting with Bundle via inventory")
   boolean shouldPlayBundleInventorySounds = BundleInventoryDefaultConfig.INSTANCE.shouldPlayBundleInventorySounds();
   @Comment("Should highlight open Bundle by rendering it differently")
   boolean indicateOpenBundle = BundleInventoryDefaultConfig.INSTANCE.indicateOpenBundle();
   @Comment("Show armor & second hand slots in Bundle inventory")
   boolean showEquipmentSlotsInBundleInventory = BundleInventoryDefaultConfig.INSTANCE.showEquipmentSlotsInBundleInventory();
   @Comment("Do client-side checks of bundle occupancy on item insertion")
   boolean clientSideItemInsertionChecks = BundleInventoryDefaultConfig.INSTANCE.clientSideItemInsertionChecks();

   public BundleInventoryClothConfig() {
   }

   public static BundleInventoryConfig create() {
      AutoConfig.register(BundleInventoryClothConfig.class, JanksonConfigSerializer::new);
      return (BundleInventoryConfig)AutoConfig.getConfigHolder(BundleInventoryClothConfig.class).getConfig();
   }

   @Override
   public Function<Screen, Screen> getScreenFactory() {
      return parent -> AutoConfigClient.getConfigScreen(BundleInventoryClothConfig.class, parent).get();
   }

   @Override
   public int bundleInventoryVisibleRows() {
      return this.bundleInventoryVisibleRows;
   }

   @Override
   public boolean shouldPlayBundleInventorySounds() {
      return this.shouldPlayBundleInventorySounds;
   }

   @Override
   public boolean indicateOpenBundle() {
      return this.indicateOpenBundle;
   }

   @Override
   public boolean showEquipmentSlotsInBundleInventory() {
      return this.showEquipmentSlotsInBundleInventory;
   }

   @Override
   public boolean clientSideItemInsertionChecks() {
      return this.clientSideItemInsertionChecks;
   }
}
