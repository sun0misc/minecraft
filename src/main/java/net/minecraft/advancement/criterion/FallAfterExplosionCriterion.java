/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FallAfterExplosionCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Vec3d startPosition, @Nullable Entity cause) {
        Vec3d lv = player.getPos();
        LootContext lv2 = cause != null ? EntityPredicate.createAdvancementEntityLootContext(player, cause) : null;
        this.trigger(player, conditions -> conditions.matches(player.getServerWorld(), startPosition, lv, lv2));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance, Optional<LootContextPredicate> cause) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(Conditions::startPosition), DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(Conditions::distance), EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("cause").forGetter(Conditions::cause)).apply((Applicative<Conditions, ?>)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(DistancePredicate distance, EntityPredicate.Builder cause) {
            return Criteria.FALL_AFTER_EXPLOSION.create(new Conditions(Optional.empty(), Optional.empty(), Optional.of(distance), Optional.of(EntityPredicate.contextPredicateFromEntityPredicate(cause))));
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.cause(), ".cause");
        }

        public boolean matches(ServerWorld world, Vec3d startPosition, Vec3d endPosition, @Nullable LootContext cause) {
            if (this.startPosition.isPresent() && !this.startPosition.get().test(world, startPosition.x, startPosition.y, startPosition.z)) {
                return false;
            }
            if (this.distance.isPresent() && !this.distance.get().test(startPosition.x, startPosition.y, startPosition.z, endPosition.x, endPosition.y, endPosition.z)) {
                return false;
            }
            return !this.cause.isPresent() || cause != null && this.cause.get().test(cause);
        }
    }
}

