package net.minecraft.client.render.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public abstract class DisplayEntityRenderer extends EntityRenderer {
   private final EntityRenderDispatcher renderDispatcher;

   protected DisplayEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
      this.renderDispatcher = arg.getRenderDispatcher();
   }

   public Identifier getTexture(DisplayEntity arg) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }

   public void render(DisplayEntity arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
      DisplayEntity.RenderState lv = arg.getRenderState();
      if (lv != null) {
         Object object = this.getData(arg);
         if (object != null) {
            float h = arg.getLerpProgress(g);
            this.shadowRadius = lv.shadowRadius().lerp(h);
            this.shadowOpacity = lv.shadowStrength().lerp(h);
            int j = lv.brightnessOverride();
            int k = j != -1 ? j : i;
            super.render(arg, f, g, arg2, arg3, k);
            arg2.push();
            arg2.multiply(this.getBillboardRotation(lv, arg));
            AffineTransformation lv2 = (AffineTransformation)lv.transformation().interpolate(h);
            arg2.multiplyPositionMatrix(lv2.getMatrix());
            arg2.peek().getNormalMatrix().rotate(lv2.getLeftRotation()).rotate(lv2.getRightRotation());
            this.render(arg, object, arg2, arg3, k, h);
            arg2.pop();
         }
      }
   }

   private Quaternionf getBillboardRotation(DisplayEntity.RenderState renderState, DisplayEntity entity) {
      Camera lv = this.renderDispatcher.camera;
      Quaternionf var10000;
      switch (renderState.billboardConstraints()) {
         case FIXED:
            var10000 = entity.getFixedRotation();
            break;
         case HORIZONTAL:
            var10000 = (new Quaternionf()).rotationYXZ(-0.017453292F * entity.getYaw(), -0.017453292F * lv.getPitch(), 0.0F);
            break;
         case VERTICAL:
            var10000 = (new Quaternionf()).rotationYXZ(3.1415927F - 0.017453292F * lv.getYaw(), 0.017453292F * entity.getPitch(), 0.0F);
            break;
         case CENTER:
            var10000 = (new Quaternionf()).rotationYXZ(3.1415927F - 0.017453292F * lv.getYaw(), -0.017453292F * lv.getPitch(), 0.0F);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   @Nullable
   protected abstract Object getData(DisplayEntity entity);

   protected abstract void render(DisplayEntity entity, Object data, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int brightness, float lerpProgress);

   @Environment(EnvType.CLIENT)
   public static class TextDisplayEntityRenderer extends DisplayEntityRenderer {
      private final TextRenderer displayTextRenderer;

      protected TextDisplayEntityRenderer(EntityRendererFactory.Context arg) {
         super(arg);
         this.displayTextRenderer = arg.getTextRenderer();
      }

      private DisplayEntity.TextDisplayEntity.TextLines getLines(Text text, int width) {
         List list = this.displayTextRenderer.wrapLines(text, width);
         List list2 = new ArrayList(list.size());
         int j = 0;
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            OrderedText lv = (OrderedText)var6.next();
            int k = this.displayTextRenderer.getWidth(lv);
            j = Math.max(j, k);
            list2.add(new DisplayEntity.TextDisplayEntity.TextLine(lv, k));
         }

         return new DisplayEntity.TextDisplayEntity.TextLines(list2, j);
      }

      @Nullable
      protected DisplayEntity.TextDisplayEntity.Data getData(DisplayEntity.TextDisplayEntity arg) {
         return arg.getData();
      }

      public void render(DisplayEntity.TextDisplayEntity arg, DisplayEntity.TextDisplayEntity.Data arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
         byte b = arg2.flags();
         boolean bl = (b & 2) != 0;
         boolean bl2 = (b & 4) != 0;
         boolean bl3 = (b & 1) != 0;
         DisplayEntity.TextDisplayEntity.TextAlignment lv = DisplayEntity.TextDisplayEntity.getAlignment(b);
         byte c = (byte)arg2.textOpacity().lerp(f);
         int j;
         float g;
         if (bl2) {
            g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
            j = (int)(g * 255.0F) << 24;
         } else {
            j = arg2.backgroundColor().lerp(f);
         }

         g = 0.0F;
         Matrix4f matrix4f = arg3.peek().getPositionMatrix();
         matrix4f.rotate(3.1415927F, 0.0F, 1.0F, 0.0F);
         matrix4f.scale(-0.025F, -0.025F, -0.025F);
         DisplayEntity.TextDisplayEntity.TextLines lv2 = arg.splitLines(this::getLines);
         Objects.requireNonNull(this.displayTextRenderer);
         int k = 9 + 1;
         int l = lv2.width();
         int m = lv2.lines().size() * k;
         matrix4f.translate(1.0F - (float)l / 2.0F, (float)(-m), 0.0F);
         if (j != 0) {
            VertexConsumer lv3 = arg4.getBuffer(bl ? RenderLayer.getTextBackgroundSeeThrough() : RenderLayer.getTextBackground());
            lv3.vertex(matrix4f, -1.0F, -1.0F, 0.0F).color(j).light(i).next();
            lv3.vertex(matrix4f, -1.0F, (float)m, 0.0F).color(j).light(i).next();
            lv3.vertex(matrix4f, (float)l, (float)m, 0.0F).color(j).light(i).next();
            lv3.vertex(matrix4f, (float)l, -1.0F, 0.0F).color(j).light(i).next();
         }

         for(Iterator var23 = lv2.lines().iterator(); var23.hasNext(); g += (float)k) {
            DisplayEntity.TextDisplayEntity.TextLine lv4 = (DisplayEntity.TextDisplayEntity.TextLine)var23.next();
            float var10000;
            switch (lv) {
               case LEFT:
                  var10000 = 0.0F;
                  break;
               case RIGHT:
                  var10000 = (float)(l - lv4.width());
                  break;
               case CENTER:
                  var10000 = (float)l / 2.0F - (float)lv4.width() / 2.0F;
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            float h = var10000;
            this.displayTextRenderer.draw((OrderedText)lv4.contents(), h, g, c << 24 | 16777215, bl3, matrix4f, arg4, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.POLYGON_OFFSET, 0, i);
         }

      }

      // $FF: synthetic method
      @Nullable
      protected Object getData(DisplayEntity entity) {
         return this.getData((DisplayEntity.TextDisplayEntity)entity);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ItemDisplayEntityRenderer extends DisplayEntityRenderer {
      private final ItemRenderer itemRenderer;

      protected ItemDisplayEntityRenderer(EntityRendererFactory.Context arg) {
         super(arg);
         this.itemRenderer = arg.getItemRenderer();
      }

      @Nullable
      protected DisplayEntity.ItemDisplayEntity.Data getData(DisplayEntity.ItemDisplayEntity arg) {
         return arg.getData();
      }

      public void render(DisplayEntity.ItemDisplayEntity arg, DisplayEntity.ItemDisplayEntity.Data arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
         this.itemRenderer.renderItem(arg2.itemStack(), arg2.itemTransform(), i, OverlayTexture.DEFAULT_UV, arg3, arg4, arg.getWorld(), arg.getId());
      }

      // $FF: synthetic method
      @Nullable
      protected Object getData(DisplayEntity entity) {
         return this.getData((DisplayEntity.ItemDisplayEntity)entity);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class BlockDisplayEntityRenderer extends DisplayEntityRenderer {
      private final BlockRenderManager blockRenderManager;

      protected BlockDisplayEntityRenderer(EntityRendererFactory.Context arg) {
         super(arg);
         this.blockRenderManager = arg.getBlockRenderManager();
      }

      @Nullable
      protected DisplayEntity.BlockDisplayEntity.Data getData(DisplayEntity.BlockDisplayEntity arg) {
         return arg.getData();
      }

      public void render(DisplayEntity.BlockDisplayEntity arg, DisplayEntity.BlockDisplayEntity.Data arg2, MatrixStack arg3, VertexConsumerProvider arg4, int i, float f) {
         this.blockRenderManager.renderBlockAsEntity(arg2.blockState(), arg3, arg4, i, OverlayTexture.DEFAULT_UV);
      }

      // $FF: synthetic method
      @Nullable
      protected Object getData(DisplayEntity entity) {
         return this.getData((DisplayEntity.BlockDisplayEntity)entity);
      }
   }
}
