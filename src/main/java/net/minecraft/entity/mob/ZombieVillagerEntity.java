package net.minecraft.entity.mob;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ZombieVillagerEntity extends ZombieEntity implements VillagerDataContainer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final TrackedData CONVERTING;
   private static final TrackedData VILLAGER_DATA;
   private static final int field_30523 = 3600;
   private static final int field_30520 = 6000;
   private static final int field_30521 = 14;
   private static final int field_30522 = 4;
   private int conversionTimer;
   @Nullable
   private UUID converter;
   @Nullable
   private NbtElement gossipData;
   @Nullable
   private NbtCompound offerData;
   private int xp;

   public ZombieVillagerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      Registries.VILLAGER_PROFESSION.getRandom(this.random).ifPresent((profession) -> {
         this.setVillagerData(this.getVillagerData().withProfession((VillagerProfession)profession.value()));
      });
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CONVERTING, false);
      this.dataTracker.startTracking(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      DataResult var10000 = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData());
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
         nbt.put("VillagerData", arg2);
      });
      if (this.offerData != null) {
         nbt.put("Offers", this.offerData);
      }

      if (this.gossipData != null) {
         nbt.put("Gossips", this.gossipData);
      }

      nbt.putInt("ConversionTime", this.isConverting() ? this.conversionTimer : -1);
      if (this.converter != null) {
         nbt.putUuid("ConversionPlayer", this.converter);
      }

      nbt.putInt("Xp", this.xp);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE)) {
         DataResult dataResult = VillagerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, nbt.get("VillagerData")));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         dataResult.resultOrPartial(var10001::error).ifPresent(this::setVillagerData);
      }

      if (nbt.contains("Offers", NbtElement.COMPOUND_TYPE)) {
         this.offerData = nbt.getCompound("Offers");
      }

      if (nbt.contains("Gossips", NbtElement.LIST_TYPE)) {
         this.gossipData = nbt.getList("Gossips", NbtElement.COMPOUND_TYPE);
      }

      if (nbt.contains("ConversionTime", NbtElement.NUMBER_TYPE) && nbt.getInt("ConversionTime") > -1) {
         this.setConverting(nbt.containsUuid("ConversionPlayer") ? nbt.getUuid("ConversionPlayer") : null, nbt.getInt("ConversionTime"));
      }

      if (nbt.contains("Xp", NbtElement.INT_TYPE)) {
         this.xp = nbt.getInt("Xp");
      }

   }

   public void tick() {
      if (!this.world.isClient && this.isAlive() && this.isConverting()) {
         int i = this.getConversionRate();
         this.conversionTimer -= i;
         if (this.conversionTimer <= 0) {
            this.finishConversion((ServerWorld)this.world);
         }
      }

      super.tick();
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isOf(Items.GOLDEN_APPLE)) {
         if (this.hasStatusEffect(StatusEffects.WEAKNESS)) {
            if (!player.getAbilities().creativeMode) {
               lv.decrement(1);
            }

            if (!this.world.isClient) {
               this.setConverting(player.getUuid(), this.random.nextInt(2401) + 3600);
            }

            return ActionResult.SUCCESS;
         } else {
            return ActionResult.CONSUME;
         }
      } else {
         return super.interactMob(player, hand);
      }
   }

   protected boolean canConvertInWater() {
      return false;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return !this.isConverting() && this.xp == 0;
   }

   public boolean isConverting() {
      return (Boolean)this.getDataTracker().get(CONVERTING);
   }

   private void setConverting(@Nullable UUID uuid, int delay) {
      this.converter = uuid;
      this.conversionTimer = delay;
      this.getDataTracker().set(CONVERTING, true);
      this.removeStatusEffect(StatusEffects.WEAKNESS);
      this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, delay, Math.min(this.world.getDifficulty().getId() - 1, 0)));
      this.world.sendEntityStatus(this, EntityStatuses.PLAY_CURE_ZOMBIE_VILLAGER_SOUND);
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.PLAY_CURE_ZOMBIE_VILLAGER_SOUND) {
         if (!this.isSilent()) {
            this.world.playSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.getSoundCategory(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
         }

      } else {
         super.handleStatus(status);
      }
   }

   private void finishConversion(ServerWorld world) {
      VillagerEntity lv = (VillagerEntity)this.convertTo(EntityType.VILLAGER, false);
      EquipmentSlot[] var3 = EquipmentSlot.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         EquipmentSlot lv2 = var3[var5];
         ItemStack lv3 = this.getEquippedStack(lv2);
         if (!lv3.isEmpty()) {
            if (EnchantmentHelper.hasBindingCurse(lv3)) {
               lv.getStackReference(lv2.getEntitySlotId() + 300).set(lv3);
            } else {
               double d = (double)this.getDropChance(lv2);
               if (d > 1.0) {
                  this.dropStack(lv3);
               }
            }
         }
      }

      lv.setVillagerData(this.getVillagerData());
      if (this.gossipData != null) {
         lv.readGossipDataNbt(this.gossipData);
      }

      if (this.offerData != null) {
         lv.setOffers(new TradeOfferList(this.offerData));
      }

      lv.setExperience(this.xp);
      lv.initialize(world, world.getLocalDifficulty(lv.getBlockPos()), SpawnReason.CONVERSION, (EntityData)null, (NbtCompound)null);
      lv.reinitializeBrain(world);
      if (this.converter != null) {
         PlayerEntity lv4 = world.getPlayerByUuid(this.converter);
         if (lv4 instanceof ServerPlayerEntity) {
            Criteria.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayerEntity)lv4, this, lv);
            world.handleInteraction(EntityInteraction.ZOMBIE_VILLAGER_CURED, lv4, lv);
         }
      }

      lv.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
      if (!this.isSilent()) {
         world.syncWorldEvent((PlayerEntity)null, WorldEvents.ZOMBIE_VILLAGER_CURED, this.getBlockPos(), 0);
      }

   }

   private int getConversionRate() {
      int i = 1;
      if (this.random.nextFloat() < 0.01F) {
         int j = 0;
         BlockPos.Mutable lv = new BlockPos.Mutable();

         for(int k = (int)this.getX() - 4; k < (int)this.getX() + 4 && j < 14; ++k) {
            for(int l = (int)this.getY() - 4; l < (int)this.getY() + 4 && j < 14; ++l) {
               for(int m = (int)this.getZ() - 4; m < (int)this.getZ() + 4 && j < 14; ++m) {
                  BlockState lv2 = this.world.getBlockState(lv.set(k, l, m));
                  if (lv2.isOf(Blocks.IRON_BARS) || lv2.getBlock() instanceof BedBlock) {
                     if (this.random.nextFloat() < 0.3F) {
                        ++i;
                     }

                     ++j;
                  }
               }
            }
         }
      }

      return i;
   }

   public float getSoundPitch() {
      return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.0F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
   }

   public SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
   }

   public SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT;
   }

   public SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH;
   }

   public SoundEvent getStepSound() {
      return SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP;
   }

   protected ItemStack getSkull() {
      return ItemStack.EMPTY;
   }

   public void setOfferData(NbtCompound offerData) {
      this.offerData = offerData;
   }

   public void setGossipData(NbtElement gossipData) {
      this.gossipData = gossipData;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.setVillagerData(this.getVillagerData().withType(VillagerType.forBiome(world.getBiome(this.getBlockPos()))));
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   public void setVillagerData(VillagerData villagerData) {
      VillagerData lv = this.getVillagerData();
      if (lv.getProfession() != villagerData.getProfession()) {
         this.offerData = null;
      }

      this.dataTracker.set(VILLAGER_DATA, villagerData);
   }

   public VillagerData getVillagerData() {
      return (VillagerData)this.dataTracker.get(VILLAGER_DATA);
   }

   public int getXp() {
      return this.xp;
   }

   public void setXp(int xp) {
      this.xp = xp;
   }

   static {
      CONVERTING = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      VILLAGER_DATA = DataTracker.registerData(ZombieVillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
   }
}
