/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ExperienceOrbEntity
extends Entity {
    private static final int DESPAWN_AGE = 6000;
    private static final int EXPENSIVE_UPDATE_INTERVAL = 20;
    private static final int field_30057 = 8;
    private static final int MERGING_CHANCE_FRACTION = 40;
    private static final double field_30059 = 0.5;
    private int orbAge;
    private int health = 5;
    private int amount;
    private int pickingCount = 1;
    private PlayerEntity target;

    public ExperienceOrbEntity(World world, double x, double y, double z, int amount) {
        this((EntityType<? extends ExperienceOrbEntity>)EntityType.EXPERIENCE_ORB, world);
        this.setPosition(x, y, z);
        this.setYaw((float)(this.random.nextDouble() * 360.0));
        this.setVelocity((this.random.nextDouble() * (double)0.2f - (double)0.1f) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * (double)0.2f - (double)0.1f) * 2.0);
        this.amount = amount;
    }

    public ExperienceOrbEntity(EntityType<? extends ExperienceOrbEntity> arg, World arg2) {
        super(arg, arg2);
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected double getGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        Vec3d lv;
        double d;
        super.tick();
        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        if (this.isSubmergedIn(FluidTags.WATER)) {
            this.applyWaterMovement();
        } else {
            this.applyGravity();
        }
        if (this.getWorld().getFluidState(this.getBlockPos()).isIn(FluidTags.LAVA)) {
            this.setVelocity((this.random.nextFloat() - this.random.nextFloat()) * 0.2f, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        if (!this.getWorld().isSpaceEmpty(this.getBoundingBox())) {
            this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }
        if (this.age % 20 == 1) {
            this.expensiveUpdate();
        }
        if (this.target != null && (this.target.isSpectator() || this.target.isDead())) {
            this.target = null;
        }
        if (this.target != null && (d = (lv = new Vec3d(this.target.getX() - this.getX(), this.target.getY() + (double)this.target.getStandingEyeHeight() / 2.0 - this.getY(), this.target.getZ() - this.getZ())).lengthSquared()) < 64.0) {
            double e = 1.0 - Math.sqrt(d) / 8.0;
            this.setVelocity(this.getVelocity().add(lv.normalize().multiply(e * e * 0.1)));
        }
        this.move(MovementType.SELF, this.getVelocity());
        float f = 0.98f;
        if (this.isOnGround()) {
            f = this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.98f;
        }
        this.setVelocity(this.getVelocity().multiply(f, 0.98, f));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(1.0, -0.9, 1.0));
        }
        ++this.orbAge;
        if (this.orbAge >= 6000) {
            this.discard();
        }
    }

    @Override
    public BlockPos getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.999999f);
    }

    private void expensiveUpdate() {
        if (this.target == null || this.target.squaredDistanceTo(this) > 64.0) {
            this.target = this.getWorld().getClosestPlayer(this, 8.0);
        }
        if (this.getWorld() instanceof ServerWorld) {
            List<ExperienceOrbEntity> list = this.getWorld().getEntitiesByType(TypeFilter.instanceOf(ExperienceOrbEntity.class), this.getBoundingBox().expand(0.5), this::isMergeable);
            for (ExperienceOrbEntity lv : list) {
                this.merge(lv);
            }
        }
    }

    public static void spawn(ServerWorld world, Vec3d pos, int amount) {
        while (amount > 0) {
            int j = ExperienceOrbEntity.roundToOrbSize(amount);
            amount -= j;
            if (ExperienceOrbEntity.wasMergedIntoExistingOrb(world, pos, j)) continue;
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX(), pos.getY(), pos.getZ(), j));
        }
    }

    private static boolean wasMergedIntoExistingOrb(ServerWorld world, Vec3d pos, int amount) {
        Box lv = Box.of(pos, 1.0, 1.0, 1.0);
        int j = world.getRandom().nextInt(40);
        List<ExperienceOrbEntity> list = world.getEntitiesByType(TypeFilter.instanceOf(ExperienceOrbEntity.class), lv, orb -> ExperienceOrbEntity.isMergeable(orb, j, amount));
        if (!list.isEmpty()) {
            ExperienceOrbEntity lv2 = list.get(0);
            ++lv2.pickingCount;
            lv2.orbAge = 0;
            return true;
        }
        return false;
    }

    private boolean isMergeable(ExperienceOrbEntity other) {
        return other != this && ExperienceOrbEntity.isMergeable(other, this.getId(), this.amount);
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
        this.setVelocity(lv.x * (double)0.99f, Math.min(lv.y + (double)5.0E-4f, (double)0.06f), lv.z * (double)0.99f);
    }

    @Override
    protected void onSwimmingStart() {
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.getWorld().isClient) {
            return true;
        }
        this.scheduleVelocityUpdate();
        this.health = (int)((float)this.health - amount);
        if (this.health <= 0) {
            this.discard();
        }
        return true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("Health", (short)this.health);
        nbt.putShort("Age", (short)this.orbAge);
        nbt.putShort("Value", (short)this.amount);
        nbt.putInt("Count", this.pickingCount);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.health = nbt.getShort("Health");
        this.orbAge = nbt.getShort("Age");
        this.amount = nbt.getShort("Value");
        this.pickingCount = Math.max(nbt.getInt("Count"), 1);
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity lv = (ServerPlayerEntity)player;
        if (player.experiencePickUpDelay == 0) {
            player.experiencePickUpDelay = 2;
            player.sendPickup(this, 1);
            int i = this.repairPlayerGears(lv, this.amount);
            if (i > 0) {
                player.addExperience(i);
            }
            --this.pickingCount;
            if (this.pickingCount == 0) {
                this.discard();
            }
        }
    }

    private int repairPlayerGears(ServerPlayerEntity player, int amount) {
        Optional<EnchantmentEffectContext> optional = EnchantmentHelper.chooseEquipmentWith(EnchantmentEffectComponentTypes.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (optional.isPresent()) {
            int l;
            ItemStack lv = optional.get().stack();
            int j = EnchantmentHelper.getRepairWithXp(player.getServerWorld(), lv, amount);
            int k = Math.min(j, lv.getDamage());
            lv.setDamage(lv.getDamage() - k);
            if (k > 0 && (l = amount - k * amount / j) > 0) {
                return this.repairPlayerGears(player, l);
            }
            return 0;
        }
        return amount;
    }

    public int getExperienceAmount() {
        return this.amount;
    }

    public int getOrbSize() {
        if (this.amount >= 2477) {
            return 10;
        }
        if (this.amount >= 1237) {
            return 9;
        }
        if (this.amount >= 617) {
            return 8;
        }
        if (this.amount >= 307) {
            return 7;
        }
        if (this.amount >= 149) {
            return 6;
        }
        if (this.amount >= 73) {
            return 5;
        }
        if (this.amount >= 37) {
            return 4;
        }
        if (this.amount >= 17) {
            return 3;
        }
        if (this.amount >= 7) {
            return 2;
        }
        if (this.amount >= 3) {
            return 1;
        }
        return 0;
    }

    public static int roundToOrbSize(int value) {
        if (value >= 2477) {
            return 2477;
        }
        if (value >= 1237) {
            return 1237;
        }
        if (value >= 617) {
            return 617;
        }
        if (value >= 307) {
            return 307;
        }
        if (value >= 149) {
            return 149;
        }
        if (value >= 73) {
            return 73;
        }
        if (value >= 37) {
            return 37;
        }
        if (value >= 17) {
            return 17;
        }
        if (value >= 7) {
            return 7;
        }
        if (value >= 3) {
            return 3;
        }
        return 1;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new ExperienceOrbSpawnS2CPacket(this);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.AMBIENT;
    }
}

