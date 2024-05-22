/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.attribute;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class EntityAttributeInstance {
    private static final String field_52008 = "base";
    private static final String field_52009 = "modifiers";
    public static final String field_52007 = "id";
    private final RegistryEntry<EntityAttribute> type;
    private final Map<EntityAttributeModifier.Operation, Map<Identifier, EntityAttributeModifier>> operationToModifiers = Maps.newEnumMap(EntityAttributeModifier.Operation.class);
    private final Map<Identifier, EntityAttributeModifier> idToModifiers = new Object2ObjectArrayMap<Identifier, EntityAttributeModifier>();
    private final Map<Identifier, EntityAttributeModifier> persistentModifiers = new Object2ObjectArrayMap<Identifier, EntityAttributeModifier>();
    private double baseValue;
    private boolean dirty = true;
    private double value;
    private final Consumer<EntityAttributeInstance> updateCallback;

    public EntityAttributeInstance(RegistryEntry<EntityAttribute> type, Consumer<EntityAttributeInstance> updateCallback) {
        this.type = type;
        this.updateCallback = updateCallback;
        this.baseValue = type.value().getDefaultValue();
    }

    public RegistryEntry<EntityAttribute> getAttribute() {
        return this.type;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double baseValue) {
        if (baseValue == this.baseValue) {
            return;
        }
        this.baseValue = baseValue;
        this.onUpdate();
    }

    @VisibleForTesting
    Map<Identifier, EntityAttributeModifier> getModifiers(EntityAttributeModifier.Operation operation) {
        return this.operationToModifiers.computeIfAbsent(operation, operationx -> new Object2ObjectOpenHashMap());
    }

    public Set<EntityAttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.idToModifiers.values());
    }

    @Nullable
    public EntityAttributeModifier getModifier(Identifier arg) {
        return this.idToModifiers.get(arg);
    }

    public boolean hasModifier(Identifier arg) {
        return this.idToModifiers.get(arg) != null;
    }

    private void addModifier(EntityAttributeModifier modifier) {
        EntityAttributeModifier lv = this.idToModifiers.putIfAbsent(modifier.uuid(), modifier);
        if (lv != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        this.getModifiers(modifier.operation()).put(modifier.uuid(), modifier);
        this.onUpdate();
    }

    public void updateModifier(EntityAttributeModifier modifier) {
        EntityAttributeModifier lv = this.idToModifiers.put(modifier.uuid(), modifier);
        if (modifier == lv) {
            return;
        }
        this.getModifiers(modifier.operation()).put(modifier.uuid(), modifier);
        this.onUpdate();
    }

    public void addTemporaryModifier(EntityAttributeModifier modifier) {
        this.addModifier(modifier);
    }

    public void addPersistentModifier(EntityAttributeModifier modifier) {
        this.addModifier(modifier);
        this.persistentModifiers.put(modifier.uuid(), modifier);
    }

    protected void onUpdate() {
        this.dirty = true;
        this.updateCallback.accept(this);
    }

    public void removeModifier(EntityAttributeModifier modifier) {
        this.removeModifier(modifier.uuid());
    }

    public void removeModifier(Identifier arg) {
        EntityAttributeModifier lv = this.idToModifiers.remove(arg);
        if (lv == null) {
            return;
        }
        this.getModifiers(lv.operation()).remove(arg);
        this.persistentModifiers.remove(arg);
        this.onUpdate();
    }

    public boolean tryRemoveModifier(Identifier arg) {
        EntityAttributeModifier lv = this.persistentModifiers.remove(arg);
        if (lv == null) {
            return false;
        }
        this.getModifiers(lv.operation()).remove(lv.uuid());
        this.idToModifiers.remove(arg);
        this.onUpdate();
        return true;
    }

    public void clearModifiers() {
        for (EntityAttributeModifier lv : this.getModifiers()) {
            this.removeModifier(lv);
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.value = this.computeValue();
            this.dirty = false;
        }
        return this.value;
    }

    private double computeValue() {
        double d = this.getBaseValue();
        for (EntityAttributeModifier lv : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADD_VALUE)) {
            d += lv.value();
        }
        double e = d;
        for (EntityAttributeModifier lv2 : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            e += d * lv2.value();
        }
        for (EntityAttributeModifier lv2 : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            e *= 1.0 + lv2.value();
        }
        return this.type.value().clamp(e);
    }

    private Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation) {
        return this.operationToModifiers.getOrDefault(operation, Map.of()).values();
    }

    public void setFrom(EntityAttributeInstance other) {
        this.baseValue = other.baseValue;
        this.idToModifiers.clear();
        this.idToModifiers.putAll(other.idToModifiers);
        this.persistentModifiers.clear();
        this.persistentModifiers.putAll(other.persistentModifiers);
        this.operationToModifiers.clear();
        other.operationToModifiers.forEach((operation, modifiers) -> this.getModifiers((EntityAttributeModifier.Operation)operation).putAll((Map<Identifier, EntityAttributeModifier>)modifiers));
        this.onUpdate();
    }

    public NbtCompound toNbt() {
        NbtCompound lv = new NbtCompound();
        RegistryKey<EntityAttribute> lv2 = this.type.getKey().orElseThrow(() -> new IllegalStateException("Tried to serialize unregistered attribute"));
        lv.putString(field_52007, lv2.getValue().toString());
        lv.putDouble(field_52008, this.baseValue);
        if (!this.persistentModifiers.isEmpty()) {
            NbtList lv3 = new NbtList();
            for (EntityAttributeModifier lv4 : this.persistentModifiers.values()) {
                lv3.add(lv4.toNbt());
            }
            lv.put(field_52009, lv3);
        }
        return lv;
    }

    public void readNbt(NbtCompound nbt) {
        this.baseValue = nbt.getDouble(field_52008);
        if (nbt.contains(field_52009, NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList(field_52009, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv.size(); ++i) {
                EntityAttributeModifier lv2 = EntityAttributeModifier.fromNbt(lv.getCompound(i));
                if (lv2 == null) continue;
                this.idToModifiers.put(lv2.uuid(), lv2);
                this.getModifiers(lv2.operation()).put(lv2.uuid(), lv2);
                this.persistentModifiers.put(lv2.uuid(), lv2);
            }
        }
        this.onUpdate();
    }
}

