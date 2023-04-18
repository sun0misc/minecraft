package net.minecraft.client.render.entity.feature;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.HorseMarking;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class HorseMarkingFeatureRenderer extends FeatureRenderer {
   private static final Map TEXTURES = (Map)Util.make(Maps.newEnumMap(HorseMarking.class), (textures) -> {
      textures.put(HorseMarking.NONE, (Object)null);
      textures.put(HorseMarking.WHITE, new Identifier("textures/entity/horse/horse_markings_white.png"));
      textures.put(HorseMarking.WHITE_FIELD, new Identifier("textures/entity/horse/horse_markings_whitefield.png"));
      textures.put(HorseMarking.WHITE_DOTS, new Identifier("textures/entity/horse/horse_markings_whitedots.png"));
      textures.put(HorseMarking.BLACK_DOTS, new Identifier("textures/entity/horse/horse_markings_blackdots.png"));
   });

   public HorseMarkingFeatureRenderer(FeatureRendererContext arg) {
      super(arg);
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, HorseEntity arg3, float f, float g, float h, float j, float k, float l) {
      Identifier lv = (Identifier)TEXTURES.get(arg3.getMarking());
      if (lv != null && !arg3.isInvisible()) {
         VertexConsumer lv2 = arg2.getBuffer(RenderLayer.getEntityTranslucent(lv));
         ((HorseEntityModel)this.getContextModel()).render(arg, lv2, i, LivingEntityRenderer.getOverlay(arg3, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
