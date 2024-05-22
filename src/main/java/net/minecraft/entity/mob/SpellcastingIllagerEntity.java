/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.function.IntFunction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class SpellcastingIllagerEntity
extends IllagerEntity {
    private static final TrackedData<Byte> SPELL = DataTracker.registerData(SpellcastingIllagerEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected int spellTicks;
    private Spell spell = Spell.NONE;

    protected SpellcastingIllagerEntity(EntityType<? extends SpellcastingIllagerEntity> arg, World arg2) {
        super((EntityType<? extends IllagerEntity>)arg, arg2);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SPELL, (byte)0);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.spellTicks = nbt.getInt("SpellTicks");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SpellTicks", this.spellTicks);
    }

    @Override
    public IllagerEntity.State getState() {
        if (this.isSpellcasting()) {
            return IllagerEntity.State.SPELLCASTING;
        }
        if (this.isCelebrating()) {
            return IllagerEntity.State.CELEBRATING;
        }
        return IllagerEntity.State.CROSSED;
    }

    public boolean isSpellcasting() {
        if (this.getWorld().isClient) {
            return this.dataTracker.get(SPELL) > 0;
        }
        return this.spellTicks > 0;
    }

    public void setSpell(Spell spell) {
        this.spell = spell;
        this.dataTracker.set(SPELL, (byte)spell.id);
    }

    protected Spell getSpell() {
        if (!this.getWorld().isClient) {
            return this.spell;
        }
        return Spell.byId(this.dataTracker.get(SPELL).byteValue());
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (this.spellTicks > 0) {
            --this.spellTicks;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient && this.isSpellcasting()) {
            Spell lv = this.getSpell();
            float f = (float)lv.particleVelocity[0];
            float g = (float)lv.particleVelocity[1];
            float h = (float)lv.particleVelocity[2];
            float i = this.bodyYaw * ((float)Math.PI / 180) + MathHelper.cos((float)this.age * 0.6662f) * 0.25f;
            float j = MathHelper.cos(i);
            float k = MathHelper.sin(i);
            double d = 0.6 * (double)this.getScale();
            double e = 1.8 * (double)this.getScale();
            this.getWorld().addParticle(EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, f, g, h), this.getX() + (double)j * d, this.getY() + e, this.getZ() + (double)k * d, 0.0, 0.0, 0.0);
            this.getWorld().addParticle(EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, f, g, h), this.getX() - (double)j * d, this.getY() + e, this.getZ() - (double)k * d, 0.0, 0.0, 0.0);
        }
    }

    protected int getSpellTicks() {
        return this.spellTicks;
    }

    protected abstract SoundEvent getCastSpellSound();

    protected static enum Spell {
        NONE(0, 0.0, 0.0, 0.0),
        SUMMON_VEX(1, 0.7, 0.7, 0.8),
        FANGS(2, 0.4, 0.3, 0.35),
        WOLOLO(3, 0.7, 0.5, 0.2),
        DISAPPEAR(4, 0.3, 0.3, 0.8),
        BLINDNESS(5, 0.1, 0.1, 0.2);

        private static final IntFunction<Spell> BY_ID;
        final int id;
        final double[] particleVelocity;

        private Spell(int id, double particleVelocityX, double particleVelocityY, double particleVelocityZ) {
            this.id = id;
            this.particleVelocity = new double[]{particleVelocityX, particleVelocityY, particleVelocityZ};
        }

        public static Spell byId(int id) {
            return BY_ID.apply(id);
        }

        static {
            BY_ID = ValueLists.createIdToValueFunction(spell -> spell.id, Spell.values(), ValueLists.OutOfBoundsHandling.ZERO);
        }
    }

    protected abstract class CastSpellGoal
    extends Goal {
        protected int spellCooldown;
        protected int startTime;

        protected CastSpellGoal() {
        }

        @Override
        public boolean canStart() {
            LivingEntity lv = SpellcastingIllagerEntity.this.getTarget();
            if (lv == null || !lv.isAlive()) {
                return false;
            }
            if (SpellcastingIllagerEntity.this.isSpellcasting()) {
                return false;
            }
            return SpellcastingIllagerEntity.this.age >= this.startTime;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity lv = SpellcastingIllagerEntity.this.getTarget();
            return lv != null && lv.isAlive() && this.spellCooldown > 0;
        }

        @Override
        public void start() {
            this.spellCooldown = this.getTickCount(this.getInitialCooldown());
            SpellcastingIllagerEntity.this.spellTicks = this.getSpellTicks();
            this.startTime = SpellcastingIllagerEntity.this.age + this.startTimeDelay();
            SoundEvent lv = this.getSoundPrepare();
            if (lv != null) {
                SpellcastingIllagerEntity.this.playSound(lv, 1.0f, 1.0f);
            }
            SpellcastingIllagerEntity.this.setSpell(this.getSpell());
        }

        @Override
        public void tick() {
            --this.spellCooldown;
            if (this.spellCooldown == 0) {
                this.castSpell();
                SpellcastingIllagerEntity.this.playSound(SpellcastingIllagerEntity.this.getCastSpellSound(), 1.0f, 1.0f);
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

    protected class LookAtTargetGoal
    extends Goal {
        public LookAtTargetGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return SpellcastingIllagerEntity.this.getSpellTicks() > 0;
        }

        @Override
        public void start() {
            super.start();
            SpellcastingIllagerEntity.this.navigation.stop();
        }

        @Override
        public void stop() {
            super.stop();
            SpellcastingIllagerEntity.this.setSpell(Spell.NONE);
        }

        @Override
        public void tick() {
            if (SpellcastingIllagerEntity.this.getTarget() != null) {
                SpellcastingIllagerEntity.this.getLookControl().lookAt(SpellcastingIllagerEntity.this.getTarget(), SpellcastingIllagerEntity.this.getMaxHeadRotation(), SpellcastingIllagerEntity.this.getMaxLookPitchChange());
            }
        }
    }
}

