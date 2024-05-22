/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseMarking;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HorseEntity
extends AbstractHorseEntity
implements VariantHolder<HorseColor> {
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(HorseEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.HORSE.getDimensions().withAttachments(EntityAttachments.builder().add(EntityAttachmentType.PASSENGER, 0.0f, EntityType.HORSE.getHeight() + 0.125f, 0.0f)).scaled(0.5f);

    public HorseEntity(EntityType<? extends HorseEntity> arg, World arg2) {
        super((EntityType<? extends AbstractHorseEntity>)arg, arg2);
    }

    @Override
    protected void initAttributes(Random random) {
        this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(HorseEntity.getChildHealthBonus(random::nextInt));
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(HorseEntity.getChildMovementSpeedBonus(random::nextDouble));
        this.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(HorseEntity.getChildJumpStrengthBonus(random::nextDouble));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getHorseVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setHorseVariant(nbt.getInt("Variant"));
    }

    private void setHorseVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    private int getHorseVariant() {
        return this.dataTracker.get(VARIANT);
    }

    private void setHorseVariant(HorseColor color, HorseMarking marking) {
        this.setHorseVariant(color.getId() & 0xFF | marking.getId() << 8 & 0xFF00);
    }

    @Override
    public HorseColor getVariant() {
        return HorseColor.byId(this.getHorseVariant() & 0xFF);
    }

    @Override
    public void setVariant(HorseColor arg) {
        this.setHorseVariant(arg.getId() & 0xFF | this.getHorseVariant() & 0xFFFFFF00);
    }

    public HorseMarking getMarking() {
        return HorseMarking.byIndex((this.getHorseVariant() & 0xFF00) >> 8);
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        ItemStack lv = this.getBodyArmor();
        super.onInventoryChanged(sender);
        ItemStack lv2 = this.getBodyArmor();
        if (this.age > 20 && this.isHorseArmor(lv2) && lv != lv2) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5f, 1.0f);
        }
    }

    @Override
    protected void playWalkSound(BlockSoundGroup group) {
        super.playWalkSound(group);
        if (this.random.nextInt(10) == 0) {
            this.playSound(SoundEvents.ENTITY_HORSE_BREATHE, group.getVolume() * 0.6f, group.getPitch());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_HORSE_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_HORSE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_HORSE_HURT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.ENTITY_HORSE_ANGRY;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        boolean bl;
        boolean bl2 = bl = !this.isBaby() && this.isTame() && player.shouldCancelInteraction();
        if (this.hasPassengers() || bl) {
            return super.interactMob(player, hand);
        }
        ItemStack lv = player.getStackInHand(hand);
        if (!lv.isEmpty()) {
            if (this.isBreedingItem(lv)) {
                return this.interactHorse(player, lv);
            }
            if (!this.isTame()) {
                this.playAngrySound();
                return ActionResult.success(this.getWorld().isClient);
            }
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        }
        if (other instanceof DonkeyEntity || other instanceof HorseEntity) {
            return this.canBreed() && ((AbstractHorseEntity)other).canBreed();
        }
        return false;
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        if (entity instanceof DonkeyEntity) {
            MuleEntity lv = EntityType.MULE.create(world);
            if (lv != null) {
                this.setChildAttributes(entity, lv);
            }
            return lv;
        }
        HorseEntity lv2 = (HorseEntity)entity;
        HorseEntity lv3 = EntityType.HORSE.create(world);
        if (lv3 != null) {
            int i = this.random.nextInt(9);
            HorseColor lv4 = i < 4 ? this.getVariant() : (i < 8 ? lv2.getVariant() : Util.getRandom(HorseColor.values(), this.random));
            int j = this.random.nextInt(5);
            HorseMarking lv5 = j < 2 ? this.getMarking() : (j < 4 ? lv2.getMarking() : Util.getRandom(HorseMarking.values(), this.random));
            lv3.setHorseVariant(lv4, lv5);
            this.setChildAttributes(entity, lv3);
        }
        return lv3;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    @Override
    public boolean isHorseArmor(ItemStack stack) {
        AnimalArmorItem lv;
        Item item = stack.getItem();
        return item instanceof AnimalArmorItem && (lv = (AnimalArmorItem)item).getType() == AnimalArmorItem.Type.EQUESTRIAN;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        HorseColor lv2;
        Random lv = world.getRandom();
        if (entityData instanceof HorseData) {
            lv2 = ((HorseData)entityData).color;
        } else {
            lv2 = Util.getRandom(HorseColor.values(), lv);
            entityData = new HorseData(lv2);
        }
        this.setHorseVariant(lv2, Util.getRandom(HorseMarking.values(), lv));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static class HorseData
    extends PassiveEntity.PassiveData {
        public final HorseColor color;

        public HorseData(HorseColor color) {
            super(true);
            this.color = color;
        }
    }
}

