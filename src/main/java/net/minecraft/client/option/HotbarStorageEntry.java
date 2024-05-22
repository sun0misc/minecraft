/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.option;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Util;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class HotbarStorageEntry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int HOTBAR_SIZE = PlayerInventory.getHotbarSize();
    public static final Codec<HotbarStorageEntry> CODEC = Codec.PASSTHROUGH.listOf().validate(stacks -> Util.decodeFixedLengthList(stacks, HOTBAR_SIZE)).xmap(HotbarStorageEntry::new, entry -> entry.stacks);
    private static final DynamicOps<NbtElement> NBT_OPS = NbtOps.INSTANCE;
    private static final Dynamic<?> EMPTY_STACK = new Dynamic<NbtElement>(NBT_OPS, ItemStack.OPTIONAL_CODEC.encodeStart(NBT_OPS, ItemStack.EMPTY).getOrThrow());
    private List<Dynamic<?>> stacks;

    private HotbarStorageEntry(List<Dynamic<?>> stacks) {
        this.stacks = stacks;
    }

    public HotbarStorageEntry() {
        this(Collections.nCopies(HOTBAR_SIZE, EMPTY_STACK));
    }

    public List<ItemStack> deserialize(RegistryWrapper.WrapperLookup registryLookup) {
        return this.stacks.stream().map(stack -> ItemStack.OPTIONAL_CODEC.parse(RegistryOps.withRegistry(stack, registryLookup)).resultOrPartial(error -> LOGGER.warn("Could not parse hotbar item: {}", error)).orElse(ItemStack.EMPTY)).toList();
    }

    public void serialize(PlayerInventory playerInventory, DynamicRegistryManager registryManager) {
        RegistryOps<NbtElement> lv = registryManager.getOps(NBT_OPS);
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(HOTBAR_SIZE);
        for (int i = 0; i < HOTBAR_SIZE; ++i) {
            ItemStack lv2 = playerInventory.getStack(i);
            Optional<Dynamic> optional = ItemStack.OPTIONAL_CODEC.encodeStart(lv, lv2).resultOrPartial(error -> LOGGER.warn("Could not encode hotbar item: {}", error)).map(nbt -> new Dynamic<NbtElement>(NBT_OPS, (NbtElement)nbt));
            builder.add(optional.orElse(EMPTY_STACK));
        }
        this.stacks = builder.build();
    }

    public boolean isEmpty() {
        for (Dynamic<?> dynamic : this.stacks) {
            if (HotbarStorageEntry.isEmpty(dynamic)) continue;
            return false;
        }
        return true;
    }

    private static boolean isEmpty(Dynamic<?> stack) {
        return EMPTY_STACK.equals(stack);
    }
}

