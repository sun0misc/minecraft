package net.minecraft.block;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class FlowerBlock extends PlantBlock implements SuspiciousStewIngredient {
   protected static final float field_31094 = 3.0F;
   protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
   private final StatusEffect effectInStew;
   private final int effectInStewDuration;

   public FlowerBlock(StatusEffect suspiciousStewEffect, int effectDuration, AbstractBlock.Settings settings) {
      super(settings);
      this.effectInStew = suspiciousStewEffect;
      if (suspiciousStewEffect.isInstant()) {
         this.effectInStewDuration = effectDuration;
      } else {
         this.effectInStewDuration = effectDuration * 20;
      }

   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Vec3d lv = state.getModelOffset(world, pos);
      return SHAPE.offset(lv.x, lv.y, lv.z);
   }

   public StatusEffect getEffectInStew() {
      return this.effectInStew;
   }

   public int getEffectInStewDuration() {
      return this.effectInStewDuration;
   }
}
