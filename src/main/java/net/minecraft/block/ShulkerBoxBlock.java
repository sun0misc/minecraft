package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxBlock extends BlockWithEntity {
   private static final float field_41075 = 1.0F;
   private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
   private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
   private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
   private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
   private static final Map SIDES_SHAPES = (Map)Util.make(Maps.newEnumMap(Direction.class), (map) -> {
      map.put(Direction.NORTH, NORTH_SHAPE);
      map.put(Direction.EAST, EAST_SHAPE);
      map.put(Direction.SOUTH, SOUTH_SHAPE);
      map.put(Direction.WEST, WEST_SHAPE);
      map.put(Direction.UP, UP_SHAPE);
      map.put(Direction.DOWN, DOWN_SHAPE);
   });
   public static final EnumProperty FACING;
   public static final Identifier CONTENTS;
   @Nullable
   private final DyeColor color;

   public ShulkerBoxBlock(@Nullable DyeColor color, AbstractBlock.Settings settings) {
      super(settings);
      this.color = color;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP));
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new ShulkerBoxBlockEntity(this.color, pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(type, BlockEntityType.SHULKER_BOX, ShulkerBoxBlockEntity::tick);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else if (player.isSpectator()) {
         return ActionResult.CONSUME;
      } else {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity lv2 = (ShulkerBoxBlockEntity)lv;
            if (canOpen(state, world, pos, lv2)) {
               player.openHandledScreen(lv2);
               player.incrementStat(Stats.OPEN_SHULKER_BOX);
               PiglinBrain.onGuardedBlockInteracted(player, true);
            }

            return ActionResult.CONSUME;
         } else {
            return ActionResult.PASS;
         }
      }
   }

   private static boolean canOpen(BlockState state, World world, BlockPos pos, ShulkerBoxBlockEntity entity) {
      if (entity.getAnimationStage() != ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
         return true;
      } else {
         Box lv = ShulkerEntity.calculateBoundingBox((Direction)state.get(FACING), 0.0F, 0.5F).offset(pos).contract(1.0E-6);
         return world.isSpaceEmpty(lv);
      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getSide());
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING);
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof ShulkerBoxBlockEntity lv2) {
         if (!world.isClient && player.isCreative() && !lv2.isEmpty()) {
            ItemStack lv3 = getItemStack(this.getColor());
            lv.setStackNbt(lv3);
            if (lv2.hasCustomName()) {
               lv3.setCustomName(lv2.getCustomName());
            }

            ItemEntity lv4 = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, lv3);
            lv4.setToDefaultPickupDelay();
            world.spawnEntity(lv4);
         } else {
            lv2.checkLootInteraction(player);
         }
      }

      super.onBreak(world, pos, state, player);
   }

   public List getDroppedStacks(BlockState state, LootContext.Builder builder) {
      BlockEntity lv = (BlockEntity)builder.getNullable(LootContextParameters.BLOCK_ENTITY);
      if (lv instanceof ShulkerBoxBlockEntity lv2) {
         builder = builder.putDrop(CONTENTS, (context, consumer) -> {
            for(int i = 0; i < lv2.size(); ++i) {
               consumer.accept(lv2.getStack(i));
            }

         });
      }

      return super.getDroppedStacks(state, builder);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof ShulkerBoxBlockEntity) {
            ((ShulkerBoxBlockEntity)lv).setCustomName(itemStack.getName());
         }
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof ShulkerBoxBlockEntity) {
            world.updateComparators(pos, state.getBlock());
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public void appendTooltip(ItemStack stack, @Nullable BlockView world, List tooltip, TooltipContext options) {
      super.appendTooltip(stack, world, tooltip, options);
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      if (lv != null) {
         if (lv.contains("LootTable", NbtElement.STRING_TYPE)) {
            tooltip.add(Text.literal("???????"));
         }

         if (lv.contains("Items", NbtElement.LIST_TYPE)) {
            DefaultedList lv2 = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(lv, lv2);
            int i = 0;
            int j = 0;
            Iterator var9 = lv2.iterator();

            while(var9.hasNext()) {
               ItemStack lv3 = (ItemStack)var9.next();
               if (!lv3.isEmpty()) {
                  ++j;
                  if (i <= 4) {
                     ++i;
                     MutableText lv4 = lv3.getName().copy();
                     lv4.append(" x").append(String.valueOf(lv3.getCount()));
                     tooltip.add(lv4);
                  }
               }
            }

            if (j - i > 0) {
               tooltip.add(Text.translatable("container.shulkerBox.more", j - i).formatted(Formatting.ITALIC));
            }
         }
      }

   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof ShulkerBoxBlockEntity lv2) {
         if (!lv2.suffocates()) {
            return (VoxelShape)SIDES_SHAPES.get(((Direction)state.get(FACING)).getOpposite());
         }
      }

      return VoxelShapes.fullCube();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      BlockEntity lv = world.getBlockEntity(pos);
      return lv instanceof ShulkerBoxBlockEntity ? VoxelShapes.cuboid(((ShulkerBoxBlockEntity)lv).getBoundingBox(state)) : VoxelShapes.fullCube();
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return ScreenHandler.calculateComparatorOutput((Inventory)world.getBlockEntity(pos));
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      ItemStack lv = super.getPickStack(world, pos, state);
      world.getBlockEntity(pos, BlockEntityType.SHULKER_BOX).ifPresent((blockEntity) -> {
         blockEntity.setStackNbt(lv);
      });
      return lv;
   }

   @Nullable
   public static DyeColor getColor(Item item) {
      return getColor(Block.getBlockFromItem(item));
   }

   @Nullable
   public static DyeColor getColor(Block block) {
      return block instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock)block).getColor() : null;
   }

   public static Block get(@Nullable DyeColor dyeColor) {
      if (dyeColor == null) {
         return Blocks.SHULKER_BOX;
      } else {
         switch (dyeColor) {
            case WHITE:
               return Blocks.WHITE_SHULKER_BOX;
            case ORANGE:
               return Blocks.ORANGE_SHULKER_BOX;
            case MAGENTA:
               return Blocks.MAGENTA_SHULKER_BOX;
            case LIGHT_BLUE:
               return Blocks.LIGHT_BLUE_SHULKER_BOX;
            case YELLOW:
               return Blocks.YELLOW_SHULKER_BOX;
            case LIME:
               return Blocks.LIME_SHULKER_BOX;
            case PINK:
               return Blocks.PINK_SHULKER_BOX;
            case GRAY:
               return Blocks.GRAY_SHULKER_BOX;
            case LIGHT_GRAY:
               return Blocks.LIGHT_GRAY_SHULKER_BOX;
            case CYAN:
               return Blocks.CYAN_SHULKER_BOX;
            case PURPLE:
            default:
               return Blocks.PURPLE_SHULKER_BOX;
            case BLUE:
               return Blocks.BLUE_SHULKER_BOX;
            case BROWN:
               return Blocks.BROWN_SHULKER_BOX;
            case GREEN:
               return Blocks.GREEN_SHULKER_BOX;
            case RED:
               return Blocks.RED_SHULKER_BOX;
            case BLACK:
               return Blocks.BLACK_SHULKER_BOX;
         }
      }
   }

   @Nullable
   public DyeColor getColor() {
      return this.color;
   }

   public static ItemStack getItemStack(@Nullable DyeColor color) {
      return new ItemStack(get(color));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   static {
      FACING = FacingBlock.FACING;
      CONTENTS = new Identifier("contents");
   }
}
