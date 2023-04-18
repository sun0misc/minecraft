package net.minecraft.block;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;

public class PowderSnowBlock extends Block implements FluidDrainable {
   private static final float field_31216 = 0.083333336F;
   private static final float HORIZONTAL_MOVEMENT_MULTIPLIER = 0.9F;
   private static final float VERTICAL_MOVEMENT_MULTIPLIER = 1.5F;
   private static final float field_31219 = 2.5F;
   private static final VoxelShape FALLING_SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.8999999761581421, 1.0);
   private static final double field_36189 = 4.0;
   private static final double SMALL_FALL_SOUND_MAX_DISTANCE = 7.0;

   public PowderSnowBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
      return stateFrom.isOf(this) ? true : super.isSideInvisible(state, stateFrom, direction);
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return VoxelShapes.empty();
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!(entity instanceof LivingEntity) || entity.getBlockStateAtPos().isOf(this)) {
         entity.slowMovement(state, new Vec3d(0.8999999761581421, 1.5, 0.8999999761581421));
         if (world.isClient) {
            Random lv = world.getRandom();
            boolean bl = entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ();
            if (bl && lv.nextBoolean()) {
               world.addParticle(ParticleTypes.SNOWFLAKE, entity.getX(), (double)(pos.getY() + 1), entity.getZ(), (double)(MathHelper.nextBetween(lv, -1.0F, 1.0F) * 0.083333336F), 0.05000000074505806, (double)(MathHelper.nextBetween(lv, -1.0F, 1.0F) * 0.083333336F));
            }
         }
      }

      entity.setInPowderSnow(true);
      if (!world.isClient) {
         if (entity.isOnFire() && (world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) || entity instanceof PlayerEntity) && entity.canModifyAt(world, pos)) {
            world.breakBlock(pos, false);
         }

         entity.setOnFire(false);
      }

   }

   public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
      if (!((double)fallDistance < 4.0) && entity instanceof LivingEntity lv) {
         LivingEntity.FallSounds lv2 = lv.getFallSounds();
         SoundEvent lv3 = (double)fallDistance < 7.0 ? lv2.small() : lv2.big();
         entity.playSound(lv3, 1.0F, 1.0F);
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if (context instanceof EntityShapeContext lv) {
         Entity lv2 = lv.getEntity();
         if (lv2 != null) {
            if (lv2.fallDistance > 2.5F) {
               return FALLING_SHAPE;
            }

            boolean bl = lv2 instanceof FallingBlockEntity;
            if (bl || canWalkOnPowderSnow(lv2) && context.isAbove(VoxelShapes.fullCube(), pos, false) && !context.isDescending()) {
               return super.getCollisionShape(state, world, pos, context);
            }
         }
      }

      return VoxelShapes.empty();
   }

   public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   public static boolean canWalkOnPowderSnow(Entity entity) {
      if (entity.getType().isIn(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
         return true;
      } else {
         return entity instanceof LivingEntity ? ((LivingEntity)entity).getEquippedStack(EquipmentSlot.FEET).isOf(Items.LEATHER_BOOTS) : false;
      }
   }

   public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
      if (!world.isClient()) {
         world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
      }

      return new ItemStack(Items.POWDER_SNOW_BUCKET);
   }

   public Optional getBucketFillSound() {
      return Optional.of(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return true;
   }
}
