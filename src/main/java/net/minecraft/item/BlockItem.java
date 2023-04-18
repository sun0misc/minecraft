package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BlockItem extends Item {
   public static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";
   public static final String BLOCK_STATE_TAG_KEY = "BlockStateTag";
   /** @deprecated */
   @Deprecated
   private final Block block;

   public BlockItem(Block block, Item.Settings settings) {
      super(settings);
      this.block = block;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      ActionResult lv = this.place(new ItemPlacementContext(context));
      if (!lv.isAccepted() && this.isFood()) {
         ActionResult lv2 = this.use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
         return lv2 == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : lv2;
      } else {
         return lv;
      }
   }

   public ActionResult place(ItemPlacementContext context) {
      if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
         return ActionResult.FAIL;
      } else if (!context.canPlace()) {
         return ActionResult.FAIL;
      } else {
         ItemPlacementContext lv = this.getPlacementContext(context);
         if (lv == null) {
            return ActionResult.FAIL;
         } else {
            BlockState lv2 = this.getPlacementState(lv);
            if (lv2 == null) {
               return ActionResult.FAIL;
            } else if (!this.place(lv, lv2)) {
               return ActionResult.FAIL;
            } else {
               BlockPos lv3 = lv.getBlockPos();
               World lv4 = lv.getWorld();
               PlayerEntity lv5 = lv.getPlayer();
               ItemStack lv6 = lv.getStack();
               BlockState lv7 = lv4.getBlockState(lv3);
               if (lv7.isOf(lv2.getBlock())) {
                  lv7 = this.placeFromNbt(lv3, lv4, lv6, lv7);
                  this.postPlacement(lv3, lv4, lv5, lv6, lv7);
                  lv7.getBlock().onPlaced(lv4, lv3, lv7, lv5, lv6);
                  if (lv5 instanceof ServerPlayerEntity) {
                     Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)lv5, lv3, lv6);
                  }
               }

               BlockSoundGroup lv8 = lv7.getSoundGroup();
               lv4.playSound(lv5, lv3, this.getPlaceSound(lv7), SoundCategory.BLOCKS, (lv8.getVolume() + 1.0F) / 2.0F, lv8.getPitch() * 0.8F);
               lv4.emitGameEvent(GameEvent.BLOCK_PLACE, lv3, GameEvent.Emitter.of(lv5, lv7));
               if (lv5 == null || !lv5.getAbilities().creativeMode) {
                  lv6.decrement(1);
               }

               return ActionResult.success(lv4.isClient);
            }
         }
      }
   }

   protected SoundEvent getPlaceSound(BlockState state) {
      return state.getSoundGroup().getPlaceSound();
   }

   @Nullable
   public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
      return context;
   }

   protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
      return writeNbtToBlockEntity(world, player, pos, stack);
   }

   @Nullable
   protected BlockState getPlacementState(ItemPlacementContext context) {
      BlockState lv = this.getBlock().getPlacementState(context);
      return lv != null && this.canPlace(context, lv) ? lv : null;
   }

   private BlockState placeFromNbt(BlockPos pos, World world, ItemStack stack, BlockState state) {
      BlockState lv = state;
      NbtCompound lv2 = stack.getNbt();
      if (lv2 != null) {
         NbtCompound lv3 = lv2.getCompound("BlockStateTag");
         StateManager lv4 = state.getBlock().getStateManager();
         Iterator var9 = lv3.getKeys().iterator();

         while(var9.hasNext()) {
            String string = (String)var9.next();
            Property lv5 = lv4.getProperty(string);
            if (lv5 != null) {
               String string2 = lv3.get(string).asString();
               lv = with(lv, lv5, string2);
            }
         }
      }

      if (lv != state) {
         world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
      }

      return lv;
   }

   private static BlockState with(BlockState state, Property property, String name) {
      return (BlockState)property.parse(name).map((value) -> {
         return (BlockState)state.with(property, value);
      }).orElse(state);
   }

   protected boolean canPlace(ItemPlacementContext context, BlockState state) {
      PlayerEntity lv = context.getPlayer();
      ShapeContext lv2 = lv == null ? ShapeContext.absent() : ShapeContext.of(lv);
      return (!this.checkStatePlacement() || state.canPlaceAt(context.getWorld(), context.getBlockPos())) && context.getWorld().canPlace(state, context.getBlockPos(), lv2);
   }

   protected boolean checkStatePlacement() {
      return true;
   }

   protected boolean place(ItemPlacementContext context, BlockState state) {
      return context.getWorld().setBlockState(context.getBlockPos(), state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
   }

   public static boolean writeNbtToBlockEntity(World world, @Nullable PlayerEntity player, BlockPos pos, ItemStack stack) {
      MinecraftServer minecraftServer = world.getServer();
      if (minecraftServer == null) {
         return false;
      } else {
         NbtCompound lv = getBlockEntityNbt(stack);
         if (lv != null) {
            BlockEntity lv2 = world.getBlockEntity(pos);
            if (lv2 != null) {
               if (!world.isClient && lv2.copyItemDataRequiresOperator() && (player == null || !player.isCreativeLevelTwoOp())) {
                  return false;
               }

               NbtCompound lv3 = lv2.createNbt();
               NbtCompound lv4 = lv3.copy();
               lv3.copyFrom(lv);
               if (!lv3.equals(lv4)) {
                  lv2.readNbt(lv3);
                  lv2.markDirty();
                  return true;
               }
            }
         }

         return false;
      }
   }

   public String getTranslationKey() {
      return this.getBlock().getTranslationKey();
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      this.getBlock().appendTooltip(stack, world, tooltip, context);
   }

   public Block getBlock() {
      return this.block;
   }

   public void appendBlocks(Map map, Item item) {
      map.put(this.getBlock(), item);
   }

   public boolean canBeNested() {
      return !(this.block instanceof ShulkerBoxBlock);
   }

   public void onItemEntityDestroyed(ItemEntity entity) {
      if (this.block instanceof ShulkerBoxBlock) {
         ItemStack lv = entity.getStack();
         NbtCompound lv2 = getBlockEntityNbt(lv);
         if (lv2 != null && lv2.contains("Items", NbtElement.LIST_TYPE)) {
            NbtList lv3 = lv2.getList("Items", NbtElement.COMPOUND_TYPE);
            Stream var10001 = lv3.stream();
            Objects.requireNonNull(NbtCompound.class);
            ItemUsage.spawnItemContents(entity, var10001.map(NbtCompound.class::cast).map(ItemStack::fromNbt));
         }
      }

   }

   @Nullable
   public static NbtCompound getBlockEntityNbt(ItemStack stack) {
      return stack.getSubNbt("BlockEntityTag");
   }

   public static void setBlockEntityNbt(ItemStack stack, BlockEntityType blockEntityType, NbtCompound tag) {
      if (tag.isEmpty()) {
         stack.removeSubNbt("BlockEntityTag");
      } else {
         BlockEntity.writeIdToNbt(tag, blockEntityType);
         stack.setSubNbt("BlockEntityTag", tag);
      }

   }

   public FeatureSet getRequiredFeatures() {
      return this.getBlock().getRequiredFeatures();
   }
}
