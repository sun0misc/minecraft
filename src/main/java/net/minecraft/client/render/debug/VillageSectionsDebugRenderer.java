package net.minecraft.client.render.debug;

import com.google.common.collect.Sets;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

@Environment(EnvType.CLIENT)
public class VillageSectionsDebugRenderer implements DebugRenderer.Renderer {
   private static final int RANGE = 60;
   private final Set sections = Sets.newHashSet();

   VillageSectionsDebugRenderer() {
   }

   public void clear() {
      this.sections.clear();
   }

   public void addSection(ChunkSectionPos pos) {
      this.sections.add(pos);
   }

   public void removeSection(ChunkSectionPos pos) {
      this.sections.remove(pos);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      BlockPos lv = BlockPos.ofFloored(cameraX, cameraY, cameraZ);
      this.sections.forEach((section) -> {
         if (lv.isWithinDistance(section.getCenterPos(), 60.0)) {
            drawBoxAtCenterOf(matrices, vertexConsumers, section);
         }

      });
   }

   private static void drawBoxAtCenterOf(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ChunkSectionPos sectionPos) {
      int i = true;
      BlockPos lv = sectionPos.getCenterPos();
      BlockPos lv2 = lv.add(-1, -1, -1);
      BlockPos lv3 = lv.add(1, 1, 1);
      DebugRenderer.drawBox(matrices, vertexConsumers, lv2, lv3, 0.2F, 1.0F, 0.2F, 0.15F);
   }
}
