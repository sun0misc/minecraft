package net.minecraft.world.gen;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.StructureLocator;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public class StructureAccessor {
   private final WorldAccess world;
   private final GeneratorOptions options;
   private final StructureLocator locator;

   public StructureAccessor(WorldAccess world, GeneratorOptions options, StructureLocator locator) {
      this.world = world;
      this.options = options;
      this.locator = locator;
   }

   public StructureAccessor forRegion(ChunkRegion region) {
      if (region.toServerWorld() != this.world) {
         ServerWorld var10002 = region.toServerWorld();
         throw new IllegalStateException("Using invalid structure manager (source level: " + var10002 + ", region: " + region);
      } else {
         return new StructureAccessor(region, this.options, this.locator);
      }
   }

   public List getStructureStarts(ChunkPos pos, Predicate predicate) {
      Map map = this.world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences();
      ImmutableList.Builder builder = ImmutableList.builder();
      Iterator var5 = map.entrySet().iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         Structure lv = (Structure)entry.getKey();
         if (predicate.test(lv)) {
            LongSet var10002 = (LongSet)entry.getValue();
            Objects.requireNonNull(builder);
            this.acceptStructureStarts(lv, var10002, builder::add);
         }
      }

      return builder.build();
   }

   public List getStructureStarts(ChunkSectionPos sectionPos, Structure structure) {
      LongSet longSet = this.world.getChunk(sectionPos.getSectionX(), sectionPos.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences(structure);
      ImmutableList.Builder builder = ImmutableList.builder();
      Objects.requireNonNull(builder);
      this.acceptStructureStarts(structure, longSet, builder::add);
      return builder.build();
   }

   public void acceptStructureStarts(Structure structure, LongSet structureStartPositions, Consumer consumer) {
      LongIterator var4 = structureStartPositions.iterator();

      while(var4.hasNext()) {
         long l = (Long)var4.next();
         ChunkSectionPos lv = ChunkSectionPos.from(new ChunkPos(l), this.world.getBottomSectionCoord());
         StructureStart lv2 = this.getStructureStart(lv, structure, this.world.getChunk(lv.getSectionX(), lv.getSectionZ(), ChunkStatus.STRUCTURE_STARTS));
         if (lv2 != null && lv2.hasChildren()) {
            consumer.accept(lv2);
         }
      }

   }

   @Nullable
   public StructureStart getStructureStart(ChunkSectionPos pos, Structure structure, StructureHolder holder) {
      return holder.getStructureStart(structure);
   }

   public void setStructureStart(ChunkSectionPos pos, Structure structure, StructureStart structureStart, StructureHolder holder) {
      holder.setStructureStart(structure, structureStart);
   }

   public void addStructureReference(ChunkSectionPos pos, Structure structure, long reference, StructureHolder holder) {
      holder.addStructureReference(structure, reference);
   }

   public boolean shouldGenerateStructures() {
      return this.options.shouldGenerateStructures();
   }

   public StructureStart getStructureAt(BlockPos pos, Structure structure) {
      Iterator var3 = this.getStructureStarts(ChunkSectionPos.from(pos), structure).iterator();

      StructureStart lv;
      do {
         if (!var3.hasNext()) {
            return StructureStart.DEFAULT;
         }

         lv = (StructureStart)var3.next();
      } while(!lv.getBoundingBox().contains(pos));

      return lv;
   }

   public StructureStart getStructureContaining(BlockPos pos, RegistryKey structure) {
      Structure lv = (Structure)this.getRegistryManager().get(RegistryKeys.STRUCTURE).get(structure);
      return lv == null ? StructureStart.DEFAULT : this.getStructureContaining(pos, lv);
   }

   public StructureStart getStructureContaining(BlockPos pos, TagKey structureTag) {
      Registry lv = this.getRegistryManager().get(RegistryKeys.STRUCTURE);
      Iterator var4 = this.getStructureStarts(new ChunkPos(pos), (structure) -> {
         return (Boolean)lv.getEntry(lv.getRawId(structure)).map((arg2) -> {
            return arg2.isIn(structureTag);
         }).orElse(false);
      }).iterator();

      StructureStart lv2;
      do {
         if (!var4.hasNext()) {
            return StructureStart.DEFAULT;
         }

         lv2 = (StructureStart)var4.next();
      } while(!this.structureContains(pos, lv2));

      return lv2;
   }

   public StructureStart getStructureContaining(BlockPos pos, Structure structure) {
      Iterator var3 = this.getStructureStarts(ChunkSectionPos.from(pos), structure).iterator();

      StructureStart lv;
      do {
         if (!var3.hasNext()) {
            return StructureStart.DEFAULT;
         }

         lv = (StructureStart)var3.next();
      } while(!this.structureContains(pos, lv));

      return lv;
   }

   public boolean structureContains(BlockPos pos, StructureStart structureStart) {
      Iterator var3 = structureStart.getChildren().iterator();

      StructurePiece lv;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         lv = (StructurePiece)var3.next();
      } while(!lv.getBoundingBox().contains(pos));

      return true;
   }

   public boolean hasStructureReferences(BlockPos pos) {
      ChunkSectionPos lv = ChunkSectionPos.from(pos);
      return this.world.getChunk(lv.getSectionX(), lv.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).hasStructureReferences();
   }

   public Map getStructureReferences(BlockPos pos) {
      ChunkSectionPos lv = ChunkSectionPos.from(pos);
      return this.world.getChunk(lv.getSectionX(), lv.getSectionZ(), ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences();
   }

   public StructurePresence getStructurePresence(ChunkPos chunkPos, Structure structure, boolean skipExistingChunk) {
      return this.locator.getStructurePresence(chunkPos, structure, skipExistingChunk);
   }

   public void incrementReferences(StructureStart structureStart) {
      structureStart.incrementReferences();
      this.locator.incrementReferences(structureStart.getPos(), structureStart.getStructure());
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.world.getRegistryManager();
   }
}
