package net.minecraft.block;

import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class CarvedPumpkinBlock extends HorizontalFacingBlock implements Equipment {
   public static final DirectionProperty FACING;
   @Nullable
   private BlockPattern snowGolemDispenserPattern;
   @Nullable
   private BlockPattern snowGolemPattern;
   @Nullable
   private BlockPattern ironGolemDispenserPattern;
   @Nullable
   private BlockPattern ironGolemPattern;
   private static final Predicate IS_GOLEM_HEAD_PREDICATE;

   protected CarvedPumpkinBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.trySpawnEntity(world, pos);
      }
   }

   public boolean canDispense(WorldView world, BlockPos pos) {
      return this.getSnowGolemDispenserPattern().searchAround(world, pos) != null || this.getIronGolemDispenserPattern().searchAround(world, pos) != null;
   }

   private void trySpawnEntity(World world, BlockPos pos) {
      BlockPattern.Result lv = this.getSnowGolemPattern().searchAround(world, pos);
      if (lv != null) {
         SnowGolemEntity lv2 = (SnowGolemEntity)EntityType.SNOW_GOLEM.create(world);
         if (lv2 != null) {
            spawnEntity(world, lv, lv2, lv.translate(0, 2, 0).getBlockPos());
         }
      } else {
         BlockPattern.Result lv3 = this.getIronGolemPattern().searchAround(world, pos);
         if (lv3 != null) {
            IronGolemEntity lv4 = (IronGolemEntity)EntityType.IRON_GOLEM.create(world);
            if (lv4 != null) {
               lv4.setPlayerCreated(true);
               spawnEntity(world, lv3, lv4, lv3.translate(1, 2, 0).getBlockPos());
            }
         }
      }

   }

   private static void spawnEntity(World world, BlockPattern.Result patternResult, Entity entity, BlockPos pos) {
      breakPatternBlocks(world, patternResult);
      entity.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY() + 0.05, (double)pos.getZ() + 0.5, 0.0F, 0.0F);
      world.spawnEntity(entity);
      Iterator var4 = world.getNonSpectatingEntities(ServerPlayerEntity.class, entity.getBoundingBox().expand(5.0)).iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
         Criteria.SUMMONED_ENTITY.trigger(lv, entity);
      }

      updatePatternBlocks(world, patternResult);
   }

   public static void breakPatternBlocks(World world, BlockPattern.Result patternResult) {
      for(int i = 0; i < patternResult.getWidth(); ++i) {
         for(int j = 0; j < patternResult.getHeight(); ++j) {
            CachedBlockPosition lv = patternResult.translate(i, j, 0);
            world.setBlockState(lv.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv.getBlockPos(), Block.getRawIdFromState(lv.getBlockState()));
         }
      }

   }

   public static void updatePatternBlocks(World world, BlockPattern.Result patternResult) {
      for(int i = 0; i < patternResult.getWidth(); ++i) {
         for(int j = 0; j < patternResult.getHeight(); ++j) {
            CachedBlockPosition lv = patternResult.translate(i, j, 0);
            world.updateNeighbors(lv.getBlockPos(), Blocks.AIR);
         }
      }

   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING);
   }

   private BlockPattern getSnowGolemDispenserPattern() {
      if (this.snowGolemDispenserPattern == null) {
         this.snowGolemDispenserPattern = BlockPatternBuilder.start().aisle(" ", "#", "#").where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
      }

      return this.snowGolemDispenserPattern;
   }

   private BlockPattern getSnowGolemPattern() {
      if (this.snowGolemPattern == null) {
         this.snowGolemPattern = BlockPatternBuilder.start().aisle("^", "#", "#").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
      }

      return this.snowGolemPattern;
   }

   private BlockPattern getIronGolemDispenserPattern() {
      if (this.ironGolemDispenserPattern == null) {
         this.ironGolemDispenserPattern = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', (pos) -> {
            return pos.getBlockState().isAir();
         }).build();
      }

      return this.ironGolemDispenserPattern;
   }

   private BlockPattern getIronGolemPattern() {
      if (this.ironGolemPattern == null) {
         this.ironGolemPattern = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE)).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', (pos) -> {
            return pos.getBlockState().isAir();
         }).build();
      }

      return this.ironGolemPattern;
   }

   public EquipmentSlot getSlotType() {
      return EquipmentSlot.HEAD;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      IS_GOLEM_HEAD_PREDICATE = (state) -> {
         return state != null && (state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN));
      };
   }
}
