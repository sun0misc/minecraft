package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BeehiveBlockEntity extends BlockEntity {
   public static final String FLOWER_POS_KEY = "FlowerPos";
   public static final String MIN_OCCUPATION_TICKS_KEY = "MinOccupationTicks";
   public static final String ENTITY_DATA_KEY = "EntityData";
   public static final String TICKS_IN_HIVE_KEY = "TicksInHive";
   public static final String HAS_NECTAR_KEY = "HasNectar";
   public static final String BEES_KEY = "Bees";
   private static final List IRRELEVANT_BEE_NBT_KEYS = Arrays.asList("Air", "ArmorDropChances", "ArmorItems", "Brain", "CanPickUpLoot", "DeathTime", "FallDistance", "FallFlying", "Fire", "HandDropChances", "HandItems", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround", "PortalCooldown", "Pos", "Rotation", "CannotEnterHiveTicks", "TicksSincePollination", "CropsGrownSincePollination", "HivePos", "Passengers", "Leash", "UUID");
   public static final int MAX_BEE_COUNT = 3;
   private static final int ANGERED_CANNOT_ENTER_HIVE_TICKS = 400;
   private static final int MIN_OCCUPATION_TICKS_WITH_NECTAR = 2400;
   public static final int MIN_OCCUPATION_TICKS_WITHOUT_NECTAR = 600;
   private final List bees = Lists.newArrayList();
   @Nullable
   private BlockPos flowerPos;

   public BeehiveBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.BEEHIVE, pos, state);
   }

   public void markDirty() {
      if (this.isNearFire()) {
         this.angerBees((PlayerEntity)null, this.world.getBlockState(this.getPos()), BeehiveBlockEntity.BeeState.EMERGENCY);
      }

      super.markDirty();
   }

   public boolean isNearFire() {
      if (this.world == null) {
         return false;
      } else {
         Iterator var1 = BlockPos.iterate(this.pos.add(-1, -1, -1), this.pos.add(1, 1, 1)).iterator();

         BlockPos lv;
         do {
            if (!var1.hasNext()) {
               return false;
            }

            lv = (BlockPos)var1.next();
         } while(!(this.world.getBlockState(lv).getBlock() instanceof FireBlock));

         return true;
      }
   }

   public boolean hasNoBees() {
      return this.bees.isEmpty();
   }

   public boolean isFullOfBees() {
      return this.bees.size() == 3;
   }

   public void angerBees(@Nullable PlayerEntity player, BlockState state, BeeState beeState) {
      List list = this.tryReleaseBee(state, beeState);
      if (player != null) {
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Entity lv = (Entity)var5.next();
            if (lv instanceof BeeEntity) {
               BeeEntity lv2 = (BeeEntity)lv;
               if (player.getPos().squaredDistanceTo(lv.getPos()) <= 16.0) {
                  if (!this.isSmoked()) {
                     lv2.setTarget(player);
                  } else {
                     lv2.setCannotEnterHiveTicks(400);
                  }
               }
            }
         }
      }

   }

   private List tryReleaseBee(BlockState state, BeeState beeState) {
      List list = Lists.newArrayList();
      this.bees.removeIf((bee) -> {
         return releaseBee(this.world, this.pos, state, bee, list, beeState, this.flowerPos);
      });
      if (!list.isEmpty()) {
         super.markDirty();
      }

      return list;
   }

   public void tryEnterHive(Entity entity, boolean hasNectar) {
      this.tryEnterHive(entity, hasNectar, 0);
   }

   @Debug
   public int getBeeCount() {
      return this.bees.size();
   }

   public static int getHoneyLevel(BlockState state) {
      return (Integer)state.get(BeehiveBlock.HONEY_LEVEL);
   }

   @Debug
   public boolean isSmoked() {
      return CampfireBlock.isLitCampfireInRange(this.world, this.getPos());
   }

   public void tryEnterHive(Entity entity, boolean hasNectar, int ticksInHive) {
      if (this.bees.size() < 3) {
         entity.stopRiding();
         entity.removeAllPassengers();
         NbtCompound lv = new NbtCompound();
         entity.saveNbt(lv);
         this.addBee(lv, ticksInHive, hasNectar);
         if (this.world != null) {
            if (entity instanceof BeeEntity) {
               BeeEntity lv2 = (BeeEntity)entity;
               if (lv2.hasFlower() && (!this.hasFlowerPos() || this.world.random.nextBoolean())) {
                  this.flowerPos = lv2.getFlowerPos();
               }
            }

            BlockPos lv3 = this.getPos();
            this.world.playSound((PlayerEntity)null, (double)lv3.getX(), (double)lv3.getY(), (double)lv3.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, lv3, GameEvent.Emitter.of(entity, this.getCachedState()));
         }

         entity.discard();
         super.markDirty();
      }
   }

   public void addBee(NbtCompound nbtCompound, int ticksInHive, boolean hasNectar) {
      this.bees.add(new Bee(nbtCompound, ticksInHive, hasNectar ? 2400 : 600));
   }

   private static boolean releaseBee(World world, BlockPos pos, BlockState state, Bee bee, @Nullable List entities, BeeState beeState, @Nullable BlockPos flowerPos) {
      if ((world.isNight() || world.isRaining()) && beeState != BeehiveBlockEntity.BeeState.EMERGENCY) {
         return false;
      } else {
         NbtCompound lv = bee.entityData.copy();
         removeIrrelevantNbtKeys(lv);
         lv.put("HivePos", NbtHelper.fromBlockPos(pos));
         lv.putBoolean("NoGravity", true);
         Direction lv2 = (Direction)state.get(BeehiveBlock.FACING);
         BlockPos lv3 = pos.offset(lv2);
         boolean bl = !world.getBlockState(lv3).getCollisionShape(world, lv3).isEmpty();
         if (bl && beeState != BeehiveBlockEntity.BeeState.EMERGENCY) {
            return false;
         } else {
            Entity lv4 = EntityType.loadEntityWithPassengers(lv, world, (arg) -> {
               return arg;
            });
            if (lv4 != null) {
               if (!lv4.getType().isIn(EntityTypeTags.BEEHIVE_INHABITORS)) {
                  return false;
               } else {
                  if (lv4 instanceof BeeEntity) {
                     BeeEntity lv5 = (BeeEntity)lv4;
                     if (flowerPos != null && !lv5.hasFlower() && world.random.nextFloat() < 0.9F) {
                        lv5.setFlowerPos(flowerPos);
                     }

                     if (beeState == BeehiveBlockEntity.BeeState.HONEY_DELIVERED) {
                        lv5.onHoneyDelivered();
                        if (state.isIn(BlockTags.BEEHIVES, (statex) -> {
                           return statex.contains(BeehiveBlock.HONEY_LEVEL);
                        })) {
                           int i = getHoneyLevel(state);
                           if (i < 5) {
                              int j = world.random.nextInt(100) == 0 ? 2 : 1;
                              if (i + j > 5) {
                                 --j;
                              }

                              world.setBlockState(pos, (BlockState)state.with(BeehiveBlock.HONEY_LEVEL, i + j));
                           }
                        }
                     }

                     ageBee(bee.ticksInHive, lv5);
                     if (entities != null) {
                        entities.add(lv5);
                     }

                     float f = lv4.getWidth();
                     double d = bl ? 0.0 : 0.55 + (double)(f / 2.0F);
                     double e = (double)pos.getX() + 0.5 + d * (double)lv2.getOffsetX();
                     double g = (double)pos.getY() + 0.5 - (double)(lv4.getHeight() / 2.0F);
                     double h = (double)pos.getZ() + 0.5 + d * (double)lv2.getOffsetZ();
                     lv4.refreshPositionAndAngles(e, g, h, lv4.getYaw(), lv4.getPitch());
                  }

                  world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv4, world.getBlockState(pos)));
                  return world.spawnEntity(lv4);
               }
            } else {
               return false;
            }
         }
      }
   }

   static void removeIrrelevantNbtKeys(NbtCompound compound) {
      Iterator var1 = IRRELEVANT_BEE_NBT_KEYS.iterator();

      while(var1.hasNext()) {
         String string = (String)var1.next();
         compound.remove(string);
      }

   }

   private static void ageBee(int ticks, BeeEntity bee) {
      int j = bee.getBreedingAge();
      if (j < 0) {
         bee.setBreedingAge(Math.min(0, j + ticks));
      } else if (j > 0) {
         bee.setBreedingAge(Math.max(0, j - ticks));
      }

      bee.setLoveTicks(Math.max(0, bee.getLoveTicks() - ticks));
   }

   private boolean hasFlowerPos() {
      return this.flowerPos != null;
   }

   private static void tickBees(World world, BlockPos pos, BlockState state, List bees, @Nullable BlockPos flowerPos) {
      boolean bl = false;

      Bee lv;
      for(Iterator iterator = bees.iterator(); iterator.hasNext(); ++lv.ticksInHive) {
         lv = (Bee)iterator.next();
         if (lv.ticksInHive > lv.minOccupationTicks) {
            BeeState lv2 = lv.entityData.getBoolean("HasNectar") ? BeehiveBlockEntity.BeeState.HONEY_DELIVERED : BeehiveBlockEntity.BeeState.BEE_RELEASED;
            if (releaseBee(world, pos, state, lv, (List)null, lv2, flowerPos)) {
               bl = true;
               iterator.remove();
            }
         }
      }

      if (bl) {
         markDirty(world, pos, state);
      }

   }

   public static void serverTick(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity) {
      tickBees(world, pos, state, blockEntity.bees, blockEntity.flowerPos);
      if (!blockEntity.bees.isEmpty() && world.getRandom().nextDouble() < 0.005) {
         double d = (double)pos.getX() + 0.5;
         double e = (double)pos.getY();
         double f = (double)pos.getZ() + 0.5;
         world.playSound((PlayerEntity)null, d, e, f, SoundEvents.BLOCK_BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

      DebugInfoSender.sendBeehiveDebugData(world, pos, state, blockEntity);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.bees.clear();
      NbtList lv = nbt.getList("Bees", NbtElement.COMPOUND_TYPE);

      for(int i = 0; i < lv.size(); ++i) {
         NbtCompound lv2 = lv.getCompound(i);
         Bee lv3 = new Bee(lv2.getCompound("EntityData"), lv2.getInt("TicksInHive"), lv2.getInt("MinOccupationTicks"));
         this.bees.add(lv3);
      }

      this.flowerPos = null;
      if (nbt.contains("FlowerPos")) {
         this.flowerPos = NbtHelper.toBlockPos(nbt.getCompound("FlowerPos"));
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.put("Bees", this.getBees());
      if (this.hasFlowerPos()) {
         nbt.put("FlowerPos", NbtHelper.fromBlockPos(this.flowerPos));
      }

   }

   public NbtList getBees() {
      NbtList lv = new NbtList();
      Iterator var2 = this.bees.iterator();

      while(var2.hasNext()) {
         Bee lv2 = (Bee)var2.next();
         NbtCompound lv3 = lv2.entityData.copy();
         lv3.remove("UUID");
         NbtCompound lv4 = new NbtCompound();
         lv4.put("EntityData", lv3);
         lv4.putInt("TicksInHive", lv2.ticksInHive);
         lv4.putInt("MinOccupationTicks", lv2.minOccupationTicks);
         lv.add(lv4);
      }

      return lv;
   }

   public static enum BeeState {
      HONEY_DELIVERED,
      BEE_RELEASED,
      EMERGENCY;

      // $FF: synthetic method
      private static BeeState[] method_36714() {
         return new BeeState[]{HONEY_DELIVERED, BEE_RELEASED, EMERGENCY};
      }
   }

   private static class Bee {
      final NbtCompound entityData;
      int ticksInHive;
      final int minOccupationTicks;

      Bee(NbtCompound entityData, int ticksInHive, int minOccupationTicks) {
         BeehiveBlockEntity.removeIrrelevantNbtKeys(entityData);
         this.entityData = entityData;
         this.ticksInHive = ticksInHive;
         this.minOccupationTicks = minOccupationTicks;
      }
   }
}
