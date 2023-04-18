package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class EndGatewayBlockEntityRenderer extends EndPortalBlockEntityRenderer {
   private static final Identifier BEAM_TEXTURE = new Identifier("textures/entity/end_gateway_beam.png");

   public EndGatewayBlockEntityRenderer(BlockEntityRendererFactory.Context arg) {
      super(arg);
   }

   public void render(EndGatewayBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      if (arg.isRecentlyGenerated() || arg.needsCooldownBeforeTeleporting()) {
         float g = arg.isRecentlyGenerated() ? arg.getRecentlyGeneratedBeamHeight(f) : arg.getCooldownBeamHeight(f);
         double d = arg.isRecentlyGenerated() ? (double)arg.getWorld().getTopY() : 50.0;
         g = MathHelper.sin(g * 3.1415927F);
         int k = MathHelper.floor((double)g * d);
         float[] fs = arg.isRecentlyGenerated() ? DyeColor.MAGENTA.getColorComponents() : DyeColor.PURPLE.getColorComponents();
         long l = arg.getWorld().getTime();
         BeaconBlockEntityRenderer.renderBeam(arg2, arg3, BEAM_TEXTURE, f, g, l, -k, k * 2, fs, 0.15F, 0.175F);
      }

      super.render((EndPortalBlockEntity)arg, f, arg2, arg3, i, j);
   }

   protected float getTopYOffset() {
      return 1.0F;
   }

   protected float getBottomYOffset() {
      return 0.0F;
   }

   protected RenderLayer getLayer() {
      return RenderLayer.getEndGateway();
   }

   public int getRenderDistance() {
      return 256;
   }
}
