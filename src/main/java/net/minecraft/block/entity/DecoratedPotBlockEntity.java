/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Sherds;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlockEntity
extends BlockEntity
implements LootableInventory,
SingleStackInventory.SingleStackBlockEntityInventory {
    public static final String SHERDS_NBT_KEY = "sherds";
    public static final String ITEM_NBT_KEY = "item";
    public static final int field_46660 = 1;
    public long lastWobbleTime;
    @Nullable
    public WobbleType lastWobbleType;
    private Sherds sherds;
    private ItemStack stack = ItemStack.EMPTY;
    @Nullable
    protected RegistryKey<LootTable> lootTableId;
    protected long lootTableSeed;

    public DecoratedPotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.DECORATED_POT, pos, state);
        this.sherds = Sherds.DEFAULT;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        this.sherds.toNbt(nbt);
        if (!this.writeLootTable(nbt) && !this.stack.isEmpty()) {
            nbt.put(ITEM_NBT_KEY, this.stack.encode(registryLookup));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.sherds = Sherds.fromNbt(nbt);
        if (!this.readLootTable(nbt)) {
            this.stack = nbt.contains(ITEM_NBT_KEY, NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(registryLookup, nbt.getCompound(ITEM_NBT_KEY)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createComponentlessNbt(registryLookup);
    }

    public Direction getHorizontalFacing() {
        return this.getCachedState().get(Properties.HORIZONTAL_FACING);
    }

    public Sherds getSherds() {
        return this.sherds;
    }

    public void readFrom(ItemStack stack) {
        this.readComponents(stack);
    }

    public ItemStack asStack() {
        ItemStack lv = Items.DECORATED_POT.getDefaultStack();
        lv.applyComponentsFrom(this.createComponentMap());
        return lv;
    }

    public static ItemStack getStackWith(Sherds sherds) {
        ItemStack lv = Items.DECORATED_POT.getDefaultStack();
        lv.set(DataComponentTypes.POT_DECORATIONS, sherds);
        return lv;
    }

    @Override
    @Nullable
    public RegistryKey<LootTable> getLootTable() {
        return this.lootTableId;
    }

    @Override
    public void setLootTable(@Nullable RegistryKey<LootTable> lootTable) {
        this.lootTableId = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(DataComponentTypes.POT_DECORATIONS, this.sherds);
        componentMapBuilder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(List.of(this.stack)));
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        this.sherds = components.getOrDefault(DataComponentTypes.POT_DECORATIONS, Sherds.DEFAULT);
        this.stack = components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyFirstStack();
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove(SHERDS_NBT_KEY);
        nbt.remove(ITEM_NBT_KEY);
    }

    @Override
    public ItemStack getStack() {
        this.generateLoot(null);
        return this.stack;
    }

    @Override
    public ItemStack decreaseStack(int count) {
        this.generateLoot(null);
        ItemStack lv = this.stack.split(count);
        if (this.stack.isEmpty()) {
            this.stack = ItemStack.EMPTY;
        }
        return lv;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.generateLoot(null);
        this.stack = stack;
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    public void wobble(WobbleType wobbleType) {
        if (this.world == null || this.world.isClient()) {
            return;
        }
        this.world.addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 1, wobbleType.ordinal());
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (this.world != null && type == 1 && data >= 0 && data < WobbleType.values().length) {
            this.lastWobbleTime = this.world.getTime();
            this.lastWobbleType = WobbleType.values()[data];
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }

    public static enum WobbleType {
        POSITIVE(7),
        NEGATIVE(10);

        public final int lengthInTicks;

        private WobbleType(int lengthInTicks) {
            this.lengthInTicks = lengthInTicks;
        }
    }
}

