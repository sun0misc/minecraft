package net.minecraft.block;

import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class TntBlock extends Block {
   public static final BooleanProperty UNSTABLE;

   public TntBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)this.getDefaultState().with(UNSTABLE, false));
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         if (world.isReceivingRedstonePower(pos)) {
            primeTnt(world, pos);
            world.removeBlock(pos, false);
         }

      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (world.isReceivingRedstonePower(pos)) {
         primeTnt(world, pos);
         world.removeBlock(pos, false);
      }

   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient() && !player.isCreative() && (Boolean)state.get(UNSTABLE)) {
         primeTnt(world, pos);
      }

      super.onBreak(world, pos, state, player);
   }

   public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
      if (!world.isClient) {
         TntEntity lv = new TntEntity(world, (double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, explosion.getCausingEntity());
         int i = lv.getFuse();
         lv.setFuse((short)(world.random.nextInt(i / 4) + i / 8));
         world.spawnEntity(lv);
      }
   }

   public static void primeTnt(World world, BlockPos pos) {
      primeTnt(world, pos, (LivingEntity)null);
   }

   private static void primeTnt(World world, BlockPos pos, @Nullable LivingEntity igniter) {
      if (!world.isClient) {
         TntEntity lv = new TntEntity(world, (double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, igniter);
         world.spawnEntity(lv);
         world.playSound((PlayerEntity)null, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack lv = player.getStackInHand(hand);
      if (!lv.isOf(Items.FLINT_AND_STEEL) && !lv.isOf(Items.FIRE_CHARGE)) {
         return super.onUse(state, world, pos, player, hand, hit);
      } else {
         primeTnt(world, pos, player);
         world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
         Item lv2 = lv.getItem();
         if (!player.isCreative()) {
            if (lv.isOf(Items.FLINT_AND_STEEL)) {
               lv.damage(1, (LivingEntity)player, (Consumer)((playerx) -> {
                  playerx.sendToolBreakStatus(hand);
               }));
            } else {
               lv.decrement(1);
            }
         }

         player.incrementStat(Stats.USED.getOrCreateStat(lv2));
         return ActionResult.success(world.isClient);
      }
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      if (!world.isClient) {
         BlockPos lv = hit.getBlockPos();
         Entity lv2 = projectile.getOwner();
         if (projectile.isOnFire() && projectile.canModifyAt(world, lv)) {
            primeTnt(world, lv, lv2 instanceof LivingEntity ? (LivingEntity)lv2 : null);
            world.removeBlock(lv, false);
         }
      }

   }

   public boolean shouldDropItemsOnExplosion(Explosion explosion) {
      return false;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(UNSTABLE);
   }

   static {
      UNSTABLE = Properties.UNSTABLE;
   }
}
