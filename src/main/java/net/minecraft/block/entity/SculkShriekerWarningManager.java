package net.minecraft.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SculkShriekerWarningManager {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codecs.NONNEGATIVE_INT.fieldOf("ticks_since_last_warning").orElse(0).forGetter((manager) -> {
         return manager.ticksSinceLastWarning;
      }), Codecs.NONNEGATIVE_INT.fieldOf("warning_level").orElse(0).forGetter((manager) -> {
         return manager.warningLevel;
      }), Codecs.NONNEGATIVE_INT.fieldOf("cooldown_ticks").orElse(0).forGetter((manager) -> {
         return manager.cooldownTicks;
      })).apply(instance, SculkShriekerWarningManager::new);
   });
   public static final int MAX_WARNING_LEVEL = 4;
   private static final double WARN_RANGE = 16.0;
   private static final int WARN_WARDEN_RANGE = 48;
   private static final int WARN_DECREASE_COOLDOWN = 12000;
   private static final int WARN_INCREASE_COOLDOWN = 200;
   private int ticksSinceLastWarning;
   private int warningLevel;
   private int cooldownTicks;

   public SculkShriekerWarningManager(int ticksSinceLastWarning, int warningLevel, int cooldownTicks) {
      this.ticksSinceLastWarning = ticksSinceLastWarning;
      this.warningLevel = warningLevel;
      this.cooldownTicks = cooldownTicks;
   }

   public void tick() {
      if (this.ticksSinceLastWarning >= 12000) {
         this.decreaseWarningLevel();
         this.ticksSinceLastWarning = 0;
      } else {
         ++this.ticksSinceLastWarning;
      }

      if (this.cooldownTicks > 0) {
         --this.cooldownTicks;
      }

   }

   public void reset() {
      this.ticksSinceLastWarning = 0;
      this.warningLevel = 0;
      this.cooldownTicks = 0;
   }

   public static OptionalInt warnNearbyPlayers(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
      if (canIncreaseWarningLevel(world, pos)) {
         return OptionalInt.empty();
      } else {
         List list = getPlayersInRange(world, pos);
         if (!list.contains(player)) {
            list.add(player);
         }

         if (list.stream().anyMatch((nearbyPlayer) -> {
            return (Boolean)nearbyPlayer.getSculkShriekerWarningManager().map(SculkShriekerWarningManager::isInCooldown).orElse(false);
         })) {
            return OptionalInt.empty();
         } else {
            Optional optional = list.stream().flatMap((playerx) -> {
               return playerx.getSculkShriekerWarningManager().stream();
            }).max(Comparator.comparingInt(SculkShriekerWarningManager::getWarningLevel));
            if (optional.isPresent()) {
               SculkShriekerWarningManager lv = (SculkShriekerWarningManager)optional.get();
               lv.increaseWarningLevel();
               list.forEach((nearbyPlayer) -> {
                  nearbyPlayer.getSculkShriekerWarningManager().ifPresent((warningManager) -> {
                     warningManager.copy(lv);
                  });
               });
               return OptionalInt.of(lv.warningLevel);
            } else {
               return OptionalInt.empty();
            }
         }
      }
   }

   private boolean isInCooldown() {
      return this.cooldownTicks > 0;
   }

   private static boolean canIncreaseWarningLevel(ServerWorld world, BlockPos pos) {
      Box lv = Box.of(Vec3d.ofCenter(pos), 48.0, 48.0, 48.0);
      return !world.getNonSpectatingEntities(WardenEntity.class, lv).isEmpty();
   }

   private static List getPlayersInRange(ServerWorld world, BlockPos pos) {
      Vec3d lv = Vec3d.ofCenter(pos);
      Predicate predicate = (player) -> {
         return player.getPos().isInRange(lv, 16.0);
      };
      return world.getPlayers(predicate.and(LivingEntity::isAlive).and(EntityPredicates.EXCEPT_SPECTATOR));
   }

   private void increaseWarningLevel() {
      if (!this.isInCooldown()) {
         this.ticksSinceLastWarning = 0;
         this.cooldownTicks = 200;
         this.setWarningLevel(this.getWarningLevel() + 1);
      }

   }

   private void decreaseWarningLevel() {
      this.setWarningLevel(this.getWarningLevel() - 1);
   }

   public void setWarningLevel(int warningLevel) {
      this.warningLevel = MathHelper.clamp(warningLevel, 0, 4);
   }

   public int getWarningLevel() {
      return this.warningLevel;
   }

   private void copy(SculkShriekerWarningManager other) {
      this.warningLevel = other.warningLevel;
      this.cooldownTicks = other.cooldownTicks;
      this.ticksSinceLastWarning = other.ticksSinceLastWarning;
   }
}
