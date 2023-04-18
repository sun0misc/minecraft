package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ChorusFlowerBlock extends Block {
   public static final int MAX_AGE = 5;
   public static final IntProperty AGE;
   private final ChorusPlantBlock plantBlock;

   protected ChorusFlowerBlock(ChorusPlantBlock plantBlock, AbstractBlock.Settings settings) {
      super(settings);
      this.plantBlock = plantBlock;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   public boolean hasRandomTicks(BlockState state) {
      return (Integer)state.get(AGE) < 5;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockPos lv = pos.up();
      if (world.isAir(lv) && lv.getY() < world.getTopY()) {
         int i = (Integer)state.get(AGE);
         if (i < 5) {
            boolean bl = false;
            boolean bl2 = false;
            BlockState lv2 = world.getBlockState(pos.down());
            int j;
            if (lv2.isOf(Blocks.END_STONE)) {
               bl = true;
            } else if (lv2.isOf(this.plantBlock)) {
               j = 1;

               for(int k = 0; k < 4; ++k) {
                  BlockState lv3 = world.getBlockState(pos.down(j + 1));
                  if (!lv3.isOf(this.plantBlock)) {
                     if (lv3.isOf(Blocks.END_STONE)) {
                        bl2 = true;
                     }
                     break;
                  }

                  ++j;
               }

               if (j < 2 || j <= random.nextInt(bl2 ? 5 : 4)) {
                  bl = true;
               }
            } else if (lv2.isAir()) {
               bl = true;
            }

            if (bl && isSurroundedByAir(world, lv, (Direction)null) && world.isAir(pos.up(2))) {
               world.setBlockState(pos, this.plantBlock.withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
               this.grow(world, lv, i);
            } else if (i < 4) {
               j = random.nextInt(4);
               if (bl2) {
                  ++j;
               }

               boolean bl3 = false;

               for(int l = 0; l < j; ++l) {
                  Direction lv4 = Direction.Type.HORIZONTAL.random(random);
                  BlockPos lv5 = pos.offset(lv4);
                  if (world.isAir(lv5) && world.isAir(lv5.down()) && isSurroundedByAir(world, lv5, lv4.getOpposite())) {
                     this.grow(world, lv5, i + 1);
                     bl3 = true;
                  }
               }

               if (bl3) {
                  world.setBlockState(pos, this.plantBlock.withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
               } else {
                  this.die(world, pos);
               }
            } else {
               this.die(world, pos);
            }

         }
      }
   }

   private void grow(World world, BlockPos pos, int age) {
      world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, age), Block.NOTIFY_LISTENERS);
      world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_GROWS, pos, 0);
   }

   private void die(World world, BlockPos pos) {
      world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
      world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_DIES, pos, 0);
   }

   private static boolean isSurroundedByAir(WorldView world, BlockPos pos, @Nullable Direction exceptDirection) {
      Iterator var3 = Direction.Type.HORIZONTAL.iterator();

      Direction lv;
      do {
         if (!var3.hasNext()) {
            return true;
         }

         lv = (Direction)var3.next();
      } while(lv == exceptDirection || world.isAir(pos.offset(lv)));

      return false;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction != Direction.UP && !state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.down());
      if (!lv.isOf(this.plantBlock) && !lv.isOf(Blocks.END_STONE)) {
         if (!lv.isAir()) {
            return false;
         } else {
            boolean bl = false;
            Iterator var6 = Direction.Type.HORIZONTAL.iterator();

            while(var6.hasNext()) {
               Direction lv2 = (Direction)var6.next();
               BlockState lv3 = world.getBlockState(pos.offset(lv2));
               if (lv3.isOf(this.plantBlock)) {
                  if (bl) {
                     return false;
                  }

                  bl = true;
               } else if (!lv3.isAir()) {
                  return false;
               }
            }

            return bl;
         }
      } else {
         return true;
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AGE);
   }

   public static void generate(WorldAccess world, BlockPos pos, Random random, int size) {
      world.setBlockState(pos, ((ChorusPlantBlock)Blocks.CHORUS_PLANT).withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
      generate(world, pos, random, pos, size, 0);
   }

   private static void generate(WorldAccess world, BlockPos pos, Random random, BlockPos rootPos, int size, int layer) {
      ChorusPlantBlock lv = (ChorusPlantBlock)Blocks.CHORUS_PLANT;
      int k = random.nextInt(4) + 1;
      if (layer == 0) {
         ++k;
      }

      for(int l = 0; l < k; ++l) {
         BlockPos lv2 = pos.up(l + 1);
         if (!isSurroundedByAir(world, lv2, (Direction)null)) {
            return;
         }

         world.setBlockState(lv2, lv.withConnectionProperties(world, lv2), Block.NOTIFY_LISTENERS);
         world.setBlockState(lv2.down(), lv.withConnectionProperties(world, lv2.down()), Block.NOTIFY_LISTENERS);
      }

      boolean bl = false;
      if (layer < 4) {
         int m = random.nextInt(4);
         if (layer == 0) {
            ++m;
         }

         for(int n = 0; n < m; ++n) {
            Direction lv3 = Direction.Type.HORIZONTAL.random(random);
            BlockPos lv4 = pos.up(k).offset(lv3);
            if (Math.abs(lv4.getX() - rootPos.getX()) < size && Math.abs(lv4.getZ() - rootPos.getZ()) < size && world.isAir(lv4) && world.isAir(lv4.down()) && isSurroundedByAir(world, lv4, lv3.getOpposite())) {
               bl = true;
               world.setBlockState(lv4, lv.withConnectionProperties(world, lv4), Block.NOTIFY_LISTENERS);
               world.setBlockState(lv4.offset(lv3.getOpposite()), lv.withConnectionProperties(world, lv4.offset(lv3.getOpposite())), Block.NOTIFY_LISTENERS);
               generate(world, lv4, random, rootPos, size, layer + 1);
            }
         }
      }

      if (!bl) {
         world.setBlockState(pos.up(k), (BlockState)Blocks.CHORUS_FLOWER.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
      }

   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      BlockPos lv = hit.getBlockPos();
      if (!world.isClient && projectile.canModifyAt(world, lv) && projectile.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES)) {
         world.breakBlock(lv, true, projectile);
      }

   }

   static {
      AGE = Properties.AGE_5;
   }
}
