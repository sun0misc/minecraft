package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ChiseledBookshelfBlock extends BlockWithEntity {
   private static final int MAX_BOOK_COUNT = 6;
   public static final int BOOK_HEIGHT = 3;
   public static final List SLOT_OCCUPIED_PROPERTIES;

   public ChiseledBookshelfBlock(AbstractBlock.Settings arg) {
      super(arg);
      BlockState lv = (BlockState)((BlockState)this.stateManager.getDefaultState()).with(HorizontalFacingBlock.FACING, Direction.NORTH);

      BooleanProperty lv2;
      for(Iterator var3 = SLOT_OCCUPIED_PROPERTIES.iterator(); var3.hasNext(); lv = (BlockState)lv.with(lv2, false)) {
         lv2 = (BooleanProperty)var3.next();
      }

      this.setDefaultState(lv);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity var8 = world.getBlockEntity(pos);
      if (var8 instanceof ChiseledBookshelfBlockEntity lv) {
         Optional optional = getHitPos(hit, (Direction)state.get(HorizontalFacingBlock.FACING));
         if (optional.isEmpty()) {
            return ActionResult.PASS;
         } else {
            int i = getSlotForHitPos((Vec2f)optional.get());
            if ((Boolean)state.get((Property)SLOT_OCCUPIED_PROPERTIES.get(i))) {
               tryRemoveBook(world, pos, player, lv, i);
               return ActionResult.success(world.isClient);
            } else {
               ItemStack lv2 = player.getStackInHand(hand);
               if (lv2.isIn(ItemTags.BOOKSHELF_BOOKS)) {
                  tryAddBook(world, pos, player, lv, lv2, i);
                  return ActionResult.success(world.isClient);
               } else {
                  return ActionResult.CONSUME;
               }
            }
         }
      } else {
         return ActionResult.PASS;
      }
   }

   private static Optional getHitPos(BlockHitResult hit, Direction facing) {
      Direction lv = hit.getSide();
      if (facing != lv) {
         return Optional.empty();
      } else {
         BlockPos lv2 = hit.getBlockPos().offset(lv);
         Vec3d lv3 = hit.getPos().subtract((double)lv2.getX(), (double)lv2.getY(), (double)lv2.getZ());
         double d = lv3.getX();
         double e = lv3.getY();
         double f = lv3.getZ();
         Optional var10000;
         switch (lv) {
            case NORTH:
               var10000 = Optional.of(new Vec2f((float)(1.0 - d), (float)e));
               break;
            case SOUTH:
               var10000 = Optional.of(new Vec2f((float)d, (float)e));
               break;
            case WEST:
               var10000 = Optional.of(new Vec2f((float)f, (float)e));
               break;
            case EAST:
               var10000 = Optional.of(new Vec2f((float)(1.0 - f), (float)e));
               break;
            case DOWN:
            case UP:
               var10000 = Optional.empty();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }

   private static int getSlotForHitPos(Vec2f hitPos) {
      int i = hitPos.y >= 0.5F ? 0 : 1;
      int j = getColumn(hitPos.x);
      return j + i * 3;
   }

   private static int getColumn(float x) {
      float g = 0.0625F;
      float h = 0.375F;
      if (x < 0.375F) {
         return 0;
      } else {
         float i = 0.6875F;
         return x < 0.6875F ? 1 : 2;
      }
   }

   private static void tryAddBook(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity, ItemStack stack, int slot) {
      if (!world.isClient) {
         player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
         SoundEvent lv = stack.isOf(Items.ENCHANTED_BOOK) ? SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT;
         blockEntity.setStack(slot, stack.split(1));
         world.playSound((PlayerEntity)null, pos, lv, SoundCategory.BLOCKS, 1.0F, 1.0F);
         if (player.isCreative()) {
            stack.increment(1);
         }

         world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
      }
   }

   private static void tryRemoveBook(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity, int slot) {
      if (!world.isClient) {
         ItemStack lv = blockEntity.removeStack(slot, 1);
         SoundEvent lv2 = lv.isOf(Items.ENCHANTED_BOOK) ? SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP;
         world.playSound((PlayerEntity)null, pos, lv2, SoundCategory.BLOCKS, 1.0F, 1.0F);
         if (!player.getInventory().insertStack(lv)) {
            player.dropItem(lv, false);
         }

         world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
      }
   }

   public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new ChiseledBookshelfBlockEntity(pos, state);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HorizontalFacingBlock.FACING);
      List var10000 = SLOT_OCCUPIED_PROPERTIES;
      Objects.requireNonNull(builder);
      var10000.forEach((property) -> {
         builder.add(property);
      });
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof ChiseledBookshelfBlockEntity) {
            ChiseledBookshelfBlockEntity lv2 = (ChiseledBookshelfBlockEntity)lv;
            if (!lv2.isEmpty()) {
               for(int i = 0; i < 6; ++i) {
                  ItemStack lv3 = lv2.getStack(i);
                  if (!lv3.isEmpty()) {
                     ItemScatterer.spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), lv3);
                  }
               }

               lv2.clear();
               world.updateComparators(pos, this);
            }
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(HorizontalFacingBlock.FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      if (world.isClient()) {
         return 0;
      } else {
         BlockEntity var5 = world.getBlockEntity(pos);
         if (var5 instanceof ChiseledBookshelfBlockEntity) {
            ChiseledBookshelfBlockEntity lv = (ChiseledBookshelfBlockEntity)var5;
            return lv.getLastInteractedSlot() + 1;
         } else {
            return 0;
         }
      }
   }

   static {
      SLOT_OCCUPIED_PROPERTIES = List.of(Properties.SLOT_0_OCCUPIED, Properties.SLOT_1_OCCUPIED, Properties.SLOT_2_OCCUPIED, Properties.SLOT_3_OCCUPIED, Properties.SLOT_4_OCCUPIED, Properties.SLOT_5_OCCUPIED);
   }
}
