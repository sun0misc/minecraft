package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class TreeDecoratorType {
   public static final TreeDecoratorType TRUNK_VINE;
   public static final TreeDecoratorType LEAVE_VINE;
   public static final TreeDecoratorType COCOA;
   public static final TreeDecoratorType BEEHIVE;
   public static final TreeDecoratorType ALTER_GROUND;
   public static final TreeDecoratorType ATTACHED_TO_LEAVES;
   private final Codec codec;

   private static TreeDecoratorType register(String id, Codec codec) {
      return (TreeDecoratorType)Registry.register(Registries.TREE_DECORATOR_TYPE, (String)id, new TreeDecoratorType(codec));
   }

   private TreeDecoratorType(Codec codec) {
      this.codec = codec;
   }

   public Codec getCodec() {
      return this.codec;
   }

   static {
      TRUNK_VINE = register("trunk_vine", TrunkVineTreeDecorator.CODEC);
      LEAVE_VINE = register("leave_vine", LeavesVineTreeDecorator.CODEC);
      COCOA = register("cocoa", CocoaBeansTreeDecorator.CODEC);
      BEEHIVE = register("beehive", BeehiveTreeDecorator.CODEC);
      ALTER_GROUND = register("alter_ground", AlterGroundTreeDecorator.CODEC);
      ATTACHED_TO_LEAVES = register("attached_to_leaves", AttachedToLeavesTreeDecorator.CODEC);
   }
}
