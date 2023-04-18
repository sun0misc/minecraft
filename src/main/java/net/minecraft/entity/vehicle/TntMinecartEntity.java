package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class TntMinecartEntity extends AbstractMinecartEntity {
   private static final byte PRIME_TNT_STATUS = 10;
   private int fuseTicks = -1;

   public TntMinecartEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public TntMinecartEntity(World world, double x, double y, double z) {
      super(EntityType.TNT_MINECART, world, x, y, z);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.TNT;
   }

   public BlockState getDefaultContainedBlock() {
      return Blocks.TNT.getDefaultState();
   }

   public void tick() {
      super.tick();
      if (this.fuseTicks > 0) {
         --this.fuseTicks;
         this.world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
      } else if (this.fuseTicks == 0) {
         this.explode(this.getVelocity().horizontalLengthSquared());
      }

      if (this.horizontalCollision) {
         double d = this.getVelocity().horizontalLengthSquared();
         if (d >= 0.009999999776482582) {
            this.explode(d);
         }
      }

   }

   public boolean damage(DamageSource source, float amount) {
      Entity lv = source.getSource();
      if (lv instanceof PersistentProjectileEntity lv2) {
         if (lv2.isOnFire()) {
            DamageSource lv3 = this.getDamageSources().explosion(this, source.getAttacker());
            this.explode(lv3, lv2.getVelocity().lengthSquared());
         }
      }

      return super.damage(source, amount);
   }

   public void dropItems(DamageSource damageSource) {
      double d = this.getVelocity().horizontalLengthSquared();
      if (!damageSource.isIn(DamageTypeTags.IS_FIRE) && !damageSource.isIn(DamageTypeTags.IS_EXPLOSION) && !(d >= 0.009999999776482582)) {
         super.dropItems(damageSource);
      } else {
         if (this.fuseTicks < 0) {
            this.prime();
            this.fuseTicks = this.random.nextInt(20) + this.random.nextInt(20);
         }

      }
   }

   protected Item getItem() {
      return Items.TNT_MINECART;
   }

   protected void explode(double power) {
      this.explode((DamageSource)null, power);
   }

   protected void explode(@Nullable DamageSource damageSource, double power) {
      if (!this.world.isClient) {
         double e = Math.sqrt(power);
         if (e > 5.0) {
            e = 5.0;
         }

         this.world.createExplosion(this, damageSource, (ExplosionBehavior)null, this.getX(), this.getY(), this.getZ(), (float)(4.0 + this.random.nextDouble() * 1.5 * e), false, World.ExplosionSourceType.TNT);
         this.discard();
      }

   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      if (fallDistance >= 3.0F) {
         float h = fallDistance / 10.0F;
         this.explode((double)(h * h));
      }

      return super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
   }

   public void onActivatorRail(int x, int y, int z, boolean powered) {
      if (powered && this.fuseTicks < 0) {
         this.prime();
      }

   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART) {
         this.prime();
      } else {
         super.handleStatus(status);
      }

   }

   public void prime() {
      this.fuseTicks = 80;
      if (!this.world.isClient) {
         this.world.sendEntityStatus(this, EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART);
         if (!this.isSilent()) {
            this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   public int getFuseTicks() {
      return this.fuseTicks;
   }

   public boolean isPrimed() {
      return this.fuseTicks > -1;
   }

   public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
      return !this.isPrimed() || !blockState.isIn(BlockTags.RAILS) && !world.getBlockState(pos.up()).isIn(BlockTags.RAILS) ? super.getEffectiveExplosionResistance(explosion, world, pos, blockState, fluidState, max) : 0.0F;
   }

   public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
      return !this.isPrimed() || !state.isIn(BlockTags.RAILS) && !world.getBlockState(pos.up()).isIn(BlockTags.RAILS) ? super.canExplosionDestroyBlock(explosion, world, pos, state, explosionPower) : false;
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("TNTFuse", NbtElement.NUMBER_TYPE)) {
         this.fuseTicks = nbt.getInt("TNTFuse");
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("TNTFuse", this.fuseTicks);
   }
}
