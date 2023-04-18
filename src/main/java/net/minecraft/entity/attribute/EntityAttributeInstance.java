package net.minecraft.entity.attribute;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

public class EntityAttributeInstance {
   private final EntityAttribute type;
   private final Map operationToModifiers = Maps.newEnumMap(EntityAttributeModifier.Operation.class);
   private final Map idToModifiers = new Object2ObjectArrayMap();
   private final Set persistentModifiers = new ObjectArraySet();
   private double baseValue;
   private boolean dirty = true;
   private double value;
   private final Consumer updateCallback;

   public EntityAttributeInstance(EntityAttribute type, Consumer updateCallback) {
      this.type = type;
      this.updateCallback = updateCallback;
      this.baseValue = type.getDefaultValue();
   }

   public EntityAttribute getAttribute() {
      return this.type;
   }

   public double getBaseValue() {
      return this.baseValue;
   }

   public void setBaseValue(double baseValue) {
      if (baseValue != this.baseValue) {
         this.baseValue = baseValue;
         this.onUpdate();
      }
   }

   public Set getModifiers(EntityAttributeModifier.Operation operation) {
      return (Set)this.operationToModifiers.computeIfAbsent(operation, (operationx) -> {
         return Sets.newHashSet();
      });
   }

   public Set getModifiers() {
      return ImmutableSet.copyOf(this.idToModifiers.values());
   }

   @Nullable
   public EntityAttributeModifier getModifier(UUID uuid) {
      return (EntityAttributeModifier)this.idToModifiers.get(uuid);
   }

   public boolean hasModifier(EntityAttributeModifier modifier) {
      return this.idToModifiers.get(modifier.getId()) != null;
   }

   private void addModifier(EntityAttributeModifier modifier) {
      EntityAttributeModifier lv = (EntityAttributeModifier)this.idToModifiers.putIfAbsent(modifier.getId(), modifier);
      if (lv != null) {
         throw new IllegalArgumentException("Modifier is already applied on this attribute!");
      } else {
         this.getModifiers(modifier.getOperation()).add(modifier);
         this.onUpdate();
      }
   }

   public void addTemporaryModifier(EntityAttributeModifier modifier) {
      this.addModifier(modifier);
   }

   public void addPersistentModifier(EntityAttributeModifier modifier) {
      this.addModifier(modifier);
      this.persistentModifiers.add(modifier);
   }

   protected void onUpdate() {
      this.dirty = true;
      this.updateCallback.accept(this);
   }

   public void removeModifier(EntityAttributeModifier modifier) {
      this.getModifiers(modifier.getOperation()).remove(modifier);
      this.idToModifiers.remove(modifier.getId());
      this.persistentModifiers.remove(modifier);
      this.onUpdate();
   }

   public void removeModifier(UUID uuid) {
      EntityAttributeModifier lv = this.getModifier(uuid);
      if (lv != null) {
         this.removeModifier(lv);
      }

   }

   public boolean tryRemoveModifier(UUID uuid) {
      EntityAttributeModifier lv = this.getModifier(uuid);
      if (lv != null && this.persistentModifiers.contains(lv)) {
         this.removeModifier(lv);
         return true;
      } else {
         return false;
      }
   }

   public void clearModifiers() {
      Iterator var1 = this.getModifiers().iterator();

      while(var1.hasNext()) {
         EntityAttributeModifier lv = (EntityAttributeModifier)var1.next();
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

      EntityAttributeModifier lv;
      for(Iterator var3 = this.getModifiersByOperation(EntityAttributeModifier.Operation.ADDITION).iterator(); var3.hasNext(); d += lv.getValue()) {
         lv = (EntityAttributeModifier)var3.next();
      }

      double e = d;

      Iterator var5;
      EntityAttributeModifier lv2;
      for(var5 = this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE).iterator(); var5.hasNext(); e += d * lv2.getValue()) {
         lv2 = (EntityAttributeModifier)var5.next();
      }

      for(var5 = this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL).iterator(); var5.hasNext(); e *= 1.0 + lv2.getValue()) {
         lv2 = (EntityAttributeModifier)var5.next();
      }

      return this.type.clamp(e);
   }

   private Collection getModifiersByOperation(EntityAttributeModifier.Operation operation) {
      return (Collection)this.operationToModifiers.getOrDefault(operation, Collections.emptySet());
   }

   public void setFrom(EntityAttributeInstance other) {
      this.baseValue = other.baseValue;
      this.idToModifiers.clear();
      this.idToModifiers.putAll(other.idToModifiers);
      this.persistentModifiers.clear();
      this.persistentModifiers.addAll(other.persistentModifiers);
      this.operationToModifiers.clear();
      other.operationToModifiers.forEach((operation, modifiers) -> {
         this.getModifiers(operation).addAll(modifiers);
      });
      this.onUpdate();
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      lv.putString("Name", Registries.ATTRIBUTE.getId(this.type).toString());
      lv.putDouble("Base", this.baseValue);
      if (!this.persistentModifiers.isEmpty()) {
         NbtList lv2 = new NbtList();
         Iterator var3 = this.persistentModifiers.iterator();

         while(var3.hasNext()) {
            EntityAttributeModifier lv3 = (EntityAttributeModifier)var3.next();
            lv2.add(lv3.toNbt());
         }

         lv.put("Modifiers", lv2);
      }

      return lv;
   }

   public void readNbt(NbtCompound nbt) {
      this.baseValue = nbt.getDouble("Base");
      if (nbt.contains("Modifiers", NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList("Modifiers", NbtElement.COMPOUND_TYPE);

         for(int i = 0; i < lv.size(); ++i) {
            EntityAttributeModifier lv2 = EntityAttributeModifier.fromNbt(lv.getCompound(i));
            if (lv2 != null) {
               this.idToModifiers.put(lv2.getId(), lv2);
               this.getModifiers(lv2.getOperation()).add(lv2);
               this.persistentModifiers.add(lv2);
            }
         }
      }

      this.onUpdate();
   }
}
