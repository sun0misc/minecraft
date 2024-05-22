/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public final class EnchantmentActiveCheckLootCondition
extends Record
implements LootCondition {
    private final boolean active;
    public static final MapCodec<EnchantmentActiveCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("active")).forGetter(EnchantmentActiveCheckLootCondition::active)).apply((Applicative<EnchantmentActiveCheckLootCondition, ?>)instance, EnchantmentActiveCheckLootCondition::new));

    public EnchantmentActiveCheckLootCondition(boolean bl) {
        this.active = bl;
    }

    @Override
    public boolean test(LootContext arg) {
        return arg.requireParameter(LootContextParameters.ENCHANTMENT_ACTIVE) == this.active;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENCHANTMENT_ACTIVE_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Set.of(LootContextParameters.ENCHANTMENT_ACTIVE);
    }

    public static LootCondition.Builder active() {
        return () -> new EnchantmentActiveCheckLootCondition(true);
    }

    public static LootCondition.Builder inactive() {
        return () -> new EnchantmentActiveCheckLootCondition(false);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EnchantmentActiveCheckLootCondition.class, "active", "active"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EnchantmentActiveCheckLootCondition.class, "active", "active"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EnchantmentActiveCheckLootCondition.class, "active", "active"}, this, object);
    }

    public boolean active() {
        return this.active;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

