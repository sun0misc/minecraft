package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class SculkCatalystBlock extends BlockWithEntity {
   public static final int BLOOM_DURATION = 8;
   public static final BooleanProperty BLOOM;
   private final IntProvider experience = ConstantIntProvider.create(5);

   public SculkCatalystBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(BLOOM, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(BLOOM);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(BLOOM)) {
         world.setBlockState(pos, (BlockState)state.with(BLOOM, false), Block.NOTIFY_ALL);
      }

   }

   public static void bloom(ServerWorld world, BlockPos pos, BlockState state, Random random) {
      world.setBlockState(pos, (BlockState)state.with(BLOOM, true), Block.NOTIFY_ALL);
      world.scheduleBlockTick(pos, state.getBlock(), 8);
      world.spawnParticles(ParticleTypes.SCULK_SOUL, (double)pos.getX() + 0.5, (double)pos.getY() + 1.15, (double)pos.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0);
      world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_SCULK_CATALYST_BLOOM, SoundCategory.BLOCKS, 2.0F, 0.6F + random.nextFloat() * 0.4F);
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SculkCatalystBlockEntity(pos, state);
   }

   @Nullable
   public GameEventListener getGameEventListener(ServerWorld world, BlockEntity blockEntity) {
      if (blockEntity instanceof SculkCatalystBlockEntity lv) {
         return lv;
      } else {
         return null;
      }
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return world.isClient ? null : checkType(type, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::tick);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.onStacksDropped(state, world, pos, tool, dropExperience);
      if (dropExperience) {
         this.dropExperienceWhenMined(world, pos, tool, this.experience);
      }

   }

   static {
      BLOOM = Properties.BLOOM;
   }
}
