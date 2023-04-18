package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.VibrationListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private VibrationListener listener;
   private final VibrationListener.Callback callback;
   private int lastVibrationFrequency;

   protected SculkSensorBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
      super(arg, arg2, arg3);
      this.callback = this.createCallback();
      this.listener = new VibrationListener(new BlockPositionSource(this.pos), this.callback);
   }

   public SculkSensorBlockEntity(BlockPos pos, BlockState state) {
      this(BlockEntityType.SCULK_SENSOR, pos, state);
   }

   public VibrationListener.Callback createCallback() {
      return new Callback(this);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.lastVibrationFrequency = nbt.getInt("last_vibration_frequency");
      if (nbt.contains("listener", NbtElement.COMPOUND_TYPE)) {
         DataResult var10000 = VibrationListener.createCodec(this.callback).parse(new Dynamic(NbtOps.INSTANCE, nbt.getCompound("listener")));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((listener) -> {
            this.listener = listener;
         });
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putInt("last_vibration_frequency", this.lastVibrationFrequency);
      DataResult var10000 = VibrationListener.createCodec(this.callback).encodeStart(NbtOps.INSTANCE, this.listener);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((listenerNbt) -> {
         nbt.put("listener", listenerNbt);
      });
   }

   public VibrationListener getEventListener() {
      return this.listener;
   }

   public int getLastVibrationFrequency() {
      return this.lastVibrationFrequency;
   }

   public void setLastVibrationFrequency(int lastVibrationFrequency) {
      this.lastVibrationFrequency = lastVibrationFrequency;
   }

   public static class Callback implements VibrationListener.Callback {
      public static final int field_43292 = 8;
      protected final SculkSensorBlockEntity blockEntity;

      public Callback(SculkSensorBlockEntity blockEntity) {
         this.blockEntity = blockEntity;
      }

      public boolean triggersAvoidCriterion() {
         return true;
      }

      public boolean accepts(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, @Nullable GameEvent.Emitter emitter) {
         return !pos.equals(this.blockEntity.getPos()) || event != GameEvent.BLOCK_DESTROY && event != GameEvent.BLOCK_PLACE ? SculkSensorBlock.isInactive(this.blockEntity.getCachedState()) : false;
      }

      public void accept(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, @Nullable Entity entity, @Nullable Entity sourceEntity, float distance) {
         BlockState lv = this.blockEntity.getCachedState();
         BlockPos lv2 = this.blockEntity.getPos();
         if (SculkSensorBlock.isInactive(lv)) {
            this.blockEntity.setLastVibrationFrequency(VibrationListener.getFrequency(event));
            int i = getPower(distance, listener.getRange());
            Block var12 = lv.getBlock();
            if (var12 instanceof SculkSensorBlock) {
               SculkSensorBlock lv3 = (SculkSensorBlock)var12;
               lv3.setActive(entity, world, lv2, lv, i, this.blockEntity.getLastVibrationFrequency());
            }
         }

      }

      public void onListen() {
         this.blockEntity.markDirty();
      }

      public int getRange() {
         return 8;
      }

      public static int getPower(float distance, int range) {
         double d = 15.0 / (double)range;
         return Math.max(1, 15 - MathHelper.floor(d * (double)distance));
      }
   }
}
