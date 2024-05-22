/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlTimer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.Sampler;
import net.minecraft.util.profiler.SamplerFactory;
import net.minecraft.util.profiler.SamplerSource;
import net.minecraft.util.profiler.ServerSamplerSource;

@Environment(value=EnvType.CLIENT)
public class ClientSamplerSource
implements SamplerSource {
    private final WorldRenderer renderer;
    private final Set<Sampler> samplers = new ObjectOpenHashSet<Sampler>();
    private final SamplerFactory factory = new SamplerFactory();

    public ClientSamplerSource(LongSupplier nanoTimeSupplier, WorldRenderer renderer) {
        this.renderer = renderer;
        this.samplers.add(ServerSamplerSource.createTickTimeTracker(nanoTimeSupplier));
        this.addInfoSamplers();
    }

    private void addInfoSamplers() {
        this.samplers.addAll(ServerSamplerSource.createSystemSamplers());
        this.samplers.add(Sampler.create("totalChunks", SampleType.CHUNK_RENDERING, this.renderer, WorldRenderer::getChunkCount));
        this.samplers.add(Sampler.create("renderedChunks", SampleType.CHUNK_RENDERING, this.renderer, WorldRenderer::getCompletedChunkCount));
        this.samplers.add(Sampler.create("lastViewDistance", SampleType.CHUNK_RENDERING, this.renderer, WorldRenderer::getViewDistance));
        ChunkBuilder lv = this.renderer.getChunkBuilder();
        this.samplers.add(Sampler.create("toUpload", SampleType.CHUNK_RENDERING_DISPATCHING, lv, ChunkBuilder::getChunksToUpload));
        this.samplers.add(Sampler.create("freeBufferCount", SampleType.CHUNK_RENDERING_DISPATCHING, lv, ChunkBuilder::getFreeBufferCount));
        this.samplers.add(Sampler.create("toBatchCount", SampleType.CHUNK_RENDERING_DISPATCHING, lv, ChunkBuilder::getToBatchCount));
        if (GlTimer.getInstance().isPresent()) {
            this.samplers.add(Sampler.create("gpuUtilization", SampleType.GPU, MinecraftClient.getInstance(), MinecraftClient::getGpuUtilizationPercentage));
        }
    }

    @Override
    public Set<Sampler> getSamplers(Supplier<ReadableProfiler> profilerSupplier) {
        this.samplers.addAll(this.factory.createSamplers(profilerSupplier));
        return this.samplers;
    }
}

