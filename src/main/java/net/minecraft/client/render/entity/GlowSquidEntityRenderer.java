package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class GlowSquidEntityRenderer extends SquidEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/squid/glow_squid.png");

   public GlowSquidEntityRenderer(EntityRendererFactory.Context arg, SquidEntityModel arg2) {
      super(arg, arg2);
   }

   public Identifier getTexture(GlowSquidEntity arg) {
      return TEXTURE;
   }

   protected int getBlockLight(GlowSquidEntity arg, BlockPos arg2) {
      int i = (int)MathHelper.clampedLerp(0.0F, 15.0F, 1.0F - (float)arg.getDarkTicksRemaining() / 10.0F);
      return i == 15 ? 15 : Math.max(i, super.getBlockLight(arg, arg2));
   }
}
