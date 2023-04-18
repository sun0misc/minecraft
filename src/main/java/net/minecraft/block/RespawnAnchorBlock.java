package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class RespawnAnchorBlock extends Block {
   public static final int NO_CHARGES = 0;
   public static final int MAX_CHARGES = 4;
   public static final IntProperty CHARGES;
   private static final ImmutableList VALID_HORIZONTAL_SPAWN_OFFSETS;
   private static final ImmutableList VALID_SPAWN_OFFSETS;

   public RespawnAnchorBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(CHARGES, 0));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack lv = player.getStackInHand(hand);
      if (hand == Hand.MAIN_HAND && !isChargeItem(lv) && isChargeItem(player.getStackInHand(Hand.OFF_HAND))) {
         return ActionResult.PASS;
      } else if (isChargeItem(lv) && canCharge(state)) {
         charge(player, world, pos, state);
         if (!player.getAbilities().creativeMode) {
            lv.decrement(1);
         }

         return ActionResult.success(world.isClient);
      } else if ((Integer)state.get(CHARGES) == 0) {
         return ActionResult.PASS;
      } else if (!isNether(world)) {
         if (!world.isClient) {
            this.explode(state, world, pos);
         }

         return ActionResult.success(world.isClient);
      } else {
         if (!world.isClient) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)player;
            if (lv2.getSpawnPointDimension() != world.getRegistryKey() || !pos.equals(lv2.getSpawnPointPosition())) {
               lv2.setSpawnPoint(world.getRegistryKey(), pos, 0.0F, false, true);
               world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
               return ActionResult.SUCCESS;
            }
         }

         return ActionResult.CONSUME;
      }
   }

   private static boolean isChargeItem(ItemStack stack) {
      return stack.isOf(Items.GLOWSTONE);
   }

   private static boolean canCharge(BlockState state) {
      return (Integer)state.get(CHARGES) < 4;
   }

   private static boolean hasStillWater(BlockPos pos, World world) {
      FluidState lv = world.getFluidState(pos);
      if (!lv.isIn(FluidTags.WATER)) {
         return false;
      } else if (lv.isStill()) {
         return true;
      } else {
         float f = (float)lv.getLevel();
         if (f < 2.0F) {
            return false;
         } else {
            FluidState lv2 = world.getFluidState(pos.down());
            return !lv2.isIn(FluidTags.WATER);
         }
      }
   }

   private void explode(BlockState state, World world, final BlockPos explodedPos) {
      world.removeBlock(explodedPos, false);
      Stream var10000 = Direction.Type.HORIZONTAL.stream();
      Objects.requireNonNull(explodedPos);
      boolean bl = var10000.map(explodedPos::offset).anyMatch((pos) -> {
         return hasStillWater(pos, world);
      });
      final boolean bl2 = bl || world.getFluidState(explodedPos.up()).isIn(FluidTags.WATER);
      ExplosionBehavior lv = new ExplosionBehavior() {
         public Optional getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            return pos.equals(explodedPos) && bl2 ? Optional.of(Blocks.WATER.getBlastResistance()) : super.getBlastResistance(explosion, world, pos, blockState, fluidState);
         }
      };
      Vec3d lv2 = explodedPos.toCenterPos();
      world.createExplosion((Entity)null, world.getDamageSources().badRespawnPoint(lv2), lv, lv2, 5.0F, true, World.ExplosionSourceType.BLOCK);
   }

   public static boolean isNether(World world) {
      return world.getDimension().respawnAnchorWorks();
   }

   public static void charge(@Nullable Entity charger, World world, BlockPos pos, BlockState state) {
      BlockState lv = (BlockState)state.with(CHARGES, (Integer)state.get(CHARGES) + 1);
      world.setBlockState(pos, lv, Block.NOTIFY_ALL);
      world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(charger, lv));
      world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Integer)state.get(CHARGES) != 0) {
         if (random.nextInt(100) == 0) {
            world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         double d = (double)pos.getX() + 0.5 + (0.5 - random.nextDouble());
         double e = (double)pos.getY() + 1.0;
         double f = (double)pos.getZ() + 0.5 + (0.5 - random.nextDouble());
         double g = (double)random.nextFloat() * 0.04;
         world.addParticle(ParticleTypes.REVERSE_PORTAL, d, e, f, 0.0, g, 0.0);
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(CHARGES);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public static int getLightLevel(BlockState state, int maxLevel) {
      return MathHelper.floor((float)((Integer)state.get(CHARGES) - 0) / 4.0F * (float)maxLevel);
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return getLightLevel(state, 15);
   }

   public static Optional findRespawnPosition(EntityType entity, CollisionView world, BlockPos pos) {
      Optional optional = findRespawnPosition(entity, world, pos, true);
      return optional.isPresent() ? optional : findRespawnPosition(entity, world, pos, false);
   }

   private static Optional findRespawnPosition(EntityType entity, CollisionView world, BlockPos pos, boolean ignoreInvalidPos) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      UnmodifiableIterator var5 = VALID_SPAWN_OFFSETS.iterator();

      Vec3d lv3;
      do {
         if (!var5.hasNext()) {
            return Optional.empty();
         }

         Vec3i lv2 = (Vec3i)var5.next();
         lv.set(pos).move(lv2);
         lv3 = Dismounting.findRespawnPos(entity, world, lv, ignoreInvalidPos);
      } while(lv3 == null);

      return Optional.of(lv3);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      CHARGES = Properties.CHARGES;
      VALID_HORIZONTAL_SPAWN_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
      VALID_SPAWN_OFFSETS = (new ImmutableList.Builder()).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::down).iterator()).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::up).iterator()).add(new Vec3i(0, 1, 0)).build();
   }
}
