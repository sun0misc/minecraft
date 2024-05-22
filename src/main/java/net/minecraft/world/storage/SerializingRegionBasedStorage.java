/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SerializingRegionBasedStorage<R>
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SECTIONS_KEY = "Sections";
    private final ChunkPosKeyedStorage storageAccess;
    private final Long2ObjectMap<Optional<R>> loadedElements = new Long2ObjectOpenHashMap<Optional<R>>();
    private final LongLinkedOpenHashSet unsavedElements = new LongLinkedOpenHashSet();
    private final Function<Runnable, Codec<R>> codecFactory;
    private final Function<Runnable, R> factory;
    private final DynamicRegistryManager registryManager;
    protected final HeightLimitView world;

    public SerializingRegionBasedStorage(ChunkPosKeyedStorage storageAccess, Function<Runnable, Codec<R>> codecFactory, Function<Runnable, R> factory, DynamicRegistryManager registryManager, HeightLimitView world) {
        this.storageAccess = storageAccess;
        this.codecFactory = codecFactory;
        this.factory = factory;
        this.registryManager = registryManager;
        this.world = world;
    }

    protected void tick(BooleanSupplier shouldKeepTicking) {
        while (this.hasUnsavedElements() && shouldKeepTicking.getAsBoolean()) {
            ChunkPos lv = ChunkSectionPos.from(this.unsavedElements.firstLong()).toChunkPos();
            this.save(lv);
        }
    }

    public boolean hasUnsavedElements() {
        return !this.unsavedElements.isEmpty();
    }

    @Nullable
    protected Optional<R> getIfLoaded(long pos) {
        return (Optional)this.loadedElements.get(pos);
    }

    protected Optional<R> get(long pos) {
        if (this.isPosInvalid(pos)) {
            return Optional.empty();
        }
        Optional<R> optional = this.getIfLoaded(pos);
        if (optional != null) {
            return optional;
        }
        this.loadDataAt(ChunkSectionPos.from(pos).toChunkPos());
        optional = this.getIfLoaded(pos);
        if (optional == null) {
            throw Util.throwOrPause(new IllegalStateException());
        }
        return optional;
    }

    protected boolean isPosInvalid(long pos) {
        int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(pos));
        return this.world.isOutOfHeightLimit(i);
    }

    protected R getOrCreate(long pos) {
        if (this.isPosInvalid(pos)) {
            throw Util.throwOrPause(new IllegalArgumentException("sectionPos out of bounds"));
        }
        Optional<R> optional = this.get(pos);
        if (optional.isPresent()) {
            return optional.get();
        }
        R object = this.factory.apply(() -> this.onUpdate(pos));
        this.loadedElements.put(pos, Optional.of(object));
        return object;
    }

    private void loadDataAt(ChunkPos pos) {
        Optional<NbtCompound> optional = this.loadNbt(pos).join();
        RegistryOps<NbtElement> lv = this.registryManager.getOps(NbtOps.INSTANCE);
        this.update(pos, lv, optional.orElse(null));
    }

    private CompletableFuture<Optional<NbtCompound>> loadNbt(ChunkPos pos) {
        return this.storageAccess.read(pos).exceptionally(throwable -> {
            if (throwable instanceof IOException) {
                IOException iOException = (IOException)throwable;
                LOGGER.error("Error reading chunk {} data from disk", (Object)pos, (Object)iOException);
                return Optional.empty();
            }
            throw new CompletionException((Throwable)throwable);
        });
    }

    private void update(ChunkPos pos, RegistryOps<NbtElement> ops, @Nullable NbtCompound nbt) {
        if (nbt == null) {
            for (int i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
                this.loadedElements.put(SerializingRegionBasedStorage.chunkSectionPosAsLong(pos, i), (Optional<R>)Optional.empty());
            }
        } else {
            int k;
            Dynamic<NbtElement> dynamic2 = new Dynamic<NbtElement>(ops, nbt);
            int j = SerializingRegionBasedStorage.getDataVersion(dynamic2);
            boolean bl = j != (k = SharedConstants.getGameVersion().getSaveVersion().getId());
            Dynamic<NbtElement> dynamic22 = this.storageAccess.update(dynamic2, j);
            OptionalDynamic<NbtElement> optionalDynamic = dynamic22.get(SECTIONS_KEY);
            for (int l = this.world.getBottomSectionCoord(); l < this.world.getTopSectionCoord(); ++l) {
                long m = SerializingRegionBasedStorage.chunkSectionPosAsLong(pos, l);
                Optional optional = optionalDynamic.get(Integer.toString(l)).result().flatMap(dynamic -> this.codecFactory.apply(() -> this.onUpdate(m)).parse(dynamic).resultOrPartial(LOGGER::error));
                this.loadedElements.put(m, (Optional<R>)optional);
                optional.ifPresent(sections -> {
                    this.onLoad(m);
                    if (bl) {
                        this.onUpdate(m);
                    }
                });
            }
        }
    }

    private void save(ChunkPos pos) {
        RegistryOps<NbtElement> lv = this.registryManager.getOps(NbtOps.INSTANCE);
        Dynamic<NbtElement> dynamic = this.serialize(pos, lv);
        NbtElement lv2 = dynamic.getValue();
        if (lv2 instanceof NbtCompound) {
            this.storageAccess.set(pos, (NbtCompound)lv2);
        } else {
            LOGGER.error("Expected compound tag, got {}", (Object)lv2);
        }
    }

    private <T> Dynamic<T> serialize(ChunkPos chunkPos, DynamicOps<T> ops) {
        HashMap map = Maps.newHashMap();
        for (int i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
            long l = SerializingRegionBasedStorage.chunkSectionPosAsLong(chunkPos, i);
            this.unsavedElements.remove(l);
            Optional optional = (Optional)this.loadedElements.get(l);
            if (optional == null || optional.isEmpty()) continue;
            DataResult<T> dataResult = this.codecFactory.apply(() -> this.onUpdate(l)).encodeStart(ops, optional.get());
            String string = Integer.toString(i);
            dataResult.resultOrPartial(LOGGER::error).ifPresent(object -> map.put(ops.createString(string), object));
        }
        return new Dynamic<T>(ops, ops.createMap(ImmutableMap.of(ops.createString(SECTIONS_KEY), ops.createMap(map), ops.createString("DataVersion"), ops.createInt(SharedConstants.getGameVersion().getSaveVersion().getId()))));
    }

    private static long chunkSectionPosAsLong(ChunkPos chunkPos, int y) {
        return ChunkSectionPos.asLong(chunkPos.x, y, chunkPos.z);
    }

    protected void onLoad(long pos) {
    }

    protected void onUpdate(long pos) {
        Optional optional = (Optional)this.loadedElements.get(pos);
        if (optional == null || optional.isEmpty()) {
            LOGGER.warn("No data for position: {}", (Object)ChunkSectionPos.from(pos));
            return;
        }
        this.unsavedElements.add(pos);
    }

    private static int getDataVersion(Dynamic<?> dynamic) {
        return dynamic.get("DataVersion").asInt(1945);
    }

    public void saveChunk(ChunkPos pos) {
        if (this.hasUnsavedElements()) {
            for (int i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
                long l = SerializingRegionBasedStorage.chunkSectionPosAsLong(pos, i);
                if (!this.unsavedElements.contains(l)) continue;
                this.save(pos);
                return;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.storageAccess.close();
    }
}

