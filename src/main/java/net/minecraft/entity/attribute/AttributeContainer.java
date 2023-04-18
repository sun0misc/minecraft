package net.minecraft.entity.attribute;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AttributeContainer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map custom = Maps.newHashMap();
   private final Set tracked = Sets.newHashSet();
   private final DefaultAttributeContainer fallback;

   public AttributeContainer(DefaultAttributeContainer defaultAttributes) {
      this.fallback = defaultAttributes;
   }

   private void updateTrackedStatus(EntityAttributeInstance instance) {
      if (instance.getAttribute().isTracked()) {
         this.tracked.add(instance);
      }

   }

   public Set getTracked() {
      return this.tracked;
   }

   public Collection getAttributesToSend() {
      return (Collection)this.custom.values().stream().filter((attribute) -> {
         return attribute.getAttribute().isTracked();
      }).collect(Collectors.toList());
   }

   @Nullable
   public EntityAttributeInstance getCustomInstance(EntityAttribute attribute) {
      return (EntityAttributeInstance)this.custom.computeIfAbsent(attribute, (attributex) -> {
         return this.fallback.createOverride(this::updateTrackedStatus, attributex);
      });
   }

   @Nullable
   public EntityAttributeInstance getCustomInstance(RegistryEntry attribute) {
      return this.getCustomInstance((EntityAttribute)attribute.value());
   }

   public boolean hasAttribute(EntityAttribute attribute) {
      return this.custom.get(attribute) != null || this.fallback.has(attribute);
   }

   public boolean hasAttribute(RegistryEntry attribute) {
      return this.hasAttribute((EntityAttribute)attribute.value());
   }

   public boolean hasModifierForAttribute(EntityAttribute attribute, UUID uuid) {
      EntityAttributeInstance lv = (EntityAttributeInstance)this.custom.get(attribute);
      return lv != null ? lv.getModifier(uuid) != null : this.fallback.hasModifier(attribute, uuid);
   }

   public boolean hasModifierForAttribute(RegistryEntry attribute, UUID uuid) {
      return this.hasModifierForAttribute((EntityAttribute)attribute.value(), uuid);
   }

   public double getValue(EntityAttribute attribute) {
      EntityAttributeInstance lv = (EntityAttributeInstance)this.custom.get(attribute);
      return lv != null ? lv.getValue() : this.fallback.getValue(attribute);
   }

   public double getBaseValue(EntityAttribute attribute) {
      EntityAttributeInstance lv = (EntityAttributeInstance)this.custom.get(attribute);
      return lv != null ? lv.getBaseValue() : this.fallback.getBaseValue(attribute);
   }

   public double getModifierValue(EntityAttribute attribute, UUID uuid) {
      EntityAttributeInstance lv = (EntityAttributeInstance)this.custom.get(attribute);
      return lv != null ? lv.getModifier(uuid).getValue() : this.fallback.getModifierValue(attribute, uuid);
   }

   public double getModifierValue(RegistryEntry attribute, UUID uuid) {
      return this.getModifierValue((EntityAttribute)attribute.value(), uuid);
   }

   public void removeModifiers(Multimap attributeModifiers) {
      attributeModifiers.asMap().forEach((attribute, modifiers) -> {
         EntityAttributeInstance lv = (EntityAttributeInstance)this.custom.get(attribute);
         if (lv != null) {
            Objects.requireNonNull(lv);
            modifiers.forEach(lv::removeModifier);
         }

      });
   }

   public void addTemporaryModifiers(Multimap attributeModifiers) {
      attributeModifiers.forEach((attribute, attributeModifier) -> {
         EntityAttributeInstance lv = this.getCustomInstance(attribute);
         if (lv != null) {
            lv.removeModifier(attributeModifier);
            lv.addTemporaryModifier(attributeModifier);
         }

      });
   }

   public void setFrom(AttributeContainer other) {
      other.custom.values().forEach((attributeInstance) -> {
         EntityAttributeInstance lv = this.getCustomInstance(attributeInstance.getAttribute());
         if (lv != null) {
            lv.setFrom(attributeInstance);
         }

      });
   }

   public NbtList toNbt() {
      NbtList lv = new NbtList();
      Iterator var2 = this.custom.values().iterator();

      while(var2.hasNext()) {
         EntityAttributeInstance lv2 = (EntityAttributeInstance)var2.next();
         lv.add(lv2.toNbt());
      }

      return lv;
   }

   public void readNbt(NbtList nbt) {
      for(int i = 0; i < nbt.size(); ++i) {
         NbtCompound lv = nbt.getCompound(i);
         String string = lv.getString("Name");
         Util.ifPresentOrElse(Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(string)), (attribute) -> {
            EntityAttributeInstance lvx = this.getCustomInstance(attribute);
            if (lvx != null) {
               lvx.readNbt(lv);
            }

         }, () -> {
            LOGGER.warn("Ignoring unknown attribute '{}'", string);
         });
      }

   }
}
