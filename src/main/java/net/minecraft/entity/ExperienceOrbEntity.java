package net.minecraft.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ExperienceOrbEntity extends Entity {
   private static final int DESPAWN_AGE = 6000;
   private static final int EXPENSIVE_UPDATE_INTERVAL = 20;
   private static final int field_30057 = 8;
   private static final int MERGING_CHANCE_FRACTION = 40;
   private static final double field_30059 = 0.5;
   private int orbAge;
   private int health;
   private int amount;
   private int pickingCount;
   private PlayerEntity target;

   public ExperienceOrbEntity(World world, double x, double y, double z, int amount) {
      this(EntityType.EXPERIENCE_ORB, world);
      this.setPosition(x, y, z);
      this.setYaw((float)(this.random.nextDouble() * 360.0));
      this.setVelocity((this.random.nextDouble() * 0.20000000298023224 - 0.10000000149011612) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.20000000298023224 - 0.10000000149011612) * 2.0);
      this.amount = amount;
   }

   public ExperienceOrbEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.health = 5;
      this.pickingCount = 1;
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.NONE;
   }

   protected void initDataTracker() {
   }

   public void tick() {
      super.tick();
      this.prevX = this.getX();
      this.prevY = this.getY();
      this.prevZ = this.getZ();
      if (this.isSubmergedIn(FluidTags.WATER)) {
         this.applyWaterMovement();
      } else if (!this.hasNoGravity()) {
         this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
      }

      if (this.world.getFluidState(this.getBlockPos()).isIn(FluidTags.LAVA)) {
         this.setVelocity((double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), 0.20000000298023224, (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F));
      }

      if (!this.world.isSpaceEmpty(this.getBoundingBox())) {
         this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
      }

      if (this.age % 20 == 1) {
         this.expensiveUpdate();
      }

      if (this.target != null && (this.target.isSpectator() || this.target.isDead())) {
         this.target = null;
      }

      if (this.target != null) {
         Vec3d lv = new Vec3d(this.target.getX() - this.getX(), this.target.getY() + (double)this.target.getStandingEyeHeight() / 2.0 - this.getY(), this.target.getZ() - this.getZ());
         double d = lv.lengthSquared();
         if (d < 64.0) {
            double e = 1.0 - Math.sqrt(d) / 8.0;
            this.setVelocity(this.getVelocity().add(lv.normalize().multiply(e * e * 0.1)));
         }
      }

      this.move(MovementType.SELF, this.getVelocity());
      float f = 0.98F;
      if (this.onGround) {
         f = this.world.getBlockState(BlockPos.ofFloored(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getSlipperiness() * 0.98F;
      }

      this.setVelocity(this.getVelocity().multiply((double)f, 0.98, (double)f));
      if (this.onGround) {
         this.setVelocity(this.getVelocity().multiply(1.0, -0.9, 1.0));
      }

      ++this.orbAge;
      if (this.orbAge >= 6000) {
         this.discard();
      }

   }

   private void expensiveUpdate() {
      if (this.target == null || this.target.squaredDistanceTo(this) > 64.0) {
         this.target = this.world.getClosestPlayer(this, 8.0);
      }

      if (this.world instanceof ServerWorld) {
         List list = this.world.getEntitiesByType(TypeFilter.instanceOf(ExperienceOrbEntity.class), this.getBoundingBox().expand(0.5), this::isMergeable);
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            ExperienceOrbEntity lv = (ExperienceOrbEntity)var2.next();
            this.merge(lv);
         }
      }

   }

   public static void spawn(ServerWorld world, Vec3d pos, int amount) {
      while(amount > 0) {
         int j = roundToOrbSize(amount);
         amount -= j;
         if (!wasMergedIntoExistingOrb(world, pos, j)) {
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX(), pos.getY(), pos.getZ(), j));
         }
      }

   }

   private static boolean wasMergedIntoExistingOrb(ServerWorld world, Vec3d pos, int amount) {
      Box lv = Box.of(pos, 1.0, 1.0, 1.0);
      int j = world.getRandom().nextInt(40);
      List list = world.getEntitiesByType(TypeFilter.instanceOf(ExperienceOrbEntity.class), lv, (orb) -> {
         return isMergeable(orb, j, amount);
      });
      if (!list.isEmpty()) {
         ExperienceOrbEntity lv2 = (ExperienceOrbEntity)list.get(0);
         ++lv2.pickingCount;
         lv2.orbAge = 0;
         return true;
      } else {
         return false;
      }
   }

   private boolean isMergeable(ExperienceOrbEntity other) {
      return other != this && isMergeable(other, this.getId(), this.amount);
   }

   private static boolean isMergeable(ExperienceOrbEntity orb, int seed, int amount) {
      return !orb.isRemoved() && (orb.getId() - seed) % 40 == 0 && orb.amount == amount;
   }

   private void merge(ExperienceOrbEntity other) {
      this.pickingCount += other.pickingCount;
      this.orbAge = Math.min(this.orbAge, other.orbAge);
      other.discard();
   }

   private void applyWaterMovement() {
      Vec3d lv = this.getVelocity();
      this.setVelocity(lv.x * 0.9900000095367432, Math.min(lv.y + 5.000000237487257E-4, 0.05999999865889549), lv.z * 0.9900000095367432);
   }

   protected void onSwimmingStart() {
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (this.world.isClient) {
         return true;
      } else {
         this.scheduleVelocityUpdate();
         this.health = (int)((float)this.health - amount);
         if (this.health <= 0) {
            this.discard();
         }

         return true;
      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putShort("Health", (short)this.health);
      nbt.putShort("Age", (short)this.orbAge);
      nbt.putShort("Value", (short)this.amount);
      nbt.putInt("Count", this.pickingCount);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      this.health = nbt.getShort("Health");
      this.orbAge = nbt.getShort("Age");
      this.amount = nbt.getShort("Value");
      this.pickingCount = Math.max(nbt.getInt("Count"), 1);
   }

   public void onPlayerCollision(PlayerEntity player) {
      if (!this.world.isClient) {
         if (player.experiencePickUpDelay == 0) {
            player.experiencePickUpDelay = 2;
            player.sendPickup(this, 1);
            int i = this.repairPlayerGears(player, this.amount);
            if (i > 0) {
               player.addExperience(i);
            }

            --this.pickingCount;
            if (this.pickingCount == 0) {
               this.discard();
            }
         }

      }
   }

   private int repairPlayerGears(PlayerEntity player, int amount) {
      Map.Entry entry = EnchantmentHelper.chooseEquipmentWith(Enchantments.MENDING, player, ItemStack::isDamaged);
      if (entry != null) {
         ItemStack lv = (ItemStack)entry.getValue();
         int j = Math.min(this.getMendingRepairAmount(this.amount), lv.getDamage());
         lv.setDamage(lv.getDamage() - j);
         int k = amount - this.getMendingRepairCost(j);
         return k > 0 ? this.repairPlayerGears(player, k) : 0;
      } else {
         return amount;
      }
   }

   private int getMendingRepairCost(int repairAmount) {
      return repairAmount / 2;
   }

   private int getMendingRepairAmount(int experienceAmount) {
      return experienceAmount * 2;
   }

   public int getExperienceAmount() {
      return this.amount;
   }

   public int getOrbSize() {
      if (this.amount >= 2477) {
         return 10;
      } else if (this.amount >= 1237) {
         return 9;
      } else if (this.amount >= 617) {
         return 8;
      } else if (this.amount >= 307) {
         return 7;
      } else if (this.amount >= 149) {
         return 6;
      } else if (this.amount >= 73) {
         return 5;
      } else if (this.amount >= 37) {
         return 4;
      } else if (this.amount >= 17) {
         return 3;
      } else if (this.amount >= 7) {
         return 2;
      } else {
         return this.amount >= 3 ? 1 : 0;
      }
   }

   public static int roundToOrbSize(int value) {
      if (value >= 2477) {
         return 2477;
      } else if (value >= 1237) {
         return 1237;
      } else if (value >= 617) {
         return 617;
      } else if (value >= 307) {
         return 307;
      } else if (value >= 149) {
         return 149;
      } else if (value >= 73) {
         return 73;
      } else if (value >= 37) {
         return 37;
      } else if (value >= 17) {
         return 17;
      } else if (value >= 7) {
         return 7;
      } else {
         return value >= 3 ? 3 : 1;
      }
   }

   public boolean isAttackable() {
      return false;
   }

   public Packet createSpawnPacket() {
      return new ExperienceOrbSpawnS2CPacket(this);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.AMBIENT;
   }
}
