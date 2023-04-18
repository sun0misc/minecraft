package net.minecraft.client.gui.hud;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class InGameHud extends DrawableHelper {
   private static final Identifier VIGNETTE_TEXTURE = new Identifier("textures/misc/vignette.png");
   private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
   private static final Identifier PUMPKIN_BLUR = new Identifier("textures/misc/pumpkinblur.png");
   private static final Identifier SPYGLASS_SCOPE = new Identifier("textures/misc/spyglass_scope.png");
   private static final Identifier POWDER_SNOW_OUTLINE = new Identifier("textures/misc/powder_snow_outline.png");
   private static final Text DEMO_EXPIRED_MESSAGE = Text.translatable("demo.demoExpired");
   private static final Text SAVING_LEVEL_TEXT = Text.translatable("menu.savingLevel");
   private static final int WHITE = 16777215;
   private static final float field_32168 = 5.0F;
   private static final int field_32169 = 10;
   private static final int field_32170 = 10;
   private static final String SCOREBOARD_JOINER = ": ";
   private static final float field_32172 = 0.2F;
   private static final int field_33942 = 9;
   private static final int field_33943 = 8;
   private static final float field_35431 = 0.2F;
   private final Random random = Random.create();
   private final MinecraftClient client;
   private final ItemRenderer itemRenderer;
   private final ChatHud chatHud;
   private int ticks;
   @Nullable
   private Text overlayMessage;
   private int overlayRemaining;
   private boolean overlayTinted;
   private boolean canShowChatDisabledScreen;
   public float vignetteDarkness = 1.0F;
   private int heldItemTooltipFade;
   private ItemStack currentStack;
   private final DebugHud debugHud;
   private final SubtitlesHud subtitlesHud;
   private final SpectatorHud spectatorHud;
   private final PlayerListHud playerListHud;
   private final BossBarHud bossBarHud;
   private int titleRemainTicks;
   @Nullable
   private Text title;
   @Nullable
   private Text subtitle;
   private int titleFadeInTicks;
   private int titleStayTicks;
   private int titleFadeOutTicks;
   private int lastHealthValue;
   private int renderHealthValue;
   private long lastHealthCheckTime;
   private long heartJumpEndTick;
   private int scaledWidth;
   private int scaledHeight;
   private float autosaveIndicatorAlpha;
   private float lastAutosaveIndicatorAlpha;
   private float spyglassScale;

   public InGameHud(MinecraftClient client, ItemRenderer itemRenderer) {
      this.currentStack = ItemStack.EMPTY;
      this.client = client;
      this.itemRenderer = itemRenderer;
      this.debugHud = new DebugHud(client);
      this.spectatorHud = new SpectatorHud(client);
      this.chatHud = new ChatHud(client);
      this.playerListHud = new PlayerListHud(client, this);
      this.bossBarHud = new BossBarHud(client);
      this.subtitlesHud = new SubtitlesHud(client);
      this.setDefaultTitleFade();
   }

   public void setDefaultTitleFade() {
      this.titleFadeInTicks = 10;
      this.titleStayTicks = 70;
      this.titleFadeOutTicks = 20;
   }

   public void render(MatrixStack matrices, float tickDelta) {
      Window lv = this.client.getWindow();
      this.scaledWidth = lv.getScaledWidth();
      this.scaledHeight = lv.getScaledHeight();
      TextRenderer lv2 = this.getTextRenderer();
      RenderSystem.enableBlend();
      if (MinecraftClient.isFancyGraphicsOrBetter()) {
         this.renderVignetteOverlay(matrices, this.client.getCameraEntity());
      } else {
         RenderSystem.enableDepthTest();
      }

      float g = this.client.getLastFrameDuration();
      this.spyglassScale = MathHelper.lerp(0.5F * g, this.spyglassScale, 1.125F);
      if (this.client.options.getPerspective().isFirstPerson()) {
         if (this.client.player.isUsingSpyglass()) {
            this.renderSpyglassOverlay(matrices, this.spyglassScale);
         } else {
            this.spyglassScale = 0.5F;
            ItemStack lv3 = this.client.player.getInventory().getArmorStack(3);
            if (lv3.isOf(Blocks.CARVED_PUMPKIN.asItem())) {
               this.renderOverlay(matrices, PUMPKIN_BLUR, 1.0F);
            }
         }
      }

      if (this.client.player.getFrozenTicks() > 0) {
         this.renderOverlay(matrices, POWDER_SNOW_OUTLINE, this.client.player.getFreezingScale());
      }

      float h = MathHelper.lerp(tickDelta, this.client.player.lastNauseaStrength, this.client.player.nextNauseaStrength);
      if (h > 0.0F && !this.client.player.hasStatusEffect(StatusEffects.NAUSEA)) {
         this.renderPortalOverlay(matrices, h);
      }

      if (this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
         this.spectatorHud.renderSpectatorMenu(matrices);
      } else if (!this.client.options.hudHidden) {
         this.renderHotbar(tickDelta, matrices);
      }

      if (!this.client.options.hudHidden) {
         RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
         RenderSystem.enableBlend();
         this.renderCrosshair(matrices);
         this.client.getProfiler().push("bossHealth");
         this.bossBarHud.render(matrices);
         this.client.getProfiler().pop();
         RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
         if (this.client.interactionManager.hasStatusBars()) {
            this.renderStatusBars(matrices);
         }

         this.renderMountHealth(matrices);
         RenderSystem.disableBlend();
         int i = this.scaledWidth / 2 - 91;
         JumpingMount lv4 = this.client.player.getJumpingMount();
         if (lv4 != null) {
            this.renderMountJumpBar(lv4, matrices, i);
         } else if (this.client.interactionManager.hasExperienceBar()) {
            this.renderExperienceBar(matrices, i);
         }

         if (this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            this.renderHeldItemTooltip(matrices);
         } else if (this.client.player.isSpectator()) {
            this.spectatorHud.render(matrices);
         }
      }

      int l;
      float j;
      if (this.client.player.getSleepTimer() > 0) {
         this.client.getProfiler().push("sleep");
         RenderSystem.disableDepthTest();
         j = (float)this.client.player.getSleepTimer();
         float k = j / 100.0F;
         if (k > 1.0F) {
            k = 1.0F - (j - 100.0F) / 10.0F;
         }

         l = (int)(220.0F * k) << 24 | 1052704;
         fill(matrices, 0, 0, this.scaledWidth, this.scaledHeight, l);
         RenderSystem.enableDepthTest();
         this.client.getProfiler().pop();
      }

      if (this.client.isDemo()) {
         this.renderDemoTimer(matrices);
      }

      this.renderStatusEffectOverlay(matrices);
      if (this.client.options.debugEnabled) {
         this.debugHud.render(matrices);
      }

      if (!this.client.options.hudHidden) {
         int n;
         int o;
         int m;
         if (this.overlayMessage != null && this.overlayRemaining > 0) {
            this.client.getProfiler().push("overlayMessage");
            j = (float)this.overlayRemaining - tickDelta;
            m = (int)(j * 255.0F / 20.0F);
            if (m > 255) {
               m = 255;
            }

            if (m > 8) {
               matrices.push();
               matrices.translate((float)(this.scaledWidth / 2), (float)(this.scaledHeight - 68), 0.0F);
               l = 16777215;
               if (this.overlayTinted) {
                  l = MathHelper.hsvToRgb(j / 50.0F, 0.7F, 0.6F) & 16777215;
               }

               n = m << 24 & -16777216;
               o = lv2.getWidth((StringVisitable)this.overlayMessage);
               this.drawTextBackground(matrices, lv2, -4, o, 16777215 | n);
               lv2.drawWithShadow(matrices, this.overlayMessage, (float)(-o / 2), -4.0F, l | n);
               matrices.pop();
            }

            this.client.getProfiler().pop();
         }

         if (this.title != null && this.titleRemainTicks > 0) {
            this.client.getProfiler().push("titleAndSubtitle");
            j = (float)this.titleRemainTicks - tickDelta;
            m = 255;
            if (this.titleRemainTicks > this.titleFadeOutTicks + this.titleStayTicks) {
               float p = (float)(this.titleFadeInTicks + this.titleStayTicks + this.titleFadeOutTicks) - j;
               m = (int)(p * 255.0F / (float)this.titleFadeInTicks);
            }

            if (this.titleRemainTicks <= this.titleFadeOutTicks) {
               m = (int)(j * 255.0F / (float)this.titleFadeOutTicks);
            }

            m = MathHelper.clamp(m, 0, 255);
            if (m > 8) {
               matrices.push();
               matrices.translate((float)(this.scaledWidth / 2), (float)(this.scaledHeight / 2), 0.0F);
               RenderSystem.enableBlend();
               matrices.push();
               matrices.scale(4.0F, 4.0F, 4.0F);
               l = m << 24 & -16777216;
               n = lv2.getWidth((StringVisitable)this.title);
               this.drawTextBackground(matrices, lv2, -10, n, 16777215 | l);
               lv2.drawWithShadow(matrices, this.title, (float)(-n / 2), -10.0F, 16777215 | l);
               matrices.pop();
               if (this.subtitle != null) {
                  matrices.push();
                  matrices.scale(2.0F, 2.0F, 2.0F);
                  o = lv2.getWidth((StringVisitable)this.subtitle);
                  this.drawTextBackground(matrices, lv2, 5, o, 16777215 | l);
                  lv2.drawWithShadow(matrices, this.subtitle, (float)(-o / 2), 5.0F, 16777215 | l);
                  matrices.pop();
               }

               RenderSystem.disableBlend();
               matrices.pop();
            }

            this.client.getProfiler().pop();
         }

         this.subtitlesHud.render(matrices);
         Scoreboard lv5 = this.client.world.getScoreboard();
         ScoreboardObjective lv6 = null;
         Team lv7 = lv5.getPlayerTeam(this.client.player.getEntityName());
         if (lv7 != null) {
            n = lv7.getColor().getColorIndex();
            if (n >= 0) {
               lv6 = lv5.getObjectiveForSlot(3 + n);
            }
         }

         ScoreboardObjective lv8 = lv6 != null ? lv6 : lv5.getObjectiveForSlot(1);
         if (lv8 != null) {
            this.renderScoreboardSidebar(matrices, lv8);
         }

         RenderSystem.enableBlend();
         o = MathHelper.floor(this.client.mouse.getX() * (double)lv.getScaledWidth() / (double)lv.getWidth());
         int q = MathHelper.floor(this.client.mouse.getY() * (double)lv.getScaledHeight() / (double)lv.getHeight());
         this.client.getProfiler().push("chat");
         this.chatHud.render(matrices, this.ticks, o, q);
         this.client.getProfiler().pop();
         lv8 = lv5.getObjectiveForSlot(0);
         if (!this.client.options.playerListKey.isPressed() || this.client.isInSingleplayer() && this.client.player.networkHandler.getListedPlayerListEntries().size() <= 1 && lv8 == null) {
            this.playerListHud.setVisible(false);
         } else {
            this.playerListHud.setVisible(true);
            this.playerListHud.render(matrices, this.scaledWidth, lv5, lv8);
         }

         this.renderAutosaveIndicator(matrices);
      }

   }

   private void drawTextBackground(MatrixStack matrices, TextRenderer textRenderer, int yOffset, int width, int color) {
      int l = this.client.options.getTextBackgroundColor(0.0F);
      if (l != 0) {
         int m = -width / 2;
         int var10001 = m - 2;
         int var10002 = yOffset - 2;
         int var10003 = m + width + 2;
         Objects.requireNonNull(textRenderer);
         fill(matrices, var10001, var10002, var10003, yOffset + 9 + 2, ColorHelper.Argb.mixColor(l, color));
      }

   }

   private void renderCrosshair(MatrixStack matrices) {
      GameOptions lv = this.client.options;
      if (lv.getPerspective().isFirstPerson()) {
         if (this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR || this.shouldRenderSpectatorCrosshair(this.client.crosshairTarget)) {
            if (lv.debugEnabled && !lv.hudHidden && !this.client.player.hasReducedDebugInfo() && !(Boolean)lv.getReducedDebugInfo().getValue()) {
               Camera lv2 = this.client.gameRenderer.getCamera();
               MatrixStack lv3 = RenderSystem.getModelViewStack();
               lv3.push();
               lv3.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
               lv3.translate((float)(this.scaledWidth / 2), (float)(this.scaledHeight / 2), 0.0F);
               lv3.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(lv2.getPitch()));
               lv3.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lv2.getYaw()));
               lv3.scale(-1.0F, -1.0F, -1.0F);
               RenderSystem.applyModelViewMatrix();
               RenderSystem.renderCrosshair(10);
               lv3.pop();
               RenderSystem.applyModelViewMatrix();
            } else {
               RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
               int i = true;
               drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15);
               if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.CROSSHAIR) {
                  float f = this.client.player.getAttackCooldownProgress(0.0F);
                  boolean bl = false;
                  if (this.client.targetedEntity != null && this.client.targetedEntity instanceof LivingEntity && f >= 1.0F) {
                     bl = this.client.player.getAttackCooldownProgressPerTick() > 5.0F;
                     bl &= this.client.targetedEntity.isAlive();
                  }

                  int j = this.scaledHeight / 2 - 7 + 16;
                  int k = this.scaledWidth / 2 - 8;
                  if (bl) {
                     drawTexture(matrices, k, j, 68, 94, 16, 16);
                  } else if (f < 1.0F) {
                     int l = (int)(f * 17.0F);
                     drawTexture(matrices, k, j, 36, 94, 16, 4);
                     drawTexture(matrices, k, j, 52, 94, l, 4);
                  }
               }

               RenderSystem.defaultBlendFunc();
            }

         }
      }
   }

   private boolean shouldRenderSpectatorCrosshair(HitResult hitResult) {
      if (hitResult == null) {
         return false;
      } else if (hitResult.getType() == HitResult.Type.ENTITY) {
         return ((EntityHitResult)hitResult).getEntity() instanceof NamedScreenHandlerFactory;
      } else if (hitResult.getType() == HitResult.Type.BLOCK) {
         BlockPos lv = ((BlockHitResult)hitResult).getBlockPos();
         World lv2 = this.client.world;
         return lv2.getBlockState(lv).createScreenHandlerFactory(lv2, lv) != null;
      } else {
         return false;
      }
   }

   protected void renderStatusEffectOverlay(MatrixStack matrices) {
      Collection collection;
      label40: {
         collection = this.client.player.getStatusEffects();
         if (!collection.isEmpty()) {
            Screen var4 = this.client.currentScreen;
            if (!(var4 instanceof AbstractInventoryScreen)) {
               break label40;
            }

            AbstractInventoryScreen lv = (AbstractInventoryScreen)var4;
            if (!lv.hideStatusEffectHud()) {
               break label40;
            }
         }

         return;
      }

      RenderSystem.enableBlend();
      int i = 0;
      int j = 0;
      StatusEffectSpriteManager lv2 = this.client.getStatusEffectSpriteManager();
      List list = Lists.newArrayListWithExpectedSize(collection.size());
      RenderSystem.setShaderTexture(0, HandledScreen.BACKGROUND_TEXTURE);
      Iterator var7 = Ordering.natural().reverse().sortedCopy(collection).iterator();

      while(var7.hasNext()) {
         StatusEffectInstance lv3 = (StatusEffectInstance)var7.next();
         StatusEffect lv4 = lv3.getEffectType();
         if (lv3.shouldShowIcon()) {
            int k = this.scaledWidth;
            int l = 1;
            if (this.client.isDemo()) {
               l += 15;
            }

            if (lv4.isBeneficial()) {
               ++i;
               k -= 25 * i;
            } else {
               ++j;
               k -= 25 * j;
               l += 26;
            }

            float f = 1.0F;
            if (lv3.isAmbient()) {
               drawTexture(matrices, k, l, 165, 166, 24, 24);
            } else {
               drawTexture(matrices, k, l, 141, 166, 24, 24);
               if (lv3.isDurationBelow(200)) {
                  int m = lv3.getDuration();
                  int n = 10 - m / 20;
                  f = MathHelper.clamp((float)m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)m * 3.1415927F / 5.0F) * MathHelper.clamp((float)n / 10.0F * 0.25F, 0.0F, 0.25F);
               }
            }

            Sprite lv5 = lv2.getSprite(lv4);
            list.add(() -> {
               RenderSystem.setShaderTexture(0, lv5.getAtlasId());
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
               drawSprite(matrices, k + 3, l + 3, 0, 18, 18, lv5);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            });
         }
      }

      list.forEach(Runnable::run);
   }

   private void renderHotbar(float tickDelta, MatrixStack matrices) {
      PlayerEntity lv = this.getCameraPlayer();
      if (lv != null) {
         RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
         ItemStack lv2 = lv.getOffHandStack();
         Arm lv3 = lv.getMainArm().getOpposite();
         int i = this.scaledWidth / 2;
         int j = true;
         int k = true;
         matrices.push();
         matrices.translate(0.0F, 0.0F, -90.0F);
         drawTexture(matrices, i - 91, this.scaledHeight - 22, 0, 0, 182, 22);
         drawTexture(matrices, i - 91 - 1 + lv.getInventory().selectedSlot * 20, this.scaledHeight - 22 - 1, 0, 22, 24, 22);
         if (!lv2.isEmpty()) {
            if (lv3 == Arm.LEFT) {
               drawTexture(matrices, i - 91 - 29, this.scaledHeight - 23, 24, 22, 29, 24);
            } else {
               drawTexture(matrices, i + 91, this.scaledHeight - 23, 53, 22, 29, 24);
            }
         }

         matrices.pop();
         int l = 1;

         int m;
         int n;
         int o;
         for(m = 0; m < 9; ++m) {
            n = i - 90 + m * 20 + 2;
            o = this.scaledHeight - 16 - 3;
            this.renderHotbarItem(matrices, n, o, tickDelta, lv, (ItemStack)lv.getInventory().main.get(m), l++);
         }

         if (!lv2.isEmpty()) {
            m = this.scaledHeight - 16 - 3;
            if (lv3 == Arm.LEFT) {
               this.renderHotbarItem(matrices, i - 91 - 26, m, tickDelta, lv, lv2, l++);
            } else {
               this.renderHotbarItem(matrices, i + 91 + 10, m, tickDelta, lv, lv2, l++);
            }
         }

         RenderSystem.enableBlend();
         if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            float g = this.client.player.getAttackCooldownProgress(0.0F);
            if (g < 1.0F) {
               n = this.scaledHeight - 20;
               o = i + 91 + 6;
               if (lv3 == Arm.RIGHT) {
                  o = i - 91 - 22;
               }

               RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
               int p = (int)(g * 19.0F);
               drawTexture(matrices, o, n, 0, 94, 18, 18);
               drawTexture(matrices, o, n + 18 - p, 18, 112 - p, 18, p);
            }
         }

         RenderSystem.disableBlend();
      }
   }

   public void renderMountJumpBar(JumpingMount mount, MatrixStack matrices, int x) {
      this.client.getProfiler().push("jumpBar");
      RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
      float f = this.client.player.getMountJumpStrength();
      int j = true;
      int k = (int)(f * 183.0F);
      int l = this.scaledHeight - 32 + 3;
      drawTexture(matrices, x, l, 0, 84, 182, 5);
      if (mount.getJumpCooldown() > 0) {
         drawTexture(matrices, x, l, 0, 74, 182, 5);
      } else if (k > 0) {
         drawTexture(matrices, x, l, 0, 89, k, 5);
      }

      this.client.getProfiler().pop();
   }

   public void renderExperienceBar(MatrixStack matrices, int x) {
      this.client.getProfiler().push("expBar");
      RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
      int j = this.client.player.getNextLevelExperience();
      int l;
      int m;
      if (j > 0) {
         int k = true;
         l = (int)(this.client.player.experienceProgress * 183.0F);
         m = this.scaledHeight - 32 + 3;
         drawTexture(matrices, x, m, 0, 64, 182, 5);
         if (l > 0) {
            drawTexture(matrices, x, m, 0, 69, l, 5);
         }
      }

      this.client.getProfiler().pop();
      if (this.client.player.experienceLevel > 0) {
         this.client.getProfiler().push("expLevel");
         String string = "" + this.client.player.experienceLevel;
         l = (this.scaledWidth - this.getTextRenderer().getWidth(string)) / 2;
         m = this.scaledHeight - 31 - 4;
         this.getTextRenderer().draw(matrices, (String)string, (float)(l + 1), (float)m, 0);
         this.getTextRenderer().draw(matrices, (String)string, (float)(l - 1), (float)m, 0);
         this.getTextRenderer().draw(matrices, (String)string, (float)l, (float)(m + 1), 0);
         this.getTextRenderer().draw(matrices, (String)string, (float)l, (float)(m - 1), 0);
         this.getTextRenderer().draw(matrices, string, (float)l, (float)m, 8453920);
         this.client.getProfiler().pop();
      }

   }

   public void renderHeldItemTooltip(MatrixStack matrices) {
      this.client.getProfiler().push("selectedItemName");
      if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty()) {
         MutableText lv = Text.empty().append(this.currentStack.getName()).formatted(this.currentStack.getRarity().formatting);
         if (this.currentStack.hasCustomName()) {
            lv.formatted(Formatting.ITALIC);
         }

         int i = this.getTextRenderer().getWidth((StringVisitable)lv);
         int j = (this.scaledWidth - i) / 2;
         int k = this.scaledHeight - 59;
         if (!this.client.interactionManager.hasStatusBars()) {
            k += 14;
         }

         int l = (int)((float)this.heldItemTooltipFade * 256.0F / 10.0F);
         if (l > 255) {
            l = 255;
         }

         if (l > 0) {
            int var10001 = j - 2;
            int var10002 = k - 2;
            int var10003 = j + i + 2;
            Objects.requireNonNull(this.getTextRenderer());
            fill(matrices, var10001, var10002, var10003, k + 9 + 2, this.client.options.getTextBackgroundColor(0));
            this.getTextRenderer().drawWithShadow(matrices, (Text)lv, (float)j, (float)k, 16777215 + (l << 24));
         }
      }

      this.client.getProfiler().pop();
   }

   public void renderDemoTimer(MatrixStack matrices) {
      this.client.getProfiler().push("demo");
      Object lv;
      if (this.client.world.getTime() >= 120500L) {
         lv = DEMO_EXPIRED_MESSAGE;
      } else {
         lv = Text.translatable("demo.remainingTime", StringHelper.formatTicks((int)(120500L - this.client.world.getTime())));
      }

      int i = this.getTextRenderer().getWidth((StringVisitable)lv);
      this.getTextRenderer().drawWithShadow(matrices, (Text)lv, (float)(this.scaledWidth - i - 10), 5.0F, 16777215);
      this.client.getProfiler().pop();
   }

   private void renderScoreboardSidebar(MatrixStack matrices, ScoreboardObjective objective) {
      Scoreboard lv = objective.getScoreboard();
      Collection collection = lv.getAllPlayerScores(objective);
      List list = (List)collection.stream().filter((score) -> {
         return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
      }).collect(Collectors.toList());
      Object collection;
      if (list.size() > 15) {
         collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
      } else {
         collection = list;
      }

      List list2 = Lists.newArrayListWithCapacity(((Collection)collection).size());
      Text lv2 = objective.getDisplayName();
      int i = this.getTextRenderer().getWidth((StringVisitable)lv2);
      int j = i;
      int k = this.getTextRenderer().getWidth(": ");

      ScoreboardPlayerScore lv3;
      MutableText lv5;
      for(Iterator var11 = ((Collection)collection).iterator(); var11.hasNext(); j = Math.max(j, this.getTextRenderer().getWidth((StringVisitable)lv5) + k + this.getTextRenderer().getWidth(Integer.toString(lv3.getScore())))) {
         lv3 = (ScoreboardPlayerScore)var11.next();
         Team lv4 = lv.getPlayerTeam(lv3.getPlayerName());
         lv5 = Team.decorateName(lv4, Text.literal(lv3.getPlayerName()));
         list2.add(Pair.of(lv3, lv5));
      }

      int var10000 = ((Collection)collection).size();
      Objects.requireNonNull(this.getTextRenderer());
      int l = var10000 * 9;
      int m = this.scaledHeight / 2 + l / 3;
      int n = true;
      int o = this.scaledWidth - j - 3;
      int p = 0;
      int q = this.client.options.getTextBackgroundColor(0.3F);
      int r = this.client.options.getTextBackgroundColor(0.4F);
      Iterator var18 = list2.iterator();

      while(var18.hasNext()) {
         Pair pair = (Pair)var18.next();
         ++p;
         ScoreboardPlayerScore lv6 = (ScoreboardPlayerScore)pair.getFirst();
         Text lv7 = (Text)pair.getSecond();
         Formatting var31 = Formatting.RED;
         String string = "" + var31 + lv6.getScore();
         Objects.requireNonNull(this.getTextRenderer());
         int t = m - p * 9;
         int u = this.scaledWidth - 3 + 2;
         int var10001 = o - 2;
         Objects.requireNonNull(this.getTextRenderer());
         fill(matrices, var10001, t, u, t + 9, q);
         this.getTextRenderer().draw(matrices, (Text)lv7, (float)o, (float)t, -1);
         this.getTextRenderer().draw(matrices, (String)string, (float)(u - this.getTextRenderer().getWidth(string)), (float)t, -1);
         if (p == ((Collection)collection).size()) {
            var10001 = o - 2;
            Objects.requireNonNull(this.getTextRenderer());
            fill(matrices, var10001, t - 9 - 1, u, t - 1, r);
            fill(matrices, o - 2, t - 1, u, t, q);
            TextRenderer var32 = this.getTextRenderer();
            float var10003 = (float)(o + j / 2 - i / 2);
            Objects.requireNonNull(this.getTextRenderer());
            var32.draw(matrices, (Text)lv2, var10003, (float)(t - 9), -1);
         }
      }

   }

   private PlayerEntity getCameraPlayer() {
      return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity)this.client.getCameraEntity();
   }

   private LivingEntity getRiddenEntity() {
      PlayerEntity lv = this.getCameraPlayer();
      if (lv != null) {
         Entity lv2 = lv.getVehicle();
         if (lv2 == null) {
            return null;
         }

         if (lv2 instanceof LivingEntity) {
            return (LivingEntity)lv2;
         }
      }

      return null;
   }

   private int getHeartCount(LivingEntity entity) {
      if (entity != null && entity.isLiving()) {
         float f = entity.getMaxHealth();
         int i = (int)(f + 0.5F) / 2;
         if (i > 30) {
            i = 30;
         }

         return i;
      } else {
         return 0;
      }
   }

   private int getHeartRows(int heartCount) {
      return (int)Math.ceil((double)heartCount / 10.0);
   }

   private void renderStatusBars(MatrixStack matrices) {
      PlayerEntity lv = this.getCameraPlayer();
      if (lv != null) {
         int i = MathHelper.ceil(lv.getHealth());
         boolean bl = this.heartJumpEndTick > (long)this.ticks && (this.heartJumpEndTick - (long)this.ticks) / 3L % 2L == 1L;
         long l = Util.getMeasuringTimeMs();
         if (i < this.lastHealthValue && lv.timeUntilRegen > 0) {
            this.lastHealthCheckTime = l;
            this.heartJumpEndTick = (long)(this.ticks + 20);
         } else if (i > this.lastHealthValue && lv.timeUntilRegen > 0) {
            this.lastHealthCheckTime = l;
            this.heartJumpEndTick = (long)(this.ticks + 10);
         }

         if (l - this.lastHealthCheckTime > 1000L) {
            this.lastHealthValue = i;
            this.renderHealthValue = i;
            this.lastHealthCheckTime = l;
         }

         this.lastHealthValue = i;
         int j = this.renderHealthValue;
         this.random.setSeed((long)(this.ticks * 312871));
         HungerManager lv2 = lv.getHungerManager();
         int k = lv2.getFoodLevel();
         int m = this.scaledWidth / 2 - 91;
         int n = this.scaledWidth / 2 + 91;
         int o = this.scaledHeight - 39;
         float f = Math.max((float)lv.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH), (float)Math.max(j, i));
         int p = MathHelper.ceil(lv.getAbsorptionAmount());
         int q = MathHelper.ceil((f + (float)p) / 2.0F / 10.0F);
         int r = Math.max(10 - (q - 2), 3);
         int s = o - (q - 1) * r - 10;
         int t = o - 10;
         int u = lv.getArmor();
         int v = -1;
         if (lv.hasStatusEffect(StatusEffects.REGENERATION)) {
            v = this.ticks % MathHelper.ceil(f + 5.0F);
         }

         this.client.getProfiler().push("armor");

         int x;
         for(int w = 0; w < 10; ++w) {
            if (u > 0) {
               x = m + w * 8;
               if (w * 2 + 1 < u) {
                  drawTexture(matrices, x, s, 34, 9, 9, 9);
               }

               if (w * 2 + 1 == u) {
                  drawTexture(matrices, x, s, 25, 9, 9, 9);
               }

               if (w * 2 + 1 > u) {
                  drawTexture(matrices, x, s, 16, 9, 9, 9);
               }
            }
         }

         this.client.getProfiler().swap("health");
         this.renderHealthBar(matrices, lv, m, o, r, v, f, i, j, p, bl);
         LivingEntity lv3 = this.getRiddenEntity();
         x = this.getHeartCount(lv3);
         int y;
         int z;
         int aa;
         int ab;
         int ac;
         if (x == 0) {
            this.client.getProfiler().swap("food");

            for(y = 0; y < 10; ++y) {
               z = o;
               aa = 16;
               ab = 0;
               if (lv.hasStatusEffect(StatusEffects.HUNGER)) {
                  aa += 36;
                  ab = 13;
               }

               if (lv.getHungerManager().getSaturationLevel() <= 0.0F && this.ticks % (k * 3 + 1) == 0) {
                  z = o + (this.random.nextInt(3) - 1);
               }

               ac = n - y * 8 - 9;
               drawTexture(matrices, ac, z, 16 + ab * 9, 27, 9, 9);
               if (y * 2 + 1 < k) {
                  drawTexture(matrices, ac, z, aa + 36, 27, 9, 9);
               }

               if (y * 2 + 1 == k) {
                  drawTexture(matrices, ac, z, aa + 45, 27, 9, 9);
               }
            }

            t -= 10;
         }

         this.client.getProfiler().swap("air");
         y = lv.getMaxAir();
         z = Math.min(lv.getAir(), y);
         if (lv.isSubmergedIn(FluidTags.WATER) || z < y) {
            aa = this.getHeartRows(x) - 1;
            t -= aa * 10;
            ab = MathHelper.ceil((double)(z - 2) * 10.0 / (double)y);
            ac = MathHelper.ceil((double)z * 10.0 / (double)y) - ab;

            for(int ad = 0; ad < ab + ac; ++ad) {
               if (ad < ab) {
                  drawTexture(matrices, n - ad * 8 - 9, t, 16, 18, 9, 9);
               } else {
                  drawTexture(matrices, n - ad * 8 - 9, t, 25, 18, 9, 9);
               }
            }
         }

         this.client.getProfiler().pop();
      }
   }

   private void renderHealthBar(MatrixStack matrices, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking) {
      HeartType lv = InGameHud.HeartType.fromPlayerState(player);
      int p = 9 * (player.world.getLevelProperties().isHardcore() ? 5 : 0);
      int q = MathHelper.ceil((double)maxHealth / 2.0);
      int r = MathHelper.ceil((double)absorption / 2.0);
      int s = q * 2;

      for(int t = q + r - 1; t >= 0; --t) {
         int u = t / 10;
         int v = t % 10;
         int w = x + v * 8;
         int x = y - u * lines;
         if (lastHealth + absorption <= 4) {
            x += this.random.nextInt(2);
         }

         if (t < q && t == regeneratingHeartIndex) {
            x -= 2;
         }

         this.drawHeart(matrices, InGameHud.HeartType.CONTAINER, w, x, p, blinking, false);
         int y = t * 2;
         boolean bl2 = t >= q;
         if (bl2) {
            int z = y - s;
            if (z < absorption) {
               boolean bl3 = z + 1 == absorption;
               this.drawHeart(matrices, lv == InGameHud.HeartType.WITHERED ? lv : InGameHud.HeartType.ABSORBING, w, x, p, false, bl3);
            }
         }

         boolean bl4;
         if (blinking && y < health) {
            bl4 = y + 1 == health;
            this.drawHeart(matrices, lv, w, x, p, true, bl4);
         }

         if (y < lastHealth) {
            bl4 = y + 1 == lastHealth;
            this.drawHeart(matrices, lv, w, x, p, false, bl4);
         }
      }

   }

   private void drawHeart(MatrixStack matrices, HeartType type, int x, int y, int v, boolean blinking, boolean halfHeart) {
      drawTexture(matrices, x, y, type.getU(halfHeart, blinking), v, 9, 9);
   }

   private void renderMountHealth(MatrixStack matrices) {
      LivingEntity lv = this.getRiddenEntity();
      if (lv != null) {
         int i = this.getHeartCount(lv);
         if (i != 0) {
            int j = (int)Math.ceil((double)lv.getHealth());
            this.client.getProfiler().swap("mountHealth");
            int k = this.scaledHeight - 39;
            int l = this.scaledWidth / 2 + 91;
            int m = k;
            int n = 0;

            for(boolean bl = false; i > 0; n += 20) {
               int o = Math.min(i, 10);
               i -= o;

               for(int p = 0; p < o; ++p) {
                  int q = true;
                  int r = 0;
                  int s = l - p * 8 - 9;
                  drawTexture(matrices, s, m, 52 + r * 9, 9, 9, 9);
                  if (p * 2 + 1 + n < j) {
                     drawTexture(matrices, s, m, 88, 9, 9, 9);
                  }

                  if (p * 2 + 1 + n == j) {
                     drawTexture(matrices, s, m, 97, 9, 9, 9);
                  }
               }

               m -= 10;
            }

         }
      }
   }

   private void renderOverlay(MatrixStack matrices, Identifier texture, float opacity) {
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      RenderSystem.setShaderTexture(0, texture);
      drawTexture(matrices, 0, 0, -90, 0.0F, 0.0F, this.scaledWidth, this.scaledHeight, this.scaledWidth, this.scaledHeight);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderSpyglassOverlay(MatrixStack matrices, float scale) {
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      float g = (float)Math.min(this.scaledWidth, this.scaledHeight);
      float i = Math.min((float)this.scaledWidth / g, (float)this.scaledHeight / g) * scale;
      int j = MathHelper.floor(g * i);
      int k = MathHelper.floor(g * i);
      int l = (this.scaledWidth - j) / 2;
      int m = (this.scaledHeight - k) / 2;
      int n = l + j;
      int o = m + k;
      RenderSystem.setShaderTexture(0, SPYGLASS_SCOPE);
      drawTexture(matrices, l, m, -90, 0.0F, 0.0F, j, k, j, k);
      fill(matrices, 0, o, this.scaledWidth, this.scaledHeight, -90, -16777216);
      fill(matrices, 0, 0, this.scaledWidth, m, -90, -16777216);
      fill(matrices, 0, m, l, o, -90, -16777216);
      fill(matrices, n, m, this.scaledWidth, o, -90, -16777216);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
   }

   private void updateVignetteDarkness(Entity entity) {
      if (entity != null) {
         BlockPos lv = BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ());
         float f = LightmapTextureManager.getBrightness(entity.world.getDimension(), entity.world.getLightLevel(lv));
         float g = MathHelper.clamp(1.0F - f, 0.0F, 1.0F);
         this.vignetteDarkness += (g - this.vignetteDarkness) * 0.01F;
      }
   }

   private void renderVignetteOverlay(MatrixStack matrices, Entity entity) {
      WorldBorder lv = this.client.world.getWorldBorder();
      float f = (float)lv.getDistanceInsideBorder(entity);
      double d = Math.min(lv.getShrinkingSpeed() * (double)lv.getWarningTime() * 1000.0, Math.abs(lv.getSizeLerpTarget() - lv.getSize()));
      double e = Math.max((double)lv.getWarningBlocks(), d);
      if ((double)f < e) {
         f = 1.0F - (float)((double)f / e);
      } else {
         f = 0.0F;
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
      if (f > 0.0F) {
         f = MathHelper.clamp(f, 0.0F, 1.0F);
         RenderSystem.setShaderColor(0.0F, f, f, 1.0F);
      } else {
         float g = this.vignetteDarkness;
         g = MathHelper.clamp(g, 0.0F, 1.0F);
         RenderSystem.setShaderColor(g, g, g, 1.0F);
      }

      RenderSystem.setShaderTexture(0, VIGNETTE_TEXTURE);
      drawTexture(matrices, 0, 0, -90, 0.0F, 0.0F, this.scaledWidth, this.scaledHeight, this.scaledWidth, this.scaledHeight);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
   }

   private void renderPortalOverlay(MatrixStack matrices, float nauseaStrength) {
      if (nauseaStrength < 1.0F) {
         nauseaStrength *= nauseaStrength;
         nauseaStrength *= nauseaStrength;
         nauseaStrength = nauseaStrength * 0.8F + 0.2F;
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, nauseaStrength);
      RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      Sprite lv = this.client.getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
      drawSprite(matrices, 0, 0, -90, this.scaledWidth, this.scaledHeight, lv);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderHotbarItem(MatrixStack arg, int i, int j, float f, PlayerEntity arg2, ItemStack arg3, int k) {
      if (!arg3.isEmpty()) {
         float g = (float)arg3.getBobbingAnimationTime() - f;
         if (g > 0.0F) {
            float h = 1.0F + g / 5.0F;
            arg.push();
            arg.translate((float)(i + 8), (float)(j + 12), 0.0F);
            arg.scale(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
            arg.translate((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);
         }

         this.itemRenderer.renderInGuiWithOverrides(arg, arg2, arg3, i, j, k);
         if (g > 0.0F) {
            arg.pop();
         }

         this.itemRenderer.renderGuiItemOverlay(arg, this.client.textRenderer, arg3, i, j);
      }
   }

   public void tick(boolean paused) {
      this.tickAutosaveIndicator();
      if (!paused) {
         this.tick();
      }

   }

   private void tick() {
      if (this.overlayRemaining > 0) {
         --this.overlayRemaining;
      }

      if (this.titleRemainTicks > 0) {
         --this.titleRemainTicks;
         if (this.titleRemainTicks <= 0) {
            this.title = null;
            this.subtitle = null;
         }
      }

      ++this.ticks;
      Entity lv = this.client.getCameraEntity();
      if (lv != null) {
         this.updateVignetteDarkness(lv);
      }

      if (this.client.player != null) {
         ItemStack lv2 = this.client.player.getInventory().getMainHandStack();
         if (lv2.isEmpty()) {
            this.heldItemTooltipFade = 0;
         } else if (!this.currentStack.isEmpty() && lv2.isOf(this.currentStack.getItem()) && lv2.getName().equals(this.currentStack.getName())) {
            if (this.heldItemTooltipFade > 0) {
               --this.heldItemTooltipFade;
            }
         } else {
            this.heldItemTooltipFade = (int)(40.0 * (Double)this.client.options.getNotificationDisplayTime().getValue());
         }

         this.currentStack = lv2;
      }

      this.chatHud.tickRemovalQueueIfExists();
   }

   private void tickAutosaveIndicator() {
      MinecraftServer minecraftServer = this.client.getServer();
      boolean bl = minecraftServer != null && minecraftServer.isSaving();
      this.lastAutosaveIndicatorAlpha = this.autosaveIndicatorAlpha;
      this.autosaveIndicatorAlpha = MathHelper.lerp(0.2F, this.autosaveIndicatorAlpha, bl ? 1.0F : 0.0F);
   }

   public void setRecordPlayingOverlay(Text description) {
      Text lv = Text.translatable("record.nowPlaying", description);
      this.setOverlayMessage(lv, true);
      this.client.getNarratorManager().narrate((Text)lv);
   }

   public void setOverlayMessage(Text message, boolean tinted) {
      this.setCanShowChatDisabledScreen(false);
      this.overlayMessage = message;
      this.overlayRemaining = 60;
      this.overlayTinted = tinted;
   }

   public void setCanShowChatDisabledScreen(boolean canShowChatDisabledScreen) {
      this.canShowChatDisabledScreen = canShowChatDisabledScreen;
   }

   public boolean shouldShowChatDisabledScreen() {
      return this.canShowChatDisabledScreen && this.overlayRemaining > 0;
   }

   public void setTitleTicks(int fadeInTicks, int stayTicks, int fadeOutTicks) {
      if (fadeInTicks >= 0) {
         this.titleFadeInTicks = fadeInTicks;
      }

      if (stayTicks >= 0) {
         this.titleStayTicks = stayTicks;
      }

      if (fadeOutTicks >= 0) {
         this.titleFadeOutTicks = fadeOutTicks;
      }

      if (this.titleRemainTicks > 0) {
         this.titleRemainTicks = this.titleFadeInTicks + this.titleStayTicks + this.titleFadeOutTicks;
      }

   }

   public void setSubtitle(Text subtitle) {
      this.subtitle = subtitle;
   }

   public void setTitle(Text title) {
      this.title = title;
      this.titleRemainTicks = this.titleFadeInTicks + this.titleStayTicks + this.titleFadeOutTicks;
   }

   public void clearTitle() {
      this.title = null;
      this.subtitle = null;
      this.titleRemainTicks = 0;
   }

   public ChatHud getChatHud() {
      return this.chatHud;
   }

   public int getTicks() {
      return this.ticks;
   }

   public TextRenderer getTextRenderer() {
      return this.client.textRenderer;
   }

   public SpectatorHud getSpectatorHud() {
      return this.spectatorHud;
   }

   public PlayerListHud getPlayerListHud() {
      return this.playerListHud;
   }

   public void clear() {
      this.playerListHud.clear();
      this.bossBarHud.clear();
      this.client.getToastManager().clear();
      this.client.options.debugEnabled = false;
      this.chatHud.clear(true);
   }

   public BossBarHud getBossBarHud() {
      return this.bossBarHud;
   }

   public void resetDebugHudChunk() {
      this.debugHud.resetChunk();
   }

   private void renderAutosaveIndicator(MatrixStack matrices) {
      if ((Boolean)this.client.options.getShowAutosaveIndicator().getValue() && (this.autosaveIndicatorAlpha > 0.0F || this.lastAutosaveIndicatorAlpha > 0.0F)) {
         int i = MathHelper.floor(255.0F * MathHelper.clamp(MathHelper.lerp(this.client.getTickDelta(), this.lastAutosaveIndicatorAlpha, this.autosaveIndicatorAlpha), 0.0F, 1.0F));
         if (i > 8) {
            TextRenderer lv = this.getTextRenderer();
            int j = lv.getWidth((StringVisitable)SAVING_LEVEL_TEXT);
            int k = 16777215 | i << 24 & -16777216;
            lv.drawWithShadow(matrices, SAVING_LEVEL_TEXT, (float)(this.scaledWidth - j - 10), (float)(this.scaledHeight - 15), k);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   static enum HeartType {
      CONTAINER(0, false),
      NORMAL(2, true),
      POISONED(4, true),
      WITHERED(6, true),
      ABSORBING(8, false),
      FROZEN(9, false);

      private final int textureIndex;
      private final boolean hasBlinkingTexture;

      private HeartType(int textureIndex, boolean hasBlinkingTexture) {
         this.textureIndex = textureIndex;
         this.hasBlinkingTexture = hasBlinkingTexture;
      }

      public int getU(boolean halfHeart, boolean blinking) {
         int i;
         if (this == CONTAINER) {
            i = blinking ? 1 : 0;
         } else {
            int j = halfHeart ? 1 : 0;
            int k = this.hasBlinkingTexture && blinking ? 2 : 0;
            i = j + k;
         }

         return 16 + (this.textureIndex * 2 + i) * 9;
      }

      static HeartType fromPlayerState(PlayerEntity player) {
         HeartType lv;
         if (player.hasStatusEffect(StatusEffects.POISON)) {
            lv = POISONED;
         } else if (player.hasStatusEffect(StatusEffects.WITHER)) {
            lv = WITHERED;
         } else if (player.isFrozen()) {
            lv = FROZEN;
         } else {
            lv = NORMAL;
         }

         return lv;
      }

      // $FF: synthetic method
      private static HeartType[] method_37300() {
         return new HeartType[]{CONTAINER, NORMAL, POISONED, WITHERED, ABSORBING, FROZEN};
      }
   }
}
