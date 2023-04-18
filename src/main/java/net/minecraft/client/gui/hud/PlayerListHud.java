package net.minecraft.client.gui.hud;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PlayerListHud extends DrawableHelper {
   private static final Comparator ENTRY_ORDERING = Comparator.comparingInt((entry) -> {
      return entry.getGameMode() == GameMode.SPECTATOR ? 1 : 0;
   }).thenComparing((entry) -> {
      return (String)Nullables.mapOrElse(entry.getScoreboardTeam(), Team::getName, "");
   }).thenComparing((entry) -> {
      return entry.getProfile().getName();
   }, String::compareToIgnoreCase);
   public static final int MAX_ROWS = 20;
   public static final int HEART_OUTLINE_U = 16;
   public static final int BLINKING_HEART_OUTLINE_U = 25;
   public static final int HEART_U = 52;
   public static final int HALF_HEART_U = 61;
   public static final int GOLDEN_HEART_U = 160;
   public static final int HALF_GOLDEN_HEART_U = 169;
   public static final int BLINKING_HEART_U = 70;
   public static final int BLINKING_HALF_HEART_U = 79;
   private final MinecraftClient client;
   private final InGameHud inGameHud;
   @Nullable
   private Text footer;
   @Nullable
   private Text header;
   private boolean visible;
   private final Map hearts = new Object2ObjectOpenHashMap();

   public PlayerListHud(MinecraftClient client, InGameHud inGameHud) {
      this.client = client;
      this.inGameHud = inGameHud;
   }

   public Text getPlayerName(PlayerListEntry entry) {
      return entry.getDisplayName() != null ? this.applyGameModeFormatting(entry, entry.getDisplayName().copy()) : this.applyGameModeFormatting(entry, Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName())));
   }

   private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
      return entry.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
   }

   public void setVisible(boolean visible) {
      if (this.visible != visible) {
         this.hearts.clear();
         this.visible = visible;
         if (visible) {
            Text lv = Texts.join(this.collectPlayerEntries(), (Text)Text.literal(", "), this::getPlayerName);
            this.client.getNarratorManager().narrate((Text)Text.translatable("multiplayer.player.list.narration", lv));
         }
      }

   }

   private List collectPlayerEntries() {
      return this.client.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80L).toList();
   }

   public void render(MatrixStack matrices, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective objective) {
      List list = this.collectPlayerEntries();
      int j = 0;
      int k = 0;
      Iterator var8 = list.iterator();

      int l;
      while(var8.hasNext()) {
         PlayerListEntry lv = (PlayerListEntry)var8.next();
         l = this.client.textRenderer.getWidth((StringVisitable)this.getPlayerName(lv));
         j = Math.max(j, l);
         if (objective != null && objective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS) {
            TextRenderer var10000 = this.client.textRenderer;
            ScoreboardPlayerScore var10001 = scoreboard.getPlayerScore(lv.getProfile().getName(), objective);
            l = var10000.getWidth(" " + var10001.getScore());
            k = Math.max(k, l);
         }
      }

      if (!this.hearts.isEmpty()) {
         Set set = (Set)list.stream().map((playerEntry) -> {
            return playerEntry.getProfile().getId();
         }).collect(Collectors.toSet());
         this.hearts.keySet().removeIf((uuid) -> {
            return !set.contains(uuid);
         });
      }

      int m = list.size();
      int n = m;

      for(l = 1; n > 20; n = (m + l - 1) / l) {
         ++l;
      }

      boolean bl = this.client.isInSingleplayer() || this.client.getNetworkHandler().getConnection().isEncrypted();
      int o;
      if (objective != null) {
         if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
            o = 90;
         } else {
            o = k;
         }
      } else {
         o = 0;
      }

      int p = Math.min(l * ((bl ? 9 : 0) + j + o + 13), scaledWindowWidth - 50) / l;
      int q = scaledWindowWidth / 2 - (p * l + (l - 1) * 5) / 2;
      int r = 10;
      int s = p * l + (l - 1) * 5;
      List list2 = null;
      if (this.header != null) {
         list2 = this.client.textRenderer.wrapLines(this.header, scaledWindowWidth - 50);

         OrderedText lv2;
         for(Iterator var18 = list2.iterator(); var18.hasNext(); s = Math.max(s, this.client.textRenderer.getWidth(lv2))) {
            lv2 = (OrderedText)var18.next();
         }
      }

      List list3 = null;
      OrderedText lv3;
      Iterator var35;
      if (this.footer != null) {
         list3 = this.client.textRenderer.wrapLines(this.footer, scaledWindowWidth - 50);

         for(var35 = list3.iterator(); var35.hasNext(); s = Math.max(s, this.client.textRenderer.getWidth(lv3))) {
            lv3 = (OrderedText)var35.next();
         }
      }

      int var10002;
      int var10003;
      int var10005;
      int t;
      int var33;
      if (list2 != null) {
         var33 = scaledWindowWidth / 2 - s / 2 - 1;
         var10002 = r - 1;
         var10003 = scaledWindowWidth / 2 + s / 2 + 1;
         var10005 = list2.size();
         Objects.requireNonNull(this.client.textRenderer);
         fill(matrices, var33, var10002, var10003, r + var10005 * 9, Integer.MIN_VALUE);

         for(var35 = list2.iterator(); var35.hasNext(); r += 9) {
            lv3 = (OrderedText)var35.next();
            t = this.client.textRenderer.getWidth(lv3);
            this.client.textRenderer.drawWithShadow(matrices, (OrderedText)lv3, (float)(scaledWindowWidth / 2 - t / 2), (float)r, -1);
            Objects.requireNonNull(this.client.textRenderer);
         }

         ++r;
      }

      fill(matrices, scaledWindowWidth / 2 - s / 2 - 1, r - 1, scaledWindowWidth / 2 + s / 2 + 1, r + n * 9, Integer.MIN_VALUE);
      int u = this.client.options.getTextBackgroundColor(553648127);

      int w;
      for(int v = 0; v < m; ++v) {
         t = v / n;
         w = v % n;
         int x = q + t * p + t * 5;
         int y = r + w * 9;
         fill(matrices, x, y, x + p, y + 8, u);
         RenderSystem.enableBlend();
         if (v < list.size()) {
            PlayerListEntry lv4 = (PlayerListEntry)list.get(v);
            GameProfile gameProfile = lv4.getProfile();
            if (bl) {
               PlayerEntity lv5 = this.client.world.getPlayerByUuid(gameProfile.getId());
               boolean bl2 = lv5 != null && LivingEntityRenderer.shouldFlipUpsideDown(lv5);
               boolean bl3 = lv5 != null && lv5.isPartVisible(PlayerModelPart.HAT);
               RenderSystem.setShaderTexture(0, lv4.getSkinTexture());
               PlayerSkinDrawer.draw(matrices, x, y, 8, bl3, bl2);
               x += 9;
            }

            this.client.textRenderer.drawWithShadow(matrices, this.getPlayerName(lv4), (float)x, (float)y, lv4.getGameMode() == GameMode.SPECTATOR ? -1862270977 : -1);
            if (objective != null && lv4.getGameMode() != GameMode.SPECTATOR) {
               int z = x + j + 1;
               int aa = z + o;
               if (aa - z > 5) {
                  this.renderScoreboardObjective(objective, y, gameProfile.getName(), z, aa, gameProfile.getId(), matrices);
               }
            }

            this.renderLatencyIcon(matrices, p, x - (bl ? 9 : 0), y, lv4);
         }
      }

      if (list3 != null) {
         r += n * 9 + 1;
         var33 = scaledWindowWidth / 2 - s / 2 - 1;
         var10002 = r - 1;
         var10003 = scaledWindowWidth / 2 + s / 2 + 1;
         var10005 = list3.size();
         Objects.requireNonNull(this.client.textRenderer);
         fill(matrices, var33, var10002, var10003, r + var10005 * 9, Integer.MIN_VALUE);

         for(Iterator var38 = list3.iterator(); var38.hasNext(); r += 9) {
            OrderedText lv6 = (OrderedText)var38.next();
            w = this.client.textRenderer.getWidth(lv6);
            this.client.textRenderer.drawWithShadow(matrices, (OrderedText)lv6, (float)(scaledWindowWidth / 2 - w / 2), (float)r, -1);
            Objects.requireNonNull(this.client.textRenderer);
         }
      }

   }

   protected void renderLatencyIcon(MatrixStack matrices, int width, int x, int y, PlayerListEntry entry) {
      RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
      int l = false;
      byte m;
      if (entry.getLatency() < 0) {
         m = 5;
      } else if (entry.getLatency() < 150) {
         m = 0;
      } else if (entry.getLatency() < 300) {
         m = 1;
      } else if (entry.getLatency() < 600) {
         m = 2;
      } else if (entry.getLatency() < 1000) {
         m = 3;
      } else {
         m = 4;
      }

      matrices.push();
      matrices.translate(0.0F, 0.0F, 100.0F);
      drawTexture(matrices, x + width - 11, y, 0, 176 + m * 8, 10, 8);
      matrices.pop();
   }

   private void renderScoreboardObjective(ScoreboardObjective objective, int y, String player, int left, int right, UUID uuid, MatrixStack matrices) {
      int l = objective.getScoreboard().getPlayerScore(player, objective).getScore();
      if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
         this.renderHearts(y, left, right, uuid, matrices, l);
      } else {
         String string2 = Formatting.YELLOW + l;
         this.client.textRenderer.drawWithShadow(matrices, string2, (float)(right - this.client.textRenderer.getWidth(string2)), (float)y, 16777215);
      }
   }

   private void renderHearts(int y, int left, int right, UUID uuid, MatrixStack matrices, int score) {
      Heart lv = (Heart)this.hearts.computeIfAbsent(uuid, (uuid2) -> {
         return new Heart(score);
      });
      lv.tick(score, (long)this.inGameHud.getTicks());
      int m = MathHelper.ceilDiv(Math.max(score, lv.getPrevScore()), 2);
      int n = Math.max(score, Math.max(lv.getPrevScore(), 20)) / 2;
      boolean bl = lv.useHighlighted((long)this.inGameHud.getTicks());
      if (m > 0) {
         RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
         int o = MathHelper.floor(Math.min((float)(right - left - 4) / (float)n, 9.0F));
         if (o <= 3) {
            float f = MathHelper.clamp((float)score / 20.0F, 0.0F, 1.0F);
            int p = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
            String string = "" + (float)score / 2.0F;
            if (right - this.client.textRenderer.getWidth(string + "hp") >= left) {
               string = string + "hp";
            }

            this.client.textRenderer.drawWithShadow(matrices, string, (float)((right + left - this.client.textRenderer.getWidth(string)) / 2), (float)y, p);
         } else {
            int q;
            for(q = m; q < n; ++q) {
               drawTexture(matrices, left + q * o, y, bl ? 25 : 16, 0, 9, 9);
            }

            for(q = 0; q < m; ++q) {
               drawTexture(matrices, left + q * o, y, bl ? 25 : 16, 0, 9, 9);
               if (bl) {
                  if (q * 2 + 1 < lv.getPrevScore()) {
                     drawTexture(matrices, left + q * o, y, 70, 0, 9, 9);
                  }

                  if (q * 2 + 1 == lv.getPrevScore()) {
                     drawTexture(matrices, left + q * o, y, 79, 0, 9, 9);
                  }
               }

               if (q * 2 + 1 < score) {
                  drawTexture(matrices, left + q * o, y, q >= 10 ? 160 : 52, 0, 9, 9);
               }

               if (q * 2 + 1 == score) {
                  drawTexture(matrices, left + q * o, y, q >= 10 ? 169 : 61, 0, 9, 9);
               }
            }

         }
      }
   }

   public void setFooter(@Nullable Text footer) {
      this.footer = footer;
   }

   public void setHeader(@Nullable Text header) {
      this.header = header;
   }

   public void clear() {
      this.header = null;
      this.footer = null;
   }

   @Environment(EnvType.CLIENT)
   static class Heart {
      private static final long COOLDOWN_TICKS = 20L;
      private static final long SCORE_DECREASE_HIGHLIGHT_TICKS = 20L;
      private static final long SCORE_INCREASE_HIGHLIGHT_TICKS = 10L;
      private int score;
      private int prevScore;
      private long lastScoreChangeTick;
      private long highlightEndTick;

      public Heart(int score) {
         this.prevScore = score;
         this.score = score;
      }

      public void tick(int score, long currentTick) {
         if (score != this.score) {
            long m = score < this.score ? 20L : 10L;
            this.highlightEndTick = currentTick + m;
            this.score = score;
            this.lastScoreChangeTick = currentTick;
         }

         if (currentTick - this.lastScoreChangeTick > 20L) {
            this.prevScore = score;
         }

      }

      public int getPrevScore() {
         return this.prevScore;
      }

      public boolean useHighlighted(long currentTick) {
         return this.highlightEndTick > currentTick && (this.highlightEndTick - currentTick) % 6L >= 3L;
      }
   }
}
