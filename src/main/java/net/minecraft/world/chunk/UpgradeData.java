package net.minecraft.world.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EightWayDirection;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.Tick;
import org.slf4j.Logger;

public class UpgradeData {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final UpgradeData NO_UPGRADE_DATA;
   private static final String INDICES_KEY = "Indices";
   private static final EightWayDirection[] EIGHT_WAYS;
   private final EnumSet sidesToUpgrade;
   private final List blockTicks;
   private final List fluidTicks;
   private final int[][] centerIndicesToUpgrade;
   static final Map BLOCK_TO_LOGIC;
   static final Set CALLBACK_LOGICS;

   private UpgradeData(HeightLimitView world) {
      this.sidesToUpgrade = EnumSet.noneOf(EightWayDirection.class);
      this.blockTicks = Lists.newArrayList();
      this.fluidTicks = Lists.newArrayList();
      this.centerIndicesToUpgrade = new int[world.countVerticalSections()][];
   }

   public UpgradeData(NbtCompound nbt, HeightLimitView world) {
      this(world);
      if (nbt.contains("Indices", NbtElement.COMPOUND_TYPE)) {
         NbtCompound lv = nbt.getCompound("Indices");

         for(int i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
            String string = String.valueOf(i);
            if (lv.contains(string, NbtElement.INT_ARRAY_TYPE)) {
               this.centerIndicesToUpgrade[i] = lv.getIntArray(string);
            }
         }
      }

      int j = nbt.getInt("Sides");
      EightWayDirection[] var9 = EightWayDirection.values();
      int var10 = var9.length;

      for(int var6 = 0; var6 < var10; ++var6) {
         EightWayDirection lv2 = var9[var6];
         if ((j & 1 << lv2.ordinal()) != 0) {
            this.sidesToUpgrade.add(lv2);
         }
      }

      addNeighborTicks(nbt, "neighbor_block_ticks", (id) -> {
         return Registries.BLOCK.getOrEmpty(Identifier.tryParse(id)).or(() -> {
            return Optional.of(Blocks.AIR);
         });
      }, this.blockTicks);
      addNeighborTicks(nbt, "neighbor_fluid_ticks", (id) -> {
         return Registries.FLUID.getOrEmpty(Identifier.tryParse(id)).or(() -> {
            return Optional.of(Fluids.EMPTY);
         });
      }, this.fluidTicks);
   }

   private static void addNeighborTicks(NbtCompound nbt, String key, Function nameToType, List ticks) {
      if (nbt.contains(key, NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList(key, NbtElement.COMPOUND_TYPE);
         Iterator var5 = lv.iterator();

         while(var5.hasNext()) {
            NbtElement lv2 = (NbtElement)var5.next();
            Optional var10000 = Tick.fromNbt((NbtCompound)lv2, nameToType);
            Objects.requireNonNull(ticks);
            var10000.ifPresent(ticks::add);
         }
      }

   }

   public void upgrade(WorldChunk chunk) {
      this.upgradeCenter(chunk);
      EightWayDirection[] var2 = EIGHT_WAYS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EightWayDirection lv = var2[var4];
         upgradeSide(chunk, lv);
      }

      World lv2 = chunk.getWorld();
      this.blockTicks.forEach((tick) -> {
         Block lv = tick.type() == Blocks.AIR ? lv2.getBlockState(tick.pos()).getBlock() : (Block)tick.type();
         lv2.scheduleBlockTick(tick.pos(), lv, tick.delay(), tick.priority());
      });
      this.fluidTicks.forEach((tick) -> {
         Fluid lv = tick.type() == Fluids.EMPTY ? lv2.getFluidState(tick.pos()).getFluid() : (Fluid)tick.type();
         lv2.scheduleFluidTick(tick.pos(), lv, tick.delay(), tick.priority());
      });
      CALLBACK_LOGICS.forEach((logic) -> {
         logic.postUpdate(lv2);
      });
   }

   private static void upgradeSide(WorldChunk chunk, EightWayDirection side) {
      World lv = chunk.getWorld();
      if (chunk.getUpgradeData().sidesToUpgrade.remove(side)) {
         Set set = side.getDirections();
         int i = false;
         int j = true;
         boolean bl = set.contains(Direction.EAST);
         boolean bl2 = set.contains(Direction.WEST);
         boolean bl3 = set.contains(Direction.SOUTH);
         boolean bl4 = set.contains(Direction.NORTH);
         boolean bl5 = set.size() == 1;
         ChunkPos lv2 = chunk.getPos();
         int k = lv2.getStartX() + (bl5 && (bl4 || bl3) ? 1 : (bl2 ? 0 : 15));
         int l = lv2.getStartX() + (bl5 && (bl4 || bl3) ? 14 : (bl2 ? 0 : 15));
         int m = lv2.getStartZ() + (!bl5 || !bl && !bl2 ? (bl4 ? 0 : 15) : 1);
         int n = lv2.getStartZ() + (!bl5 || !bl && !bl2 ? (bl4 ? 0 : 15) : 14);
         Direction[] lvs = Direction.values();
         BlockPos.Mutable lv3 = new BlockPos.Mutable();
         Iterator var18 = BlockPos.iterate(k, lv.getBottomY(), m, l, lv.getTopY() - 1, n).iterator();

         while(var18.hasNext()) {
            BlockPos lv4 = (BlockPos)var18.next();
            BlockState lv5 = lv.getBlockState(lv4);
            BlockState lv6 = lv5;
            Direction[] var22 = lvs;
            int var23 = lvs.length;

            for(int var24 = 0; var24 < var23; ++var24) {
               Direction lv7 = var22[var24];
               lv3.set(lv4, (Direction)lv7);
               lv6 = applyAdjacentBlock(lv6, lv7, lv, lv4, lv3);
            }

            Block.replace(lv5, lv6, lv, lv4, 18);
         }

      }
   }

   private static BlockState applyAdjacentBlock(BlockState oldState, Direction dir, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
      return ((Logic)BLOCK_TO_LOGIC.getOrDefault(oldState.getBlock(), UpgradeData.BuiltinLogic.DEFAULT)).getUpdatedState(oldState, dir, world.getBlockState(otherPos), world, currentPos, otherPos);
   }

   private void upgradeCenter(WorldChunk chunk) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      BlockPos.Mutable lv2 = new BlockPos.Mutable();
      ChunkPos lv3 = chunk.getPos();
      WorldAccess lv4 = chunk.getWorld();

      int i;
      for(i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
         ChunkSection lv5 = chunk.getSection(i);
         int[] is = this.centerIndicesToUpgrade[i];
         this.centerIndicesToUpgrade[i] = null;
         if (is != null && is.length > 0) {
            Direction[] lvs = Direction.values();
            PalettedContainer lv6 = lv5.getBlockStateContainer();
            int[] var11 = is;
            int var12 = is.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               int j = var11[var13];
               int k = j & 15;
               int l = j >> 8 & 15;
               int m = j >> 4 & 15;
               lv.set(lv3.getStartX() + k, lv5.getYOffset() + l, lv3.getStartZ() + m);
               BlockState lv7 = (BlockState)lv6.get(j);
               BlockState lv8 = lv7;
               Direction[] var20 = lvs;
               int var21 = lvs.length;

               for(int var22 = 0; var22 < var21; ++var22) {
                  Direction lv9 = var20[var22];
                  lv2.set(lv, (Direction)lv9);
                  if (ChunkSectionPos.getSectionCoord(lv.getX()) == lv3.x && ChunkSectionPos.getSectionCoord(lv.getZ()) == lv3.z) {
                     lv8 = applyAdjacentBlock(lv8, lv9, lv4, lv, lv2);
                  }
               }

               Block.replace(lv7, lv8, lv4, lv, 18);
            }
         }
      }

      for(i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
         if (this.centerIndicesToUpgrade[i] != null) {
            LOGGER.warn("Discarding update data for section {} for chunk ({} {})", new Object[]{lv4.sectionIndexToCoord(i), lv3.x, lv3.z});
         }

         this.centerIndicesToUpgrade[i] = null;
      }

   }

   public boolean isDone() {
      int[][] var1 = this.centerIndicesToUpgrade;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] is = var1[var3];
         if (is != null) {
            return false;
         }
      }

      return this.sidesToUpgrade.isEmpty();
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      NbtCompound lv2 = new NbtCompound();

      int i;
      for(i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
         String string = String.valueOf(i);
         if (this.centerIndicesToUpgrade[i] != null && this.centerIndicesToUpgrade[i].length != 0) {
            lv2.putIntArray(string, this.centerIndicesToUpgrade[i]);
         }
      }

      if (!lv2.isEmpty()) {
         lv.put("Indices", lv2);
      }

      i = 0;

      EightWayDirection lv3;
      for(Iterator var6 = this.sidesToUpgrade.iterator(); var6.hasNext(); i |= 1 << lv3.ordinal()) {
         lv3 = (EightWayDirection)var6.next();
      }

      lv.putByte("Sides", (byte)i);
      NbtList lv4;
      if (!this.blockTicks.isEmpty()) {
         lv4 = new NbtList();
         this.blockTicks.forEach((blockTick) -> {
            lv4.add(blockTick.toNbt((block) -> {
               return Registries.BLOCK.getId(block).toString();
            }));
         });
         lv.put("neighbor_block_ticks", lv4);
      }

      if (!this.fluidTicks.isEmpty()) {
         lv4 = new NbtList();
         this.fluidTicks.forEach((fluidTick) -> {
            lv4.add(fluidTick.toNbt((fluid) -> {
               return Registries.FLUID.getId(fluid).toString();
            }));
         });
         lv.put("neighbor_fluid_ticks", lv4);
      }

      return lv;
   }

   static {
      NO_UPGRADE_DATA = new UpgradeData(EmptyBlockView.INSTANCE);
      EIGHT_WAYS = EightWayDirection.values();
      BLOCK_TO_LOGIC = new IdentityHashMap();
      CALLBACK_LOGICS = Sets.newHashSet();
   }

   static enum BuiltinLogic implements Logic {
      BLACKLIST(new Block[]{Blocks.OBSERVER, Blocks.NETHER_PORTAL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DRAGON_EGG, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN}) {
         public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
            return oldState;
         }
      },
      DEFAULT(new Block[0]) {
         public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
            return oldState.getStateForNeighborUpdate(direction, world.getBlockState(otherPos), world, currentPos, otherPos);
         }
      },
      CHEST(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST}) {
         public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
            if (otherState.isOf(oldState.getBlock()) && direction.getAxis().isHorizontal() && oldState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE && otherState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
               Direction lv = (Direction)oldState.get(ChestBlock.FACING);
               if (direction.getAxis() != lv.getAxis() && lv == otherState.get(ChestBlock.FACING)) {
                  ChestType lv2 = direction == lv.rotateYClockwise() ? ChestType.LEFT : ChestType.RIGHT;
                  world.setBlockState(otherPos, (BlockState)otherState.with(ChestBlock.CHEST_TYPE, lv2.getOpposite()), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                  if (lv == Direction.NORTH || lv == Direction.EAST) {
                     BlockEntity lv3 = world.getBlockEntity(currentPos);
                     BlockEntity lv4 = world.getBlockEntity(otherPos);
                     if (lv3 instanceof ChestBlockEntity && lv4 instanceof ChestBlockEntity) {
                        ChestBlockEntity.copyInventory((ChestBlockEntity)lv3, (ChestBlockEntity)lv4);
                     }
                  }

                  return (BlockState)oldState.with(ChestBlock.CHEST_TYPE, lv2);
               }
            }

            return oldState;
         }
      },
      LEAVES(true, new Block[]{Blocks.ACACIA_LEAVES, Blocks.CHERRY_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES}) {
         private final ThreadLocal distanceToPositions = ThreadLocal.withInitial(() -> {
            return Lists.newArrayListWithCapacity(7);
         });

         public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
            BlockState lv = oldState.getStateForNeighborUpdate(direction, world.getBlockState(otherPos), world, currentPos, otherPos);
            if (oldState != lv) {
               int i = (Integer)lv.get(Properties.DISTANCE_1_7);
               List list = (List)this.distanceToPositions.get();
               if (list.isEmpty()) {
                  for(int j = 0; j < 7; ++j) {
                     list.add(new ObjectOpenHashSet());
                  }
               }

               ((ObjectSet)list.get(i)).add(currentPos.toImmutable());
            }

            return oldState;
         }

         public void postUpdate(WorldAccess world) {
            BlockPos.Mutable lv = new BlockPos.Mutable();
            List list = (List)this.distanceToPositions.get();

            label44:
            for(int i = 2; i < list.size(); ++i) {
               int j = i - 1;
               ObjectSet objectSet = (ObjectSet)list.get(j);
               ObjectSet objectSet2 = (ObjectSet)list.get(i);
               ObjectIterator var8 = objectSet.iterator();

               while(true) {
                  BlockPos lv2;
                  BlockState lv3;
                  do {
                     do {
                        if (!var8.hasNext()) {
                           continue label44;
                        }

                        lv2 = (BlockPos)var8.next();
                        lv3 = world.getBlockState(lv2);
                     } while((Integer)lv3.get(Properties.DISTANCE_1_7) < j);

                     world.setBlockState(lv2, (BlockState)lv3.with(Properties.DISTANCE_1_7, j), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                  } while(i == 7);

                  Direction[] var11 = DIRECTIONS;
                  int var12 = var11.length;

                  for(int var13 = 0; var13 < var12; ++var13) {
                     Direction lv4 = var11[var13];
                     lv.set(lv2, (Direction)lv4);
                     BlockState lv5 = world.getBlockState(lv);
                     if (lv5.contains(Properties.DISTANCE_1_7) && (Integer)lv3.get(Properties.DISTANCE_1_7) > i) {
                        objectSet2.add(lv.toImmutable());
                     }
                  }
               }
            }

            list.clear();
         }
      },
      STEM_BLOCK(new Block[]{Blocks.MELON_STEM, Blocks.PUMPKIN_STEM}) {
         public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
            if ((Integer)oldState.get(StemBlock.AGE) == 7) {
               GourdBlock lv = ((StemBlock)oldState.getBlock()).getGourdBlock();
               if (otherState.isOf(lv)) {
                  return (BlockState)lv.getAttachedStem().getDefaultState().with(HorizontalFacingBlock.FACING, direction);
               }
            }

            return oldState;
         }
      };

      public static final Direction[] DIRECTIONS = Direction.values();

      BuiltinLogic(Block... blocks) {
         this(false, blocks);
      }

      BuiltinLogic(boolean addCallback, Block... blocks) {
         Block[] var5 = blocks;
         int var6 = blocks.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Block lv = var5[var7];
            UpgradeData.BLOCK_TO_LOGIC.put(lv, this);
         }

         if (addCallback) {
            UpgradeData.CALLBACK_LOGICS.add(this);
         }

      }

      // $FF: synthetic method
      private static BuiltinLogic[] method_36743() {
         return new BuiltinLogic[]{BLACKLIST, DEFAULT, CHEST, LEAVES, STEM_BLOCK};
      }
   }

   public interface Logic {
      BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos);

      default void postUpdate(WorldAccess world) {
      }
   }
}
