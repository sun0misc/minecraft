package net.minecraft.entity.projectile;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public class ArrowEntity extends PersistentProjectileEntity {
   private static final int MAX_POTION_DURATION_TICKS = 600;
   private static final int NO_POTION_COLOR = -1;
   private static final TrackedData COLOR;
   private static final byte PARTICLE_EFFECT_STATUS = 0;
   private Potion potion;
   private final Set effects;
   private boolean colorSet;

   public ArrowEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.potion = Potions.EMPTY;
      this.effects = Sets.newHashSet();
   }

   public ArrowEntity(World world, double x, double y, double z) {
      super(EntityType.ARROW, x, y, z, world);
      this.potion = Potions.EMPTY;
      this.effects = Sets.newHashSet();
   }

   public ArrowEntity(World world, LivingEntity owner) {
      super(EntityType.ARROW, owner, world);
      this.potion = Potions.EMPTY;
      this.effects = Sets.newHashSet();
   }

   public void initFromStack(ItemStack stack) {
      if (stack.isOf(Items.TIPPED_ARROW)) {
         this.potion = PotionUtil.getPotion(stack);
         Collection collection = PotionUtil.getCustomPotionEffects(stack);
         if (!collection.isEmpty()) {
            Iterator var3 = collection.iterator();

            while(var3.hasNext()) {
               StatusEffectInstance lv = (StatusEffectInstance)var3.next();
               this.effects.add(new StatusEffectInstance(lv));
            }
         }

         int i = getCustomPotionColor(stack);
         if (i == -1) {
            this.initColor();
         } else {
            this.setColor(i);
         }
      } else if (stack.isOf(Items.ARROW)) {
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.dataTracker.set(COLOR, -1);
      }

   }

   public static int getCustomPotionColor(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      return lv != null && lv.contains("CustomPotionColor", NbtElement.NUMBER_TYPE) ? lv.getInt("CustomPotionColor") : -1;
   }

   private void initColor() {
      this.colorSet = false;
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.dataTracker.set(COLOR, -1);
      } else {
         this.dataTracker.set(COLOR, PotionUtil.getColor((Collection)PotionUtil.getPotionEffects(this.potion, this.effects)));
      }

   }

   public void addEffect(StatusEffectInstance effect) {
      this.effects.add(effect);
      this.getDataTracker().set(COLOR, PotionUtil.getColor((Collection)PotionUtil.getPotionEffects(this.potion, this.effects)));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(COLOR, -1);
   }

   public void tick() {
      super.tick();
      if (this.world.isClient) {
         if (this.inGround) {
            if (this.inGroundTime % 5 == 0) {
               this.spawnParticles(1);
            }
         } else {
            this.spawnParticles(2);
         }
      } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
         this.world.sendEntityStatus(this, (byte)0);
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.dataTracker.set(COLOR, -1);
      }

   }

   private void spawnParticles(int amount) {
      int j = this.getColor();
      if (j != -1 && amount > 0) {
         double d = (double)(j >> 16 & 255) / 255.0;
         double e = (double)(j >> 8 & 255) / 255.0;
         double f = (double)(j >> 0 & 255) / 255.0;

         for(int k = 0; k < amount; ++k) {
            this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), d, e, f);
         }

      }
   }

   public int getColor() {
      return (Integer)this.dataTracker.get(COLOR);
   }

   private void setColor(int color) {
      this.colorSet = true;
      this.dataTracker.set(COLOR, color);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.potion != Potions.EMPTY) {
         nbt.putString("Potion", Registries.POTION.getId(this.potion).toString());
      }

      if (this.colorSet) {
         nbt.putInt("Color", this.getColor());
      }

      if (!this.effects.isEmpty()) {
         NbtList lv = new NbtList();
         Iterator var3 = this.effects.iterator();

         while(var3.hasNext()) {
            StatusEffectInstance lv2 = (StatusEffectInstance)var3.next();
            lv.add(lv2.writeNbt(new NbtCompound()));
         }

         nbt.put("CustomPotionEffects", lv);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Potion", NbtElement.STRING_TYPE)) {
         this.potion = PotionUtil.getPotion(nbt);
      }

      Iterator var2 = PotionUtil.getCustomPotionEffects(nbt).iterator();

      while(var2.hasNext()) {
         StatusEffectInstance lv = (StatusEffectInstance)var2.next();
         this.addEffect(lv);
      }

      if (nbt.contains("Color", NbtElement.NUMBER_TYPE)) {
         this.setColor(nbt.getInt("Color"));
      } else {
         this.initColor();
      }

   }

   protected void onHit(LivingEntity target) {
      super.onHit(target);
      Entity lv = this.getEffectCause();
      Iterator var3 = this.potion.getEffects().iterator();

      StatusEffectInstance lv2;
      while(var3.hasNext()) {
         lv2 = (StatusEffectInstance)var3.next();
         target.addStatusEffect(new StatusEffectInstance(lv2.getEffectType(), Math.max(lv2.mapDuration((i) -> {
            return i / 8;
         }), 1), lv2.getAmplifier(), lv2.isAmbient(), lv2.shouldShowParticles()), lv);
      }

      if (!this.effects.isEmpty()) {
         var3 = this.effects.iterator();

         while(var3.hasNext()) {
            lv2 = (StatusEffectInstance)var3.next();
            target.addStatusEffect(lv2, lv);
         }
      }

   }

   protected ItemStack asItemStack() {
      if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
         return new ItemStack(Items.ARROW);
      } else {
         ItemStack lv = new ItemStack(Items.TIPPED_ARROW);
         PotionUtil.setPotion(lv, this.potion);
         PotionUtil.setCustomPotionEffects(lv, this.effects);
         if (this.colorSet) {
            lv.getOrCreateNbt().putInt("CustomPotionColor", this.getColor());
         }

         return lv;
      }
   }

   public void handleStatus(byte status) {
      if (status == 0) {
         int i = this.getColor();
         if (i != -1) {
            double d = (double)(i >> 16 & 255) / 255.0;
            double e = (double)(i >> 8 & 255) / 255.0;
            double f = (double)(i >> 0 & 255) / 255.0;

            for(int j = 0; j < 20; ++j) {
               this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), d, e, f);
            }
         }
      } else {
         super.handleStatus(status);
      }

   }

   static {
      COLOR = DataTracker.registerData(ArrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }
}
