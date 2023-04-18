package net.minecraft.client.render.model.json;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ItemModelGenerator {
   public static final List LAYERS = Lists.newArrayList(new String[]{"layer0", "layer1", "layer2", "layer3", "layer4"});
   private static final float field_32806 = 7.5F;
   private static final float field_32807 = 8.5F;

   public JsonUnbakedModel create(Function textureGetter, JsonUnbakedModel blockModel) {
      Map map = Maps.newHashMap();
      List list = Lists.newArrayList();

      for(int i = 0; i < LAYERS.size(); ++i) {
         String string = (String)LAYERS.get(i);
         if (!blockModel.textureExists(string)) {
            break;
         }

         SpriteIdentifier lv = blockModel.resolveSprite(string);
         map.put(string, Either.left(lv));
         SpriteContents lv2 = ((Sprite)textureGetter.apply(lv)).getContents();
         list.addAll(this.addLayerElements(i, string, lv2));
      }

      map.put("particle", blockModel.textureExists("particle") ? Either.left(blockModel.resolveSprite("particle")) : (Either)map.get("layer0"));
      JsonUnbakedModel lv3 = new JsonUnbakedModel((Identifier)null, list, map, false, blockModel.getGuiLight(), blockModel.getTransformations(), blockModel.getOverrides());
      lv3.id = blockModel.id;
      return lv3;
   }

   private List addLayerElements(int layer, String key, SpriteContents sprite) {
      Map map = Maps.newHashMap();
      map.put(Direction.SOUTH, new ModelElementFace((Direction)null, layer, key, new ModelElementTexture(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
      map.put(Direction.NORTH, new ModelElementFace((Direction)null, layer, key, new ModelElementTexture(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
      List list = Lists.newArrayList();
      list.add(new ModelElement(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, (ModelRotation)null, true));
      list.addAll(this.addSubComponents(sprite, key, layer));
      return list;
   }

   private List addSubComponents(SpriteContents sprite, String key, int layer) {
      float f = (float)sprite.getWidth();
      float g = (float)sprite.getHeight();
      List list = Lists.newArrayList();
      Iterator var7 = this.getFrames(sprite).iterator();

      while(var7.hasNext()) {
         Frame lv = (Frame)var7.next();
         float h = 0.0F;
         float j = 0.0F;
         float k = 0.0F;
         float l = 0.0F;
         float m = 0.0F;
         float n = 0.0F;
         float o = 0.0F;
         float p = 0.0F;
         float q = 16.0F / f;
         float r = 16.0F / g;
         float s = (float)lv.getMin();
         float t = (float)lv.getMax();
         float u = (float)lv.getLevel();
         Side lv2 = lv.getSide();
         switch (lv2) {
            case UP:
               m = s;
               h = s;
               k = n = t + 1.0F;
               o = u;
               j = u;
               l = u;
               p = u + 1.0F;
               break;
            case DOWN:
               o = u;
               p = u + 1.0F;
               m = s;
               h = s;
               k = n = t + 1.0F;
               j = u + 1.0F;
               l = u + 1.0F;
               break;
            case LEFT:
               m = u;
               h = u;
               k = u;
               n = u + 1.0F;
               p = s;
               j = s;
               l = o = t + 1.0F;
               break;
            case RIGHT:
               m = u;
               n = u + 1.0F;
               h = u + 1.0F;
               k = u + 1.0F;
               p = s;
               j = s;
               l = o = t + 1.0F;
         }

         h *= q;
         k *= q;
         j *= r;
         l *= r;
         j = 16.0F - j;
         l = 16.0F - l;
         m *= q;
         n *= q;
         o *= r;
         p *= r;
         Map map = Maps.newHashMap();
         map.put(lv2.getDirection(), new ModelElementFace((Direction)null, layer, key, new ModelElementTexture(new float[]{m, o, n, p}, 0)));
         switch (lv2) {
            case UP:
               list.add(new ModelElement(new Vector3f(h, j, 7.5F), new Vector3f(k, j, 8.5F), map, (ModelRotation)null, true));
               break;
            case DOWN:
               list.add(new ModelElement(new Vector3f(h, l, 7.5F), new Vector3f(k, l, 8.5F), map, (ModelRotation)null, true));
               break;
            case LEFT:
               list.add(new ModelElement(new Vector3f(h, j, 7.5F), new Vector3f(h, l, 8.5F), map, (ModelRotation)null, true));
               break;
            case RIGHT:
               list.add(new ModelElement(new Vector3f(k, j, 7.5F), new Vector3f(k, l, 8.5F), map, (ModelRotation)null, true));
         }
      }

      return list;
   }

   private List getFrames(SpriteContents sprite) {
      int i = sprite.getWidth();
      int j = sprite.getHeight();
      List list = Lists.newArrayList();
      sprite.getDistinctFrameCount().forEach((frame) -> {
         for(int l = 0; l < j; ++l) {
            for(int m = 0; m < i; ++m) {
               boolean bl = !this.isPixelTransparent(sprite, frame, m, l, i, j);
               this.buildCube(ItemModelGenerator.Side.UP, list, sprite, frame, m, l, i, j, bl);
               this.buildCube(ItemModelGenerator.Side.DOWN, list, sprite, frame, m, l, i, j, bl);
               this.buildCube(ItemModelGenerator.Side.LEFT, list, sprite, frame, m, l, i, j, bl);
               this.buildCube(ItemModelGenerator.Side.RIGHT, list, sprite, frame, m, l, i, j, bl);
            }
         }

      });
      return list;
   }

   private void buildCube(Side side, List cubes, SpriteContents sprite, int frame, int x, int y, int width, int height, boolean bl) {
      boolean bl2 = this.isPixelTransparent(sprite, frame, x + side.getOffsetX(), y + side.getOffsetY(), width, height) && bl;
      if (bl2) {
         this.buildCube(cubes, side, x, y);
      }

   }

   private void buildCube(List cubes, Side side, int x, int y) {
      Frame lv = null;
      Iterator var6 = cubes.iterator();

      while(var6.hasNext()) {
         Frame lv2 = (Frame)var6.next();
         if (lv2.getSide() == side) {
            int k = side.isVertical() ? y : x;
            if (lv2.getLevel() == k) {
               lv = lv2;
               break;
            }
         }
      }

      int l = side.isVertical() ? y : x;
      int m = side.isVertical() ? x : y;
      if (lv == null) {
         cubes.add(new Frame(side, m, l));
      } else {
         lv.expand(m);
      }

   }

   private boolean isPixelTransparent(SpriteContents sprite, int frame, int x, int y, int width, int height) {
      return x >= 0 && y >= 0 && x < width && y < height ? sprite.isPixelTransparent(frame, x, y) : true;
   }

   @Environment(EnvType.CLIENT)
   private static class Frame {
      private final Side side;
      private int min;
      private int max;
      private final int level;

      public Frame(Side side, int width, int depth) {
         this.side = side;
         this.min = width;
         this.max = width;
         this.level = depth;
      }

      public void expand(int newValue) {
         if (newValue < this.min) {
            this.min = newValue;
         } else if (newValue > this.max) {
            this.max = newValue;
         }

      }

      public Side getSide() {
         return this.side;
      }

      public int getMin() {
         return this.min;
      }

      public int getMax() {
         return this.max;
      }

      public int getLevel() {
         return this.level;
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum Side {
      UP(Direction.UP, 0, -1),
      DOWN(Direction.DOWN, 0, 1),
      LEFT(Direction.EAST, -1, 0),
      RIGHT(Direction.WEST, 1, 0);

      private final Direction direction;
      private final int offsetX;
      private final int offsetY;

      private Side(Direction direction, int offsetX, int offsetY) {
         this.direction = direction;
         this.offsetX = offsetX;
         this.offsetY = offsetY;
      }

      public Direction getDirection() {
         return this.direction;
      }

      public int getOffsetX() {
         return this.offsetX;
      }

      public int getOffsetY() {
         return this.offsetY;
      }

      boolean isVertical() {
         return this == DOWN || this == UP;
      }

      // $FF: synthetic method
      private static Side[] method_36921() {
         return new Side[]{UP, DOWN, LEFT, RIGHT};
      }
   }
}
