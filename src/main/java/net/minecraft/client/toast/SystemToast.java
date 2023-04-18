package net.minecraft.client.toast;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SystemToast implements Toast {
   private static final int MIN_WIDTH = 200;
   private static final int LINE_HEIGHT = 12;
   private static final int PADDING_Y = 10;
   private final Type type;
   private Text title;
   private List lines;
   private long startTime;
   private boolean justUpdated;
   private final int width;

   public SystemToast(Type type, Text title, @Nullable Text description) {
      this(type, title, getTextAsList(description), Math.max(160, 30 + Math.max(MinecraftClient.getInstance().textRenderer.getWidth((StringVisitable)title), description == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth((StringVisitable)description))));
   }

   public static SystemToast create(MinecraftClient client, Type type, Text title, Text description) {
      TextRenderer lv = client.textRenderer;
      List list = lv.wrapLines(description, 200);
      Stream var10001 = list.stream();
      Objects.requireNonNull(lv);
      int i = Math.max(200, var10001.mapToInt(lv::getWidth).max().orElse(200));
      return new SystemToast(type, title, list, i + 30);
   }

   private SystemToast(Type type, Text title, List lines, int width) {
      this.type = type;
      this.title = title;
      this.lines = lines;
      this.width = width;
   }

   private static ImmutableList getTextAsList(@Nullable Text text) {
      return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return 20 + Math.max(this.lines.size(), 1) * 12;
   }

   public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
      if (this.justUpdated) {
         this.startTime = startTime;
         this.justUpdated = false;
      }

      RenderSystem.setShaderTexture(0, TEXTURE);
      int i = this.getWidth();
      int j;
      if (i == 160 && this.lines.size() <= 1) {
         DrawableHelper.drawTexture(matrices, 0, 0, 0, 64, i, this.getHeight());
      } else {
         j = this.getHeight();
         int k = true;
         int m = Math.min(4, j - 28);
         this.drawPart(matrices, manager, i, 0, 0, 28);

         for(int n = 28; n < j - m; n += 10) {
            this.drawPart(matrices, manager, i, 16, n, Math.min(16, j - n - m));
         }

         this.drawPart(matrices, manager, i, 32 - m, j - m, m);
      }

      if (this.lines == null) {
         manager.getClient().textRenderer.draw(matrices, (Text)this.title, 18.0F, 12.0F, -256);
      } else {
         manager.getClient().textRenderer.draw(matrices, (Text)this.title, 18.0F, 7.0F, -256);

         for(j = 0; j < this.lines.size(); ++j) {
            manager.getClient().textRenderer.draw(matrices, (OrderedText)((OrderedText)this.lines.get(j)), 18.0F, (float)(18 + j * 12), -1);
         }
      }

      return (double)(startTime - this.startTime) < (double)this.type.displayDuration * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
   }

   private void drawPart(MatrixStack matrices, ToastManager manager, int width, int textureV, int y, int height) {
      int m = textureV == 0 ? 20 : 5;
      int n = Math.min(60, width - m);
      DrawableHelper.drawTexture(matrices, 0, y, 0, 64 + textureV, m, height);

      for(int o = m; o < width - n; o += 64) {
         DrawableHelper.drawTexture(matrices, o, y, 32, 64 + textureV, Math.min(64, width - o - n), height);
      }

      DrawableHelper.drawTexture(matrices, width - n, y, 160 - n, 64 + textureV, n, height);
   }

   public void setContent(Text title, @Nullable Text description) {
      this.title = title;
      this.lines = getTextAsList(description);
      this.justUpdated = true;
   }

   public Type getType() {
      return this.type;
   }

   public static void add(ToastManager manager, Type type, Text title, @Nullable Text description) {
      manager.add(new SystemToast(type, title, description));
   }

   public static void show(ToastManager manager, Type type, Text title, @Nullable Text description) {
      SystemToast lv = (SystemToast)manager.getToast(SystemToast.class, type);
      if (lv == null) {
         add(manager, type, title, description);
      } else {
         lv.setContent(title, description);
      }

   }

   public static void addWorldAccessFailureToast(MinecraftClient client, String worldName) {
      add(client.getToastManager(), SystemToast.Type.WORLD_ACCESS_FAILURE, Text.translatable("selectWorld.access_failure"), Text.literal(worldName));
   }

   public static void addWorldDeleteFailureToast(MinecraftClient client, String worldName) {
      add(client.getToastManager(), SystemToast.Type.WORLD_ACCESS_FAILURE, Text.translatable("selectWorld.delete_failure"), Text.literal(worldName));
   }

   public static void addPackCopyFailure(MinecraftClient client, String directory) {
      add(client.getToastManager(), SystemToast.Type.PACK_COPY_FAILURE, Text.translatable("pack.copyFailure"), Text.literal(directory));
   }

   // $FF: synthetic method
   public Object getType() {
      return this.getType();
   }

   @Environment(EnvType.CLIENT)
   public static enum Type {
      TUTORIAL_HINT,
      NARRATOR_TOGGLE,
      WORLD_BACKUP,
      PACK_LOAD_FAILURE,
      WORLD_ACCESS_FAILURE,
      PACK_COPY_FAILURE,
      PERIODIC_NOTIFICATION,
      UNSECURE_SERVER_WARNING(10000L);

      final long displayDuration;

      private Type(long displayDuration) {
         this.displayDuration = displayDuration;
      }

      private Type() {
         this(5000L);
      }

      // $FF: synthetic method
      private static Type[] method_36871() {
         return new Type[]{TUTORIAL_HINT, NARRATOR_TOGGLE, WORLD_BACKUP, PACK_LOAD_FAILURE, WORLD_ACCESS_FAILURE, PACK_COPY_FAILURE, PERIODIC_NOTIFICATION, UNSECURE_SERVER_WARNING};
      }
   }
}
