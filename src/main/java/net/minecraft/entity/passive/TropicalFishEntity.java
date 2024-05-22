/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class TropicalFishEntity
extends SchoolingFishEntity
implements VariantHolder<Variety> {
    public static final String BUCKET_VARIANT_TAG_KEY = "BucketVariantTag";
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(TropicalFishEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final List<Variant> COMMON_VARIANTS = List.of(new Variant(Variety.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), new Variant(Variety.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), new Variant(Variety.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), new Variant(Variety.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), new Variant(Variety.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), new Variant(Variety.KOB, DyeColor.ORANGE, DyeColor.WHITE), new Variant(Variety.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), new Variant(Variety.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), new Variant(Variety.CLAYFISH, DyeColor.WHITE, DyeColor.RED), new Variant(Variety.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Variety.GLITTER, DyeColor.WHITE, DyeColor.GRAY), new Variant(Variety.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), new Variant(Variety.DASHER, DyeColor.CYAN, DyeColor.PINK), new Variant(Variety.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), new Variant(Variety.BETTY, DyeColor.RED, DyeColor.WHITE), new Variant(Variety.SNOOPER, DyeColor.GRAY, DyeColor.RED), new Variant(Variety.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), new Variant(Variety.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Variety.KOB, DyeColor.RED, DyeColor.WHITE), new Variant(Variety.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), new Variant(Variety.DASHER, DyeColor.CYAN, DyeColor.YELLOW), new Variant(Variety.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));
    private boolean commonSpawn = true;

    public TropicalFishEntity(EntityType<? extends TropicalFishEntity> arg, World arg2) {
        super((EntityType<? extends SchoolingFishEntity>)arg, arg2);
    }

    public static String getToolTipForVariant(int variant) {
        return "entity.minecraft.tropical_fish.predefined." + variant;
    }

    static int getVariantId(Variety variety, DyeColor baseColor, DyeColor patternColor) {
        return variety.getId() & 0xFFFF | (baseColor.getId() & 0xFF) << 16 | (patternColor.getId() & 0xFF) << 24;
    }

    public static DyeColor getBaseDyeColor(int variant) {
        return DyeColor.byId(variant >> 16 & 0xFF);
    }

    public static DyeColor getPatternDyeColor(int variant) {
        return DyeColor.byId(variant >> 24 & 0xFF);
    }

    public static Variety getVariety(int variant) {
        return Variety.fromId(variant & 0xFFFF);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getTropicalFishVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setTropicalFishVariant(nbt.getInt("Variant"));
    }

    private void setTropicalFishVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    @Override
    public boolean spawnsTooManyForEachTry(int count) {
        return !this.commonSpawn;
    }

    private int getTropicalFishVariant() {
        return this.dataTracker.get(VARIANT);
    }

    public DyeColor getBaseColorComponents() {
        return TropicalFishEntity.getBaseDyeColor(this.getTropicalFishVariant());
    }

    public DyeColor getPatternColorComponents() {
        return TropicalFishEntity.getPatternDyeColor(this.getTropicalFishVariant());
    }

    @Override
    public Variety getVariant() {
        return TropicalFishEntity.getVariety(this.getTropicalFishVariant());
    }

    @Override
    public void setVariant(Variety arg) {
        int i = this.getTropicalFishVariant();
        DyeColor lv = TropicalFishEntity.getBaseDyeColor(i);
        DyeColor lv2 = TropicalFishEntity.getPatternDyeColor(i);
        this.setTropicalFishVariant(TropicalFishEntity.getVariantId(arg, lv, lv2));
    }

    @Override
    public void copyDataToStack(ItemStack stack) {
        super.copyDataToStack(stack);
        NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, arg -> arg.putInt(BUCKET_VARIANT_TAG_KEY, this.getTropicalFishVariant()));
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(Items.TROPICAL_FISH_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_TROPICAL_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
    }

    @Override
    public void copyDataFromNbt(NbtCompound nbt) {
        super.copyDataFromNbt(nbt);
        if (nbt.contains(BUCKET_VARIANT_TAG_KEY, NbtElement.INT_TYPE)) {
            this.setTropicalFishVariant(nbt.getInt(BUCKET_VARIANT_TAG_KEY));
        }
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variant lv3;
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        Random lv = world.getRandom();
        if (entityData instanceof TropicalFishData) {
            TropicalFishData lv2 = (TropicalFishData)entityData;
            lv3 = lv2.variant;
        } else if ((double)lv.nextFloat() < 0.9) {
            lv3 = Util.getRandom(COMMON_VARIANTS, lv);
            entityData = new TropicalFishData(this, lv3);
        } else {
            this.commonSpawn = false;
            Variety[] lvs = Variety.values();
            DyeColor[] lvs2 = DyeColor.values();
            Variety lv4 = Util.getRandom(lvs, lv);
            DyeColor lv5 = Util.getRandom(lvs2, lv);
            DyeColor lv6 = Util.getRandom(lvs2, lv);
            lv3 = new Variant(lv4, lv5, lv6);
        }
        this.setTropicalFishVariant(lv3.getId());
        return entityData;
    }

    public static boolean canTropicalFishSpawn(EntityType<TropicalFishEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getFluidState(pos.down()).isIn(FluidTags.WATER) && world.getBlockState(pos.up()).isOf(Blocks.WATER) && (world.getBiome(pos).isIn(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT) || WaterCreatureEntity.canSpawn(type, world, reason, pos, random));
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static enum Variety implements StringIdentifiable
    {
        KOB("kob", Size.SMALL, 0),
        SUNSTREAK("sunstreak", Size.SMALL, 1),
        SNOOPER("snooper", Size.SMALL, 2),
        DASHER("dasher", Size.SMALL, 3),
        BRINELY("brinely", Size.SMALL, 4),
        SPOTTY("spotty", Size.SMALL, 5),
        FLOPPER("flopper", Size.LARGE, 0),
        STRIPEY("stripey", Size.LARGE, 1),
        GLITTER("glitter", Size.LARGE, 2),
        BLOCKFISH("blockfish", Size.LARGE, 3),
        BETTY("betty", Size.LARGE, 4),
        CLAYFISH("clayfish", Size.LARGE, 5);

        public static final Codec<Variety> CODEC;
        private static final IntFunction<Variety> BY_ID;
        private final String name;
        private final Text text;
        private final Size size;
        private final int id;

        private Variety(String name, Size size, int id) {
            this.name = name;
            this.size = size;
            this.id = size.id | id << 8;
            this.text = Text.translatable("entity.minecraft.tropical_fish.type." + this.name);
        }

        public static Variety fromId(int id) {
            return BY_ID.apply(id);
        }

        public Size getSize() {
            return this.size;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public Text getText() {
            return this.text;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Variety::values);
            BY_ID = ValueLists.createIdToValueFunction(Variety::getId, Variety.values(), KOB);
        }
    }

    static class TropicalFishData
    extends SchoolingFishEntity.FishData {
        final Variant variant;

        TropicalFishData(TropicalFishEntity leader, Variant variant) {
            super(leader);
            this.variant = variant;
        }
    }

    public record Variant(Variety variety, DyeColor baseColor, DyeColor patternColor) {
        public static final Codec<Variant> CODEC = Codec.INT.xmap(Variant::new, Variant::getId);

        public Variant(int id) {
            this(TropicalFishEntity.getVariety(id), TropicalFishEntity.getBaseDyeColor(id), TropicalFishEntity.getPatternDyeColor(id));
        }

        public int getId() {
            return TropicalFishEntity.getVariantId(this.variety, this.baseColor, this.patternColor);
        }
    }

    public static enum Size {
        SMALL(0),
        LARGE(1);

        final int id;

        private Size(int id) {
            this.id = id;
        }
    }
}

