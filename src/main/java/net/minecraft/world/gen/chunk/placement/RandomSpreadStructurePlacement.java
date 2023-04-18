package net.minecraft.world.gen.chunk.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;

public class RandomSpreadStructurePlacement extends StructurePlacement {
   public static final Codec CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      return buildCodec(instance).and(instance.group(Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::getSpacing), Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::getSeparation), SpreadType.CODEC.optionalFieldOf("spread_type", SpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::getSpreadType))).apply(instance, RandomSpreadStructurePlacement::new);
   }).flatXmap((placement) -> {
      return placement.spacing <= placement.separation ? DataResult.error(() -> {
         return "Spacing has to be larger than separation";
      }) : DataResult.success(placement);
   }, DataResult::success).codec();
   private final int spacing;
   private final int separation;
   private final SpreadType spreadType;

   public RandomSpreadStructurePlacement(Vec3i locateOffset, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional exclusionZone, int spacing, int separation, SpreadType spreadType) {
      super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
      this.spacing = spacing;
      this.separation = separation;
      this.spreadType = spreadType;
   }

   public RandomSpreadStructurePlacement(int spacing, int separation, SpreadType spreadType, int salt) {
      this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, salt, Optional.empty(), spacing, separation, spreadType);
   }

   public int getSpacing() {
      return this.spacing;
   }

   public int getSeparation() {
      return this.separation;
   }

   public SpreadType getSpreadType() {
      return this.spreadType;
   }

   public ChunkPos getStartChunk(long seed, int chunkX, int chunkZ) {
      int k = Math.floorDiv(chunkX, this.spacing);
      int m = Math.floorDiv(chunkZ, this.spacing);
      ChunkRandom lv = new ChunkRandom(new CheckedRandom(0L));
      lv.setRegionSeed(seed, k, m, this.getSalt());
      int n = this.spacing - this.separation;
      int o = this.spreadType.get(lv, n);
      int p = this.spreadType.get(lv, n);
      return new ChunkPos(k * this.spacing + o, m * this.spacing + p);
   }

   protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
      ChunkPos lv = this.getStartChunk(calculator.getStructureSeed(), chunkX, chunkZ);
      return lv.x == chunkX && lv.z == chunkZ;
   }

   public StructurePlacementType getType() {
      return StructurePlacementType.RANDOM_SPREAD;
   }
}
