package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.AxolotlEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class AxolotlEntityRenderer extends MobEntityRenderer {
   private static final Map TEXTURES = (Map)Util.make(Maps.newHashMap(), (variants) -> {
      AxolotlEntity.Variant[] var1 = AxolotlEntity.Variant.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         AxolotlEntity.Variant lv = var1[var3];
         variants.put(lv, new Identifier(String.format(Locale.ROOT, "textures/entity/axolotl/axolotl_%s.png", lv.getName())));
      }

   });

   public AxolotlEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new AxolotlEntityModel(arg.getPart(EntityModelLayers.AXOLOTL)), 0.5F);
   }

   public Identifier getTexture(AxolotlEntity arg) {
      return (Identifier)TEXTURES.get(arg.getVariant());
   }
}
