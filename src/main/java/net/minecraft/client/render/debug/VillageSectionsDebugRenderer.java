/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.debug;

import com.google.common.collect.Sets;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

@Environment(value=EnvType.CLIENT)
public class VillageSectionsDebugRenderer
implements DebugRenderer.Renderer {
    private static final int RANGE = 60;
    private final Set<ChunkSectionPos> sections = Sets.newHashSet();

    VillageSectionsDebugRenderer() {
    }

    @Override
    public void clear() {
        this.sections.clear();
    }

    public void addSection(ChunkSectionPos pos) {
        this.sections.add(pos);
    }

    public void removeSection(ChunkSectionPos pos) {
        this.sections.remove(pos);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        BlockPos lv = BlockPos.ofFloored(cameraX, cameraY, cameraZ);
        this.sections.forEach(section -> {
            if (lv.isWithinDistance(section.getCenterPos(), 60.0)) {
                VillageSectionsDebugRenderer.drawBoxAtCenterOf(matrices, vertexConsumers, section);
            }
        });
    }

    private static void drawBoxAtCenterOf(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ChunkSectionPos sectionPos) {
        DebugRenderer.drawBlockBox(matrices, vertexConsumers, sectionPos.getCenterPos(), 0.2f, 1.0f, 0.2f, 0.15f);
    }
}

