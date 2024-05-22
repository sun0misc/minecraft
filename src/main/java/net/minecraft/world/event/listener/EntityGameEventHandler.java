/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.event.listener;

import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class EntityGameEventHandler<T extends GameEventListener> {
    private final T listener;
    @Nullable
    private ChunkSectionPos sectionPos;

    public EntityGameEventHandler(T listener) {
        this.listener = listener;
    }

    public void onEntitySetPosCallback(ServerWorld world) {
        this.onEntitySetPos(world);
    }

    public T getListener() {
        return this.listener;
    }

    public void onEntityRemoval(ServerWorld world) {
        EntityGameEventHandler.updateDispatcher(world, this.sectionPos, dispatcher -> dispatcher.removeListener((GameEventListener)this.listener));
    }

    public void onEntitySetPos(ServerWorld world) {
        this.listener.getPositionSource().getPos(world).map(ChunkSectionPos::from).ifPresent(sectionPos -> {
            if (this.sectionPos == null || !this.sectionPos.equals(sectionPos)) {
                EntityGameEventHandler.updateDispatcher(world, this.sectionPos, dispatcher -> dispatcher.removeListener((GameEventListener)this.listener));
                this.sectionPos = sectionPos;
                EntityGameEventHandler.updateDispatcher(world, this.sectionPos, dispatcher -> dispatcher.addListener((GameEventListener)this.listener));
            }
        });
    }

    private static void updateDispatcher(WorldView world, @Nullable ChunkSectionPos sectionPos, Consumer<GameEventDispatcher> dispatcherConsumer) {
        if (sectionPos == null) {
            return;
        }
        Chunk lv = world.getChunk(sectionPos.getSectionX(), sectionPos.getSectionZ(), ChunkStatus.FULL, false);
        if (lv != null) {
            dispatcherConsumer.accept(lv.getGameEventDispatcher(sectionPos.getSectionY()));
        }
    }
}

