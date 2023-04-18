package net.minecraft.entity.ai.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MobNavigation extends EntityNavigation {
   private boolean avoidSunlight;

   public MobNavigation(MobEntity arg, World arg2) {
      super(arg, arg2);
   }

   protected PathNodeNavigator createPathNodeNavigator(int range) {
      this.nodeMaker = new LandPathNodeMaker();
      this.nodeMaker.setCanEnterOpenDoors(true);
      return new PathNodeNavigator(this.nodeMaker, range);
   }

   protected boolean isAtValidPosition() {
      return this.entity.isOnGround() || this.isInLiquid() || this.entity.hasVehicle();
   }

   protected Vec3d getPos() {
      return new Vec3d(this.entity.getX(), (double)this.getPathfindingY(), this.entity.getZ());
   }

   public Path findPathTo(BlockPos target, int distance) {
      BlockPos lv;
      if (this.world.getBlockState(target).isAir()) {
         for(lv = target.down(); lv.getY() > this.world.getBottomY() && this.world.getBlockState(lv).isAir(); lv = lv.down()) {
         }

         if (lv.getY() > this.world.getBottomY()) {
            return super.findPathTo(lv.up(), distance);
         }

         while(lv.getY() < this.world.getTopY() && this.world.getBlockState(lv).isAir()) {
            lv = lv.up();
         }

         target = lv;
      }

      if (!this.world.getBlockState(target).getMaterial().isSolid()) {
         return super.findPathTo(target, distance);
      } else {
         for(lv = target.up(); lv.getY() < this.world.getTopY() && this.world.getBlockState(lv).getMaterial().isSolid(); lv = lv.up()) {
         }

         return super.findPathTo(lv, distance);
      }
   }

   public Path findPathTo(Entity entity, int distance) {
      return this.findPathTo(entity.getBlockPos(), distance);
   }

   private int getPathfindingY() {
      if (this.entity.isTouchingWater() && this.canSwim()) {
         int i = this.entity.getBlockY();
         BlockState lv = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), (double)i, this.entity.getZ()));
         int j = 0;

         do {
            if (!lv.isOf(Blocks.WATER)) {
               return i;
            }

            ++i;
            lv = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), (double)i, this.entity.getZ()));
            ++j;
         } while(j <= 16);

         return this.entity.getBlockY();
      } else {
         return MathHelper.floor(this.entity.getY() + 0.5);
      }
   }

   protected void adjustPath() {
      super.adjustPath();
      if (this.avoidSunlight) {
         if (this.world.isSkyVisible(BlockPos.ofFloored(this.entity.getX(), this.entity.getY() + 0.5, this.entity.getZ()))) {
            return;
         }

         for(int i = 0; i < this.currentPath.getLength(); ++i) {
            PathNode lv = this.currentPath.getNode(i);
            if (this.world.isSkyVisible(new BlockPos(lv.x, lv.y, lv.z))) {
               this.currentPath.setLength(i);
               return;
            }
         }
      }

   }

   protected boolean canWalkOnPath(PathNodeType pathType) {
      if (pathType == PathNodeType.WATER) {
         return false;
      } else if (pathType == PathNodeType.LAVA) {
         return false;
      } else {
         return pathType != PathNodeType.OPEN;
      }
   }

   public void setCanPathThroughDoors(boolean canPathThroughDoors) {
      this.nodeMaker.setCanOpenDoors(canPathThroughDoors);
   }

   public boolean method_35140() {
      return this.nodeMaker.canEnterOpenDoors();
   }

   public void setCanEnterOpenDoors(boolean canEnterOpenDoors) {
      this.nodeMaker.setCanEnterOpenDoors(canEnterOpenDoors);
   }

   public boolean canEnterOpenDoors() {
      return this.nodeMaker.canEnterOpenDoors();
   }

   public void setAvoidSunlight(boolean avoidSunlight) {
      this.avoidSunlight = avoidSunlight;
   }

   public void setCanWalkOverFences(boolean canWalkOverFences) {
      this.nodeMaker.setCanWalkOverFences(canWalkOverFences);
   }
}
