/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.event.listener;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public record Vibration(RegistryEntry<GameEvent> gameEvent, float distance, Vec3d pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity) {
    public static final Codec<Vibration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)GameEvent.CODEC.fieldOf("game_event")).forGetter(Vibration::gameEvent), ((MapCodec)Codec.floatRange(0.0f, Float.MAX_VALUE).fieldOf("distance")).forGetter(Vibration::distance), ((MapCodec)Vec3d.CODEC.fieldOf("pos")).forGetter(Vibration::pos), Uuids.INT_STREAM_CODEC.lenientOptionalFieldOf("source").forGetter(vibration -> Optional.ofNullable(vibration.uuid())), Uuids.INT_STREAM_CODEC.lenientOptionalFieldOf("projectile_owner").forGetter(vibration -> Optional.ofNullable(vibration.projectileOwnerUuid()))).apply((Applicative<Vibration, ?>)instance, (event, distance, pos, uuid, projectileOwnerUuid) -> new Vibration((RegistryEntry<GameEvent>)event, distance.floatValue(), (Vec3d)pos, uuid.orElse(null), projectileOwnerUuid.orElse(null))));

    public Vibration(RegistryEntry<GameEvent> gameEvent, float distance, Vec3d pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid) {
        this(gameEvent, distance, pos, uuid, projectileOwnerUuid, null);
    }

    public Vibration(RegistryEntry<GameEvent> gameEvent, float distance, Vec3d pos, @Nullable Entity entity) {
        this(gameEvent, distance, pos, entity == null ? null : entity.getUuid(), Vibration.getOwnerUuid(entity), entity);
    }

    @Nullable
    private static UUID getOwnerUuid(@Nullable Entity entity) {
        ProjectileEntity lv;
        if (entity instanceof ProjectileEntity && (lv = (ProjectileEntity)entity).getOwner() != null) {
            return lv.getOwner().getUuid();
        }
        return null;
    }

    public Optional<Entity> getEntity(ServerWorld world) {
        return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(world::getEntity));
    }

    public Optional<Entity> getOwner(ServerWorld world) {
        return this.getEntity(world).filter(entity -> entity instanceof ProjectileEntity).map(entity -> (ProjectileEntity)entity).map(ProjectileEntity::getOwner).or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(world::getEntity));
    }

    @Nullable
    public UUID uuid() {
        return this.uuid;
    }

    @Nullable
    public UUID projectileOwnerUuid() {
        return this.projectileOwnerUuid;
    }

    @Nullable
    public Entity entity() {
        return this.entity;
    }
}

