package net.minecraft.entity.decoration;

import java.util.Optional;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class EndCrystalEntity extends Entity {
   private static final TrackedData BEAM_TARGET;
   private static final TrackedData SHOW_BOTTOM;
   public int endCrystalAge;

   public EndCrystalEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.intersectionChecked = true;
      this.endCrystalAge = this.random.nextInt(100000);
   }

   public EndCrystalEntity(World world, double x, double y, double z) {
      this(EntityType.END_CRYSTAL, world);
      this.setPosition(x, y, z);
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.NONE;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(BEAM_TARGET, Optional.empty());
      this.getDataTracker().startTracking(SHOW_BOTTOM, true);
   }

   public void tick() {
      ++this.endCrystalAge;
      if (this.world instanceof ServerWorld) {
         BlockPos lv = this.getBlockPos();
         if (((ServerWorld)this.world).getEnderDragonFight() != null && this.world.getBlockState(lv).isAir()) {
            this.world.setBlockState(lv, AbstractFireBlock.getState(this.world, lv));
         }
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      if (this.getBeamTarget() != null) {
         nbt.put("BeamTarget", NbtHelper.fromBlockPos(this.getBeamTarget()));
      }

      nbt.putBoolean("ShowBottom", this.shouldShowBottom());
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      if (nbt.contains("BeamTarget", NbtElement.COMPOUND_TYPE)) {
         this.setBeamTarget(NbtHelper.toBlockPos(nbt.getCompound("BeamTarget")));
      }

      if (nbt.contains("ShowBottom", NbtElement.BYTE_TYPE)) {
         this.setShowBottom(nbt.getBoolean("ShowBottom"));
      }

   }

   public boolean canHit() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (source.getAttacker() instanceof EnderDragonEntity) {
         return false;
      } else {
         if (!this.isRemoved() && !this.world.isClient) {
            this.remove(Entity.RemovalReason.KILLED);
            if (!source.isIn(DamageTypeTags.IS_EXPLOSION)) {
               DamageSource lv = source.getAttacker() != null ? this.getDamageSources().explosion(this, source.getAttacker()) : null;
               this.world.createExplosion(this, lv, (ExplosionBehavior)null, this.getX(), this.getY(), this.getZ(), 6.0F, false, World.ExplosionSourceType.BLOCK);
            }

            this.crystalDestroyed(source);
         }

         return true;
      }
   }

   public void kill() {
      this.crystalDestroyed(this.getDamageSources().generic());
      super.kill();
   }

   private void crystalDestroyed(DamageSource source) {
      if (this.world instanceof ServerWorld) {
         EnderDragonFight lv = ((ServerWorld)this.world).getEnderDragonFight();
         if (lv != null) {
            lv.crystalDestroyed(this, source);
         }
      }

   }

   public void setBeamTarget(@Nullable BlockPos beamTarget) {
      this.getDataTracker().set(BEAM_TARGET, Optional.ofNullable(beamTarget));
   }

   @Nullable
   public BlockPos getBeamTarget() {
      return (BlockPos)((Optional)this.getDataTracker().get(BEAM_TARGET)).orElse((Object)null);
   }

   public void setShowBottom(boolean showBottom) {
      this.getDataTracker().set(SHOW_BOTTOM, showBottom);
   }

   public boolean shouldShowBottom() {
      return (Boolean)this.getDataTracker().get(SHOW_BOTTOM);
   }

   public boolean shouldRender(double distance) {
      return super.shouldRender(distance) || this.getBeamTarget() != null;
   }

   public ItemStack getPickBlockStack() {
      return new ItemStack(Items.END_CRYSTAL);
   }

   static {
      BEAM_TARGET = DataTracker.registerData(EndCrystalEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
      SHOW_BOTTOM = DataTracker.registerData(EndCrystalEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
