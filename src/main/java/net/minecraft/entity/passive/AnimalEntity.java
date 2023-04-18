package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class AnimalEntity extends PassiveEntity {
   protected static final int BREEDING_COOLDOWN = 6000;
   private int loveTicks;
   @Nullable
   private UUID lovingPlayer;

   protected AnimalEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 16.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
   }

   protected void mobTick() {
      if (this.getBreedingAge() != 0) {
         this.loveTicks = 0;
      }

      super.mobTick();
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.getBreedingAge() != 0) {
         this.loveTicks = 0;
      }

      if (this.loveTicks > 0) {
         --this.loveTicks;
         if (this.loveTicks % 10 == 0) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.world.addParticle(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
         }
      }

   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.loveTicks = 0;
         return super.damage(source, amount);
      }
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK) ? 10.0F : world.getPhototaxisFavor(pos);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("InLove", this.loveTicks);
      if (this.lovingPlayer != null) {
         nbt.putUuid("LoveCause", this.lovingPlayer);
      }

   }

   public double getHeightOffset() {
      return 0.14;
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.loveTicks = nbt.getInt("InLove");
      this.lovingPlayer = nbt.containsUuid("LoveCause") ? nbt.getUuid("LoveCause") : null;
   }

   public static boolean isValidNaturalSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isIn(BlockTags.ANIMALS_SPAWNABLE_ON) && isLightLevelValidForNaturalSpawn(world, pos);
   }

   protected static boolean isLightLevelValidForNaturalSpawn(BlockRenderView world, BlockPos pos) {
      return world.getBaseLightLevel(pos, 0) > 8;
   }

   public int getMinAmbientSoundDelay() {
      return 120;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }

   public int getXpToDrop() {
      return 1 + this.world.random.nextInt(3);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return stack.isOf(Items.WHEAT);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (this.isBreedingItem(lv)) {
         int i = this.getBreedingAge();
         if (!this.world.isClient && i == 0 && this.canEat()) {
            this.eat(player, hand, lv);
            this.lovePlayer(player);
            return ActionResult.SUCCESS;
         }

         if (this.isBaby()) {
            this.eat(player, hand, lv);
            this.growUp(toGrowUpAge(-i), true);
            return ActionResult.success(this.world.isClient);
         }

         if (this.world.isClient) {
            return ActionResult.CONSUME;
         }
      }

      return super.interactMob(player, hand);
   }

   protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
      if (!player.getAbilities().creativeMode) {
         stack.decrement(1);
      }

   }

   public boolean canEat() {
      return this.loveTicks <= 0;
   }

   public void lovePlayer(@Nullable PlayerEntity player) {
      this.loveTicks = 600;
      if (player != null) {
         this.lovingPlayer = player.getUuid();
      }

      this.world.sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);
   }

   public void setLoveTicks(int loveTicks) {
      this.loveTicks = loveTicks;
   }

   public int getLoveTicks() {
      return this.loveTicks;
   }

   @Nullable
   public ServerPlayerEntity getLovingPlayer() {
      if (this.lovingPlayer == null) {
         return null;
      } else {
         PlayerEntity lv = this.world.getPlayerByUuid(this.lovingPlayer);
         return lv instanceof ServerPlayerEntity ? (ServerPlayerEntity)lv : null;
      }
   }

   public boolean isInLove() {
      return this.loveTicks > 0;
   }

   public void resetLoveTicks() {
      this.loveTicks = 0;
   }

   public boolean canBreedWith(AnimalEntity other) {
      if (other == this) {
         return false;
      } else if (other.getClass() != this.getClass()) {
         return false;
      } else {
         return this.isInLove() && other.isInLove();
      }
   }

   public void breed(ServerWorld world, AnimalEntity other) {
      PassiveEntity lv = this.createChild(world, other);
      if (lv != null) {
         lv.setBaby(true);
         lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
         this.breed(world, other, lv);
         world.spawnEntityAndPassengers(lv);
      }
   }

   public void breed(ServerWorld world, AnimalEntity other, @Nullable PassiveEntity baby) {
      Optional.ofNullable(this.getLovingPlayer()).or(() -> {
         return Optional.ofNullable(other.getLovingPlayer());
      }).ifPresent((player) -> {
         player.incrementStat(Stats.ANIMALS_BRED);
         Criteria.BRED_ANIMALS.trigger(player, this, other, baby);
      });
      this.setBreedingAge(6000);
      other.setBreedingAge(6000);
      this.resetLoveTicks();
      other.resetLoveTicks();
      world.sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);
      if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
         world.spawnEntity(new ExperienceOrbEntity(world, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
      }

   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.ADD_BREEDING_PARTICLES) {
         for(int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.world.addParticle(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
         }
      } else {
         super.handleStatus(status);
      }

   }
}
