package net.minecraft.world.gen.noise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public record NoiseRouter(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction initialDensityWithoutJaggedness, DensityFunction finalDensity, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(field("barrier", NoiseRouter::barrierNoise), field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise), field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise), field("lava", NoiseRouter::lavaNoise), field("temperature", NoiseRouter::temperature), field("vegetation", NoiseRouter::vegetation), field("continents", NoiseRouter::continents), field("erosion", NoiseRouter::erosion), field("depth", NoiseRouter::depth), field("ridges", NoiseRouter::ridges), field("initial_density_without_jaggedness", NoiseRouter::initialDensityWithoutJaggedness), field("final_density", NoiseRouter::finalDensity), field("vein_toggle", NoiseRouter::veinToggle), field("vein_ridged", NoiseRouter::veinRidged), field("vein_gap", NoiseRouter::veinGap)).apply(instance, NoiseRouter::new);
   });

   public NoiseRouter(DensityFunction arg, DensityFunction arg2, DensityFunction arg3, DensityFunction arg4, DensityFunction arg5, DensityFunction arg6, DensityFunction arg7, DensityFunction arg8, DensityFunction arg9, DensityFunction arg10, DensityFunction arg11, DensityFunction arg12, DensityFunction arg13, DensityFunction arg14, DensityFunction arg15) {
      this.barrierNoise = arg;
      this.fluidLevelFloodednessNoise = arg2;
      this.fluidLevelSpreadNoise = arg3;
      this.lavaNoise = arg4;
      this.temperature = arg5;
      this.vegetation = arg6;
      this.continents = arg7;
      this.erosion = arg8;
      this.depth = arg9;
      this.ridges = arg10;
      this.initialDensityWithoutJaggedness = arg11;
      this.finalDensity = arg12;
      this.veinToggle = arg13;
      this.veinRidged = arg14;
      this.veinGap = arg15;
   }

   private static RecordCodecBuilder field(String name, Function getter) {
      return DensityFunction.FUNCTION_CODEC.fieldOf(name).forGetter(getter);
   }

   public NoiseRouter apply(DensityFunction.DensityFunctionVisitor visitor) {
      return new NoiseRouter(this.barrierNoise.apply(visitor), this.fluidLevelFloodednessNoise.apply(visitor), this.fluidLevelSpreadNoise.apply(visitor), this.lavaNoise.apply(visitor), this.temperature.apply(visitor), this.vegetation.apply(visitor), this.continents.apply(visitor), this.erosion.apply(visitor), this.depth.apply(visitor), this.ridges.apply(visitor), this.initialDensityWithoutJaggedness.apply(visitor), this.finalDensity.apply(visitor), this.veinToggle.apply(visitor), this.veinRidged.apply(visitor), this.veinGap.apply(visitor));
   }

   public DensityFunction barrierNoise() {
      return this.barrierNoise;
   }

   public DensityFunction fluidLevelFloodednessNoise() {
      return this.fluidLevelFloodednessNoise;
   }

   public DensityFunction fluidLevelSpreadNoise() {
      return this.fluidLevelSpreadNoise;
   }

   public DensityFunction lavaNoise() {
      return this.lavaNoise;
   }

   public DensityFunction temperature() {
      return this.temperature;
   }

   public DensityFunction vegetation() {
      return this.vegetation;
   }

   public DensityFunction continents() {
      return this.continents;
   }

   public DensityFunction erosion() {
      return this.erosion;
   }

   public DensityFunction depth() {
      return this.depth;
   }

   public DensityFunction ridges() {
      return this.ridges;
   }

   public DensityFunction initialDensityWithoutJaggedness() {
      return this.initialDensityWithoutJaggedness;
   }

   public DensityFunction finalDensity() {
      return this.finalDensity;
   }

   public DensityFunction veinToggle() {
      return this.veinToggle;
   }

   public DensityFunction veinRidged() {
      return this.veinRidged;
   }

   public DensityFunction veinGap() {
      return this.veinGap;
   }
}
