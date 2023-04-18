package net.minecraft.entity;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class LightningEntity extends Entity {
   private static final int field_30062 = 2;
   private static final double field_33906 = 3.0;
   private static final double field_33907 = 15.0;
   private int ambientTick;
   public long seed;
   private int remainingActions;
   private boolean cosmetic;
   @Nullable
   private ServerPlayerEntity channeler;
   private final Set struckEntities = Sets.newHashSet();
   private int blocksSetOnFire;

   public LightningEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.ignoreCameraFrustum = true;
      this.ambientTick = 2;
      this.seed = this.random.nextLong();
      this.remainingActions = this.random.nextInt(3) + 1;
   }

   public void setCosmetic(boolean cosmetic) {
      this.cosmetic = cosmetic;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.WEATHER;
   }

   @Nullable
   public ServerPlayerEntity getChanneler() {
      return this.channeler;
   }

   public void setChanneler(@Nullable ServerPlayerEntity channeler) {
      this.channeler = channeler;
   }

   private void powerLightningRod() {
      BlockPos lv = this.getAffectedBlockPos();
      BlockState lv2 = this.world.getBlockState(lv);
      if (lv2.isOf(Blocks.LIGHTNING_ROD)) {
         ((LightningRodBlock)lv2.getBlock()).setPowered(lv2, this.world, lv);
      }

   }

   public void tick() {
      super.tick();
      if (this.ambientTick == 2) {
         if (this.world.isClient()) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F + this.random.nextFloat() * 0.2F, false);
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.random.nextFloat() * 0.2F, false);
         } else {
            Difficulty lv = this.world.getDifficulty();
            if (lv == Difficulty.NORMAL || lv == Difficulty.HARD) {
               this.spawnFire(4);
            }

            this.powerLightningRod();
            cleanOxidation(this.world, this.getAffectedBlockPos());
            this.emitGameEvent(GameEvent.LIGHTNING_STRIKE);
         }
      }

      --this.ambientTick;
      Iterator var2;
      List list;
      if (this.ambientTick < 0) {
         if (this.remainingActions == 0) {
            if (this.world instanceof ServerWorld) {
               list = this.world.getOtherEntities(this, new Box(this.getX() - 15.0, this.getY() - 15.0, this.getZ() - 15.0, this.getX() + 15.0, this.getY() + 6.0 + 15.0, this.getZ() + 15.0), (arg) -> {
                  return arg.isAlive() && !this.struckEntities.contains(arg);
               });
               var2 = ((ServerWorld)this.world).getPlayers((arg) -> {
                  return arg.distanceTo(this) < 256.0F;
               }).iterator();

               while(var2.hasNext()) {
                  ServerPlayerEntity lv2 = (ServerPlayerEntity)var2.next();
                  Criteria.LIGHTNING_STRIKE.trigger(lv2, this, list);
               }
            }

            this.discard();
         } else if (this.ambientTick < -this.random.nextInt(10)) {
            --this.remainingActions;
            this.ambientTick = 1;
            this.seed = this.random.nextLong();
            this.spawnFire(0);
         }
      }

      if (this.ambientTick >= 0) {
         if (!(this.world instanceof ServerWorld)) {
            this.world.setLightningTicksLeft(2);
         } else if (!this.cosmetic) {
            list = this.world.getOtherEntities(this, new Box(this.getX() - 3.0, this.getY() - 3.0, this.getZ() - 3.0, this.getX() + 3.0, this.getY() + 6.0 + 3.0, this.getZ() + 3.0), Entity::isAlive);
            var2 = list.iterator();

            while(var2.hasNext()) {
               Entity lv3 = (Entity)var2.next();
               lv3.onStruckByLightning((ServerWorld)this.world, this);
            }

            this.struckEntities.addAll(list);
            if (this.channeler != null) {
               Criteria.CHANNELED_LIGHTNING.trigger(this.channeler, list);
            }
         }
      }

   }

   private BlockPos getAffectedBlockPos() {
      Vec3d lv = this.getPos();
      return BlockPos.ofFloored(lv.x, lv.y - 1.0E-6, lv.z);
   }

   private void spawnFire(int spreadAttempts) {
      if (!this.cosmetic && !this.world.isClient && this.world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
         BlockPos lv = this.getBlockPos();
         BlockState lv2 = AbstractFireBlock.getState(this.world, lv);
         if (this.world.getBlockState(lv).isAir() && lv2.canPlaceAt(this.world, lv)) {
            this.world.setBlockState(lv, lv2);
            ++this.blocksSetOnFire;
         }

         for(int j = 0; j < spreadAttempts; ++j) {
            BlockPos lv3 = lv.add(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
            lv2 = AbstractFireBlock.getState(this.world, lv3);
            if (this.world.getBlockState(lv3).isAir() && lv2.canPlaceAt(this.world, lv3)) {
               this.world.setBlockState(lv3, lv2);
               ++this.blocksSetOnFire;
            }
         }

      }
   }

   private static void cleanOxidation(World world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      BlockPos lv2;
      BlockState lv3;
      if (lv.isOf(Blocks.LIGHTNING_ROD)) {
         lv2 = pos.offset(((Direction)lv.get(LightningRodBlock.FACING)).getOpposite());
         lv3 = world.getBlockState(lv2);
      } else {
         lv2 = pos;
         lv3 = lv;
      }

      if (lv3.getBlock() instanceof Oxidizable) {
         world.setBlockState(lv2, Oxidizable.getUnaffectedOxidationState(world.getBlockState(lv2)));
         BlockPos.Mutable lv4 = pos.mutableCopy();
         int i = world.random.nextInt(3) + 3;

         for(int j = 0; j < i; ++j) {
            int k = world.random.nextInt(8) + 1;
            cleanOxidationAround(world, lv2, lv4, k);
         }

      }
   }

   private static void cleanOxidationAround(World world, BlockPos pos, BlockPos.Mutable mutablePos, int count) {
      mutablePos.set(pos);

      for(int j = 0; j < count; ++j) {
         Optional optional = cleanOxidationAround(world, mutablePos);
         if (!optional.isPresent()) {
            break;
         }

         mutablePos.set((Vec3i)optional.get());
      }

   }

   private static Optional cleanOxidationAround(World world, BlockPos pos) {
      Iterator var2 = BlockPos.iterateRandomly(world.random, 10, pos, 1).iterator();

      BlockPos lv;
      BlockState lv2;
      do {
         if (!var2.hasNext()) {
            return Optional.empty();
         }

         lv = (BlockPos)var2.next();
         lv2 = world.getBlockState(lv);
      } while(!(lv2.getBlock() instanceof Oxidizable));

      Oxidizable.getDecreasedOxidationState(lv2).ifPresent((state) -> {
         world.setBlockState(lv, state);
      });
      world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, lv, -1);
      return Optional.of(lv);
   }

   public boolean shouldRender(double distance) {
      double e = 64.0 * getRenderDistanceMultiplier();
      return distance < e * e;
   }

   protected void initDataTracker() {
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
   }

   public int getBlocksSetOnFire() {
      return this.blocksSetOnFire;
   }

   public Stream getStruckEntities() {
      return this.struckEntities.stream().filter(Entity::isAlive);
   }
}
