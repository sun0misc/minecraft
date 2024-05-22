/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.class_9793;
import net.minecraft.class_9794;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class JukeboxBlockEntity
extends BlockEntity
implements Clearable,
SingleStackInventory.SingleStackBlockEntityInventory {
    public static final String field_52064 = "RecordItem";
    public static final String field_52065 = "ticks_since_song_started";
    private ItemStack recordStack = ItemStack.EMPTY;
    private final class_9794 field_52066 = new class_9794(this::method_60785, this.getPos());

    public JukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.JUKEBOX, pos, state);
    }

    public class_9794 method_60784() {
        return this.field_52066;
    }

    public void method_60785() {
        this.world.updateNeighborsAlways(this.getPos(), this.getCachedState().getBlock());
        this.markDirty();
    }

    private void method_60782(boolean bl) {
        if (this.world == null || this.world.getBlockState(this.getPos()) != this.getCachedState()) {
            return;
        }
        this.world.setBlockState(this.getPos(), (BlockState)this.getCachedState().with(JukeboxBlock.HAS_RECORD, bl), Block.NOTIFY_LISTENERS);
        this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(this.getCachedState()));
    }

    public void dropRecord() {
        if (this.world == null || this.world.isClient) {
            return;
        }
        BlockPos lv = this.getPos();
        ItemStack lv2 = this.getStack();
        if (lv2.isEmpty()) {
            return;
        }
        this.emptyStack();
        Vec3d lv3 = Vec3d.add(lv, 0.5, 1.01, 0.5).addRandom(this.world.random, 0.7f);
        ItemStack lv4 = lv2.copy();
        ItemEntity lv5 = new ItemEntity(this.world, lv3.getX(), lv3.getY(), lv3.getZ(), lv4);
        lv5.setToDefaultPickupDelay();
        this.world.spawnEntity(lv5);
    }

    public static void tick(World world, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity) {
        blockEntity.field_52066.method_60760(world, state);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.recordStack = nbt.contains(field_52064, NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(registryLookup, nbt.getCompound(field_52064)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        if (nbt.contains(field_52065, NbtElement.LONG_TYPE)) {
            class_9793.method_60753(registryLookup, this.recordStack).ifPresent(arg2 -> this.field_52066.method_60758((RegistryEntry<class_9793>)arg2, nbt.getLong(field_52065)));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!this.getStack().isEmpty()) {
            nbt.put(field_52064, this.getStack().encode(registryLookup));
        }
        if (this.field_52066.method_60759() != null) {
            nbt.putLong(field_52065, this.field_52066.method_60761());
        }
    }

    @Override
    public ItemStack getStack() {
        return this.recordStack;
    }

    @Override
    public ItemStack decreaseStack(int count) {
        ItemStack lv = this.recordStack;
        this.setStack(ItemStack.EMPTY);
        return lv;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.recordStack = stack;
        boolean bl = !this.recordStack.isEmpty();
        Optional<RegistryEntry<class_9793>> optional = class_9793.method_60753(this.world.getRegistryManager(), this.recordStack);
        this.method_60782(bl);
        if (bl && optional.isPresent()) {
            this.field_52066.method_60757(this.world, optional.get());
        } else {
            this.field_52066.method_60755(this.world, this.getCachedState());
        }
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE) && this.getStack(slot).isEmpty();
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return hopperInventory.containsAny(ItemStack::isEmpty);
    }

    @VisibleForTesting
    public void setDisc(ItemStack stack) {
        this.recordStack = stack;
        class_9793.method_60753(this.world.getRegistryManager(), stack).ifPresent(arg -> this.field_52066.method_60758((RegistryEntry<class_9793>)arg, 0L));
        this.world.updateNeighborsAlways(this.getPos(), this.getCachedState().getBlock());
        this.markDirty();
    }

    @VisibleForTesting
    public void method_60786() {
        class_9793.method_60753(this.world.getRegistryManager(), this.getStack()).ifPresent(arg -> this.field_52066.method_60757(this.world, (RegistryEntry<class_9793>)arg));
    }
}

