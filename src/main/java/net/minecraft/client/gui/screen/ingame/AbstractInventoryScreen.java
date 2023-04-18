package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public abstract class AbstractInventoryScreen extends HandledScreen {
   public AbstractInventoryScreen(ScreenHandler arg, PlayerInventory arg2, Text arg3) {
      super(arg, arg2, arg3);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      super.render(matrices, mouseX, mouseY, delta);
      this.drawStatusEffects(matrices, mouseX, mouseY);
   }

   public boolean hideStatusEffectHud() {
      int i = this.x + this.backgroundWidth + 2;
      int j = this.width - i;
      return j >= 32;
   }

   private void drawStatusEffects(MatrixStack matrices, int mouseX, int mouseY) {
      int k = this.x + this.backgroundWidth + 2;
      int l = this.width - k;
      Collection collection = this.client.player.getStatusEffects();
      if (!collection.isEmpty() && l >= 32) {
         boolean bl = l >= 120;
         int m = 33;
         if (collection.size() > 5) {
            m = 132 / (collection.size() - 1);
         }

         Iterable iterable = Ordering.natural().sortedCopy(collection);
         this.drawStatusEffectBackgrounds(matrices, k, m, iterable, bl);
         this.drawStatusEffectSprites(matrices, k, m, iterable, bl);
         if (bl) {
            this.drawStatusEffectDescriptions(matrices, k, m, iterable);
         } else if (mouseX >= k && mouseX <= k + 33) {
            int n = this.y;
            StatusEffectInstance lv = null;

            for(Iterator var12 = iterable.iterator(); var12.hasNext(); n += m) {
               StatusEffectInstance lv2 = (StatusEffectInstance)var12.next();
               if (mouseY >= n && mouseY <= n + m) {
                  lv = lv2;
               }
            }

            if (lv != null) {
               List list = List.of(this.getStatusEffectDescription(lv), StatusEffectUtil.durationToString(lv, 1.0F));
               this.renderTooltip(matrices, list, Optional.empty(), mouseX, mouseY);
            }
         }

      }
   }

   private void drawStatusEffectBackgrounds(MatrixStack matrices, int x, int height, Iterable statusEffects, boolean wide) {
      RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
      int k = this.y;

      for(Iterator var7 = statusEffects.iterator(); var7.hasNext(); k += height) {
         StatusEffectInstance lv = (StatusEffectInstance)var7.next();
         if (wide) {
            drawTexture(matrices, x, k, 0, 166, 120, 32);
         } else {
            drawTexture(matrices, x, k, 0, 198, 32, 32);
         }
      }

   }

   private void drawStatusEffectSprites(MatrixStack matrices, int x, int height, Iterable statusEffects, boolean wide) {
      StatusEffectSpriteManager lv = this.client.getStatusEffectSpriteManager();
      int k = this.y;

      for(Iterator var8 = statusEffects.iterator(); var8.hasNext(); k += height) {
         StatusEffectInstance lv2 = (StatusEffectInstance)var8.next();
         StatusEffect lv3 = lv2.getEffectType();
         Sprite lv4 = lv.getSprite(lv3);
         RenderSystem.setShaderTexture(0, lv4.getAtlasId());
         drawSprite(matrices, x + (wide ? 6 : 7), k + 7, 0, 18, 18, lv4);
      }

   }

   private void drawStatusEffectDescriptions(MatrixStack matrices, int x, int height, Iterable statusEffects) {
      int k = this.y;

      for(Iterator var6 = statusEffects.iterator(); var6.hasNext(); k += height) {
         StatusEffectInstance lv = (StatusEffectInstance)var6.next();
         Text lv2 = this.getStatusEffectDescription(lv);
         this.textRenderer.drawWithShadow(matrices, lv2, (float)(x + 10 + 18), (float)(k + 6), 16777215);
         Text lv3 = StatusEffectUtil.durationToString(lv, 1.0F);
         this.textRenderer.drawWithShadow(matrices, lv3, (float)(x + 10 + 18), (float)(k + 6 + 10), 8355711);
      }

   }

   private Text getStatusEffectDescription(StatusEffectInstance statusEffect) {
      MutableText lv = statusEffect.getEffectType().getName().copy();
      if (statusEffect.getAmplifier() >= 1 && statusEffect.getAmplifier() <= 9) {
         MutableText var10000 = lv.append(ScreenTexts.SPACE);
         int var10001 = statusEffect.getAmplifier();
         var10000.append((Text)Text.translatable("enchantment.level." + (var10001 + 1)));
      }

      return lv;
   }
}
