/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CampfireBlockEntity
extends BlockEntity
implements Clearable {
    private static final int field_31330 = 2;
    private static final int field_31331 = 4;
    private final DefaultedList<ItemStack> itemsBeingCooked = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final int[] cookingTimes = new int[4];
    private final int[] cookingTotalTimes = new int[4];
    private final RecipeManager.MatchGetter<SingleStackRecipeInput, CampfireCookingRecipe> matchGetter = RecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING);

    public CampfireBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.CAMPFIRE, pos, state);
    }

    public static void litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        boolean bl = false;
        for (int i = 0; i < campfire.itemsBeingCooked.size(); ++i) {
            SingleStackRecipeInput lv2;
            ItemStack lv3;
            ItemStack lv = campfire.itemsBeingCooked.get(i);
            if (lv.isEmpty()) continue;
            bl = true;
            int n = i;
            campfire.cookingTimes[n] = campfire.cookingTimes[n] + 1;
            if (campfire.cookingTimes[i] < campfire.cookingTotalTimes[i] || !(lv3 = campfire.matchGetter.getFirstMatch(lv2 = new SingleStackRecipeInput(lv), world).map(recipe -> ((CampfireCookingRecipe)recipe.value()).craft(lv2, (RegistryWrapper.WrapperLookup)world.getRegistryManager())).orElse(lv)).isItemEnabled(world.getEnabledFeatures())) continue;
            ItemScatterer.spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), lv3);
            campfire.itemsBeingCooked.set(i, ItemStack.EMPTY);
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
        }
        if (bl) {
            CampfireBlockEntity.markDirty(world, pos, state);
        }
    }

    public static void unlitServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        boolean bl = false;
        for (int i = 0; i < campfire.itemsBeingCooked.size(); ++i) {
            if (campfire.cookingTimes[i] <= 0) continue;
            bl = true;
            campfire.cookingTimes[i] = MathHelper.clamp(campfire.cookingTimes[i] - 2, 0, campfire.cookingTotalTimes[i]);
        }
        if (bl) {
            CampfireBlockEntity.markDirty(world, pos, state);
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        int i;
        Random lv = world.random;
        if (lv.nextFloat() < 0.11f) {
            for (i = 0; i < lv.nextInt(2) + 2; ++i) {
                CampfireBlock.spawnSmokeParticle(world, pos, state.get(CampfireBlock.SIGNAL_FIRE), false);
            }
        }
        i = state.get(CampfireBlock.FACING).getHorizontal();
        for (int j = 0; j < campfire.itemsBeingCooked.size(); ++j) {
            if (campfire.itemsBeingCooked.get(j).isEmpty() || !(lv.nextFloat() < 0.2f)) continue;
            Direction lv2 = Direction.fromHorizontal(Math.floorMod(j + i, 4));
            float f = 0.3125f;
            double d = (double)pos.getX() + 0.5 - (double)((float)lv2.getOffsetX() * 0.3125f) + (double)((float)lv2.rotateYClockwise().getOffsetX() * 0.3125f);
            double e = (double)pos.getY() + 0.5;
            double g = (double)pos.getZ() + 0.5 - (double)((float)lv2.getOffsetZ() * 0.3125f) + (double)((float)lv2.rotateYClockwise().getOffsetZ() * 0.3125f);
            for (int k = 0; k < 4; ++k) {
                world.addParticle(ParticleTypes.SMOKE, d, e, g, 0.0, 5.0E-4, 0.0);
            }
        }
    }

    public DefaultedList<ItemStack> getItemsBeingCooked() {
        return this.itemsBeingCooked;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        int[] is;
        super.readNbt(nbt, registryLookup);
        this.itemsBeingCooked.clear();
        Inventories.readNbt(nbt, this.itemsBeingCooked, registryLookup);
        if (nbt.contains("CookingTimes", NbtElement.INT_ARRAY_TYPE)) {
            is = nbt.getIntArray("CookingTimes");
            System.arraycopy(is, 0, this.cookingTimes, 0, Math.min(this.cookingTotalTimes.length, is.length));
        }
        if (nbt.contains("CookingTotalTimes", NbtElement.INT_ARRAY_TYPE)) {
            is = nbt.getIntArray("CookingTotalTimes");
            System.arraycopy(is, 0, this.cookingTotalTimes, 0, Math.min(this.cookingTotalTimes.length, is.length));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, this.itemsBeingCooked, true, registryLookup);
        nbt.putIntArray("CookingTimes", this.cookingTimes);
        nbt.putIntArray("CookingTotalTimes", this.cookingTotalTimes);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound lv = new NbtCompound();
        Inventories.writeNbt(lv, this.itemsBeingCooked, true, registryLookup);
        return lv;
    }

    public Optional<RecipeEntry<CampfireCookingRecipe>> getRecipeFor(ItemStack stack) {
        if (this.itemsBeingCooked.stream().noneMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        return this.matchGetter.getFirstMatch(new SingleStackRecipeInput(stack), this.world);
    }

    public boolean addItem(@Nullable LivingEntity user, ItemStack stack, int cookTime) {
        for (int j = 0; j < this.itemsBeingCooked.size(); ++j) {
            ItemStack lv = this.itemsBeingCooked.get(j);
            if (!lv.isEmpty()) continue;
            this.cookingTotalTimes[j] = cookTime;
            this.cookingTimes[j] = 0;
            this.itemsBeingCooked.set(j, stack.splitUnlessCreative(1, user));
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(user, this.getCachedState()));
            this.updateListeners();
            return true;
        }
        return false;
    }

    private void updateListeners() {
        this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    @Override
    public void clear() {
        this.itemsBeingCooked.clear();
    }

    public void spawnItemsBeingCooked() {
        if (this.world != null) {
            this.updateListeners();
        }
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(this.getItemsBeingCooked());
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(this.getItemsBeingCooked()));
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        nbt.remove("Items");
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

