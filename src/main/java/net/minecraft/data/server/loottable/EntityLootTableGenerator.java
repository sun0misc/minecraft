/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.loottable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.data.server.loottable.LootTableGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.AnyOfLootCondition;
import net.minecraft.loot.condition.DamageSourcePropertiesLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.predicate.item.ItemSubPredicateTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.resource.featuretoggle.FeatureSet;

public abstract class EntityLootTableGenerator
implements LootTableGenerator {
    private static final Set<EntityType<?>> ENTITY_TYPES_IN_MISC_GROUP_TO_CHECK = ImmutableSet.of(EntityType.PLAYER, EntityType.ARMOR_STAND, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.VILLAGER);
    protected final RegistryWrapper.WrapperLookup registryLookup;
    private final FeatureSet requiredFeatures;
    private final FeatureSet featureSet;
    private final Map<EntityType<?>, Map<RegistryKey<LootTable>, LootTable.Builder>> lootTables = Maps.newHashMap();

    protected final AnyOfLootCondition.Builder createSmeltLootCondition() {
        RegistryWrapper.Impl<Enchantment> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return AnyOfLootCondition.builder(EntityPropertiesLootCondition.builder(LootContext.EntityTarget.THIS, EntityPredicate.Builder.create().flags(EntityFlagsPredicate.Builder.create().onFire(true))), EntityPropertiesLootCondition.builder(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.create().equipment(EntityEquipmentPredicate.Builder.create().mainhand(ItemPredicate.Builder.create().subPredicate(ItemSubPredicateTypes.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(lv.getOrThrow(EnchantmentTags.SMELTS_LOOT), NumberRange.IntRange.ANY))))))));
    }

    protected EntityLootTableGenerator(FeatureSet requiredFeatures, RegistryWrapper.WrapperLookup registryLookup) {
        this(requiredFeatures, requiredFeatures, registryLookup);
    }

    protected EntityLootTableGenerator(FeatureSet requiredFeatures, FeatureSet featureSet, RegistryWrapper.WrapperLookup registryLookup) {
        this.requiredFeatures = requiredFeatures;
        this.featureSet = featureSet;
        this.registryLookup = registryLookup;
    }

    protected static LootTable.Builder createForSheep(ItemConvertible item) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(item))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(LootTableEntry.builder(EntityType.SHEEP.getLootTableId())));
    }

    public abstract void generate();

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        this.generate();
        HashSet set = new HashSet();
        Registries.ENTITY_TYPE.streamEntries().forEach(entityType -> {
            EntityType lv = (EntityType)entityType.value();
            if (!lv.isEnabled(this.requiredFeatures)) {
                return;
            }
            if (EntityLootTableGenerator.shouldCheck(lv)) {
                Map<RegistryKey<LootTable>, LootTable.Builder> map = this.lootTables.remove(lv);
                RegistryKey<LootTable> lv2 = lv.getLootTableId();
                if (lv2 != LootTables.EMPTY && lv.isEnabled(this.featureSet) && (map == null || !map.containsKey(lv2))) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", lv2, entityType.registryKey().getValue()));
                }
                if (map != null) {
                    map.forEach((tableKey, lootTableBuilder) -> {
                        if (!set.add(tableKey)) {
                            throw new IllegalStateException(String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", tableKey, entityType.registryKey().getValue()));
                        }
                        lootTableBiConsumer.accept((RegistryKey<LootTable>)tableKey, (LootTable.Builder)lootTableBuilder);
                    });
                }
            } else {
                Map<RegistryKey<LootTable>, LootTable.Builder> map = this.lootTables.remove(lv);
                if (map != null) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Weird loottables '%s' for '%s', not a LivingEntity so should not have loot", map.keySet().stream().map(arg -> arg.getValue().toString()).collect(Collectors.joining(",")), entityType.registryKey().getValue()));
                }
            }
        });
        if (!this.lootTables.isEmpty()) {
            throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + String.valueOf(this.lootTables.keySet()));
        }
    }

    private static boolean shouldCheck(EntityType<?> entityType) {
        return ENTITY_TYPES_IN_MISC_GROUP_TO_CHECK.contains(entityType) || entityType.getSpawnGroup() != SpawnGroup.MISC;
    }

    protected LootCondition.Builder killedByFrog() {
        return DamageSourcePropertiesLootCondition.builder(DamageSourcePredicate.Builder.create().sourceEntity(EntityPredicate.Builder.create().type(EntityType.FROG)));
    }

    protected LootCondition.Builder killedByFrog(RegistryKey<FrogVariant> frogVariant) {
        return DamageSourcePropertiesLootCondition.builder(DamageSourcePredicate.Builder.create().sourceEntity(EntityPredicate.Builder.create().type(EntityType.FROG).typeSpecific(EntitySubPredicateTypes.frogVariant(Registries.FROG_VARIANT.entryOf(frogVariant)))));
    }

    protected void register(EntityType<?> entityType, LootTable.Builder lootTable) {
        this.register(entityType, entityType.getLootTableId(), lootTable);
    }

    protected void register(EntityType<?> entityType, RegistryKey<LootTable> tableKey, LootTable.Builder lootTable) {
        this.lootTables.computeIfAbsent(entityType, type -> new HashMap()).put(tableKey, lootTable);
    }
}

