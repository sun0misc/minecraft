package net.minecraft.client.texture;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationFrameResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class MissingSprite {
   private static final int WIDTH = 16;
   private static final int HEIGHT = 16;
   private static final String MISSINGNO_ID = "missingno";
   private static final Identifier MISSINGNO = new Identifier("missingno");
   private static final AnimationResourceMetadata METADATA = new AnimationResourceMetadata(ImmutableList.of(new AnimationFrameResourceMetadata(0, -1)), 16, 16, 1, false);
   @Nullable
   private static NativeImageBackedTexture texture;

   private static NativeImage createImage(int width, int height) {
      NativeImage lv = new NativeImage(width, height, false);
      int k = -16777216;
      int l = -524040;

      for(int m = 0; m < height; ++m) {
         for(int n = 0; n < width; ++n) {
            if (m < height / 2 ^ n < width / 2) {
               lv.setColor(n, m, -524040);
            } else {
               lv.setColor(n, m, -16777216);
            }
         }
      }

      return lv;
   }

   public static SpriteContents createSpriteContents() {
      NativeImage lv = createImage(16, 16);
      return new SpriteContents(MISSINGNO, new SpriteDimensions(16, 16), lv, METADATA);
   }

   public static Identifier getMissingSpriteId() {
      return MISSINGNO;
   }

   public static NativeImageBackedTexture getMissingSpriteTexture() {
      if (texture == null) {
         NativeImage lv = createImage(16, 16);
         lv.untrack();
         texture = new NativeImageBackedTexture(lv);
         MinecraftClient.getInstance().getTextureManager().registerTexture(MISSINGNO, texture);
      }

      return texture;
   }
}
