package net.minecraft.world;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;

public class Heightmap {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Predicate NOT_AIR = (state) -> {
      return !state.isAir();
   };
   static final Predicate SUFFOCATES = (state) -> {
      return state.getMaterial().blocksMovement();
   };
   private final PaletteStorage storage;
   private final Predicate blockPredicate;
   private final Chunk chunk;

   public Heightmap(Chunk chunk, Type type) {
      this.blockPredicate = type.getBlockPredicate();
      this.chunk = chunk;
      int i = MathHelper.ceilLog2(chunk.getHeight() + 1);
      this.storage = new PackedIntegerArray(i, 256);
   }

   public static void populateHeightmaps(Chunk chunk, Set types) {
      int i = types.size();
      ObjectList objectList = new ObjectArrayList(i);
      ObjectListIterator objectListIterator = objectList.iterator();
      int j = chunk.getHighestNonEmptySectionYOffset() + 16;
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            Iterator var9 = types.iterator();

            while(var9.hasNext()) {
               Type lv2 = (Type)var9.next();
               objectList.add(chunk.getHeightmap(lv2));
            }

            for(int m = j - 1; m >= chunk.getBottomY(); --m) {
               lv.set(k, m, l);
               BlockState lv3 = chunk.getBlockState(lv);
               if (!lv3.isOf(Blocks.AIR)) {
                  while(objectListIterator.hasNext()) {
                     Heightmap lv4 = (Heightmap)objectListIterator.next();
                     if (lv4.blockPredicate.test(lv3)) {
                        lv4.set(k, l, m + 1);
                        objectListIterator.remove();
                     }
                  }

                  if (objectList.isEmpty()) {
                     break;
                  }

                  objectListIterator.back(i);
               }
            }
         }
      }

   }

   public boolean trackUpdate(int x, int y, int z, BlockState state) {
      int l = this.get(x, z);
      if (y <= l - 2) {
         return false;
      } else {
         if (this.blockPredicate.test(state)) {
            if (y >= l) {
               this.set(x, z, y + 1);
               return true;
            }
         } else if (l - 1 == y) {
            BlockPos.Mutable lv = new BlockPos.Mutable();

            for(int m = y - 1; m >= this.chunk.getBottomY(); --m) {
               lv.set(x, m, z);
               if (this.blockPredicate.test(this.chunk.getBlockState(lv))) {
                  this.set(x, z, m + 1);
                  return true;
               }
            }

            this.set(x, z, this.chunk.getBottomY());
            return true;
         }

         return false;
      }
   }

   public int get(int x, int z) {
      return this.get(toIndex(x, z));
   }

   public int method_35334(int i, int j) {
      return this.get(toIndex(i, j)) - 1;
   }

   private int get(int index) {
      return this.storage.get(index) + this.chunk.getBottomY();
   }

   private void set(int x, int z, int height) {
      this.storage.set(toIndex(x, z), height - this.chunk.getBottomY());
   }

   public void setTo(Chunk chunk, Type type, long[] values) {
      long[] ms = this.storage.getData();
      if (ms.length == values.length) {
         System.arraycopy(values, 0, ms, 0, values.length);
      } else {
         Logger var10000 = LOGGER;
         ChunkPos var10001 = chunk.getPos();
         var10000.warn("Ignoring heightmap data for chunk " + var10001 + ", size does not match; expected: " + ms.length + ", got: " + values.length);
         populateHeightmaps(chunk, EnumSet.of(type));
      }
   }

   public long[] asLongArray() {
      return this.storage.getData();
   }

   private static int toIndex(int x, int z) {
      return x + z * 16;
   }

   public static enum Type implements StringIdentifiable {
      WORLD_SURFACE_WG("WORLD_SURFACE_WG", Heightmap.Purpose.WORLDGEN, Heightmap.NOT_AIR),
      WORLD_SURFACE("WORLD_SURFACE", Heightmap.Purpose.CLIENT, Heightmap.NOT_AIR),
      OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Heightmap.Purpose.WORLDGEN, Heightmap.SUFFOCATES),
      OCEAN_FLOOR("OCEAN_FLOOR", Heightmap.Purpose.LIVE_WORLD, Heightmap.SUFFOCATES),
      MOTION_BLOCKING("MOTION_BLOCKING", Heightmap.Purpose.CLIENT, (state) -> {
         return state.getMaterial().blocksMovement() || !state.getFluidState().isEmpty();
      }),
      MOTION_BLOCKING_NO_LEAVES("MOTION_BLOCKING_NO_LEAVES", Heightmap.Purpose.LIVE_WORLD, (state) -> {
         return (state.getMaterial().blocksMovement() || !state.getFluidState().isEmpty()) && !(state.getBlock() instanceof LeavesBlock);
      });

      public static final Codec CODEC = StringIdentifiable.createCodec(Type::values);
      private final String name;
      private final Purpose purpose;
      private final Predicate blockPredicate;

      private Type(String name, Purpose purpose, Predicate blockPredicate) {
         this.name = name;
         this.purpose = purpose;
         this.blockPredicate = blockPredicate;
      }

      public String getName() {
         return this.name;
      }

      public boolean shouldSendToClient() {
         return this.purpose == Heightmap.Purpose.CLIENT;
      }

      public boolean isStoredServerSide() {
         return this.purpose != Heightmap.Purpose.WORLDGEN;
      }

      public Predicate getBlockPredicate() {
         return this.blockPredicate;
      }

      public String asString() {
         return this.name;
      }

      // $FF: synthetic method
      private static Type[] method_36752() {
         return new Type[]{WORLD_SURFACE_WG, WORLD_SURFACE, OCEAN_FLOOR_WG, OCEAN_FLOOR, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES};
      }
   }

   public static enum Purpose {
      WORLDGEN,
      LIVE_WORLD,
      CLIENT;

      // $FF: synthetic method
      private static Purpose[] method_36753() {
         return new Purpose[]{WORLDGEN, LIVE_WORLD, CLIENT};
      }
   }
}
