package net.minecraft.entity.projectile.thrown;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class PotionEntity extends ThrownItemEntity implements FlyingItemEntity {
   public static final double field_30667 = 4.0;
   private static final double field_30668 = 16.0;
   public static final Predicate AFFECTED_BY_WATER = (entity) -> {
      return entity.hurtByWater() || entity.isOnFire();
   };

   public PotionEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public PotionEntity(World world, LivingEntity owner) {
      super(EntityType.POTION, owner, world);
   }

   public PotionEntity(World world, double x, double y, double z) {
      super(EntityType.POTION, x, y, z, world);
   }

   protected Item getDefaultItem() {
      return Items.SPLASH_POTION;
   }

   protected float getGravity() {
      return 0.05F;
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      if (!this.world.isClient) {
         ItemStack lv = this.getStack();
         Potion lv2 = PotionUtil.getPotion(lv);
         List list = PotionUtil.getPotionEffects(lv);
         boolean bl = lv2 == Potions.WATER && list.isEmpty();
         Direction lv3 = blockHitResult.getSide();
         BlockPos lv4 = blockHitResult.getBlockPos();
         BlockPos lv5 = lv4.offset(lv3);
         if (bl) {
            this.extinguishFire(lv5);
            this.extinguishFire(lv5.offset(lv3.getOpposite()));
            Iterator var9 = Direction.Type.HORIZONTAL.iterator();

            while(var9.hasNext()) {
               Direction lv6 = (Direction)var9.next();
               this.extinguishFire(lv5.offset(lv6));
            }
         }

      }
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      if (!this.world.isClient) {
         ItemStack lv = this.getStack();
         Potion lv2 = PotionUtil.getPotion(lv);
         List list = PotionUtil.getPotionEffects(lv);
         boolean bl = lv2 == Potions.WATER && list.isEmpty();
         if (bl) {
            this.applyWater();
         } else if (!list.isEmpty()) {
            if (this.isLingering()) {
               this.applyLingeringPotion(lv, lv2);
            } else {
               this.applySplashPotion(list, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)hitResult).getEntity() : null);
            }
         }

         int i = lv2.hasInstantEffect() ? WorldEvents.INSTANT_SPLASH_POTION_SPLASHED : WorldEvents.SPLASH_POTION_SPLASHED;
         this.world.syncWorldEvent(i, this.getBlockPos(), PotionUtil.getColor(lv));
         this.discard();
      }
   }

   private void applyWater() {
      Box lv = this.getBoundingBox().expand(4.0, 2.0, 4.0);
      List list = this.world.getEntitiesByClass(LivingEntity.class, lv, AFFECTED_BY_WATER);
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         LivingEntity lv2 = (LivingEntity)var3.next();
         double d = this.squaredDistanceTo(lv2);
         if (d < 16.0) {
            if (lv2.hurtByWater()) {
               lv2.damage(this.getDamageSources().indirectMagic(this, this.getOwner()), 1.0F);
            }

            if (lv2.isOnFire() && lv2.isAlive()) {
               lv2.extinguishWithSound();
            }
         }
      }

      List list2 = this.world.getNonSpectatingEntities(AxolotlEntity.class, lv);
      Iterator var8 = list2.iterator();

      while(var8.hasNext()) {
         AxolotlEntity lv3 = (AxolotlEntity)var8.next();
         lv3.hydrateFromPotion();
      }

   }

   private void applySplashPotion(List statusEffects, @Nullable Entity entity) {
      Box lv = this.getBoundingBox().expand(4.0, 2.0, 4.0);
      List list2 = this.world.getNonSpectatingEntities(LivingEntity.class, lv);
      if (!list2.isEmpty()) {
         Entity lv2 = this.getEffectCause();
         Iterator var6 = list2.iterator();

         while(true) {
            LivingEntity lv3;
            double d;
            do {
               do {
                  if (!var6.hasNext()) {
                     return;
                  }

                  lv3 = (LivingEntity)var6.next();
               } while(!lv3.isAffectedBySplashPotions());

               d = this.squaredDistanceTo(lv3);
            } while(!(d < 16.0));

            double e;
            if (lv3 == entity) {
               e = 1.0;
            } else {
               e = 1.0 - Math.sqrt(d) / 4.0;
            }

            Iterator var12 = statusEffects.iterator();

            while(var12.hasNext()) {
               StatusEffectInstance lv4 = (StatusEffectInstance)var12.next();
               StatusEffect lv5 = lv4.getEffectType();
               if (lv5.isInstant()) {
                  lv5.applyInstantEffect(this, this.getOwner(), lv3, lv4.getAmplifier(), e);
               } else {
                  int i = lv4.mapDuration((ix) -> {
                     return (int)(e * (double)ix + 0.5);
                  });
                  StatusEffectInstance lv6 = new StatusEffectInstance(lv5, i, lv4.getAmplifier(), lv4.isAmbient(), lv4.shouldShowParticles());
                  if (!lv6.isDurationBelow(20)) {
                     lv3.addStatusEffect(lv6, lv2);
                  }
               }
            }
         }
      }
   }

   private void applyLingeringPotion(ItemStack stack, Potion potion) {
      AreaEffectCloudEntity lv = new AreaEffectCloudEntity(this.world, this.getX(), this.getY(), this.getZ());
      Entity lv2 = this.getOwner();
      if (lv2 instanceof LivingEntity) {
         lv.setOwner((LivingEntity)lv2);
      }

      lv.setRadius(3.0F);
      lv.setRadiusOnUse(-0.5F);
      lv.setWaitTime(10);
      lv.setRadiusGrowth(-lv.getRadius() / (float)lv.getDuration());
      lv.setPotion(potion);
      Iterator var5 = PotionUtil.getCustomPotionEffects(stack).iterator();

      while(var5.hasNext()) {
         StatusEffectInstance lv3 = (StatusEffectInstance)var5.next();
         lv.addEffect(new StatusEffectInstance(lv3));
      }

      NbtCompound lv4 = stack.getNbt();
      if (lv4 != null && lv4.contains("CustomPotionColor", NbtElement.NUMBER_TYPE)) {
         lv.setColor(lv4.getInt("CustomPotionColor"));
      }

      this.world.spawnEntity(lv);
   }

   private boolean isLingering() {
      return this.getStack().isOf(Items.LINGERING_POTION);
   }

   private void extinguishFire(BlockPos pos) {
      BlockState lv = this.world.getBlockState(pos);
      if (lv.isIn(BlockTags.FIRE)) {
         this.world.removeBlock(pos, false);
      } else if (AbstractCandleBlock.isLitCandle(lv)) {
         AbstractCandleBlock.extinguish((PlayerEntity)null, lv, this.world, pos);
      } else if (CampfireBlock.isLitCampfire(lv)) {
         this.world.syncWorldEvent((PlayerEntity)null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
         CampfireBlock.extinguish(this.getOwner(), this.world, pos, lv);
         this.world.setBlockState(pos, (BlockState)lv.with(CampfireBlock.LIT, false));
      }

   }
}
