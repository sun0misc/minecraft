package net.minecraft.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IllusionerEntity extends SpellcastingIllagerEntity implements RangedAttackMob {
   private static final int field_30473 = 4;
   private static final int field_30471 = 3;
   private static final int field_30472 = 3;
   private int mirrorSpellTimer;
   private final Vec3d[][] mirrorCopyOffsets;

   public IllusionerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.experiencePoints = 5;
      this.mirrorCopyOffsets = new Vec3d[2][4];

      for(int i = 0; i < 4; ++i) {
         this.mirrorCopyOffsets[0][i] = Vec3d.ZERO;
         this.mirrorCopyOffsets[1][i] = Vec3d.ZERO;
      }

   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new SpellcastingIllagerEntity.LookAtTargetGoal());
      this.goalSelector.add(4, new GiveInvisibilityGoal());
      this.goalSelector.add(5, new BlindTargetGoal());
      this.goalSelector.add(6, new BowAttackGoal(this, 0.5, 20, 15.0F));
      this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
      this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(2, (new ActiveTargetGoal(this, PlayerEntity.class, true)).setMaxTimeWithoutVisibility(300));
      this.targetSelector.add(3, (new ActiveTargetGoal(this, MerchantEntity.class, false)).setMaxTimeWithoutVisibility(300));
      this.targetSelector.add(3, (new ActiveTargetGoal(this, IronGolemEntity.class, false)).setMaxTimeWithoutVisibility(300));
   }

   public static DefaultAttributeContainer.Builder createIllusionerAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 18.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 32.0);
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   protected void initDataTracker() {
      super.initDataTracker();
   }

   public Box getVisibilityBoundingBox() {
      return this.getBoundingBox().expand(3.0, 0.0, 3.0);
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.world.isClient && this.isInvisible()) {
         --this.mirrorSpellTimer;
         if (this.mirrorSpellTimer < 0) {
            this.mirrorSpellTimer = 0;
         }

         if (this.hurtTime != 1 && this.age % 1200 != 0) {
            if (this.hurtTime == this.maxHurtTime - 1) {
               this.mirrorSpellTimer = 3;

               for(int k = 0; k < 4; ++k) {
                  this.mirrorCopyOffsets[0][k] = this.mirrorCopyOffsets[1][k];
                  this.mirrorCopyOffsets[1][k] = new Vec3d(0.0, 0.0, 0.0);
               }
            }
         } else {
            this.mirrorSpellTimer = 3;
            float f = -6.0F;
            int i = true;

            int j;
            for(j = 0; j < 4; ++j) {
               this.mirrorCopyOffsets[0][j] = this.mirrorCopyOffsets[1][j];
               this.mirrorCopyOffsets[1][j] = new Vec3d((double)(-6.0F + (float)this.random.nextInt(13)) * 0.5, (double)Math.max(0, this.random.nextInt(6) - 4), (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5);
            }

            for(j = 0; j < 16; ++j) {
               this.world.addParticle(ParticleTypes.CLOUD, this.getParticleX(0.5), this.getRandomBodyY(), this.offsetZ(0.5), 0.0, 0.0, 0.0);
            }

            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, this.getSoundCategory(), 1.0F, 1.0F, false);
         }
      }

   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_ILLUSIONER_AMBIENT;
   }

   public Vec3d[] getMirrorCopyOffsets(float tickDelta) {
      if (this.mirrorSpellTimer <= 0) {
         return this.mirrorCopyOffsets[1];
      } else {
         double d = (double)(((float)this.mirrorSpellTimer - tickDelta) / 3.0F);
         d = Math.pow(d, 0.25);
         Vec3d[] lvs = new Vec3d[4];

         for(int i = 0; i < 4; ++i) {
            lvs[i] = this.mirrorCopyOffsets[1][i].multiply(1.0 - d).add(this.mirrorCopyOffsets[0][i].multiply(d));
         }

         return lvs;
      }
   }

   public boolean isTeammate(Entity other) {
      if (super.isTeammate(other)) {
         return true;
      } else if (other instanceof LivingEntity && ((LivingEntity)other).getGroup() == EntityGroup.ILLAGER) {
         return this.getScoreboardTeam() == null && other.getScoreboardTeam() == null;
      } else {
         return false;
      }
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ILLUSIONER_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ILLUSIONER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ILLUSIONER_HURT;
   }

   protected SoundEvent getCastSpellSound() {
      return SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL;
   }

   public void addBonusForWave(int wave, boolean unused) {
   }

   public void attack(LivingEntity target, float pullProgress) {
      ItemStack lv = this.getProjectileType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
      PersistentProjectileEntity lv2 = ProjectileUtil.createArrowProjectile(this, lv, pullProgress);
      double d = target.getX() - this.getX();
      double e = target.getBodyY(0.3333333333333333) - lv2.getY();
      double g = target.getZ() - this.getZ();
      double h = Math.sqrt(d * d + g * g);
      lv2.setVelocity(d, e + h * 0.20000000298023224, g, 1.6F, (float)(14 - this.world.getDifficulty().getId() * 4));
      this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.world.spawnEntity(lv2);
   }

   public IllagerEntity.State getState() {
      if (this.isSpellcasting()) {
         return IllagerEntity.State.SPELLCASTING;
      } else {
         return this.isAttacking() ? IllagerEntity.State.BOW_AND_ARROW : IllagerEntity.State.CROSSED;
      }
   }

   class GiveInvisibilityGoal extends SpellcastingIllagerEntity.CastSpellGoal {
      GiveInvisibilityGoal() {
         super();
      }

      public boolean canStart() {
         if (!super.canStart()) {
            return false;
         } else {
            return !IllusionerEntity.this.hasStatusEffect(StatusEffects.INVISIBILITY);
         }
      }

      protected int getSpellTicks() {
         return 20;
      }

      protected int startTimeDelay() {
         return 340;
      }

      protected void castSpell() {
         IllusionerEntity.this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 1200));
      }

      @Nullable
      protected SoundEvent getSoundPrepare() {
         return SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR;
      }

      protected SpellcastingIllagerEntity.Spell getSpell() {
         return SpellcastingIllagerEntity.Spell.DISAPPEAR;
      }
   }

   class BlindTargetGoal extends SpellcastingIllagerEntity.CastSpellGoal {
      private int targetId;

      BlindTargetGoal() {
         super();
      }

      public boolean canStart() {
         if (!super.canStart()) {
            return false;
         } else if (IllusionerEntity.this.getTarget() == null) {
            return false;
         } else if (IllusionerEntity.this.getTarget().getId() == this.targetId) {
            return false;
         } else {
            return IllusionerEntity.this.world.getLocalDifficulty(IllusionerEntity.this.getBlockPos()).isHarderThan((float)Difficulty.NORMAL.ordinal());
         }
      }

      public void start() {
         super.start();
         LivingEntity lv = IllusionerEntity.this.getTarget();
         if (lv != null) {
            this.targetId = lv.getId();
         }

      }

      protected int getSpellTicks() {
         return 20;
      }

      protected int startTimeDelay() {
         return 180;
      }

      protected void castSpell() {
         IllusionerEntity.this.getTarget().addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 400), IllusionerEntity.this);
      }

      protected SoundEvent getSoundPrepare() {
         return SoundEvents.ENTITY_ILLUSIONER_PREPARE_BLINDNESS;
      }

      protected SpellcastingIllagerEntity.Spell getSpell() {
         return SpellcastingIllagerEntity.Spell.BLINDNESS;
      }
   }
}
