/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffectType;
import net.minecraft.enchantment.effect.EnchantmentValueEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public interface AllOfEnchantmentEffectTypes {
    public static <T, A extends T> MapCodec<A> buildCodec(Codec<T> baseCodec, Function<List<T>, A> fromList, Function<A, List<T>> toList) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)baseCodec.listOf().fieldOf("effects")).forGetter(toList)).apply(instance, fromList));
    }

    public static EntityEffects allOf(EnchantmentEntityEffectType ... entityEffects) {
        return new EntityEffects(List.of(entityEffects));
    }

    public static LocationBasedEffects allOf(EnchantmentLocationBasedEffectType ... locationBasedEffects) {
        return new LocationBasedEffects(List.of(locationBasedEffects));
    }

    public static ValueEffects allOf(EnchantmentValueEffectType ... valueEffects) {
        return new ValueEffects(List.of(valueEffects));
    }

    public record EntityEffects(List<EnchantmentEntityEffectType> effects) implements EnchantmentEntityEffectType
    {
        public static final MapCodec<EntityEffects> CODEC = AllOfEnchantmentEffectTypes.buildCodec(EnchantmentEntityEffectType.CODEC, EntityEffects::new, EntityEffects::effects);

        @Override
        public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
            for (EnchantmentEntityEffectType lv : this.effects) {
                lv.apply(world, level, context, user, pos);
            }
        }

        public MapCodec<EntityEffects> getCodec() {
            return CODEC;
        }
    }

    public record LocationBasedEffects(List<EnchantmentLocationBasedEffectType> effects) implements EnchantmentLocationBasedEffectType
    {
        public static final MapCodec<LocationBasedEffects> CODEC = AllOfEnchantmentEffectTypes.buildCodec(EnchantmentLocationBasedEffectType.CODEC, LocationBasedEffects::new, LocationBasedEffects::effects);

        @Override
        public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos, boolean newlyApplied) {
            for (EnchantmentLocationBasedEffectType lv : this.effects) {
                lv.apply(world, level, context, user, pos, newlyApplied);
            }
        }

        @Override
        public void remove(EnchantmentEffectContext context, Entity user, Vec3d pos, int level) {
            for (EnchantmentLocationBasedEffectType lv : this.effects) {
                lv.remove(context, user, pos, level);
            }
        }

        public MapCodec<LocationBasedEffects> getCodec() {
            return CODEC;
        }
    }

    public record ValueEffects(List<EnchantmentValueEffectType> effects) implements EnchantmentValueEffectType
    {
        public static final MapCodec<ValueEffects> CODEC = AllOfEnchantmentEffectTypes.buildCodec(EnchantmentValueEffectType.CODEC, ValueEffects::new, ValueEffects::effects);

        @Override
        public float apply(int level, Random random, float inputValue) {
            for (EnchantmentValueEffectType lv : this.effects) {
                inputValue = lv.apply(level, random, inputValue);
            }
            return inputValue;
        }

        public MapCodec<ValueEffects> getCodec() {
            return CODEC;
        }
    }
}

