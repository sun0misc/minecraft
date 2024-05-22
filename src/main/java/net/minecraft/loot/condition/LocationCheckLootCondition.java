/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public record LocationCheckLootCondition(Optional<LocationPredicate> predicate, BlockPos offset) implements LootCondition
{
    private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX), Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY), Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)).apply((Applicative<BlockPos, ?>)instance, BlockPos::new));
    public static final MapCodec<LocationCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(LocationPredicate.CODEC.optionalFieldOf("predicate").forGetter(LocationCheckLootCondition::predicate), OFFSET_CODEC.forGetter(LocationCheckLootCondition::offset)).apply((Applicative<LocationCheckLootCondition, ?>)instance, LocationCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.LOCATION_CHECK;
    }

    @Override
    public boolean test(LootContext arg) {
        Vec3d lv = arg.get(LootContextParameters.ORIGIN);
        return lv != null && (this.predicate.isEmpty() || this.predicate.get().test(arg.getWorld(), lv.getX() + (double)this.offset.getX(), lv.getY() + (double)this.offset.getY(), lv.getZ() + (double)this.offset.getZ()));
    }

    public static LootCondition.Builder builder(LocationPredicate.Builder predicateBuilder) {
        return () -> new LocationCheckLootCondition(Optional.of(predicateBuilder.build()), BlockPos.ORIGIN);
    }

    public static LootCondition.Builder builder(LocationPredicate.Builder predicateBuilder, BlockPos pos) {
        return () -> new LocationCheckLootCondition(Optional.of(predicateBuilder.build()), pos);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

