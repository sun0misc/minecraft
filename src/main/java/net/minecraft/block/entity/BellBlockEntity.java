package net.minecraft.block.entity;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;

public class BellBlockEntity extends BlockEntity {
   private static final int MAX_RINGING_TICKS = 50;
   private static final int field_31317 = 60;
   private static final int field_31318 = 60;
   private static final int MAX_RESONATING_TICKS = 40;
   private static final int field_31320 = 5;
   private static final int field_31321 = 48;
   private static final int MAX_BELL_HEARING_DISTANCE = 32;
   private static final int field_31323 = 48;
   private long lastRingTime;
   public int ringTicks;
   public boolean ringing;
   public Direction lastSideHit;
   private List hearingEntities;
   private boolean resonating;
   private int resonateTime;

   public BellBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.BELL, pos, state);
   }

   public boolean onSyncedBlockEvent(int type, int data) {
      if (type == 1) {
         this.notifyMemoriesOfBell();
         this.resonateTime = 0;
         this.lastSideHit = Direction.byId(data);
         this.ringTicks = 0;
         this.ringing = true;
         return true;
      } else {
         return super.onSyncedBlockEvent(type, data);
      }
   }

   private static void tick(World world, BlockPos pos, BlockState state, BellBlockEntity blockEntity, Effect bellEffect) {
      if (blockEntity.ringing) {
         ++blockEntity.ringTicks;
      }

      if (blockEntity.ringTicks >= 50) {
         blockEntity.ringing = false;
         blockEntity.ringTicks = 0;
      }

      if (blockEntity.ringTicks >= 5 && blockEntity.resonateTime == 0 && raidersHearBell(pos, blockEntity.hearingEntities)) {
         blockEntity.resonating = true;
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

      if (blockEntity.resonating) {
         if (blockEntity.resonateTime < 40) {
            ++blockEntity.resonateTime;
         } else {
            bellEffect.run(world, pos, blockEntity.hearingEntities);
            blockEntity.resonating = false;
         }
      }

   }

   public static void clientTick(World world, BlockPos pos, BlockState state, BellBlockEntity blockEntity) {
      tick(world, pos, state, blockEntity, BellBlockEntity::applyParticlesToRaiders);
   }

   public static void serverTick(World world, BlockPos pos, BlockState state, BellBlockEntity blockEntity) {
      tick(world, pos, state, blockEntity, BellBlockEntity::applyGlowToRaiders);
   }

   public void activate(Direction direction) {
      BlockPos lv = this.getPos();
      this.lastSideHit = direction;
      if (this.ringing) {
         this.ringTicks = 0;
      } else {
         this.ringing = true;
      }

      this.world.addSyncedBlockEvent(lv, this.getCachedState().getBlock(), 1, direction.getId());
   }

   private void notifyMemoriesOfBell() {
      BlockPos lv = this.getPos();
      if (this.world.getTime() > this.lastRingTime + 60L || this.hearingEntities == null) {
         this.lastRingTime = this.world.getTime();
         Box lv2 = (new Box(lv)).expand(48.0);
         this.hearingEntities = this.world.getNonSpectatingEntities(LivingEntity.class, lv2);
      }

      if (!this.world.isClient) {
         Iterator var4 = this.hearingEntities.iterator();

         while(var4.hasNext()) {
            LivingEntity lv3 = (LivingEntity)var4.next();
            if (lv3.isAlive() && !lv3.isRemoved() && lv.isWithinDistance(lv3.getPos(), 32.0)) {
               lv3.getBrain().remember(MemoryModuleType.HEARD_BELL_TIME, (Object)this.world.getTime());
            }
         }
      }

   }

   private static boolean raidersHearBell(BlockPos pos, List hearingEntities) {
      Iterator var2 = hearingEntities.iterator();

      LivingEntity lv;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         lv = (LivingEntity)var2.next();
      } while(!lv.isAlive() || lv.isRemoved() || !pos.isWithinDistance(lv.getPos(), 32.0) || !lv.getType().isIn(EntityTypeTags.RAIDERS));

      return true;
   }

   private static void applyGlowToRaiders(World world, BlockPos pos, List hearingEntities) {
      hearingEntities.stream().filter((entity) -> {
         return isRaiderEntity(pos, entity);
      }).forEach(BellBlockEntity::applyGlowToEntity);
   }

   private static void applyParticlesToRaiders(World world, BlockPos pos, List hearingEntities) {
      MutableInt mutableInt = new MutableInt(16700985);
      int i = (int)hearingEntities.stream().filter((entity) -> {
         return pos.isWithinDistance(entity.getPos(), 48.0);
      }).count();
      hearingEntities.stream().filter((entity) -> {
         return isRaiderEntity(pos, entity);
      }).forEach((entity) -> {
         float f = 1.0F;
         double d = Math.sqrt((entity.getX() - (double)pos.getX()) * (entity.getX() - (double)pos.getX()) + (entity.getZ() - (double)pos.getZ()) * (entity.getZ() - (double)pos.getZ()));
         double e = (double)((float)pos.getX() + 0.5F) + 1.0 / d * (entity.getX() - (double)pos.getX());
         double g = (double)((float)pos.getZ() + 0.5F) + 1.0 / d * (entity.getZ() - (double)pos.getZ());
         int j = MathHelper.clamp((i - 21) / -2, 3, 15);

         for(int k = 0; k < j; ++k) {
            int l = mutableInt.addAndGet(5);
            double h = (double)ColorHelper.Argb.getRed(l) / 255.0;
            double m = (double)ColorHelper.Argb.getGreen(l) / 255.0;
            double n = (double)ColorHelper.Argb.getBlue(l) / 255.0;
            world.addParticle(ParticleTypes.ENTITY_EFFECT, e, (double)((float)pos.getY() + 0.5F), g, h, m, n);
         }

      });
   }

   private static boolean isRaiderEntity(BlockPos pos, LivingEntity entity) {
      return entity.isAlive() && !entity.isRemoved() && pos.isWithinDistance(entity.getPos(), 48.0) && entity.getType().isIn(EntityTypeTags.RAIDERS);
   }

   private static void applyGlowToEntity(LivingEntity entity) {
      entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60));
   }

   @FunctionalInterface
   interface Effect {
      void run(World world, BlockPos pos, List hearingEntities);
   }
}
