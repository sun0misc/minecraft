/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.slf4j.Logger;

public class ReferenceLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<ReferenceLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ReferenceLootFunction.addConditionsField(instance).and(((MapCodec)RegistryKey.createCodec(RegistryKeys.ITEM_MODIFIER).fieldOf("name")).forGetter(function -> function.name)).apply((Applicative<ReferenceLootFunction, ?>)instance, ReferenceLootFunction::new));
    private final RegistryKey<LootFunction> name;

    private ReferenceLootFunction(List<LootCondition> conditions, RegistryKey<LootFunction> name) {
        super(conditions);
        this.name = name;
    }

    public LootFunctionType<ReferenceLootFunction> getType() {
        return LootFunctionTypes.REFERENCE;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        if (reporter.isInStack(this.name)) {
            reporter.report("Function " + String.valueOf(this.name.getValue()) + " is recursively called");
            return;
        }
        super.validate(reporter);
        reporter.getDataLookup().getOptionalEntry(RegistryKeys.ITEM_MODIFIER, this.name).ifPresentOrElse(arg2 -> ((LootFunction)arg2.value()).validate(reporter.makeChild(".{" + String.valueOf(this.name.getValue()) + "}", this.name)), () -> reporter.report("Unknown function table called " + String.valueOf(this.name.getValue())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        LootFunction lv = context.getLookup().getOptionalEntry(RegistryKeys.ITEM_MODIFIER, this.name).map(RegistryEntry::value).orElse(null);
        if (lv == null) {
            LOGGER.warn("Unknown function: {}", (Object)this.name.getValue());
            return stack;
        }
        LootContext.Entry<LootFunction> lv2 = LootContext.itemModifier(lv);
        if (context.markActive(lv2)) {
            try {
                ItemStack itemStack = (ItemStack)lv.apply(stack, context);
                return itemStack;
            } finally {
                context.markInactive(lv2);
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(RegistryKey<LootFunction> name) {
        return ReferenceLootFunction.builder((List<LootCondition> conditions) -> new ReferenceLootFunction((List<LootCondition>)conditions, name));
    }
}

