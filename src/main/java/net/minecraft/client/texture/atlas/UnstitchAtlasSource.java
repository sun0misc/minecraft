package net.minecraft.client.texture.atlas;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class UnstitchAtlasSource implements AtlasSource {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Identifier.CODEC.fieldOf("resource").forGetter((arg) -> {
         return arg.resource;
      }), Codecs.nonEmptyList(UnstitchAtlasSource.Region.CODEC.listOf()).fieldOf("regions").forGetter((arg) -> {
         return arg.regions;
      }), Codec.DOUBLE.optionalFieldOf("divisor_x", 1.0).forGetter((arg) -> {
         return arg.divisorX;
      }), Codec.DOUBLE.optionalFieldOf("divisor_y", 1.0).forGetter((arg) -> {
         return arg.divisorY;
      })).apply(instance, UnstitchAtlasSource::new);
   });
   private final Identifier resource;
   private final List regions;
   private final double divisorX;
   private final double divisorY;

   public UnstitchAtlasSource(Identifier resource, List regions, double divisorX, double divisorY) {
      this.resource = resource;
      this.regions = regions;
      this.divisorX = divisorX;
      this.divisorY = divisorY;
   }

   public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
      Identifier lv = RESOURCE_FINDER.toResourcePath(this.resource);
      Optional optional = resourceManager.getResource(lv);
      if (optional.isPresent()) {
         Sprite lv2 = new Sprite(lv, (Resource)optional.get(), this.regions.size());
         Iterator var6 = this.regions.iterator();

         while(var6.hasNext()) {
            Region lv3 = (Region)var6.next();
            regions.add(lv3.sprite, (AtlasSource.SpriteRegion)(new SpriteRegion(lv2, lv3, this.divisorX, this.divisorY)));
         }
      } else {
         LOGGER.warn("Missing sprite: {}", lv);
      }

   }

   public AtlasSourceType getType() {
      return AtlasSourceManager.UNSTITCH;
   }

   @Environment(EnvType.CLIENT)
   static record Region(Identifier sprite, double x, double y, double width, double height) {
      final Identifier sprite;
      final double x;
      final double y;
      final double width;
      final double height;
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Identifier.CODEC.fieldOf("sprite").forGetter(Region::sprite), Codec.DOUBLE.fieldOf("x").forGetter(Region::x), Codec.DOUBLE.fieldOf("y").forGetter(Region::y), Codec.DOUBLE.fieldOf("width").forGetter(Region::width), Codec.DOUBLE.fieldOf("height").forGetter(Region::height)).apply(instance, Region::new);
      });

      private Region(Identifier arg, double d, double e, double f, double g) {
         this.sprite = arg;
         this.x = d;
         this.y = e;
         this.width = f;
         this.height = g;
      }

      public Identifier sprite() {
         return this.sprite;
      }

      public double x() {
         return this.x;
      }

      public double y() {
         return this.y;
      }

      public double width() {
         return this.width;
      }

      public double height() {
         return this.height;
      }
   }

   @Environment(EnvType.CLIENT)
   static class SpriteRegion implements AtlasSource.SpriteRegion {
      private final Sprite sprite;
      private final Region region;
      private final double divisorX;
      private final double divisorY;

      SpriteRegion(Sprite sprite, Region region, double divisorX, double divisorY) {
         this.sprite = sprite;
         this.region = region;
         this.divisorX = divisorX;
         this.divisorY = divisorY;
      }

      public SpriteContents get() {
         try {
            NativeImage lv = this.sprite.read();
            double d = (double)lv.getWidth() / this.divisorX;
            double e = (double)lv.getHeight() / this.divisorY;
            int i = MathHelper.floor(this.region.x * d);
            int j = MathHelper.floor(this.region.y * e);
            int k = MathHelper.floor(this.region.width * d);
            int l = MathHelper.floor(this.region.height * e);
            NativeImage lv2 = new NativeImage(NativeImage.Format.RGBA, k, l, false);
            lv.copyRect(lv2, i, j, 0, 0, k, l, false, false);
            SpriteContents var11 = new SpriteContents(this.region.sprite, new SpriteDimensions(k, l), lv2, AnimationResourceMetadata.EMPTY);
            return var11;
         } catch (Exception var15) {
            UnstitchAtlasSource.LOGGER.error("Failed to unstitch region {}", this.region.sprite, var15);
         } finally {
            this.sprite.close();
         }

         return MissingSprite.createSpriteContents();
      }

      public void close() {
         this.sprite.close();
      }

      // $FF: synthetic method
      public Object get() {
         return this.get();
      }
   }
}
