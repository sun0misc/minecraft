package net.minecraft.entity.decoration;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GlowItemFrameEntity extends ItemFrameEntity {
   public GlowItemFrameEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public GlowItemFrameEntity(World arg, BlockPos arg2, Direction arg3) {
      super(EntityType.GLOW_ITEM_FRAME, arg, arg2, arg3);
   }

   public SoundEvent getRemoveItemSound() {
      return SoundEvents.ENTITY_GLOW_ITEM_FRAME_REMOVE_ITEM;
   }

   public SoundEvent getBreakSound() {
      return SoundEvents.ENTITY_GLOW_ITEM_FRAME_BREAK;
   }

   public SoundEvent getPlaceSound() {
      return SoundEvents.ENTITY_GLOW_ITEM_FRAME_PLACE;
   }

   public SoundEvent getAddItemSound() {
      return SoundEvents.ENTITY_GLOW_ITEM_FRAME_ADD_ITEM;
   }

   public SoundEvent getRotateItemSound() {
      return SoundEvents.ENTITY_GLOW_ITEM_FRAME_ROTATE_ITEM;
   }

   protected ItemStack getAsItemStack() {
      return new ItemStack(Items.GLOW_ITEM_FRAME);
   }
}
