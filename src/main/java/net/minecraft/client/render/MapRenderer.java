package net.minecraft.client.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class MapRenderer implements AutoCloseable {
   private static final Identifier MAP_ICONS_TEXTURE = new Identifier("textures/map/map_icons.png");
   static final RenderLayer MAP_ICONS_RENDER_LAYER;
   private static final int DEFAULT_IMAGE_WIDTH = 128;
   private static final int DEFAULT_IMAGE_HEIGHT = 128;
   final TextureManager textureManager;
   private final Int2ObjectMap mapTextures = new Int2ObjectOpenHashMap();

   public MapRenderer(TextureManager textureManager) {
      this.textureManager = textureManager;
   }

   public void updateTexture(int id, MapState state) {
      this.getMapTexture(id, state).setNeedsUpdate();
   }

   public void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int id, MapState state, boolean hidePlayerIcons, int light) {
      this.getMapTexture(id, state).draw(matrices, vertexConsumers, hidePlayerIcons, light);
   }

   private MapTexture getMapTexture(int id, MapState state) {
      return (MapTexture)this.mapTextures.compute(id, (id2, texture) -> {
         if (texture == null) {
            return new MapTexture(id2, state);
         } else {
            texture.setState(state);
            return texture;
         }
      });
   }

   public void clearStateTextures() {
      ObjectIterator var1 = this.mapTextures.values().iterator();

      while(var1.hasNext()) {
         MapTexture lv = (MapTexture)var1.next();
         lv.close();
      }

      this.mapTextures.clear();
   }

   public void close() {
      this.clearStateTextures();
   }

   static {
      MAP_ICONS_RENDER_LAYER = RenderLayer.getText(MAP_ICONS_TEXTURE);
   }

   @Environment(EnvType.CLIENT)
   class MapTexture implements AutoCloseable {
      private MapState state;
      private final NativeImageBackedTexture texture;
      private final RenderLayer renderLayer;
      private boolean needsUpdate = true;

      MapTexture(int id, MapState state) {
         this.state = state;
         this.texture = new NativeImageBackedTexture(128, 128, true);
         Identifier lv = MapRenderer.this.textureManager.registerDynamicTexture("map/" + id, this.texture);
         this.renderLayer = RenderLayer.getText(lv);
      }

      void setState(MapState state) {
         boolean bl = this.state != state;
         this.state = state;
         this.needsUpdate |= bl;
      }

      public void setNeedsUpdate() {
         this.needsUpdate = true;
      }

      private void updateTexture() {
         for(int i = 0; i < 128; ++i) {
            for(int j = 0; j < 128; ++j) {
               int k = j + i * 128;
               this.texture.getImage().setColor(j, i, MapColor.getRenderColor(this.state.colors[k]));
            }
         }

         this.texture.upload();
      }

      void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean hidePlayerIcons, int light) {
         if (this.needsUpdate) {
            this.updateTexture();
            this.needsUpdate = false;
         }

         int j = false;
         int k = false;
         float f = 0.0F;
         Matrix4f matrix4f = matrices.peek().getPositionMatrix();
         VertexConsumer lv = vertexConsumers.getBuffer(this.renderLayer);
         lv.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(light).next();
         lv.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(light).next();
         lv.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(light).next();
         lv.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(light).next();
         int l = 0;
         Iterator var11 = this.state.getIcons().iterator();

         while(true) {
            MapIcon lv2;
            do {
               if (!var11.hasNext()) {
                  return;
               }

               lv2 = (MapIcon)var11.next();
            } while(hidePlayerIcons && !lv2.isAlwaysRendered());

            matrices.push();
            matrices.translate(0.0F + (float)lv2.getX() / 2.0F + 64.0F, 0.0F + (float)lv2.getZ() / 2.0F + 64.0F, -0.02F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(lv2.getRotation() * 360) / 16.0F));
            matrices.scale(4.0F, 4.0F, 3.0F);
            matrices.translate(-0.125F, 0.125F, 0.0F);
            byte b = lv2.getTypeId();
            float g = (float)(b % 16 + 0) / 16.0F;
            float h = (float)(b / 16 + 0) / 16.0F;
            float m = (float)(b % 16 + 1) / 16.0F;
            float n = (float)(b / 16 + 1) / 16.0F;
            Matrix4f matrix4f2 = matrices.peek().getPositionMatrix();
            float o = -0.001F;
            VertexConsumer lv3 = vertexConsumers.getBuffer(MapRenderer.MAP_ICONS_RENDER_LAYER);
            lv3.vertex(matrix4f2, -1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(g, h).light(light).next();
            lv3.vertex(matrix4f2, 1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(m, h).light(light).next();
            lv3.vertex(matrix4f2, 1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(m, n).light(light).next();
            lv3.vertex(matrix4f2, -1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).texture(g, n).light(light).next();
            matrices.pop();
            if (lv2.getText() != null) {
               TextRenderer lv4 = MinecraftClient.getInstance().textRenderer;
               Text lv5 = lv2.getText();
               float p = (float)lv4.getWidth((StringVisitable)lv5);
               float var10000 = 25.0F / p;
               Objects.requireNonNull(lv4);
               float q = MathHelper.clamp(var10000, 0.0F, 6.0F / 9.0F);
               matrices.push();
               matrices.translate(0.0F + (float)lv2.getX() / 2.0F + 64.0F - p * q / 2.0F, 0.0F + (float)lv2.getZ() / 2.0F + 64.0F + 4.0F, -0.025F);
               matrices.scale(q, q, 1.0F);
               matrices.translate(0.0F, 0.0F, -0.1F);
               lv4.draw((Text)lv5, 0.0F, 0.0F, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, Integer.MIN_VALUE, light);
               matrices.pop();
            }

            ++l;
         }
      }

      public void close() {
         this.texture.close();
      }
   }
}
