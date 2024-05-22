/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.event.GameEvent;

public record SetBlockPropertiesEnchantmentEffectType(BlockStateComponent properties, Vec3i offset, Optional<RegistryEntry<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffectType
{
    public static final MapCodec<SetBlockPropertiesEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockStateComponent.CODEC.fieldOf("properties")).forGetter(SetBlockPropertiesEnchantmentEffectType::properties), Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(SetBlockPropertiesEnchantmentEffectType::offset), GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(SetBlockPropertiesEnchantmentEffectType::triggerGameEvent)).apply((Applicative<SetBlockPropertiesEnchantmentEffectType, ?>)instance, SetBlockPropertiesEnchantmentEffectType::new));

    public SetBlockPropertiesEnchantmentEffectType(BlockStateComponent properties) {
        this(properties, Vec3i.ZERO, Optional.of(GameEvent.BLOCK_CHANGE));
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        BlockState lv3;
        BlockPos lv = BlockPos.ofFloored(pos).add(this.offset);
        BlockState lv2 = user.getWorld().getBlockState(lv);
        if (!lv2.equals(lv3 = this.properties.applyToState(lv2)) && user.getWorld().setBlockState(lv, lv3, Block.NOTIFY_ALL)) {
            this.triggerGameEvent.ifPresent(gameEvent -> world.emitGameEvent(user, (RegistryEntry<GameEvent>)gameEvent, lv));
        }
    }

    public MapCodec<SetBlockPropertiesEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

