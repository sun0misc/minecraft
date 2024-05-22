/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.session.telemetry;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.TelemetryLogger;
import net.minecraft.client.session.telemetry.ThreadedLogWriter;
import net.minecraft.util.Util;
import net.minecraft.util.logging.LogFileCompressor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TelemetryLogManager
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_EXTENSION = ".json";
    private static final int RETENTION_DAYS = 7;
    private final LogFileCompressor compressor;
    @Nullable
    private CompletableFuture<Optional<ThreadedLogWriter>> writer;

    private TelemetryLogManager(LogFileCompressor compressor) {
        this.compressor = compressor;
    }

    public static CompletableFuture<Optional<TelemetryLogManager>> create(Path directory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LogFileCompressor lv = LogFileCompressor.create(directory, FILE_EXTENSION);
                lv.getAll().removeExpired(LocalDate.now(), 7).compressAll();
                return Optional.of(new TelemetryLogManager(lv));
            } catch (Exception exception) {
                LOGGER.error("Failed to create telemetry log manager", exception);
                return Optional.empty();
            }
        }, Util.getMainWorkerExecutor());
    }

    public CompletableFuture<Optional<TelemetryLogger>> getLogger() {
        if (this.writer == null) {
            this.writer = CompletableFuture.supplyAsync(() -> {
                try {
                    LogFileCompressor.Uncompressed lv = this.compressor.createLogFile(LocalDate.now());
                    FileChannel fileChannel = lv.open();
                    return Optional.of(new ThreadedLogWriter(fileChannel, Util.getMainWorkerExecutor()));
                } catch (IOException iOException) {
                    LOGGER.error("Failed to open channel for telemetry event log", iOException);
                    return Optional.empty();
                }
            }, Util.getMainWorkerExecutor());
        }
        return this.writer.thenApply(writer -> writer.map(ThreadedLogWriter::getLogger));
    }

    @Override
    public void close() {
        if (this.writer != null) {
            this.writer.thenAccept(writer -> writer.ifPresent(ThreadedLogWriter::close));
        }
    }
}

