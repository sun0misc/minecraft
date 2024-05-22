/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record ReplaceBlockEnchantmentEffectType(Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState, Optional<RegistryEntry<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffectType
{
    public static final MapCodec<ReplaceBlockEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceBlockEnchantmentEffectType::offset), BlockPredicate.BASE_CODEC.optionalFieldOf("predicate").forGetter(ReplaceBlockEnchantmentEffectType::predicate), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("block_state")).forGetter(ReplaceBlockEnchantmentEffectType::blockState), GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceBlockEnchantmentEffectType::triggerGameEvent)).apply((Applicative<ReplaceBlockEnchantmentEffectType, ?>)instance, ReplaceBlockEnchantmentEffectType::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        BlockPos lv = BlockPos.ofFloored(pos).add(this.offset);
        if (this.predicate.map(predicate -> predicate.test(world, lv)).orElse(true).booleanValue() && world.setBlockState(lv, this.blockState.get(user.getRandom(), lv))) {
            this.triggerGameEvent.ifPresent(gameEvent -> world.emitGameEvent(user, (RegistryEntry<GameEvent>)gameEvent, lv));
        }
    }

    public MapCodec<ReplaceBlockEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

