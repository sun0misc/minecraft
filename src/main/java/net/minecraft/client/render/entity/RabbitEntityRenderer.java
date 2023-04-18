package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.RabbitEntityModel;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RabbitEntityRenderer extends MobEntityRenderer {
   private static final Identifier BROWN_TEXTURE = new Identifier("textures/entity/rabbit/brown.png");
   private static final Identifier WHITE_TEXTURE = new Identifier("textures/entity/rabbit/white.png");
   private static final Identifier BLACK_TEXTURE = new Identifier("textures/entity/rabbit/black.png");
   private static final Identifier GOLD_TEXTURE = new Identifier("textures/entity/rabbit/gold.png");
   private static final Identifier SALT_TEXTURE = new Identifier("textures/entity/rabbit/salt.png");
   private static final Identifier WHITE_SPLOTCHED_TEXTURE = new Identifier("textures/entity/rabbit/white_splotched.png");
   private static final Identifier TOAST_TEXTURE = new Identifier("textures/entity/rabbit/toast.png");
   private static final Identifier CAERBANNOG_TEXTURE = new Identifier("textures/entity/rabbit/caerbannog.png");

   public RabbitEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new RabbitEntityModel(arg.getPart(EntityModelLayers.RABBIT)), 0.3F);
   }

   public Identifier getTexture(RabbitEntity arg) {
      String string = Formatting.strip(arg.getName().getString());
      if ("Toast".equals(string)) {
         return TOAST_TEXTURE;
      } else {
         Identifier var10000;
         switch (arg.getVariant()) {
            case BROWN:
               var10000 = BROWN_TEXTURE;
               break;
            case WHITE:
               var10000 = WHITE_TEXTURE;
               break;
            case BLACK:
               var10000 = BLACK_TEXTURE;
               break;
            case GOLD:
               var10000 = GOLD_TEXTURE;
               break;
            case SALT:
               var10000 = SALT_TEXTURE;
               break;
            case WHITE_SPLOTCHED:
               var10000 = WHITE_SPLOTCHED_TEXTURE;
               break;
            case EVIL:
               var10000 = CAERBANNOG_TEXTURE;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }
}
