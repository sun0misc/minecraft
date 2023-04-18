package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.DonkeyEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DonkeyEntityRenderer extends AbstractHorseEntityRenderer {
   private static final Map TEXTURES;

   public DonkeyEntityRenderer(EntityRendererFactory.Context ctx, float scale, EntityModelLayer layer) {
      super(ctx, new DonkeyEntityModel(ctx.getPart(layer)), scale);
   }

   public Identifier getTexture(AbstractDonkeyEntity arg) {
      return (Identifier)TEXTURES.get(arg.getType());
   }

   static {
      TEXTURES = Maps.newHashMap(ImmutableMap.of(EntityType.DONKEY, new Identifier("textures/entity/horse/donkey.png"), EntityType.MULE, new Identifier("textures/entity/horse/mule.png")));
   }
}
