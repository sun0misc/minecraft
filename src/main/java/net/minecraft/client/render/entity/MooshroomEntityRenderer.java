package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.MooshroomMushroomFeatureRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class MooshroomEntityRenderer extends MobEntityRenderer {
   private static final Map TEXTURES = (Map)Util.make(Maps.newHashMap(), (map) -> {
      map.put(MooshroomEntity.Type.BROWN, new Identifier("textures/entity/cow/brown_mooshroom.png"));
      map.put(MooshroomEntity.Type.RED, new Identifier("textures/entity/cow/red_mooshroom.png"));
   });

   public MooshroomEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new CowEntityModel(arg.getPart(EntityModelLayers.MOOSHROOM)), 0.7F);
      this.addFeature(new MooshroomMushroomFeatureRenderer(this, arg.getBlockRenderManager()));
   }

   public Identifier getTexture(MooshroomEntity arg) {
      return (Identifier)TEXTURES.get(arg.getVariant());
   }
}
