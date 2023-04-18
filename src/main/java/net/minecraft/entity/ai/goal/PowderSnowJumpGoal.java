package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class PowderSnowJumpGoal extends Goal {
   private final MobEntity entity;
   private final World world;

   public PowderSnowJumpGoal(MobEntity entity, World world) {
      this.entity = entity;
      this.world = world;
      this.setControls(EnumSet.of(Goal.Control.JUMP));
   }

   public boolean canStart() {
      boolean bl = this.entity.wasInPowderSnow || this.entity.inPowderSnow;
      if (bl && this.entity.getType().isIn(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
         BlockPos lv = this.entity.getBlockPos().up();
         BlockState lv2 = this.world.getBlockState(lv);
         return lv2.isOf(Blocks.POWDER_SNOW) || lv2.getCollisionShape(this.world, lv) == VoxelShapes.empty();
      } else {
         return false;
      }
   }

   public boolean shouldRunEveryTick() {
      return true;
   }

   public void tick() {
      this.entity.getJumpControl().setActive();
   }
}
