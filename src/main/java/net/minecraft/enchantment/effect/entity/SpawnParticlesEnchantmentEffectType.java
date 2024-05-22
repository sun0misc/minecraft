/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.random.Random;

public record SpawnParticlesEnchantmentEffectType(ParticleEffect particle, PositionSource horizontalPosition, PositionSource verticalPosition, VelocitySource horizontalVelocity, VelocitySource verticalVelocity, FloatProvider speed) implements EnchantmentEntityEffectType
{
    public static final MapCodec<SpawnParticlesEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("particle")).forGetter(SpawnParticlesEnchantmentEffectType::particle), PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticlesEnchantmentEffectType::horizontalPosition), PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticlesEnchantmentEffectType::verticalPosition), VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticlesEnchantmentEffectType::horizontalVelocity), VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticlesEnchantmentEffectType::verticalVelocity), FloatProvider.VALUE_CODEC.optionalFieldOf("speed", ConstantFloatProvider.ZERO).forGetter(SpawnParticlesEnchantmentEffectType::speed)).apply((Applicative<SpawnParticlesEnchantmentEffectType, ?>)instance, SpawnParticlesEnchantmentEffectType::new));

    public static PositionSource entityPosition(float offset) {
        return new PositionSource(PositionSourceType.ENTITY_POSITION, offset, 1.0f);
    }

    public static PositionSource withinBoundingBox() {
        return new PositionSource(PositionSourceType.BOUNDING_BOX, 0.0f, 1.0f);
    }

    public static VelocitySource scaledVelocity(float movementScale) {
        return new VelocitySource(movementScale, ConstantFloatProvider.ZERO);
    }

    public static VelocitySource fixedVelocity(FloatProvider base) {
        return new VelocitySource(0.0f, base);
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Random lv = user.getRandom();
        Vec3d lv2 = user.getMovement();
        float f = user.getWidth();
        float g = user.getHeight();
        world.spawnParticles(this.particle, this.horizontalPosition.getPosition(pos.getX(), f, lv), this.verticalPosition.getPosition(pos.getY(), g, lv), this.horizontalPosition.getPosition(pos.getZ(), f, lv), 0, this.horizontalVelocity.getVelocity(lv2.getX(), lv), this.verticalVelocity.getVelocity(lv2.getY(), lv), this.horizontalVelocity.getVelocity(lv2.getZ(), lv), this.speed.get(lv));
    }

    public MapCodec<SpawnParticlesEnchantmentEffectType> getCodec() {
        return CODEC;
    }

    public record PositionSource(PositionSourceType type, float offset, float scale) {
        public static final MapCodec<PositionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)PositionSourceType.CODEC.fieldOf("type")).forGetter(PositionSource::type), Codec.FLOAT.optionalFieldOf("offset", Float.valueOf(0.0f)).forGetter(PositionSource::offset), Codecs.POSITIVE_FLOAT.optionalFieldOf("scale", Float.valueOf(1.0f)).forGetter(PositionSource::scale)).apply((Applicative<PositionSource, ?>)instance, PositionSource::new)).validate(source -> {
            if (source.type() == PositionSourceType.ENTITY_POSITION && source.scale() != 1.0f) {
                return DataResult.error(() -> "Cannot scale an entity position coordinate source");
            }
            return DataResult.success(source);
        });

        public double getPosition(double entityPosition, float boundingBoxSize, Random random) {
            return this.type.getCoordinate(entityPosition, boundingBoxSize * this.scale, random) + (double)this.offset;
        }
    }

    public record VelocitySource(float movementScale, FloatProvider base) {
        public static final MapCodec<VelocitySource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.FLOAT.optionalFieldOf("movement_scale", Float.valueOf(0.0f)).forGetter(VelocitySource::movementScale), FloatProvider.VALUE_CODEC.optionalFieldOf("base", ConstantFloatProvider.ZERO).forGetter(VelocitySource::base)).apply((Applicative<VelocitySource, ?>)instance, VelocitySource::new));

        public double getVelocity(double entityVelocity, Random random) {
            return entityVelocity * (double)this.movementScale + (double)this.base.get(random);
        }
    }

    public static enum PositionSourceType implements StringIdentifiable
    {
        ENTITY_POSITION("entity_position", (entityPosition, boundingBoxSize, random) -> entityPosition),
        BOUNDING_BOX("in_bounding_box", (entityPosition, boundingBoxSize, random) -> entityPosition + (random.nextDouble() - 0.5) * (double)boundingBoxSize);

        public static final Codec<PositionSourceType> CODEC;
        private final String id;
        private final CoordinateSource coordinateSource;

        private PositionSourceType(String id, CoordinateSource coordinateSource) {
            this.id = id;
            this.coordinateSource = coordinateSource;
        }

        public double getCoordinate(double entityPosition, float boundingBoxSize, Random random) {
            return this.coordinateSource.getCoordinate(entityPosition, boundingBoxSize, random);
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(PositionSourceType::values);
        }

        @FunctionalInterface
        static interface CoordinateSource {
            public double getCoordinate(double var1, float var3, Random var4);
        }
    }
}

