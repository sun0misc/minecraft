package net.minecraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record MessageIndicator(int indicatorColor, @Nullable Icon icon, @Nullable Text text, @Nullable String loggedName) {
   private static final Text SYSTEM_TEXT = Text.translatable("chat.tag.system");
   private static final Text SINGLE_PLAYER_TEXT = Text.translatable("chat.tag.system_single_player");
   private static final Text NOT_SECURE_TEXT = Text.translatable("chat.tag.not_secure");
   private static final Text MODIFIED_TEXT = Text.translatable("chat.tag.modified");
   private static final int NOT_SECURE_COLOR = 13684944;
   private static final int MODIFIED_COLOR = 6316128;
   private static final MessageIndicator SYSTEM;
   private static final MessageIndicator SINGLE_PLAYER;
   private static final MessageIndicator NOT_SECURE;
   static final Identifier CHAT_TAGS_TEXTURE;

   public MessageIndicator(int i, @Nullable Icon arg, @Nullable Text arg2, @Nullable String string) {
      this.indicatorColor = i;
      this.icon = arg;
      this.text = arg2;
      this.loggedName = string;
   }

   public static MessageIndicator system() {
      return SYSTEM;
   }

   public static MessageIndicator singlePlayer() {
      return SINGLE_PLAYER;
   }

   public static MessageIndicator notSecure() {
      return NOT_SECURE;
   }

   public static MessageIndicator modified(String originalText) {
      Text lv = Text.literal(originalText).formatted(Formatting.GRAY);
      Text lv2 = Text.empty().append(MODIFIED_TEXT).append(ScreenTexts.LINE_BREAK).append((Text)lv);
      return new MessageIndicator(6316128, MessageIndicator.Icon.CHAT_MODIFIED, lv2, "Modified");
   }

   public int indicatorColor() {
      return this.indicatorColor;
   }

   @Nullable
   public Icon icon() {
      return this.icon;
   }

   @Nullable
   public Text text() {
      return this.text;
   }

   @Nullable
   public String loggedName() {
      return this.loggedName;
   }

   static {
      SYSTEM = new MessageIndicator(13684944, (Icon)null, SYSTEM_TEXT, "System");
      SINGLE_PLAYER = new MessageIndicator(13684944, (Icon)null, SINGLE_PLAYER_TEXT, "System");
      NOT_SECURE = new MessageIndicator(13684944, (Icon)null, NOT_SECURE_TEXT, "Not Secure");
      CHAT_TAGS_TEXTURE = new Identifier("textures/gui/chat_tags.png");
   }

   @Environment(EnvType.CLIENT)
   public static enum Icon {
      CHAT_MODIFIED(0, 0, 9, 9);

      public final int u;
      public final int v;
      public final int width;
      public final int height;

      private Icon(int u, int v, int width, int height) {
         this.u = u;
         this.v = v;
         this.width = width;
         this.height = height;
      }

      public void draw(MatrixStack matrices, int x, int y) {
         RenderSystem.setShaderTexture(0, MessageIndicator.CHAT_TAGS_TEXTURE);
         DrawableHelper.drawTexture(matrices, x, y, (float)this.u, (float)this.v, this.width, this.height, 32, 32);
      }

      // $FF: synthetic method
      private static Icon[] method_44711() {
         return new Icon[]{CHAT_MODIFIED};
      }
   }
}
