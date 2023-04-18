package net.minecraft.entity;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class LargeEntitySpawnHelper {
   public static Optional trySpawnAt(EntityType entityType, SpawnReason reason, ServerWorld world, BlockPos pos, int tries, int horizontalRange, int verticalRange, Requirements requirements) {
      BlockPos.Mutable lv = pos.mutableCopy();

      for(int l = 0; l < tries; ++l) {
         int m = MathHelper.nextBetween(world.random, -horizontalRange, horizontalRange);
         int n = MathHelper.nextBetween(world.random, -horizontalRange, horizontalRange);
         lv.set((Vec3i)pos, m, verticalRange, n);
         if (world.getWorldBorder().contains((BlockPos)lv) && findSpawnPos(world, verticalRange, lv, requirements)) {
            MobEntity lv2 = (MobEntity)entityType.create(world, (NbtCompound)null, (Consumer)null, lv, reason, false, false);
            if (lv2 != null) {
               if (lv2.canSpawn(world, reason) && lv2.canSpawn(world)) {
                  world.spawnEntityAndPassengers(lv2);
                  return Optional.of(lv2);
               }

               lv2.discard();
            }
         }
      }

      return Optional.empty();
   }

   private static boolean findSpawnPos(ServerWorld world, int verticalRange, BlockPos.Mutable pos, Requirements requirements) {
      BlockPos.Mutable lv = (new BlockPos.Mutable()).set(pos);
      BlockState lv2 = world.getBlockState(lv);

      for(int j = verticalRange; j >= -verticalRange; --j) {
         pos.move(Direction.DOWN);
         lv.set(pos, (Direction)Direction.UP);
         BlockState lv3 = world.getBlockState(pos);
         if (requirements.canSpawnOn(world, pos, lv3, lv, lv2)) {
            pos.move(Direction.UP);
            return true;
         }

         lv2 = lv3;
      }

      return false;
   }

   public interface Requirements {
      Requirements IRON_GOLEM = (world, pos, state, abovePos, aboveState) -> {
         return (aboveState.isAir() || aboveState.isLiquid()) && state.getMaterial().blocksLight();
      };
      Requirements WARDEN = (world, pos, state, abovePos, aboveState) -> {
         return aboveState.getCollisionShape(world, abovePos).isEmpty() && Block.isFaceFullSquare(state.getCollisionShape(world, pos), Direction.UP);
      };

      boolean canSpawnOn(ServerWorld world, BlockPos pos, BlockState state, BlockPos abovePos, BlockState aboveState);
   }
}
