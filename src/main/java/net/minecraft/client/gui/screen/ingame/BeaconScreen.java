package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BeaconScreen extends HandledScreen {
   static final Identifier TEXTURE = new Identifier("textures/gui/container/beacon.png");
   private static final Text PRIMARY_POWER_TEXT = Text.translatable("block.minecraft.beacon.primary");
   private static final Text SECONDARY_POWER_TEXT = Text.translatable("block.minecraft.beacon.secondary");
   private final List buttons = Lists.newArrayList();
   @Nullable
   StatusEffect primaryEffect;
   @Nullable
   StatusEffect secondaryEffect;

   public BeaconScreen(final BeaconScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 230;
      this.backgroundHeight = 219;
      handler.addListener(new ScreenHandlerListener() {
         public void onSlotUpdate(ScreenHandler handlerx, int slotId, ItemStack stack) {
         }

         public void onPropertyUpdate(ScreenHandler handlerx, int property, int value) {
            BeaconScreen.this.primaryEffect = handler.getPrimaryEffect();
            BeaconScreen.this.secondaryEffect = handler.getSecondaryEffect();
         }
      });
   }

   private void addButton(ClickableWidget button) {
      this.addDrawableChild(button);
      this.buttons.add((BeaconButtonWidget)button);
   }

   protected void init() {
      super.init();
      this.buttons.clear();
      this.addButton(new DoneButtonWidget(this.x + 164, this.y + 107));
      this.addButton(new CancelButtonWidget(this.x + 190, this.y + 107));

      int j;
      int k;
      int l;
      StatusEffect lv;
      EffectButtonWidget lv2;
      for(int i = 0; i <= 2; ++i) {
         j = BeaconBlockEntity.EFFECTS_BY_LEVEL[i].length;
         k = j * 22 + (j - 1) * 2;

         for(l = 0; l < j; ++l) {
            lv = BeaconBlockEntity.EFFECTS_BY_LEVEL[i][l];
            lv2 = new EffectButtonWidget(this.x + 76 + l * 24 - k / 2, this.y + 22 + i * 25, lv, true, i);
            lv2.active = false;
            this.addButton(lv2);
         }
      }

      int i = true;
      j = BeaconBlockEntity.EFFECTS_BY_LEVEL[3].length + 1;
      k = j * 22 + (j - 1) * 2;

      for(l = 0; l < j - 1; ++l) {
         lv = BeaconBlockEntity.EFFECTS_BY_LEVEL[3][l];
         lv2 = new EffectButtonWidget(this.x + 167 + l * 24 - k / 2, this.y + 47, lv, false, 3);
         lv2.active = false;
         this.addButton(lv2);
      }

      EffectButtonWidget lv3 = new LevelTwoEffectButtonWidget(this.x + 167 + (j - 1) * 24 - k / 2, this.y + 47, BeaconBlockEntity.EFFECTS_BY_LEVEL[0][0]);
      lv3.visible = false;
      this.addButton(lv3);
   }

   public void handledScreenTick() {
      super.handledScreenTick();
      this.tickButtons();
   }

   void tickButtons() {
      int i = ((BeaconScreenHandler)this.handler).getProperties();
      this.buttons.forEach((button) -> {
         button.tick(i);
      });
   }

   protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
      drawCenteredTextWithShadow(matrices, this.textRenderer, PRIMARY_POWER_TEXT, 62, 10, 14737632);
      drawCenteredTextWithShadow(matrices, this.textRenderer, SECONDARY_POWER_TEXT, 169, 10, 14737632);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      RenderSystem.setShaderTexture(0, TEXTURE);
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      drawTexture(matrices, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      matrices.push();
      matrices.translate(0.0F, 0.0F, 100.0F);
      this.itemRenderer.renderInGuiWithOverrides(matrices, new ItemStack(Items.NETHERITE_INGOT), k + 20, l + 109);
      this.itemRenderer.renderInGuiWithOverrides(matrices, new ItemStack(Items.EMERALD), k + 41, l + 109);
      this.itemRenderer.renderInGuiWithOverrides(matrices, new ItemStack(Items.DIAMOND), k + 41 + 22, l + 109);
      this.itemRenderer.renderInGuiWithOverrides(matrices, new ItemStack(Items.GOLD_INGOT), k + 42 + 44, l + 109);
      this.itemRenderer.renderInGuiWithOverrides(matrices, new ItemStack(Items.IRON_INGOT), k + 42 + 66, l + 109);
      matrices.pop();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   @Environment(EnvType.CLIENT)
   private interface BeaconButtonWidget {
      void tick(int level);
   }

   @Environment(EnvType.CLIENT)
   private class DoneButtonWidget extends IconButtonWidget {
      public DoneButtonWidget(int x, int y) {
         super(x, y, 90, 220, ScreenTexts.DONE);
      }

      public void onPress() {
         BeaconScreen.this.client.getNetworkHandler().sendPacket(new UpdateBeaconC2SPacket(Optional.ofNullable(BeaconScreen.this.primaryEffect), Optional.ofNullable(BeaconScreen.this.secondaryEffect)));
         BeaconScreen.this.client.player.closeHandledScreen();
      }

      public void tick(int level) {
         this.active = ((BeaconScreenHandler)BeaconScreen.this.handler).hasPayment() && BeaconScreen.this.primaryEffect != null;
      }
   }

   @Environment(EnvType.CLIENT)
   private class CancelButtonWidget extends IconButtonWidget {
      public CancelButtonWidget(int x, int y) {
         super(x, y, 112, 220, ScreenTexts.CANCEL);
      }

      public void onPress() {
         BeaconScreen.this.client.player.closeHandledScreen();
      }

      public void tick(int level) {
      }
   }

   @Environment(EnvType.CLIENT)
   private class EffectButtonWidget extends BaseButtonWidget {
      private final boolean primary;
      protected final int level;
      private StatusEffect effect;
      private Sprite sprite;

      public EffectButtonWidget(int x, int y, StatusEffect statusEffect, boolean primary, int level) {
         super(x, y);
         this.primary = primary;
         this.level = level;
         this.init(statusEffect);
      }

      protected void init(StatusEffect statusEffect) {
         this.effect = statusEffect;
         this.sprite = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(statusEffect);
         this.setTooltip(Tooltip.of(this.getEffectName(statusEffect), (Text)null));
      }

      protected MutableText getEffectName(StatusEffect statusEffect) {
         return Text.translatable(statusEffect.getTranslationKey());
      }

      public void onPress() {
         if (!this.isDisabled()) {
            if (this.primary) {
               BeaconScreen.this.primaryEffect = this.effect;
            } else {
               BeaconScreen.this.secondaryEffect = this.effect;
            }

            BeaconScreen.this.tickButtons();
         }
      }

      protected void renderExtra(MatrixStack matrices) {
         RenderSystem.setShaderTexture(0, this.sprite.getAtlasId());
         drawSprite(matrices, this.getX() + 2, this.getY() + 2, 0, 18, 18, this.sprite);
      }

      public void tick(int level) {
         this.active = this.level < level;
         this.setDisabled(this.effect == (this.primary ? BeaconScreen.this.primaryEffect : BeaconScreen.this.secondaryEffect));
      }

      protected MutableText getNarrationMessage() {
         return this.getEffectName(this.effect);
      }
   }

   @Environment(EnvType.CLIENT)
   class LevelTwoEffectButtonWidget extends EffectButtonWidget {
      public LevelTwoEffectButtonWidget(int x, int y, StatusEffect statusEffect) {
         super(x, y, statusEffect, false, 3);
      }

      protected MutableText getEffectName(StatusEffect statusEffect) {
         return Text.translatable(statusEffect.getTranslationKey()).append(" II");
      }

      public void tick(int level) {
         if (BeaconScreen.this.primaryEffect != null) {
            this.visible = true;
            this.init(BeaconScreen.this.primaryEffect);
            super.tick(level);
         } else {
            this.visible = false;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private abstract static class IconButtonWidget extends BaseButtonWidget {
      private final int u;
      private final int v;

      protected IconButtonWidget(int i, int j, int k, int l, Text arg) {
         super(i, j, arg);
         this.u = k;
         this.v = l;
      }

      protected void renderExtra(MatrixStack matrices) {
         drawTexture(matrices, this.getX() + 2, this.getY() + 2, this.u, this.v, 18, 18);
      }
   }

   @Environment(EnvType.CLIENT)
   private abstract static class BaseButtonWidget extends PressableWidget implements BeaconButtonWidget {
      private boolean disabled;

      protected BaseButtonWidget(int x, int y) {
         super(x, y, 22, 22, ScreenTexts.EMPTY);
      }

      protected BaseButtonWidget(int x, int y, Text message) {
         super(x, y, 22, 22, message);
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RenderSystem.setShaderTexture(0, BeaconScreen.TEXTURE);
         int k = true;
         int l = 0;
         if (!this.active) {
            l += this.width * 2;
         } else if (this.disabled) {
            l += this.width * 1;
         } else if (this.isSelected()) {
            l += this.width * 3;
         }

         drawTexture(matrices, this.getX(), this.getY(), l, 219, this.width, this.height);
         this.renderExtra(matrices);
      }

      protected abstract void renderExtra(MatrixStack matrices);

      public boolean isDisabled() {
         return this.disabled;
      }

      public void setDisabled(boolean disabled) {
         this.disabled = disabled;
      }

      public void appendClickableNarrations(NarrationMessageBuilder builder) {
         this.appendDefaultNarrations(builder);
      }
   }
}
