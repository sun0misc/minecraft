package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WitherSkullBlock extends SkullBlock {
   @Nullable
   private static BlockPattern witherBossPattern;
   @Nullable
   private static BlockPattern witherDispenserPattern;

   protected WitherSkullBlock(AbstractBlock.Settings arg) {
      super(SkullBlock.Type.WITHER_SKELETON, arg);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof SkullBlockEntity) {
         onPlaced(world, pos, (SkullBlockEntity)lv);
      }

   }

   public static void onPlaced(World world, BlockPos pos, SkullBlockEntity blockEntity) {
      if (!world.isClient) {
         BlockState lv = blockEntity.getCachedState();
         boolean bl = lv.isOf(Blocks.WITHER_SKELETON_SKULL) || lv.isOf(Blocks.WITHER_SKELETON_WALL_SKULL);
         if (bl && pos.getY() >= world.getBottomY() && world.getDifficulty() != Difficulty.PEACEFUL) {
            BlockPattern.Result lv2 = getWitherBossPattern().searchAround(world, pos);
            if (lv2 != null) {
               WitherEntity lv3 = (WitherEntity)EntityType.WITHER.create(world);
               if (lv3 != null) {
                  CarvedPumpkinBlock.breakPatternBlocks(world, lv2);
                  BlockPos lv4 = lv2.translate(1, 2, 0).getBlockPos();
                  lv3.refreshPositionAndAngles((double)lv4.getX() + 0.5, (double)lv4.getY() + 0.55, (double)lv4.getZ() + 0.5, lv2.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                  lv3.bodyYaw = lv2.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                  lv3.onSummoned();
                  Iterator var8 = world.getNonSpectatingEntities(ServerPlayerEntity.class, lv3.getBoundingBox().expand(50.0)).iterator();

                  while(var8.hasNext()) {
                     ServerPlayerEntity lv5 = (ServerPlayerEntity)var8.next();
                     Criteria.SUMMONED_ENTITY.trigger(lv5, lv3);
                  }

                  world.spawnEntity(lv3);
                  CarvedPumpkinBlock.updatePatternBlocks(world, lv2);
               }

            }
         }
      }
   }

   public static boolean canDispense(World world, BlockPos pos, ItemStack stack) {
      if (stack.isOf(Items.WITHER_SKELETON_SKULL) && pos.getY() >= world.getBottomY() + 2 && world.getDifficulty() != Difficulty.PEACEFUL && !world.isClient) {
         return getWitherDispenserPattern().searchAround(world, pos) != null;
      } else {
         return false;
      }
   }

   private static BlockPattern getWitherBossPattern() {
      if (witherBossPattern == null) {
         witherBossPattern = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', (pos) -> {
            return pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
         }).where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', (pos) -> {
            return pos.getBlockState().isAir();
         }).build();
      }

      return witherBossPattern;
   }

   private static BlockPattern getWitherDispenserPattern() {
      if (witherDispenserPattern == null) {
         witherDispenserPattern = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', (pos) -> {
            return pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
         }).where('~', (pos) -> {
            return pos.getBlockState().isAir();
         }).build();
      }

      return witherDispenserPattern;
   }
}
