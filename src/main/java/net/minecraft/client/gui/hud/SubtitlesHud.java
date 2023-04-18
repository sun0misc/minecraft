package net.minecraft.client.gui.hud;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class SubtitlesHud extends DrawableHelper implements SoundInstanceListener {
   private static final long REMOVE_DELAY = 3000L;
   private final MinecraftClient client;
   private final List entries = Lists.newArrayList();
   private boolean enabled;

   public SubtitlesHud(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices) {
      if (!this.enabled && (Boolean)this.client.options.getShowSubtitles().getValue()) {
         this.client.getSoundManager().registerListener(this);
         this.enabled = true;
      } else if (this.enabled && !(Boolean)this.client.options.getShowSubtitles().getValue()) {
         this.client.getSoundManager().unregisterListener(this);
         this.enabled = false;
      }

      if (this.enabled && !this.entries.isEmpty()) {
         Vec3d lv = new Vec3d(this.client.player.getX(), this.client.player.getEyeY(), this.client.player.getZ());
         Vec3d lv2 = (new Vec3d(0.0, 0.0, -1.0)).rotateX(-this.client.player.getPitch() * 0.017453292F).rotateY(-this.client.player.getYaw() * 0.017453292F);
         Vec3d lv3 = (new Vec3d(0.0, 1.0, 0.0)).rotateX(-this.client.player.getPitch() * 0.017453292F).rotateY(-this.client.player.getYaw() * 0.017453292F);
         Vec3d lv4 = lv2.crossProduct(lv3);
         int i = 0;
         int j = 0;
         double d = (Double)this.client.options.getNotificationDisplayTime().getValue();
         Iterator iterator = this.entries.iterator();

         SubtitleEntry lv5;
         while(iterator.hasNext()) {
            lv5 = (SubtitleEntry)iterator.next();
            if ((double)lv5.getTime() + 3000.0 * d <= (double)Util.getMeasuringTimeMs()) {
               iterator.remove();
            } else {
               j = Math.max(j, this.client.textRenderer.getWidth((StringVisitable)lv5.getText()));
            }
         }

         j += this.client.textRenderer.getWidth("<") + this.client.textRenderer.getWidth(" ") + this.client.textRenderer.getWidth(">") + this.client.textRenderer.getWidth(" ");

         for(iterator = this.entries.iterator(); iterator.hasNext(); ++i) {
            lv5 = (SubtitleEntry)iterator.next();
            int k = true;
            Text lv6 = lv5.getText();
            Vec3d lv7 = lv5.getPosition().subtract(lv).normalize();
            double e = -lv4.dotProduct(lv7);
            double f = -lv2.dotProduct(lv7);
            boolean bl = f > 0.5;
            int l = j / 2;
            Objects.requireNonNull(this.client.textRenderer);
            int m = 9;
            int n = m / 2;
            float g = 1.0F;
            int o = this.client.textRenderer.getWidth((StringVisitable)lv6);
            int p = MathHelper.floor(MathHelper.clampedLerp(255.0F, 75.0F, (float)(Util.getMeasuringTimeMs() - lv5.getTime()) / (float)(3000.0 * d)));
            int q = p << 16 | p << 8 | p;
            matrices.push();
            matrices.translate((float)this.client.getWindow().getScaledWidth() - (float)l * 1.0F - 2.0F, (float)(this.client.getWindow().getScaledHeight() - 35) - (float)(i * (m + 1)) * 1.0F, 0.0F);
            matrices.scale(1.0F, 1.0F, 1.0F);
            fill(matrices, -l - 1, -n - 1, l + 1, n + 1, this.client.options.getTextBackgroundColor(0.8F));
            int r = q + -16777216;
            if (!bl) {
               if (e > 0.0) {
                  drawTextWithShadow(matrices, this.client.textRenderer, ">", l - this.client.textRenderer.getWidth(">"), -n, r);
               } else if (e < 0.0) {
                  drawTextWithShadow(matrices, this.client.textRenderer, "<", -l, -n, r);
               }
            }

            drawTextWithShadow(matrices, this.client.textRenderer, lv6, -o / 2, -n, r);
            matrices.pop();
         }

      }
   }

   public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet) {
      if (soundSet.getSubtitle() != null) {
         Text lv = soundSet.getSubtitle();
         if (!this.entries.isEmpty()) {
            Iterator var4 = this.entries.iterator();

            while(var4.hasNext()) {
               SubtitleEntry lv2 = (SubtitleEntry)var4.next();
               if (lv2.getText().equals(lv)) {
                  lv2.reset(new Vec3d(sound.getX(), sound.getY(), sound.getZ()));
                  return;
               }
            }
         }

         this.entries.add(new SubtitleEntry(lv, new Vec3d(sound.getX(), sound.getY(), sound.getZ())));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SubtitleEntry {
      private final Text text;
      private long time;
      private Vec3d pos;

      public SubtitleEntry(Text text, Vec3d pos) {
         this.text = text;
         this.pos = pos;
         this.time = Util.getMeasuringTimeMs();
      }

      public Text getText() {
         return this.text;
      }

      public long getTime() {
         return this.time;
      }

      public Vec3d getPosition() {
         return this.pos;
      }

      public void reset(Vec3d pos) {
         this.pos = pos;
         this.time = Util.getMeasuringTimeMs();
      }
   }
}
