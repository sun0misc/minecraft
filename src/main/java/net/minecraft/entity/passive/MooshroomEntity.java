package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class MooshroomEntity extends CowEntity implements Shearable, VariantHolder {
   private static final TrackedData TYPE;
   private static final int MUTATION_CHANCE = 1024;
   @Nullable
   private StatusEffect stewEffect;
   private int stewEffectDuration;
   @Nullable
   private UUID lightningId;

   public MooshroomEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return world.getBlockState(pos.down()).isOf(Blocks.MYCELIUM) ? 10.0F : world.getPhototaxisFavor(pos);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isIn(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      UUID uUID = lightning.getUuid();
      if (!uUID.equals(this.lightningId)) {
         this.setVariant(this.getVariant() == MooshroomEntity.Type.RED ? MooshroomEntity.Type.BROWN : MooshroomEntity.Type.RED);
         this.lightningId = uUID;
         this.playSound(SoundEvents.ENTITY_MOOSHROOM_CONVERT, 2.0F, 1.0F);
      }

   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(TYPE, MooshroomEntity.Type.RED.name);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isOf(Items.BOWL) && !this.isBaby()) {
         boolean bl = false;
         ItemStack lv2;
         if (this.stewEffect != null) {
            bl = true;
            lv2 = new ItemStack(Items.SUSPICIOUS_STEW);
            SuspiciousStewItem.addEffectToStew(lv2, this.stewEffect, this.stewEffectDuration);
            this.stewEffect = null;
            this.stewEffectDuration = 0;
         } else {
            lv2 = new ItemStack(Items.MUSHROOM_STEW);
         }

         ItemStack lv3 = ItemUsage.exchangeStack(lv, player, lv2, false);
         player.setStackInHand(hand, lv3);
         SoundEvent lv4;
         if (bl) {
            lv4 = SoundEvents.ENTITY_MOOSHROOM_SUSPICIOUS_MILK;
         } else {
            lv4 = SoundEvents.ENTITY_MOOSHROOM_MILK;
         }

         this.playSound(lv4, 1.0F, 1.0F);
         return ActionResult.success(this.world.isClient);
      } else if (lv.isOf(Items.SHEARS) && this.isShearable()) {
         this.sheared(SoundCategory.PLAYERS);
         this.emitGameEvent(GameEvent.SHEAR, player);
         if (!this.world.isClient) {
            lv.damage(1, (LivingEntity)player, (Consumer)((playerx) -> {
               playerx.sendToolBreakStatus(hand);
            }));
         }

         return ActionResult.success(this.world.isClient);
      } else if (this.getVariant() == MooshroomEntity.Type.BROWN && lv.isIn(ItemTags.SMALL_FLOWERS)) {
         if (this.stewEffect != null) {
            for(int i = 0; i < 2; ++i) {
               this.world.addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0, this.getBodyY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
            }
         } else {
            Optional optional = this.getStewEffectFrom(lv);
            if (!optional.isPresent()) {
               return ActionResult.PASS;
            }

            Pair pair = (Pair)optional.get();
            if (!player.getAbilities().creativeMode) {
               lv.decrement(1);
            }

            for(int j = 0; j < 4; ++j) {
               this.world.addParticle(ParticleTypes.EFFECT, this.getX() + this.random.nextDouble() / 2.0, this.getBodyY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
            }

            this.stewEffect = (StatusEffect)pair.getLeft();
            this.stewEffectDuration = (Integer)pair.getRight();
            this.playSound(SoundEvents.ENTITY_MOOSHROOM_EAT, 2.0F, 1.0F);
         }

         return ActionResult.success(this.world.isClient);
      } else {
         return super.interactMob(player, hand);
      }
   }

   public void sheared(SoundCategory shearedSoundCategory) {
      this.world.playSoundFromEntity((PlayerEntity)null, this, SoundEvents.ENTITY_MOOSHROOM_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
      if (!this.world.isClient()) {
         CowEntity lv = (CowEntity)EntityType.COW.create(this.world);
         if (lv != null) {
            ((ServerWorld)this.world).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            this.discard();
            lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            lv.setHealth(this.getHealth());
            lv.bodyYaw = this.bodyYaw;
            if (this.hasCustomName()) {
               lv.setCustomName(this.getCustomName());
               lv.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistent()) {
               lv.setPersistent();
            }

            lv.setInvulnerable(this.isInvulnerable());
            this.world.spawnEntity(lv);

            for(int i = 0; i < 5; ++i) {
               this.world.spawnEntity(new ItemEntity(this.world, this.getX(), this.getBodyY(1.0), this.getZ(), new ItemStack(this.getVariant().mushroom.getBlock())));
            }
         }
      }

   }

   public boolean isShearable() {
      return this.isAlive() && !this.isBaby();
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putString("Type", this.getVariant().asString());
      if (this.stewEffect != null) {
         nbt.putInt("EffectId", StatusEffect.getRawId(this.stewEffect));
         nbt.putInt("EffectDuration", this.stewEffectDuration);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setVariant(MooshroomEntity.Type.fromName(nbt.getString("Type")));
      if (nbt.contains("EffectId", NbtElement.NUMBER_TYPE)) {
         this.stewEffect = StatusEffect.byRawId(nbt.getInt("EffectId"));
      }

      if (nbt.contains("EffectDuration", NbtElement.NUMBER_TYPE)) {
         this.stewEffectDuration = nbt.getInt("EffectDuration");
      }

   }

   private Optional getStewEffectFrom(ItemStack flower) {
      SuspiciousStewIngredient lv = SuspiciousStewIngredient.of(flower.getItem());
      return lv != null ? Optional.of(Pair.of(lv.getEffectInStew(), lv.getEffectInStewDuration())) : Optional.empty();
   }

   public void setVariant(Type arg) {
      this.dataTracker.set(TYPE, arg.name);
   }

   public Type getVariant() {
      return MooshroomEntity.Type.fromName((String)this.dataTracker.get(TYPE));
   }

   @Nullable
   public MooshroomEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      MooshroomEntity lv = (MooshroomEntity)EntityType.MOOSHROOM.create(arg);
      if (lv != null) {
         lv.setVariant(this.chooseBabyType((MooshroomEntity)arg2));
      }

      return lv;
   }

   private Type chooseBabyType(MooshroomEntity mooshroom) {
      Type lv = this.getVariant();
      Type lv2 = mooshroom.getVariant();
      Type lv3;
      if (lv == lv2 && this.random.nextInt(1024) == 0) {
         lv3 = lv == MooshroomEntity.Type.BROWN ? MooshroomEntity.Type.RED : MooshroomEntity.Type.BROWN;
      } else {
         lv3 = this.random.nextBoolean() ? lv : lv2;
      }

      return lv3;
   }

   // $FF: synthetic method
   @Nullable
   public CowEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      return this.createChild(arg, arg2);
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   // $FF: synthetic method
   public Object getVariant() {
      return this.getVariant();
   }

   static {
      TYPE = DataTracker.registerData(MooshroomEntity.class, TrackedDataHandlerRegistry.STRING);
   }

   public static enum Type implements StringIdentifiable {
      RED("red", Blocks.RED_MUSHROOM.getDefaultState()),
      BROWN("brown", Blocks.BROWN_MUSHROOM.getDefaultState());

      public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(Type::values);
      final String name;
      final BlockState mushroom;

      private Type(String name, BlockState mushroom) {
         this.name = name;
         this.mushroom = mushroom;
      }

      public BlockState getMushroomState() {
         return this.mushroom;
      }

      public String asString() {
         return this.name;
      }

      static Type fromName(String name) {
         return (Type)CODEC.byId(name, RED);
      }

      // $FF: synthetic method
      private static Type[] method_36639() {
         return new Type[]{RED, BROWN};
      }
   }
}
