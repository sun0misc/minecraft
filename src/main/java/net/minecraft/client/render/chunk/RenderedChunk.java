package net.minecraft.client.render.chunk;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
class RenderedChunk {
   private final Map blockEntities;
   @Nullable
   private final List blockStateContainers;
   private final boolean debugWorld;
   private final WorldChunk chunk;

   RenderedChunk(WorldChunk chunk) {
      this.chunk = chunk;
      this.debugWorld = chunk.getWorld().isDebugWorld();
      this.blockEntities = ImmutableMap.copyOf(chunk.getBlockEntities());
      if (chunk instanceof EmptyChunk) {
         this.blockStateContainers = null;
      } else {
         ChunkSection[] lvs = chunk.getSectionArray();
         this.blockStateContainers = new ArrayList(lvs.length);
         ChunkSection[] var3 = lvs;
         int var4 = lvs.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            ChunkSection lv = var3[var5];
            this.blockStateContainers.add(lv.isEmpty() ? null : lv.getBlockStateContainer().copy());
         }
      }

   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return (BlockEntity)this.blockEntities.get(pos);
   }

   public BlockState getBlockState(BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      if (this.debugWorld) {
         BlockState lv = null;
         if (j == 60) {
            lv = Blocks.BARRIER.getDefaultState();
         }

         if (j == 70) {
            lv = DebugChunkGenerator.getBlockState(i, k);
         }

         return lv == null ? Blocks.AIR.getDefaultState() : lv;
      } else if (this.blockStateContainers == null) {
         return Blocks.AIR.getDefaultState();
      } else {
         try {
            int l = this.chunk.getSectionIndex(j);
            if (l >= 0 && l < this.blockStateContainers.size()) {
               PalettedContainer lv2 = (PalettedContainer)this.blockStateContainers.get(l);
               if (lv2 != null) {
                  return (BlockState)lv2.get(i & 15, j & 15, k & 15);
               }
            }

            return Blocks.AIR.getDefaultState();
         } catch (Throwable var8) {
            CrashReport lv3 = CrashReport.create(var8, "Getting block state");
            CrashReportSection lv4 = lv3.addElement("Block being got");
            lv4.add("Location", () -> {
               return CrashReportSection.createPositionString(this.chunk, i, j, k);
            });
            throw new CrashException(lv3);
         }
      }
   }
}
