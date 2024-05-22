/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.random.Random;

public record PlaySoundEnchantmentEffectType(RegistryEntry<SoundEvent> soundEvent, FloatProvider volume, FloatProvider pitch) implements EnchantmentEntityEffectType
{
    public static final MapCodec<PlaySoundEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)SoundEvent.ENTRY_CODEC.fieldOf("sound")).forGetter(PlaySoundEnchantmentEffectType::soundEvent), ((MapCodec)FloatProvider.createValidatedCodec(1.0E-5f, 10.0f).fieldOf("volume")).forGetter(PlaySoundEnchantmentEffectType::volume), ((MapCodec)FloatProvider.createValidatedCodec(1.0E-5f, 2.0f).fieldOf("pitch")).forGetter(PlaySoundEnchantmentEffectType::pitch)).apply((Applicative<PlaySoundEnchantmentEffectType, ?>)instance, PlaySoundEnchantmentEffectType::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Random lv = user.getRandom();
        if (!user.isSilent()) {
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), this.soundEvent, user.getSoundCategory(), this.volume.get(lv), this.pitch.get(lv));
        }
    }

    public MapCodec<PlaySoundEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

