/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class class_9810 {
    private final BlockRenderManager field_52164;
    private final BlockEntityRenderDispatcher field_52165;

    public class_9810(BlockRenderManager arg, BlockEntityRenderDispatcher arg2) {
        this.field_52164 = arg;
        this.field_52165 = arg2;
    }

    public class_9811 method_60904(ChunkSectionPos arg, ChunkRendererRegion arg2, VertexSorter arg3, BlockBufferBuilderStorage arg4) {
        class_9811 lv = new class_9811();
        BlockPos lv2 = arg.getMinPos();
        BlockPos lv3 = lv2.add(15, 15, 15);
        ChunkOcclusionDataBuilder lv4 = new ChunkOcclusionDataBuilder();
        MatrixStack lv5 = new MatrixStack();
        BlockModelRenderer.enableBrightnessCache();
        Reference2ObjectArrayMap<RenderLayer, BufferBuilder> map = new Reference2ObjectArrayMap<RenderLayer, BufferBuilder>(RenderLayer.getBlockLayers().size());
        Random lv6 = Random.create();
        for (BlockPos blockPos : BlockPos.iterate(lv2, lv3)) {
            BufferBuilder lv12;
            RenderLayer lv11;
            FluidState lv10;
            BlockEntity lv9;
            BlockState lv8 = arg2.getBlockState(blockPos);
            if (lv8.isOpaqueFullCube(arg2, blockPos)) {
                lv4.markClosed(blockPos);
            }
            if (lv8.hasBlockEntity() && (lv9 = arg2.getBlockEntity(blockPos)) != null) {
                this.method_60902(lv, lv9);
            }
            if (!(lv10 = lv8.getFluidState()).isEmpty()) {
                lv11 = RenderLayers.getFluidLayer(lv10);
                lv12 = this.method_60903(map, arg4, lv11);
                this.field_52164.renderFluid(blockPos, arg2, lv12, lv8, lv10);
            }
            if (lv8.getRenderType() != BlockRenderType.MODEL) continue;
            lv11 = RenderLayers.getBlockLayer(lv8);
            lv12 = this.method_60903(map, arg4, lv11);
            lv5.push();
            lv5.translate(ChunkSectionPos.getLocalCoord(blockPos.getX()), ChunkSectionPos.getLocalCoord(blockPos.getY()), ChunkSectionPos.getLocalCoord(blockPos.getZ()));
            this.field_52164.renderBlock(lv8, blockPos, arg2, lv5, lv12, true, lv6);
            lv5.pop();
        }
        for (Map.Entry entry : map.entrySet()) {
            RenderLayer lv13 = (RenderLayer)entry.getKey();
            class_9801 lv14 = ((BufferBuilder)entry.getValue()).method_60794();
            if (lv14 == null) continue;
            if (lv13 == RenderLayer.getTranslucent()) {
                lv.field_52170 = lv14.method_60819(arg4.get(RenderLayer.getTranslucent()), arg3);
            }
            lv.field_52168.put(lv13, lv14);
        }
        BlockModelRenderer.disableBrightnessCache();
        lv.field_52169 = lv4.build();
        return lv;
    }

    private BufferBuilder method_60903(Map<RenderLayer, BufferBuilder> map, BlockBufferBuilderStorage arg, RenderLayer arg2) {
        BufferBuilder lv = map.get(arg2);
        if (lv == null) {
            class_9799 lv2 = arg.get(arg2);
            lv = new BufferBuilder(lv2, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            map.put(arg2, lv);
        }
        return lv;
    }

    private <E extends BlockEntity> void method_60902(class_9811 arg, E arg2) {
        BlockEntityRenderer<E> lv = this.field_52165.get(arg2);
        if (lv != null) {
            arg.field_52167.add(arg2);
            if (lv.rendersOutsideBoundingBox(arg2)) {
                arg.field_52166.add(arg2);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class class_9811 {
        public final List<BlockEntity> field_52166 = new ArrayList<BlockEntity>();
        public final List<BlockEntity> field_52167 = new ArrayList<BlockEntity>();
        public final Map<RenderLayer, class_9801> field_52168 = new Reference2ObjectArrayMap<RenderLayer, class_9801>();
        public ChunkOcclusionData field_52169 = new ChunkOcclusionData();
        @Nullable
        public class_9801.class_9802 field_52170;

        public void method_60905() {
            this.field_52168.values().forEach(class_9801::close);
        }
    }
}

