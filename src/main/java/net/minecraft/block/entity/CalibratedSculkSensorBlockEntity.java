package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CalibratedSculkSensorBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.VibrationListener;
import org.jetbrains.annotations.Nullable;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
   public CalibratedSculkSensorBlockEntity(BlockPos arg, BlockState arg2) {
      super(BlockEntityType.CALIBRATED_SCULK_SENSOR, arg, arg2);
   }

   public VibrationListener.Callback createCallback() {
      return new Callback(this);
   }

   public static class Callback extends SculkSensorBlockEntity.Callback {
      public Callback(SculkSensorBlockEntity arg) {
         super(arg);
      }

      public int getRange() {
         return 16;
      }

      public boolean accepts(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, @Nullable GameEvent.Emitter emitter) {
         BlockPos lv = this.blockEntity.getPos();
         int i = this.getCalibrationFrequency(world, lv, this.blockEntity.getCachedState());
         return i != 0 && VibrationListener.getFrequency(event) != i ? false : super.accepts(world, listener, pos, event, emitter);
      }

      private int getCalibrationFrequency(World world, BlockPos pos, BlockState state) {
         Direction lv = ((Direction)state.get(CalibratedSculkSensorBlock.FACING)).getOpposite();
         return world.getEmittedRedstonePower(pos.offset(lv), lv);
      }
   }
}
