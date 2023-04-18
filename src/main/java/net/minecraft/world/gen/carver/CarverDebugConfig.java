package net.minecraft.world.gen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class CarverDebugConfig {
   public static final CarverDebugConfig DEFAULT;
   public static final Codec CODEC;
   private final boolean debugMode;
   private final BlockState airState;
   private final BlockState waterState;
   private final BlockState lavaState;
   private final BlockState barrierState;

   public static CarverDebugConfig create(boolean debugMode, BlockState airState, BlockState waterState, BlockState lavaState, BlockState barrierState) {
      return new CarverDebugConfig(debugMode, airState, waterState, lavaState, barrierState);
   }

   public static CarverDebugConfig create(BlockState airState, BlockState waterState, BlockState lavaState, BlockState barrierState) {
      return new CarverDebugConfig(false, airState, waterState, lavaState, barrierState);
   }

   public static CarverDebugConfig create(boolean debugMode, BlockState debugState) {
      return new CarverDebugConfig(debugMode, debugState, DEFAULT.getWaterState(), DEFAULT.getLavaState(), DEFAULT.getBarrierState());
   }

   private CarverDebugConfig(boolean debugMode, BlockState airState, BlockState waterState, BlockState lavaState, BlockState barrierState) {
      this.debugMode = debugMode;
      this.airState = airState;
      this.waterState = waterState;
      this.lavaState = lavaState;
      this.barrierState = barrierState;
   }

   public boolean isDebugMode() {
      return this.debugMode;
   }

   public BlockState getAirState() {
      return this.airState;
   }

   public BlockState getWaterState() {
      return this.waterState;
   }

   public BlockState getLavaState() {
      return this.lavaState;
   }

   public BlockState getBarrierState() {
      return this.barrierState;
   }

   static {
      DEFAULT = new CarverDebugConfig(false, Blocks.ACACIA_BUTTON.getDefaultState(), Blocks.CANDLE.getDefaultState(), Blocks.ORANGE_STAINED_GLASS.getDefaultState(), Blocks.GLASS.getDefaultState());
      CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.BOOL.optionalFieldOf("debug_mode", false).forGetter(CarverDebugConfig::isDebugMode), BlockState.CODEC.optionalFieldOf("air_state", DEFAULT.getAirState()).forGetter(CarverDebugConfig::getAirState), BlockState.CODEC.optionalFieldOf("water_state", DEFAULT.getAirState()).forGetter(CarverDebugConfig::getWaterState), BlockState.CODEC.optionalFieldOf("lava_state", DEFAULT.getAirState()).forGetter(CarverDebugConfig::getLavaState), BlockState.CODEC.optionalFieldOf("barrier_state", DEFAULT.getAirState()).forGetter(CarverDebugConfig::getBarrierState)).apply(instance, CarverDebugConfig::new);
      });
   }
}
