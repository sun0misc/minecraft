package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.model.SheepWoolEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SheepWoolFeatureRenderer extends FeatureRenderer {
   private static final Identifier SKIN = new Identifier("textures/entity/sheep/sheep_fur.png");
   private final SheepWoolEntityModel model;

   public SheepWoolFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_FUR));
   }

   public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, SheepEntity arg3, float f, float g, float h, float j, float k, float l) {
      if (!arg3.isSheared()) {
         if (arg3.isInvisible()) {
            MinecraftClient lv = MinecraftClient.getInstance();
            boolean bl = lv.hasOutline(arg3);
            if (bl) {
               ((SheepEntityModel)this.getContextModel()).copyStateTo(this.model);
               this.model.animateModel(arg3, f, g, h);
               this.model.setAngles(arg3, f, g, j, k, l);
               VertexConsumer lv2 = arg2.getBuffer(RenderLayer.getOutline(SKIN));
               this.model.render(arg, lv2, i, LivingEntityRenderer.getOverlay(arg3, 0.0F), 0.0F, 0.0F, 0.0F, 1.0F);
            }

         } else {
            float s;
            float t;
            float u;
            if (arg3.hasCustomName() && "jeb_".equals(arg3.getName().getString())) {
               int m = true;
               int n = arg3.age / 25 + arg3.getId();
               int o = DyeColor.values().length;
               int p = n % o;
               int q = (n + 1) % o;
               float r = ((float)(arg3.age % 25) + h) / 25.0F;
               float[] fs = SheepEntity.getRgbColor(DyeColor.byId(p));
               float[] gs = SheepEntity.getRgbColor(DyeColor.byId(q));
               s = fs[0] * (1.0F - r) + gs[0] * r;
               t = fs[1] * (1.0F - r) + gs[1] * r;
               u = fs[2] * (1.0F - r) + gs[2] * r;
            } else {
               float[] hs = SheepEntity.getRgbColor(arg3.getColor());
               s = hs[0];
               t = hs[1];
               u = hs[2];
            }

            render(this.getContextModel(), this.model, SKIN, arg, arg2, i, arg3, f, g, j, k, l, h, s, t, u);
         }
      }
   }
}
