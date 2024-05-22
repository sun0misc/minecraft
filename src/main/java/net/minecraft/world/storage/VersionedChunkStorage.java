/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.FeatureUpdater;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.storage.NbtScannable;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.StorageKey;
import org.jetbrains.annotations.Nullable;

public class VersionedChunkStorage
implements AutoCloseable {
    public static final int FEATURE_UPDATING_VERSION = 1493;
    private final StorageIoWorker worker;
    protected final DataFixer dataFixer;
    @Nullable
    private volatile FeatureUpdater featureUpdater;

    public VersionedChunkStorage(StorageKey storageKey, Path directory, DataFixer dataFixer, boolean dsync) {
        this.dataFixer = dataFixer;
        this.worker = new StorageIoWorker(storageKey, directory, dsync);
    }

    public boolean needsBlending(ChunkPos chunkPos, int checkRadius) {
        return this.worker.needsBlending(chunkPos, checkRadius);
    }

    public NbtCompound updateChunkNbt(RegistryKey<World> worldKey, Supplier<PersistentStateManager> persistentStateManagerFactory, NbtCompound nbt, Optional<RegistryKey<MapCodec<? extends ChunkGenerator>>> generatorCodecKey) {
        int i = VersionedChunkStorage.getDataVersion(nbt);
        if (i == SharedConstants.getGameVersion().getSaveVersion().getId()) {
            return nbt;
        }
        try {
            if (i < 1493 && (nbt = DataFixTypes.CHUNK.update(this.dataFixer, nbt, i, 1493)).getCompound("Level").getBoolean("hasLegacyStructureData")) {
                FeatureUpdater lv = this.getFeatureUpdater(worldKey, persistentStateManagerFactory);
                nbt = lv.getUpdatedReferences(nbt);
            }
            VersionedChunkStorage.saveContextToNbt(nbt, worldKey, generatorCodecKey);
            nbt = DataFixTypes.CHUNK.update(this.dataFixer, nbt, Math.max(1493, i));
            VersionedChunkStorage.removeContext(nbt);
            NbtHelper.putDataVersion(nbt);
            return nbt;
        } catch (Exception exception) {
            CrashReport lv2 = CrashReport.create(exception, "Updated chunk");
            CrashReportSection lv3 = lv2.addElement("Updated chunk details");
            lv3.add("Data version", i);
            throw new CrashException(lv2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FeatureUpdater getFeatureUpdater(RegistryKey<World> worldKey, Supplier<PersistentStateManager> stateManagerGetter) {
        FeatureUpdater lv = this.featureUpdater;
        if (lv == null) {
            VersionedChunkStorage versionedChunkStorage = this;
            synchronized (versionedChunkStorage) {
                lv = this.featureUpdater;
                if (lv == null) {
                    this.featureUpdater = lv = FeatureUpdater.create(worldKey, stateManagerGetter.get());
                }
            }
        }
        return lv;
    }

    public static void saveContextToNbt(NbtCompound nbt, RegistryKey<World> worldKey, Optional<RegistryKey<MapCodec<? extends ChunkGenerator>>> generatorCodecKey) {
        NbtCompound lv = new NbtCompound();
        lv.putString("dimension", worldKey.getValue().toString());
        generatorCodecKey.ifPresent(key -> lv.putString("generator", key.getValue().toString()));
        nbt.put("__context", lv);
    }

    private static void removeContext(NbtCompound nbt) {
        nbt.remove("__context");
    }

    public static int getDataVersion(NbtCompound nbt) {
        return NbtHelper.getDataVersion(nbt, -1);
    }

    public CompletableFuture<Optional<NbtCompound>> getNbt(ChunkPos chunkPos) {
        return this.worker.readChunkData(chunkPos);
    }

    public CompletableFuture<Void> setNbt(ChunkPos chunkPos, NbtCompound nbt) {
        this.markFeatureUpdateResolved(chunkPos);
        return this.worker.setResult(chunkPos, nbt);
    }

    protected void markFeatureUpdateResolved(ChunkPos chunkPos) {
        if (this.featureUpdater != null) {
            this.featureUpdater.markResolved(chunkPos.toLong());
        }
    }

    public void completeAll() {
        this.worker.completeAll(true).join();
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }

    public NbtScannable getWorker() {
        return this.worker;
    }
}

