package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.ChestBoatEntityModel;
import net.minecraft.client.render.entity.model.ChestRaftEntityModel;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ModelWithWaterPatch;
import net.minecraft.client.render.entity.model.RaftEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class BoatEntityRenderer extends EntityRenderer {
   private final Map texturesAndModels;

   public BoatEntityRenderer(EntityRendererFactory.Context ctx, boolean chest) {
      super(ctx);
      this.shadowRadius = 0.8F;
      this.texturesAndModels = (Map)Stream.of(BoatEntity.Type.values()).collect(ImmutableMap.toImmutableMap((type) -> {
         return type;
      }, (type) -> {
         return Pair.of(new Identifier(getTexture(type, chest)), this.createModel(ctx, type, chest));
      }));
   }

   private CompositeEntityModel createModel(EntityRendererFactory.Context ctx, BoatEntity.Type type, boolean chest) {
      EntityModelLayer lv = chest ? EntityModelLayers.createChestBoat(type) : EntityModelLayers.createBoat(type);
      ModelPart lv2 = ctx.getPart(lv);
      if (type == BoatEntity.Type.BAMBOO) {
         return (CompositeEntityModel)(chest ? new ChestRaftEntityModel(lv2) : new RaftEntityModel(lv2));
      } else {
         return (CompositeEntityModel)(chest ? new ChestBoatEntityModel(lv2) : new BoatEntityModel(lv2));
      }
   }

   private static String getTexture(BoatEntity.Type type, boolean chest) {
      return chest ? "textures/entity/chest_boat/" + type.getName() + ".png" : "textures/entity/boat/" + type.getName() + ".png";
   }

   public void render(BoatEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      arg2.push();
      arg2.translate(0.0F, 0.375F, 0.0F);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - f));
      float h = (float)arg.getDamageWobbleTicks() - g;
      float j = arg.getDamageWobbleStrength() - g;
      if (j < 0.0F) {
         j = 0.0F;
      }

      if (h > 0.0F) {
         arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(h) * h * j / 10.0F * (float)arg.getDamageWobbleSide()));
      }

      float k = arg.interpolateBubbleWobble(g);
      if (!MathHelper.approximatelyEquals(k, 0.0F)) {
         arg2.multiply((new Quaternionf()).setAngleAxis(arg.interpolateBubbleWobble(g) * 0.017453292F, 1.0F, 0.0F, 1.0F));
      }

      Pair pair = (Pair)this.texturesAndModels.get(arg.getVariant());
      Identifier lv = (Identifier)pair.getFirst();
      CompositeEntityModel lv2 = (CompositeEntityModel)pair.getSecond();
      arg2.scale(-1.0F, -1.0F, 1.0F);
      arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
      lv2.setAngles(arg, g, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer lv3 = arg3.getBuffer(lv2.getLayer(lv));
      lv2.render(arg2, lv3, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      if (!arg.isSubmergedInWater()) {
         VertexConsumer lv4 = arg3.getBuffer(RenderLayer.getWaterMask());
         if (lv2 instanceof ModelWithWaterPatch) {
            ModelWithWaterPatch lv5 = (ModelWithWaterPatch)lv2;
            lv5.getWaterPatch().render(arg2, lv4, i, OverlayTexture.DEFAULT_UV);
         }
      }

      arg2.pop();
      super.render(arg, f, g, arg2, arg3, i);
   }

   public Identifier getTexture(BoatEntity arg) {
      return (Identifier)((Pair)this.texturesAndModels.get(arg.getVariant())).getFirst();
   }
}
