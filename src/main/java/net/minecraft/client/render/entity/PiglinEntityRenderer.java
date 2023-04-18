package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PiglinEntityRenderer extends BipedEntityRenderer {
   private static final Map TEXTURES;
   private static final float HORIZONTAL_SCALE = 1.0019531F;

   public PiglinEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer mainLayer, EntityModelLayer innerArmorLayer, EntityModelLayer outerArmorLayer, boolean zombie) {
      super(ctx, getPiglinModel(ctx.getModelLoader(), mainLayer, zombie), 0.5F, 1.0019531F, 1.0F, 1.0019531F);
      this.addFeature(new ArmorFeatureRenderer(this, new ArmorEntityModel(ctx.getPart(innerArmorLayer)), new ArmorEntityModel(ctx.getPart(outerArmorLayer)), ctx.getModelManager()));
   }

   private static PiglinEntityModel getPiglinModel(EntityModelLoader modelLoader, EntityModelLayer layer, boolean zombie) {
      PiglinEntityModel lv = new PiglinEntityModel(modelLoader.getModelPart(layer));
      if (zombie) {
         lv.rightEar.visible = false;
      }

      return lv;
   }

   public Identifier getTexture(MobEntity arg) {
      Identifier lv = (Identifier)TEXTURES.get(arg.getType());
      if (lv == null) {
         throw new IllegalArgumentException("I don't know what texture to use for " + arg.getType());
      } else {
         return lv;
      }
   }

   protected boolean isShaking(MobEntity arg) {
      return super.isShaking(arg) || arg instanceof AbstractPiglinEntity && ((AbstractPiglinEntity)arg).shouldZombify();
   }

   // $FF: synthetic method
   protected boolean isShaking(LivingEntity entity) {
      return this.isShaking((MobEntity)entity);
   }

   // $FF: synthetic method
   public Identifier getTexture(Entity entity) {
      return this.getTexture((MobEntity)entity);
   }

   static {
      TEXTURES = ImmutableMap.of(EntityType.PIGLIN, new Identifier("textures/entity/piglin/piglin.png"), EntityType.ZOMBIFIED_PIGLIN, new Identifier("textures/entity/piglin/zombified_piglin.png"), EntityType.PIGLIN_BRUTE, new Identifier("textures/entity/piglin/piglin_brute.png"));
   }
}
