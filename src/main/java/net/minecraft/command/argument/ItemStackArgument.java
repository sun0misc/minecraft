/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemStackArgument {
    private static final Dynamic2CommandExceptionType OVERSTACKED_EXCEPTION = new Dynamic2CommandExceptionType((item, maxCount) -> Text.stringifiedTranslatable("arguments.item.overstacked", item, maxCount));
    private final RegistryEntry<Item> item;
    private final ComponentChanges components;

    public ItemStackArgument(RegistryEntry<Item> item, ComponentChanges components) {
        this.item = item;
        this.components = components;
    }

    public Item getItem() {
        return this.item.value();
    }

    public ItemStack createStack(int amount, boolean checkOverstack) throws CommandSyntaxException {
        ItemStack lv = new ItemStack(this.item, amount);
        lv.applyUnvalidatedChanges(this.components);
        if (checkOverstack && amount > lv.getMaxCount()) {
            throw OVERSTACKED_EXCEPTION.create(this.getIdString(), lv.getMaxCount());
        }
        return lv;
    }

    public String asString(RegistryWrapper.WrapperLookup registries) {
        StringBuilder stringBuilder = new StringBuilder(this.getIdString());
        String string = this.componentsAsString(registries);
        if (!string.isEmpty()) {
            stringBuilder.append('[');
            stringBuilder.append(string);
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    private String componentsAsString(RegistryWrapper.WrapperLookup registries) {
        RegistryOps<NbtElement> dynamicOps = registries.getOps(NbtOps.INSTANCE);
        return this.components.entrySet().stream().flatMap(entry -> {
            ComponentType lv = (ComponentType)entry.getKey();
            Identifier lv2 = Registries.DATA_COMPONENT_TYPE.getId(lv);
            if (lv2 == null) {
                return Stream.empty();
            }
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                Component lv3 = Component.of(lv, optional.get());
                return lv3.encode(dynamicOps).result().stream().map(value -> lv2.toString() + "=" + String.valueOf(value));
            }
            return Stream.of("!" + lv2.toString());
        }).collect(Collectors.joining(String.valueOf(',')));
    }

    private String getIdString() {
        return this.item.getKey().map(RegistryKey::getValue).orElseGet(() -> "unknown[" + String.valueOf(this.item) + "]").toString();
    }
}

