package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.function.IntFunction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class SpellcastingIllagerEntity extends IllagerEntity {
   private static final TrackedData SPELL;
   protected int spellTicks;
   private Spell spell;

   protected SpellcastingIllagerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.spell = SpellcastingIllagerEntity.Spell.NONE;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SPELL, (byte)0);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.spellTicks = nbt.getInt("SpellTicks");
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("SpellTicks", this.spellTicks);
   }

   public IllagerEntity.State getState() {
      if (this.isSpellcasting()) {
         return IllagerEntity.State.SPELLCASTING;
      } else {
         return this.isCelebrating() ? IllagerEntity.State.CELEBRATING : IllagerEntity.State.CROSSED;
      }
   }

   public boolean isSpellcasting() {
      if (this.world.isClient) {
         return (Byte)this.dataTracker.get(SPELL) > 0;
      } else {
         return this.spellTicks > 0;
      }
   }

   public void setSpell(Spell spell) {
      this.spell = spell;
      this.dataTracker.set(SPELL, (byte)spell.id);
   }

   protected Spell getSpell() {
      return !this.world.isClient ? this.spell : SpellcastingIllagerEntity.Spell.byId((Byte)this.dataTracker.get(SPELL));
   }

   protected void mobTick() {
      super.mobTick();
      if (this.spellTicks > 0) {
         --this.spellTicks;
      }

   }

   public void tick() {
      super.tick();
      if (this.world.isClient && this.isSpellcasting()) {
         Spell lv = this.getSpell();
         double d = lv.particleVelocity[0];
         double e = lv.particleVelocity[1];
         double f = lv.particleVelocity[2];
         float g = this.bodyYaw * 0.017453292F + MathHelper.cos((float)this.age * 0.6662F) * 0.25F;
         float h = MathHelper.cos(g);
         float i = MathHelper.sin(g);
         this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + (double)h * 0.6, this.getY() + 1.8, this.getZ() + (double)i * 0.6, d, e, f);
         this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() - (double)h * 0.6, this.getY() + 1.8, this.getZ() - (double)i * 0.6, d, e, f);
      }

   }

   protected int getSpellTicks() {
      return this.spellTicks;
   }

   protected abstract SoundEvent getCastSpellSound();

   static {
      SPELL = DataTracker.registerData(SpellcastingIllagerEntity.class, TrackedDataHandlerRegistry.BYTE);
   }

   protected static enum Spell {
      NONE(0, 0.0, 0.0, 0.0),
      SUMMON_VEX(1, 0.7, 0.7, 0.8),
      FANGS(2, 0.4, 0.3, 0.35),
      WOLOLO(3, 0.7, 0.5, 0.2),
      DISAPPEAR(4, 0.3, 0.3, 0.8),
      BLINDNESS(5, 0.1, 0.1, 0.2);

      private static final IntFunction BY_ID = ValueLists.createIdToValueFunction((spell) -> {
         return spell.id;
      }, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);
      final int id;
      final double[] particleVelocity;

      private Spell(int id, double particleVelocityX, double particleVelocityY, double particleVelocityZ) {
         this.id = id;
         this.particleVelocity = new double[]{particleVelocityX, particleVelocityY, particleVelocityZ};
      }

      public static Spell byId(int id) {
         return (Spell)BY_ID.apply(id);
      }

      // $FF: synthetic method
      private static Spell[] method_36658() {
         return new Spell[]{NONE, SUMMON_VEX, FANGS, WOLOLO, DISAPPEAR, BLINDNESS};
      }
   }

   protected abstract class CastSpellGoal extends Goal {
      protected int spellCooldown;
      protected int startTime;

      public boolean canStart() {
         LivingEntity lv = SpellcastingIllagerEntity.this.getTarget();
         if (lv != null && lv.isAlive()) {
            if (SpellcastingIllagerEntity.this.isSpellcasting()) {
               return false;
            } else {
               return SpellcastingIllagerEntity.this.age >= this.startTime;
            }
         } else {
            return false;
         }
      }

      public boolean shouldContinue() {
         LivingEntity lv = SpellcastingIllagerEntity.this.getTarget();
         return lv != null && lv.isAlive() && this.spellCooldown > 0;
      }

      public void start() {
         this.spellCooldown = this.getTickCount(this.getInitialCooldown());
         SpellcastingIllagerEntity.this.spellTicks = this.getSpellTicks();
         this.startTime = SpellcastingIllagerEntity.this.age + this.startTimeDelay();
         SoundEvent lv = this.getSoundPrepare();
         if (lv != null) {
            SpellcastingIllagerEntity.this.playSound(lv, 1.0F, 1.0F);
         }

         SpellcastingIllagerEntity.this.setSpell(this.getSpell());
      }

      public void tick() {
         --this.spellCooldown;
         if (this.spellCooldown == 0) {
            this.castSpell();
            SpellcastingIllagerEntity.this.playSound(SpellcastingIllagerEntity.this.getCastSpellSound(), 1.0F, 1.0F);
         }

      }

      protected abstract void castSpell();

      protected int getInitialCooldown() {
         return 20;
      }

      protected abstract int getSpellTicks();

      protected abstract int startTimeDelay();

      @Nullable
      protected abstract SoundEvent getSoundPrepare();

      protected abstract Spell getSpell();
   }

   protected class LookAtTargetGoal extends Goal {
      public LookAtTargetGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         return SpellcastingIllagerEntity.this.getSpellTicks() > 0;
      }

      public void start() {
         super.start();
         SpellcastingIllagerEntity.this.navigation.stop();
      }

      public void stop() {
         super.stop();
         SpellcastingIllagerEntity.this.setSpell(SpellcastingIllagerEntity.Spell.NONE);
      }

      public void tick() {
         if (SpellcastingIllagerEntity.this.getTarget() != null) {
            SpellcastingIllagerEntity.this.getLookControl().lookAt(SpellcastingIllagerEntity.this.getTarget(), (float)SpellcastingIllagerEntity.this.getMaxHeadRotation(), (float)SpellcastingIllagerEntity.this.getMaxLookPitchChange());
         }

      }
   }
}
