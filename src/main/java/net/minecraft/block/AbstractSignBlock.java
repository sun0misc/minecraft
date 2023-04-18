package net.minecraft.block;

import java.util.Arrays;
import java.util.UUID;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignChangingItem;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSignBlock extends BlockWithEntity implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;
   protected static final float field_31243 = 4.0F;
   protected static final VoxelShape SHAPE;
   private final WoodType type;

   protected AbstractSignBlock(AbstractBlock.Settings settings, WoodType type) {
      super(settings);
      this.type = type;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public boolean canMobSpawnInside(BlockState state) {
      return true;
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SignBlockEntity(pos, state);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack lv = player.getStackInHand(hand);
      Item lv2 = lv.getItem();
      Item var11 = lv.getItem();
      SignChangingItem var10000;
      if (var11 instanceof SignChangingItem lv3) {
         var10000 = lv3;
      } else {
         var10000 = null;
      }

      SignChangingItem lv4 = var10000;
      boolean bl = lv4 != null && player.canModifyBlocks();
      BlockEntity var12 = world.getBlockEntity(pos);
      if (var12 instanceof SignBlockEntity lv5) {
         if (!world.isClient) {
            boolean bl2 = lv5.isPlayerFacingFront(player);
            SignText lv6 = lv5.getText(bl2);
            boolean bl3 = lv5.runCommandClickEvent(player, world, pos, bl2);
            if (lv5.isWaxed()) {
               world.playSound((PlayerEntity)null, lv5.getPos(), SoundEvents.BLOCK_SIGN_WAXED_INTERACT_FAIL, SoundCategory.BLOCKS);
               return ActionResult.PASS;
            } else if (bl && !this.isOtherPlayerEditing(player, lv5) && lv4.canUseOnSignText(lv6, player) && lv4.useOnSign(world, lv5, bl2, player)) {
               if (!player.isCreative()) {
                  lv.decrement(1);
               }

               world.emitGameEvent(GameEvent.BLOCK_CHANGE, lv5.getPos(), GameEvent.Emitter.of(player, lv5.getCachedState()));
               player.incrementStat(Stats.USED.getOrCreateStat(lv2));
               return ActionResult.SUCCESS;
            } else if (bl3) {
               return ActionResult.SUCCESS;
            } else if (!this.isOtherPlayerEditing(player, lv5) && player.canModifyBlocks() && this.isTextLiteralOrEmpty(player, lv5, bl2)) {
               this.openEditScreen(player, lv5, bl2);
               return ActionResult.SUCCESS;
            } else {
               return ActionResult.PASS;
            }
         } else {
            return !bl && !lv5.isWaxed() ? ActionResult.CONSUME : ActionResult.SUCCESS;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   private boolean isTextLiteralOrEmpty(PlayerEntity player, SignBlockEntity blockEntity, boolean front) {
      SignText lv = blockEntity.getText(front);
      return Arrays.stream(lv.getMessages(player.shouldFilterText())).allMatch((message) -> {
         return message.equals(ScreenTexts.EMPTY) || message.getContent() instanceof LiteralTextContent;
      });
   }

   public abstract float getRotationDegrees(BlockState state);

   public Vec3d getCenter(BlockState state) {
      return new Vec3d(0.5, 0.5, 0.5);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public WoodType getWoodType() {
      return this.type;
   }

   public static WoodType getWoodType(Block block) {
      WoodType lv;
      if (block instanceof AbstractSignBlock) {
         lv = ((AbstractSignBlock)block).getWoodType();
      } else {
         lv = WoodType.OAK;
      }

      return lv;
   }

   public void openEditScreen(PlayerEntity player, SignBlockEntity blockEntity, boolean front) {
      blockEntity.setEditor(player.getUuid());
      player.openEditSignScreen(blockEntity, front);
   }

   private boolean isOtherPlayerEditing(PlayerEntity player, SignBlockEntity blockEntity) {
      UUID uUID = blockEntity.getEditor();
      return uUID != null && !uUID.equals(player.getUuid());
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(type, BlockEntityType.SIGN, SignBlockEntity::tick);
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
   }
}
