/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BeaconBlockEntity
extends BlockEntity
implements NamedScreenHandlerFactory,
Nameable {
    private static final int field_31304 = 4;
    public static final List<List<RegistryEntry<StatusEffect>>> EFFECTS_BY_LEVEL = List.of(List.of(StatusEffects.SPEED, StatusEffects.HASTE), List.of(StatusEffects.RESISTANCE, StatusEffects.JUMP_BOOST), List.of(StatusEffects.STRENGTH), List.of(StatusEffects.REGENERATION));
    private static final Set<RegistryEntry<StatusEffect>> EFFECTS = EFFECTS_BY_LEVEL.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    public static final int LEVEL_PROPERTY_INDEX = 0;
    public static final int PRIMARY_PROPERTY_INDEX = 1;
    public static final int SECONDARY_PROPERTY_INDEX = 2;
    public static final int PROPERTY_COUNT = 3;
    private static final int field_31305 = 10;
    private static final Text CONTAINER_NAME_TEXT = Text.translatable("container.beacon");
    private static final String PRIMARY_EFFECT_NBT_KEY = "primary_effect";
    private static final String SECONDARY_EFFECT_NBT_KEY = "secondary_effect";
    List<BeamSegment> beamSegments = Lists.newArrayList();
    private List<BeamSegment> field_19178 = Lists.newArrayList();
    int level;
    private int minY;
    @Nullable
    RegistryEntry<StatusEffect> primary;
    @Nullable
    RegistryEntry<StatusEffect> secondary;
    @Nullable
    private Text customName;
    private ContainerLock lock = ContainerLock.EMPTY;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate(){

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> BeaconBlockEntity.this.level;
                case 1 -> BeaconScreenHandler.getRawIdForStatusEffect(BeaconBlockEntity.this.primary);
                case 2 -> BeaconScreenHandler.getRawIdForStatusEffect(BeaconBlockEntity.this.secondary);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: {
                    BeaconBlockEntity.this.level = value;
                    break;
                }
                case 1: {
                    if (!BeaconBlockEntity.this.world.isClient && !BeaconBlockEntity.this.beamSegments.isEmpty()) {
                        BeaconBlockEntity.playSound(BeaconBlockEntity.this.world, BeaconBlockEntity.this.pos, SoundEvents.BLOCK_BEACON_POWER_SELECT);
                    }
                    BeaconBlockEntity.this.primary = BeaconBlockEntity.getEffectOrNull(BeaconScreenHandler.getStatusEffectForRawId(value));
                    break;
                }
                case 2: {
                    BeaconBlockEntity.this.secondary = BeaconBlockEntity.getEffectOrNull(BeaconScreenHandler.getStatusEffectForRawId(value));
                }
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    @Nullable
    static RegistryEntry<StatusEffect> getEffectOrNull(@Nullable RegistryEntry<StatusEffect> effect) {
        return EFFECTS.contains(effect) ? effect : null;
    }

    public BeaconBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BEACON, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity) {
        int m;
        BlockPos lv;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (blockEntity.minY < j) {
            lv = pos;
            blockEntity.field_19178 = Lists.newArrayList();
            blockEntity.minY = lv.getY() - 1;
        } else {
            lv = new BlockPos(i, blockEntity.minY + 1, k);
        }
        BeamSegment lv2 = blockEntity.field_19178.isEmpty() ? null : blockEntity.field_19178.get(blockEntity.field_19178.size() - 1);
        int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, i, k);
        for (m = 0; m < 10 && lv.getY() <= l; ++m) {
            block18: {
                BlockState lv3;
                block16: {
                    int n;
                    block17: {
                        lv3 = world.getBlockState(lv);
                        Block lv4 = lv3.getBlock();
                        if (!(lv4 instanceof Stainable)) break block16;
                        Stainable lv5 = (Stainable)((Object)lv4);
                        n = lv5.getColor().getColorComponents();
                        if (blockEntity.field_19178.size() > 1) break block17;
                        lv2 = new BeamSegment(n);
                        blockEntity.field_19178.add(lv2);
                        break block18;
                    }
                    if (lv2 == null) break block18;
                    if (n == lv2.color) {
                        lv2.increaseHeight();
                    } else {
                        lv2 = new BeamSegment(ColorHelper.Argb.method_60676(lv2.color, n));
                        blockEntity.field_19178.add(lv2);
                    }
                    break block18;
                }
                if (lv2 != null && (lv3.getOpacity(world, lv) < 15 || lv3.isOf(Blocks.BEDROCK))) {
                    lv2.increaseHeight();
                } else {
                    blockEntity.field_19178.clear();
                    blockEntity.minY = l;
                    break;
                }
            }
            lv = lv.up();
            ++blockEntity.minY;
        }
        m = blockEntity.level;
        if (world.getTime() % 80L == 0L) {
            if (!blockEntity.beamSegments.isEmpty()) {
                blockEntity.level = BeaconBlockEntity.updateLevel(world, i, j, k);
            }
            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
                BeaconBlockEntity.applyPlayerEffects(world, pos, blockEntity.level, blockEntity.primary, blockEntity.secondary);
                BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
            }
        }
        if (blockEntity.minY >= l) {
            blockEntity.minY = world.getBottomY() - 1;
            boolean bl = m > 0;
            blockEntity.beamSegments = blockEntity.field_19178;
            if (!world.isClient) {
                boolean bl2;
                boolean bl3 = bl2 = blockEntity.level > 0;
                if (!bl && bl2) {
                    BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_ACTIVATE);
                    for (ServerPlayerEntity lv6 : world.getNonSpectatingEntities(ServerPlayerEntity.class, new Box(i, j, k, i, j - 4, k).expand(10.0, 5.0, 10.0))) {
                        Criteria.CONSTRUCT_BEACON.trigger(lv6, blockEntity.level);
                    }
                } else if (bl && !bl2) {
                    BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateLevel(World world, int x, int y, int z) {
        int n;
        int l = 0;
        int m = 1;
        while (m <= 4 && (n = y - m) >= world.getBottomY()) {
            boolean bl = true;
            block1: for (int o = x - m; o <= x + m && bl; ++o) {
                for (int p = z - m; p <= z + m; ++p) {
                    if (world.getBlockState(new BlockPos(o, n, p)).isIn(BlockTags.BEACON_BASE_BLOCKS)) continue;
                    bl = false;
                    continue block1;
                }
            }
            if (!bl) break;
            l = m++;
        }
        return l;
    }

    @Override
    public void markRemoved() {
        BeaconBlockEntity.playSound(this.world, this.pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
        super.markRemoved();
    }

    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable RegistryEntry<StatusEffect> primaryEffect, @Nullable RegistryEntry<StatusEffect> secondaryEffect) {
        if (world.isClient || primaryEffect == null) {
            return;
        }
        double d = beaconLevel * 10 + 10;
        int j = 0;
        if (beaconLevel >= 4 && Objects.equals(primaryEffect, secondaryEffect)) {
            j = 1;
        }
        int k = (9 + beaconLevel * 2) * 20;
        Box lv = new Box(pos).expand(d).stretch(0.0, world.getHeight(), 0.0);
        List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, lv);
        for (PlayerEntity lv2 : list) {
            lv2.addStatusEffect(new StatusEffectInstance(primaryEffect, k, j, true, true));
        }
        if (beaconLevel >= 4 && !Objects.equals(primaryEffect, secondaryEffect) && secondaryEffect != null) {
            for (PlayerEntity lv2 : list) {
                lv2.addStatusEffect(new StatusEffectInstance(secondaryEffect, k, 0, true, true));
            }
        }
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound) {
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    public List<BeamSegment> getBeamSegments() {
        return this.level == 0 ? ImmutableList.of() : this.beamSegments;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createComponentlessNbt(registryLookup);
    }

    private static void writeStatusEffect(NbtCompound nbt, String key, @Nullable RegistryEntry<StatusEffect> effect) {
        if (effect != null) {
            effect.getKey().ifPresent(entryKey -> nbt.putString(key, entryKey.getValue().toString()));
        }
    }

    @Nullable
    private static RegistryEntry<StatusEffect> readStatusEffect(NbtCompound nbt, String key) {
        if (nbt.contains(key, NbtElement.STRING_TYPE)) {
            Identifier lv = Identifier.tryParse(nbt.getString(key));
            if (lv == null) {
                return null;
            }
            return Registries.STATUS_EFFECT.getEntry(lv).map(BeaconBlockEntity::getEffectOrNull).orElse(null);
        }
        return null;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.primary = BeaconBlockEntity.readStatusEffect(nbt, PRIMARY_EFFECT_NBT_KEY);
        this.secondary = BeaconBlockEntity.readStatusEffect(nbt, SECONDARY_EFFECT_NBT_KEY);
        if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
            this.customName = BeaconBlockEntity.tryParseCustomName(nbt.getString("CustomName"), registryLookup);
        }
        this.lock = ContainerLock.fromNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        BeaconBlockEntity.writeStatusEffect(nbt, PRIMARY_EFFECT_NBT_KEY, this.primary);
        BeaconBlockEntity.writeStatusEffect(nbt, SECONDARY_EFFECT_NBT_KEY, this.secondary);
        nbt.putInt("Levels", this.level);
        if (this.customName != null) {
            nbt.putString("CustomName", Text.Serialization.toJsonString(this.customName, registryLookup));
        }
        this.lock.writeNbt(nbt);
    }

    public void setCustomName(@Nullable Text customName) {
        this.customName = customName;
    }

    @Override
    @Nullable
    public Text getCustomName() {
        return this.customName;
    }

    @Override
    @Nullable
    public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
        if (LockableContainerBlockEntity.checkUnlocked(arg2, this.lock, this.getDisplayName())) {
            return new BeaconScreenHandler(i, arg, this.propertyDelegate, ScreenHandlerContext.create(this.world, this.getPos()));
        }
        return null;
    }

    @Override
    public Text getDisplayName() {
        return this.getName();
    }

    @Override
    public Text getName() {
        if (this.customName != null) {
            return this.customName;
        }
        return CONTAINER_NAME_TEXT;
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
        this.lock = components.getOrDefault(DataComponentTypes.LOCK, ContainerLock.EMPTY);
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
        if (!this.lock.equals(ContainerLock.EMPTY)) {
            componentMapBuilder.add(DataComponentTypes.LOCK, this.lock);
        }
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        nbt.remove("CustomName");
        nbt.remove("Lock");
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.minY = world.getBottomY() - 1;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }

    public static class BeamSegment {
        final int color;
        private int height;

        public BeamSegment(int i) {
            this.color = i;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        public int getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}

