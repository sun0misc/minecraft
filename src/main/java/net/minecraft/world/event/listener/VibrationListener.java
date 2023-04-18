package net.minecraft.world.event.listener;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.GameEventTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import org.jetbrains.annotations.Nullable;

public class VibrationListener implements GameEventListener {
   public static final GameEvent[] RESONATIONS;
   @VisibleForTesting
   public static final Object2IntMap FREQUENCIES;
   private final PositionSource positionSource;
   private final Callback callback;
   @Nullable
   private Vibration vibration;
   private int delay;
   private final VibrationSelector selector;

   public static Codec createCodec(Callback callback) {
      return RecordCodecBuilder.create((instance) -> {
         return instance.group(PositionSource.CODEC.fieldOf("source").forGetter((listener) -> {
            return listener.positionSource;
         }), Vibration.CODEC.optionalFieldOf("event").forGetter((listener) -> {
            return Optional.ofNullable(listener.vibration);
         }), VibrationSelector.CODEC.fieldOf("selector").forGetter((listener) -> {
            return listener.selector;
         }), Codecs.NONNEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter((listener) -> {
            return listener.delay;
         })).apply(instance, (positionSource, optional, arg3, integer) -> {
            return new VibrationListener(positionSource, callback, (Vibration)optional.orElse((Object)null), arg3, integer);
         });
      });
   }

   private VibrationListener(PositionSource positionSource, Callback callback, @Nullable Vibration vibration, VibrationSelector selector, int delay) {
      this.positionSource = positionSource;
      this.callback = callback;
      this.vibration = vibration;
      this.delay = delay;
      this.selector = selector;
   }

   public VibrationListener(PositionSource positionSource, Callback callback) {
      this(positionSource, callback, (Vibration)null, new VibrationSelector(), 0);
   }

   public static int getFrequency(GameEvent event) {
      return FREQUENCIES.getOrDefault(event, 0);
   }

   public static GameEvent getResonation(int frequency) {
      return RESONATIONS[frequency - 1];
   }

   public Callback getCallback() {
      return this.callback;
   }

   public void tick(World world) {
      if (world instanceof ServerWorld lv) {
         if (this.vibration == null) {
            this.selector.getVibrationToTick(lv.getTime()).ifPresent((vibration) -> {
               this.vibration = vibration;
               Vec3d lvx = this.vibration.pos();
               this.delay = MathHelper.floor(this.vibration.distance());
               lv.spawnParticles(new VibrationParticleEffect(this.positionSource, this.delay), lvx.x, lvx.y, lvx.z, 1, 0.0, 0.0, 0.0, 0.0);
               this.callback.onListen();
               this.selector.clear();
            });
         }

         if (this.vibration != null) {
            --this.delay;
            if (this.delay <= 0) {
               this.delay = 0;
               BlockPos lv2 = BlockPos.ofFloored(this.vibration.pos());
               BlockPos lv3 = (BlockPos)this.positionSource.getPos(lv).map(BlockPos::ofFloored).orElse(lv2);
               this.callback.accept(lv, this, lv2, this.vibration.gameEvent(), (Entity)this.vibration.getEntity(lv).orElse((Object)null), (Entity)this.vibration.getOwner(lv).orElse((Object)null), getDistanceBetween(lv2, lv3));
               this.vibration = null;
            }
         }
      }

   }

   public PositionSource getPositionSource() {
      return this.positionSource;
   }

   public int getRange() {
      return this.callback.getRange();
   }

   public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
      if (this.vibration != null) {
         return false;
      } else if (!this.callback.canAccept(event, emitter)) {
         return false;
      } else {
         Optional optional = this.positionSource.getPos(world);
         if (optional.isEmpty()) {
            return false;
         } else {
            Vec3d lv = (Vec3d)optional.get();
            if (!this.callback.accepts(world, this, BlockPos.ofFloored(emitterPos), event, emitter)) {
               return false;
            } else if (isOccluded(world, emitterPos, lv)) {
               return false;
            } else {
               this.trySelect(world, event, emitter, emitterPos, lv);
               return true;
            }
         }
      }
   }

   public void forceListen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
      this.positionSource.getPos(world).ifPresent((listenerPos) -> {
         this.trySelect(world, event, emitter, emitterPos, listenerPos);
      });
   }

   public void trySelect(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, Vec3d listenerPos) {
      this.selector.tryAccept(new Vibration(event, (float)emitterPos.distanceTo(listenerPos), emitterPos, emitter.sourceEntity()), world.getTime());
   }

   public static float getDistanceBetween(BlockPos a, BlockPos b) {
      return (float)Math.sqrt(a.getSquaredDistance(b));
   }

   private static boolean isOccluded(World world, Vec3d start, Vec3d end) {
      Vec3d lv = new Vec3d((double)MathHelper.floor(start.x) + 0.5, (double)MathHelper.floor(start.y) + 0.5, (double)MathHelper.floor(start.z) + 0.5);
      Vec3d lv2 = new Vec3d((double)MathHelper.floor(end.x) + 0.5, (double)MathHelper.floor(end.y) + 0.5, (double)MathHelper.floor(end.z) + 0.5);
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv3 = var5[var7];
         Vec3d lv4 = lv.offset(lv3, 9.999999747378752E-6);
         if (world.raycast(new BlockStateRaycastContext(lv4, lv2, (state) -> {
            return state.isIn(BlockTags.OCCLUDES_VIBRATION_SIGNALS);
         })).getType() != HitResult.Type.BLOCK) {
            return false;
         }
      }

      return true;
   }

   static {
      RESONATIONS = new GameEvent[]{GameEvent.RESONATE_1, GameEvent.RESONATE_2, GameEvent.RESONATE_3, GameEvent.RESONATE_4, GameEvent.RESONATE_5, GameEvent.RESONATE_6, GameEvent.RESONATE_7, GameEvent.RESONATE_8, GameEvent.RESONATE_9, GameEvent.RESONATE_10, GameEvent.RESONATE_11, GameEvent.RESONATE_12, GameEvent.RESONATE_13, GameEvent.RESONATE_14, GameEvent.RESONATE_15};
      FREQUENCIES = Object2IntMaps.unmodifiable((Object2IntMap)Util.make(new Object2IntOpenHashMap(), (frequencies) -> {
         frequencies.put(GameEvent.STEP, 1);
         frequencies.put(GameEvent.SWIM, 1);
         frequencies.put(GameEvent.FLAP, 1);
         frequencies.put(GameEvent.PROJECTILE_LAND, 2);
         frequencies.put(GameEvent.HIT_GROUND, 2);
         frequencies.put(GameEvent.SPLASH, 2);
         frequencies.put(GameEvent.ITEM_INTERACT_FINISH, 3);
         frequencies.put(GameEvent.PROJECTILE_SHOOT, 3);
         frequencies.put(GameEvent.INSTRUMENT_PLAY, 3);
         frequencies.put(GameEvent.ENTITY_ROAR, 4);
         frequencies.put(GameEvent.ENTITY_SHAKE, 4);
         frequencies.put(GameEvent.ELYTRA_GLIDE, 4);
         frequencies.put(GameEvent.ENTITY_DISMOUNT, 5);
         frequencies.put(GameEvent.EQUIP, 5);
         frequencies.put(GameEvent.ENTITY_INTERACT, 6);
         frequencies.put(GameEvent.SHEAR, 6);
         frequencies.put(GameEvent.ENTITY_MOUNT, 6);
         frequencies.put(GameEvent.ENTITY_DAMAGE, 7);
         frequencies.put(GameEvent.DRINK, 8);
         frequencies.put(GameEvent.EAT, 8);
         frequencies.put(GameEvent.CONTAINER_CLOSE, 9);
         frequencies.put(GameEvent.BLOCK_CLOSE, 9);
         frequencies.put(GameEvent.BLOCK_DEACTIVATE, 9);
         frequencies.put(GameEvent.BLOCK_DETACH, 9);
         frequencies.put(GameEvent.CONTAINER_OPEN, 10);
         frequencies.put(GameEvent.BLOCK_OPEN, 10);
         frequencies.put(GameEvent.BLOCK_ACTIVATE, 10);
         frequencies.put(GameEvent.BLOCK_ATTACH, 10);
         frequencies.put(GameEvent.PRIME_FUSE, 10);
         frequencies.put(GameEvent.NOTE_BLOCK_PLAY, 10);
         frequencies.put(GameEvent.BLOCK_CHANGE, 11);
         frequencies.put(GameEvent.BLOCK_DESTROY, 12);
         frequencies.put(GameEvent.FLUID_PICKUP, 12);
         frequencies.put(GameEvent.BLOCK_PLACE, 13);
         frequencies.put(GameEvent.FLUID_PLACE, 13);
         frequencies.put(GameEvent.ENTITY_PLACE, 14);
         frequencies.put(GameEvent.LIGHTNING_STRIKE, 14);
         frequencies.put(GameEvent.TELEPORT, 14);
         frequencies.put(GameEvent.ENTITY_DIE, 15);
         frequencies.put(GameEvent.EXPLODE, 15);

         for(int i = 1; i <= 15; ++i) {
            frequencies.put(getResonation(i), i);
         }

      }));
   }

   public interface Callback {
      int getRange();

      default TagKey getTag() {
         return GameEventTags.VIBRATIONS;
      }

      default boolean triggersAvoidCriterion() {
         return false;
      }

      default boolean canAccept(GameEvent gameEvent, GameEvent.Emitter emitter) {
         if (!gameEvent.isIn(this.getTag())) {
            return false;
         } else {
            Entity lv = emitter.sourceEntity();
            if (lv != null) {
               if (lv.isSpectator()) {
                  return false;
               }

               if (lv.bypassesSteppingEffects() && gameEvent.isIn(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                  if (this.triggersAvoidCriterion() && lv instanceof ServerPlayerEntity) {
                     ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
                     Criteria.AVOID_VIBRATION.trigger(lv2);
                  }

                  return false;
               }

               if (lv.occludeVibrationSignals()) {
                  return false;
               }
            }

            if (emitter.affectedState() != null) {
               return !emitter.affectedState().isIn(BlockTags.DAMPENS_VIBRATIONS);
            } else {
               return true;
            }
         }
      }

      boolean accepts(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, GameEvent.Emitter emitter);

      void accept(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, @Nullable Entity entity, @Nullable Entity sourceEntity, float distance);

      default void onListen() {
      }
   }
}
