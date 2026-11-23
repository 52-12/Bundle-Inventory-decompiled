package io.socol.bundleinventory.nil;

import java.util.Set;

import nilloader.api.ASMTransformer;
import nilloader.api.ClassRetransformer;
import nilloader.api.lib.asm.Opcodes;
import nilloader.api.lib.asm.tree.ClassNode;
import nilloader.api.lib.asm.tree.FieldNode;

/**
 * Replaces the Fabric access widener at runtime: makes the four formerly
 * widened members public as the game classes are defined (the same access-flag
 * flip NilLoader itself applies for mappings.json widen entries).
 *
 * The compile-time half of the widener is handled by the build, which puts a
 * widened copy of the game jar on javac's classpath.
 */
public class AccessWidenerTransformer implements ASMTransformer, ClassRetransformer {
   private static final String ABSTRACT_CONTAINER_SCREEN =
      "net/minecraft/client/gui/screens/inventory/AbstractContainerScreen";
   private static final String SLOT_WRAPPER =
      "net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$SlotWrapper";
   private static final String ARMOR_SLOT = "net/minecraft/world/inventory/ArmorSlot";

   private static final Set<String> TARGETS = Set.of(
      ABSTRACT_CONTAINER_SCREEN, SLOT_WRAPPER, ARMOR_SLOT);

   @Override
   public Set<String> getTargets() {
      return TARGETS;
   }

   @Override
   public boolean canTransform(ClassLoader loader, String className) {
      return TARGETS.contains(className.replace('.', '/'));
   }

   @Override
   public boolean transform(ClassLoader loader, ClassNode clazz) {
      clazz.access = makePublic(clazz.access);
      for (FieldNode fn : clazz.fields) {
         if ("target".equals(fn.name)
            && "Lnet/minecraft/world/inventory/Slot;".equals(fn.desc)) {
            fn.access = makePublic(fn.access);
         }
         if ("skipNextRelease".equals(fn.name) && "Z".equals(fn.desc)) {
            fn.access = makePublic(fn.access);
         }
      }
      // ArmorSlot is already public on 26.2/26.3; the class-level flip above
      // is a no-op there and fixes the 26.1 package-private case.
      return false; // access flag changes never affect stack map frames
   }

   private static int makePublic(int access) {
      return (access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
   }
}
