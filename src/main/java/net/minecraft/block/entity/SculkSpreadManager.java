package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.SculkSpreadable;
import net.minecraft.block.SculkVeinBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SculkSpreadManager {
   public static final int field_37609 = 24;
   public static final int MAX_CHARGE = 1000;
   public static final float field_37611 = 0.5F;
   private static final int MAX_CURSORS = 32;
   public static final int field_37612 = 11;
   final boolean worldGen;
   private final TagKey replaceableTag;
   private final int extraBlockChance;
   private final int maxDistance;
   private final int spreadChance;
   private final int decayChance;
   private List cursors = new ArrayList();
   private static final Logger LOGGER = LogUtils.getLogger();

   public SculkSpreadManager(boolean worldGen, TagKey replaceableTag, int extraBlockChance, int maxDistance, int spreadChance, int decayChance) {
      this.worldGen = worldGen;
      this.replaceableTag = replaceableTag;
      this.extraBlockChance = extraBlockChance;
      this.maxDistance = maxDistance;
      this.spreadChance = spreadChance;
      this.decayChance = decayChance;
   }

   public static SculkSpreadManager create() {
      return new SculkSpreadManager(false, BlockTags.SCULK_REPLACEABLE, 10, 4, 10, 5);
   }

   public static SculkSpreadManager createWorldGen() {
      return new SculkSpreadManager(true, BlockTags.SCULK_REPLACEABLE_WORLD_GEN, 50, 1, 5, 10);
   }

   public TagKey getReplaceableTag() {
      return this.replaceableTag;
   }

   public int getExtraBlockChance() {
      return this.extraBlockChance;
   }

   public int getMaxDistance() {
      return this.maxDistance;
   }

   public int getSpreadChance() {
      return this.spreadChance;
   }

   public int getDecayChance() {
      return this.decayChance;
   }

   public boolean isWorldGen() {
      return this.worldGen;
   }

   @VisibleForTesting
   public List getCursors() {
      return this.cursors;
   }

   public void clearCursors() {
      this.cursors.clear();
   }

   public void readNbt(NbtCompound nbt) {
      if (nbt.contains("cursors", NbtElement.LIST_TYPE)) {
         this.cursors.clear();
         DataResult var10000 = SculkSpreadManager.Cursor.CODEC.listOf().parse(new Dynamic(NbtOps.INSTANCE, nbt.getList("cursors", NbtElement.COMPOUND_TYPE)));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         List list = (List)var10000.resultOrPartial(var10001::error).orElseGet(ArrayList::new);
         int i = Math.min(list.size(), 32);

         for(int j = 0; j < i; ++j) {
            this.addCursor((Cursor)list.get(j));
         }
      }

   }

   public void writeNbt(NbtCompound nbt) {
      DataResult var10000 = SculkSpreadManager.Cursor.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.cursors);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((cursorsNbt) -> {
         nbt.put("cursors", cursorsNbt);
      });
   }

   public void spread(BlockPos pos, int charge) {
      while(charge > 0) {
         int j = Math.min(charge, 1000);
         this.addCursor(new Cursor(pos, j));
         charge -= j;
      }

   }

   private void addCursor(Cursor cursor) {
      if (this.cursors.size() < 32) {
         this.cursors.add(cursor);
      }
   }

   public void tick(WorldAccess world, BlockPos pos, Random random, boolean shouldConvertToBlock) {
      if (!this.cursors.isEmpty()) {
         List list = new ArrayList();
         Map map = new HashMap();
         Object2IntMap object2IntMap = new Object2IntOpenHashMap();
         Iterator var8 = this.cursors.iterator();

         while(true) {
            BlockPos lv2;
            while(var8.hasNext()) {
               Cursor lv = (Cursor)var8.next();
               lv.spread(world, pos, random, this, shouldConvertToBlock);
               if (lv.charge <= 0) {
                  world.syncWorldEvent(WorldEvents.SCULK_CHARGE, lv.getPos(), 0);
               } else {
                  lv2 = lv.getPos();
                  object2IntMap.computeInt(lv2, (posx, charge) -> {
                     return (charge == null ? 0 : charge) + lv.charge;
                  });
                  Cursor lv3 = (Cursor)map.get(lv2);
                  if (lv3 == null) {
                     map.put(lv2, lv);
                     list.add(lv);
                  } else if (!this.isWorldGen() && lv.charge + lv3.charge <= 1000) {
                     lv3.merge(lv);
                  } else {
                     list.add(lv);
                     if (lv.charge < lv3.charge) {
                        map.put(lv2, lv);
                     }
                  }
               }
            }

            ObjectIterator var16 = object2IntMap.object2IntEntrySet().iterator();

            while(var16.hasNext()) {
               Object2IntMap.Entry entry = (Object2IntMap.Entry)var16.next();
               lv2 = (BlockPos)entry.getKey();
               int i = entry.getIntValue();
               Cursor lv4 = (Cursor)map.get(lv2);
               Collection collection = lv4 == null ? null : lv4.getFaces();
               if (i > 0 && collection != null) {
                  int j = (int)(Math.log1p((double)i) / 2.299999952316284) + 1;
                  int k = (j << 6) + MultifaceGrowthBlock.directionsToFlag(collection);
                  world.syncWorldEvent(WorldEvents.SCULK_CHARGE, lv2, k);
               }
            }

            this.cursors = list;
            return;
         }
      }
   }

   public static class Cursor {
      private static final ObjectArrayList OFFSETS = (ObjectArrayList)Util.make(new ObjectArrayList(18), (objectArrayList) -> {
         Stream var10000 = BlockPos.stream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).filter((pos) -> {
            return (pos.getX() == 0 || pos.getY() == 0 || pos.getZ() == 0) && !pos.equals(BlockPos.ORIGIN);
         }).map(BlockPos::toImmutable);
         Objects.requireNonNull(objectArrayList);
         var10000.forEach(objectArrayList::add);
      });
      public static final int field_37622 = 1;
      private BlockPos pos;
      int charge;
      private int update;
      private int decay;
      @Nullable
      private Set faces;
      private static final Codec DIRECTION_SET_CODEC;
      public static final Codec CODEC;

      private Cursor(BlockPos pos, int charge, int decay, int update, Optional faces) {
         this.pos = pos;
         this.charge = charge;
         this.decay = decay;
         this.update = update;
         this.faces = (Set)faces.orElse((Object)null);
      }

      public Cursor(BlockPos pos, int charge) {
         this(pos, charge, 1, 0, Optional.empty());
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public int getCharge() {
         return this.charge;
      }

      public int getDecay() {
         return this.decay;
      }

      @Nullable
      public Set getFaces() {
         return this.faces;
      }

      private boolean canSpread(WorldAccess world, BlockPos pos, boolean worldGen) {
         if (this.charge <= 0) {
            return false;
         } else if (worldGen) {
            return true;
         } else if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            return lv.shouldTickBlockPos(pos);
         } else {
            return false;
         }
      }

      public void spread(WorldAccess world, BlockPos pos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock) {
         if (this.canSpread(world, pos, spreadManager.worldGen)) {
            if (this.update > 0) {
               --this.update;
            } else {
               BlockState lv = world.getBlockState(this.pos);
               SculkSpreadable lv2 = getSpreadable(lv);
               if (shouldConvertToBlock && lv2.spread(world, this.pos, lv, this.faces, spreadManager.isWorldGen())) {
                  if (lv2.shouldConvertToSpreadable()) {
                     lv = world.getBlockState(this.pos);
                     lv2 = getSpreadable(lv);
                  }

                  world.playSound((PlayerEntity)null, this.pos, SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.BLOCKS, 1.0F, 1.0F);
               }

               this.charge = lv2.spread(this, world, pos, random, spreadManager, shouldConvertToBlock);
               if (this.charge <= 0) {
                  lv2.spreadAtSamePosition(world, lv, this.pos, random);
               } else {
                  BlockPos lv3 = getSpreadPos(world, this.pos, random);
                  if (lv3 != null) {
                     lv2.spreadAtSamePosition(world, lv, this.pos, random);
                     this.pos = lv3.toImmutable();
                     if (spreadManager.isWorldGen() && !this.pos.isWithinDistance(new Vec3i(pos.getX(), this.pos.getY(), pos.getZ()), 15.0)) {
                        this.charge = 0;
                        return;
                     }

                     lv = world.getBlockState(lv3);
                  }

                  if (lv.getBlock() instanceof SculkSpreadable) {
                     this.faces = MultifaceGrowthBlock.collectDirections(lv);
                  }

                  this.decay = lv2.getDecay(this.decay);
                  this.update = lv2.getUpdate();
               }
            }
         }
      }

      void merge(Cursor cursor) {
         this.charge += cursor.charge;
         cursor.charge = 0;
         this.update = Math.min(this.update, cursor.update);
      }

      private static SculkSpreadable getSpreadable(BlockState state) {
         Block var2 = state.getBlock();
         SculkSpreadable var10000;
         if (var2 instanceof SculkSpreadable lv) {
            var10000 = lv;
         } else {
            var10000 = SculkSpreadable.VEIN_ONLY_SPREADER;
         }

         return var10000;
      }

      private static List shuffleOffsets(Random random) {
         return Util.copyShuffled(OFFSETS, random);
      }

      @Nullable
      private static BlockPos getSpreadPos(WorldAccess world, BlockPos pos, Random random) {
         BlockPos.Mutable lv = pos.mutableCopy();
         BlockPos.Mutable lv2 = pos.mutableCopy();
         Iterator var5 = shuffleOffsets(random).iterator();

         while(var5.hasNext()) {
            Vec3i lv3 = (Vec3i)var5.next();
            lv2.set(pos, (Vec3i)lv3);
            BlockState lv4 = world.getBlockState(lv2);
            if (lv4.getBlock() instanceof SculkSpreadable && canSpread(world, pos, (BlockPos)lv2)) {
               lv.set(lv2);
               if (SculkVeinBlock.veinCoversSculkReplaceable(world, lv4, lv2)) {
                  break;
               }
            }
         }

         return lv.equals(pos) ? null : lv;
      }

      private static boolean canSpread(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
         if (sourcePos.getManhattanDistance(targetPos) == 1) {
            return true;
         } else {
            BlockPos lv = targetPos.subtract(sourcePos);
            Direction lv2 = Direction.from(Direction.Axis.X, lv.getX() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction lv3 = Direction.from(Direction.Axis.Y, lv.getY() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction lv4 = Direction.from(Direction.Axis.Z, lv.getZ() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            if (lv.getX() == 0) {
               return canSpread(world, sourcePos, lv3) || canSpread(world, sourcePos, lv4);
            } else if (lv.getY() == 0) {
               return canSpread(world, sourcePos, lv2) || canSpread(world, sourcePos, lv4);
            } else {
               return canSpread(world, sourcePos, lv2) || canSpread(world, sourcePos, lv3);
            }
         }
      }

      private static boolean canSpread(WorldAccess world, BlockPos pos, Direction direction) {
         BlockPos lv = pos.offset(direction);
         return !world.getBlockState(lv).isSideSolidFullSquare(world, lv, direction.getOpposite());
      }

      static {
         DIRECTION_SET_CODEC = Direction.CODEC.listOf().xmap((directions) -> {
            return Sets.newEnumSet(directions, Direction.class);
         }, Lists::newArrayList);
         CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(Cursor::getPos), Codec.intRange(0, 1000).fieldOf("charge").orElse(0).forGetter(Cursor::getCharge), Codec.intRange(0, 1).fieldOf("decay_delay").orElse(1).forGetter(Cursor::getDecay), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("update_delay").orElse(0).forGetter((cursor) -> {
               return cursor.update;
            }), DIRECTION_SET_CODEC.optionalFieldOf("facings").forGetter((cursor) -> {
               return Optional.ofNullable(cursor.getFaces());
            })).apply(instance, Cursor::new);
         });
      }
   }
}
