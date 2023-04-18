package net.minecraft.client.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SpriteAtlasTexture extends AbstractTexture implements DynamicTexture, TextureTickListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   /** @deprecated */
   @Deprecated
   public static final Identifier BLOCK_ATLAS_TEXTURE;
   /** @deprecated */
   @Deprecated
   public static final Identifier PARTICLE_ATLAS_TEXTURE;
   private List spritesToLoad = List.of();
   private List animatedSprites = List.of();
   private Map sprites = Map.of();
   private final Identifier id;
   private final int maxTextureSize;
   private int width;
   private int height;
   private int mipLevel;

   public SpriteAtlasTexture(Identifier id) {
      this.id = id;
      this.maxTextureSize = RenderSystem.maxSupportedTextureSize();
   }

   public void load(ResourceManager manager) {
   }

   public void upload(SpriteLoader.StitchResult stitchResult) {
      LOGGER.info("Created: {}x{}x{} {}-atlas", new Object[]{stitchResult.width(), stitchResult.height(), stitchResult.mipLevel(), this.id});
      TextureUtil.prepareImage(this.getGlId(), stitchResult.mipLevel(), stitchResult.width(), stitchResult.height());
      this.width = stitchResult.width();
      this.height = stitchResult.height();
      this.mipLevel = stitchResult.mipLevel();
      this.clear();
      this.sprites = Map.copyOf(stitchResult.regions());
      List list = new ArrayList();
      List list2 = new ArrayList();
      Iterator var4 = stitchResult.regions().values().iterator();

      while(var4.hasNext()) {
         Sprite lv = (Sprite)var4.next();
         list.add(lv.getContents());

         try {
            lv.upload();
         } catch (Throwable var9) {
            CrashReport lv2 = CrashReport.create(var9, "Stitching texture atlas");
            CrashReportSection lv3 = lv2.addElement("Texture being stitched together");
            lv3.add("Atlas path", (Object)this.id);
            lv3.add("Sprite", (Object)lv);
            throw new CrashException(lv2);
         }

         Sprite.TickableAnimation lv4 = lv.createAnimation();
         if (lv4 != null) {
            list2.add(lv4);
         }
      }

      this.spritesToLoad = List.copyOf(list);
      this.animatedSprites = List.copyOf(list2);
   }

   public void save(Identifier id, Path path) throws IOException {
      String string = id.toUnderscoreSeparatedString();
      TextureUtil.writeAsPNG(path, string, this.getGlId(), this.mipLevel, this.width, this.height);
      dumpAtlasInfos(path, string, this.sprites);
   }

   private static void dumpAtlasInfos(Path path, String id, Map sprites) {
      Path path2 = path.resolve(id + ".txt");

      try {
         Writer writer = Files.newBufferedWriter(path2);

         try {
            Iterator var5 = sprites.entrySet().stream().sorted(Entry.comparingByKey()).toList().iterator();

            while(var5.hasNext()) {
               Map.Entry entry = (Map.Entry)var5.next();
               Sprite lv = (Sprite)entry.getValue();
               writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), lv.getX(), lv.getY(), lv.getContents().getWidth(), lv.getContents().getHeight()));
            }
         } catch (Throwable var9) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException var10) {
         LOGGER.warn("Failed to write file {}", path2, var10);
      }

   }

   public void tickAnimatedSprites() {
      this.bindTexture();
      Iterator var1 = this.animatedSprites.iterator();

      while(var1.hasNext()) {
         Sprite.TickableAnimation lv = (Sprite.TickableAnimation)var1.next();
         lv.tick();
      }

   }

   public void tick() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::tickAnimatedSprites);
      } else {
         this.tickAnimatedSprites();
      }

   }

   public Sprite getSprite(Identifier id) {
      Sprite lv = (Sprite)this.sprites.get(id);
      return lv == null ? (Sprite)this.sprites.get(MissingSprite.getMissingSpriteId()) : lv;
   }

   public void clear() {
      this.spritesToLoad.forEach(SpriteContents::close);
      this.animatedSprites.forEach(Sprite.TickableAnimation::close);
      this.spritesToLoad = List.of();
      this.animatedSprites = List.of();
      this.sprites = Map.of();
   }

   public Identifier getId() {
      return this.id;
   }

   public int getMaxTextureSize() {
      return this.maxTextureSize;
   }

   int getWidth() {
      return this.width;
   }

   int getHeight() {
      return this.height;
   }

   public void applyTextureFilter(SpriteLoader.StitchResult data) {
      this.setFilter(false, data.mipLevel() > 0);
   }

   static {
      BLOCK_ATLAS_TEXTURE = PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
      PARTICLE_ATLAS_TEXTURE = new Identifier("textures/atlas/particles.png");
   }
}
