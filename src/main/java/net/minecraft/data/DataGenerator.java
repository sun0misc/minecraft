/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.Bootstrap;
import net.minecraft.GameVersion;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import org.slf4j.Logger;

public class DataGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path outputPath;
    private final DataOutput output;
    final Set<String> providerNames = new HashSet<String>();
    final Map<String, DataProvider> runningProviders = new LinkedHashMap<String, DataProvider>();
    private final GameVersion gameVersion;
    private final boolean ignoreCache;

    public DataGenerator(Path outputPath, GameVersion gameVersion, boolean ignoreCache) {
        this.outputPath = outputPath;
        this.output = new DataOutput(this.outputPath);
        this.gameVersion = gameVersion;
        this.ignoreCache = ignoreCache;
    }

    public void run() throws IOException {
        DataCache lv = new DataCache(this.outputPath, this.providerNames, this.gameVersion);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch stopwatch2 = Stopwatch.createUnstarted();
        this.runningProviders.forEach((name, provider) -> {
            if (!this.ignoreCache && !lv.isVersionDifferent((String)name)) {
                LOGGER.debug("Generator {} already run for version {}", name, (Object)this.gameVersion.getName());
                return;
            }
            LOGGER.info("Starting provider: {}", name);
            stopwatch2.start();
            lv.store(lv.run((String)name, provider::run).join());
            stopwatch2.stop();
            LOGGER.info("{} finished after {} ms", name, (Object)stopwatch2.elapsed(TimeUnit.MILLISECONDS));
            stopwatch2.reset();
        });
        LOGGER.info("All providers took: {} ms", (Object)stopwatch.elapsed(TimeUnit.MILLISECONDS));
        lv.write();
    }

    public Pack createVanillaPack(boolean shouldRun) {
        return new Pack(shouldRun, "vanilla", this.output);
    }

    public Pack createVanillaSubPack(boolean shouldRun, String packName) {
        Path path = this.output.resolvePath(DataOutput.OutputType.DATA_PACK).resolve("minecraft").resolve("datapacks").resolve(packName);
        return new Pack(shouldRun, packName, new DataOutput(path));
    }

    static {
        Bootstrap.initialize();
    }

    public class Pack {
        private final boolean shouldRun;
        private final String packName;
        private final DataOutput output;

        Pack(boolean shouldRun, String name, DataOutput output) {
            this.shouldRun = shouldRun;
            this.packName = name;
            this.output = output;
        }

        public <T extends DataProvider> T addProvider(DataProvider.Factory<T> factory) {
            T lv = factory.create(this.output);
            String string = this.packName + "/" + lv.getName();
            if (!DataGenerator.this.providerNames.add(string)) {
                throw new IllegalStateException("Duplicate provider: " + string);
            }
            if (this.shouldRun) {
                DataGenerator.this.runningProviders.put(string, (DataProvider)lv);
            }
            return lv;
        }
    }
}

