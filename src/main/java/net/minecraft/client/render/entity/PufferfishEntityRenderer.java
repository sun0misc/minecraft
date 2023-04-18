package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LargePufferfishEntityModel;
import net.minecraft.client.render.entity.model.MediumPufferfishEntityModel;
import net.minecraft.client.render.entity.model.SmallPufferfishEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PufferfishEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fish/pufferfish.png");
   private int modelSize = 3;
   private final EntityModel smallModel;
   private final EntityModel mediumModel;
   private final EntityModel largeModel = this.getModel();

   public PufferfishEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new LargePufferfishEntityModel(arg.getPart(EntityModelLayers.PUFFERFISH_BIG)), 0.2F);
      this.mediumModel = new MediumPufferfishEntityModel(arg.getPart(EntityModelLayers.PUFFERFISH_MEDIUM));
      this.smallModel = new SmallPufferfishEntityModel(arg.getPart(EntityModelLayers.PUFFERFISH_SMALL));
   }

   public Identifier getTexture(PufferfishEntity arg) {
      return TEXTURE;
   }

   public void render(PufferfishEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      int j = arg.getPuffState();
      if (j != this.modelSize) {
         if (j == 0) {
            this.model = this.smallModel;
         } else if (j == 1) {
            this.model = this.mediumModel;
         } else {
            this.model = this.largeModel;
         }
      }

      this.modelSize = j;
      this.shadowRadius = 0.1F + 0.1F * (float)j;
      super.render((MobEntity)arg, f, g, arg2, arg3, i);
   }

   protected void setupTransforms(PufferfishEntity arg, MatrixStack arg2, float f, float g, float h) {
      arg2.translate(0.0F, MathHelper.cos(f * 0.05F) * 0.08F, 0.0F);
      super.setupTransforms(arg, arg2, f, g, h);
   }
}
