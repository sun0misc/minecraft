/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class DustColorTransitionParticleEffect
extends AbstractDustParticleEffect {
    public static final Vector3f SCULK_BLUE = Vec3d.unpackRgb(3790560).toVector3f();
    public static final DustColorTransitionParticleEffect DEFAULT = new DustColorTransitionParticleEffect(SCULK_BLUE, DustParticleEffect.RED, 1.0f);
    public static final MapCodec<DustColorTransitionParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.VECTOR_3F.fieldOf("from_color")).forGetter(effect -> effect.fromColor), ((MapCodec)Codecs.VECTOR_3F.fieldOf("to_color")).forGetter(effect -> effect.toColor), ((MapCodec)SCALE_CODEC.fieldOf("scale")).forGetter(AbstractDustParticleEffect::getScale)).apply((Applicative<DustColorTransitionParticleEffect, ?>)instance, DustColorTransitionParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, DustColorTransitionParticleEffect> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VECTOR3F, effect -> effect.fromColor, PacketCodecs.VECTOR3F, effect -> effect.toColor, PacketCodecs.FLOAT, AbstractDustParticleEffect::getScale, DustColorTransitionParticleEffect::new);
    private final Vector3f fromColor;
    private final Vector3f toColor;

    public DustColorTransitionParticleEffect(Vector3f fromColor, Vector3f toColor, float scale) {
        super(scale);
        this.fromColor = fromColor;
        this.toColor = toColor;
    }

    public Vector3f getFromColor() {
        return this.fromColor;
    }

    public Vector3f getToColor() {
        return this.toColor;
    }

    public ParticleType<DustColorTransitionParticleEffect> getType() {
        return ParticleTypes.DUST_COLOR_TRANSITION;
    }
}

