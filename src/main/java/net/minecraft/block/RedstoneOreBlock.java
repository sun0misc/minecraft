package net.minecraft.block;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class RedstoneOreBlock extends Block {
   public static final BooleanProperty LIT;

   public RedstoneOreBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)this.getDefaultState().with(LIT, false));
   }

   public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
      light(state, world, pos);
      super.onBlockBreakStart(state, world, pos, player);
   }

   public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
      if (!entity.bypassesSteppingEffects()) {
         light(state, world, pos);
      }

      super.onSteppedOn(world, pos, state, entity);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         spawnParticles(world, pos);
      } else {
         light(state, world, pos);
      }

      ItemStack lv = player.getStackInHand(hand);
      return lv.getItem() instanceof BlockItem && (new ItemPlacementContext(player, hand, lv, hit)).canPlace() ? ActionResult.PASS : ActionResult.SUCCESS;
   }

   private static void light(BlockState state, World world, BlockPos pos) {
      spawnParticles(world, pos);
      if (!(Boolean)state.get(LIT)) {
         world.setBlockState(pos, (BlockState)state.with(LIT, true), Block.NOTIFY_ALL);
      }

   }

   public boolean hasRandomTicks(BlockState state) {
      return (Boolean)state.get(LIT);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         world.setBlockState(pos, (BlockState)state.with(LIT, false), Block.NOTIFY_ALL);
      }

   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.onStacksDropped(state, world, pos, tool, dropExperience);
      if (dropExperience && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
         int i = 1 + world.random.nextInt(5);
         this.dropExperience(world, pos, i);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         spawnParticles(world, pos);
      }

   }

   private static void spawnParticles(World world, BlockPos pos) {
      double d = 0.5625;
      Random lv = world.random;
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv2 = var5[var7];
         BlockPos lv3 = pos.offset(lv2);
         if (!world.getBlockState(lv3).isOpaqueFullCube(world, lv3)) {
            Direction.Axis lv4 = lv2.getAxis();
            double e = lv4 == Direction.Axis.X ? 0.5 + 0.5625 * (double)lv2.getOffsetX() : (double)lv.nextFloat();
            double f = lv4 == Direction.Axis.Y ? 0.5 + 0.5625 * (double)lv2.getOffsetY() : (double)lv.nextFloat();
            double g = lv4 == Direction.Axis.Z ? 0.5 + 0.5625 * (double)lv2.getOffsetZ() : (double)lv.nextFloat();
            world.addParticle(DustParticleEffect.DEFAULT, (double)pos.getX() + e, (double)pos.getY() + f, (double)pos.getZ() + g, 0.0, 0.0, 0.0);
         }
      }

   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LIT);
   }

   static {
      LIT = RedstoneTorchBlock.LIT;
   }
}
