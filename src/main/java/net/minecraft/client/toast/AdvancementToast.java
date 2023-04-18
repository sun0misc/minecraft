package net.minecraft.client.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class AdvancementToast implements Toast {
   public static final int field_41812 = 5000;
   private final Advancement advancement;
   private boolean soundPlayed;

   public AdvancementToast(Advancement advancement) {
      this.advancement = advancement;
   }

   public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
      RenderSystem.setShaderTexture(0, TEXTURE);
      AdvancementDisplay lv = this.advancement.getDisplay();
      DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());
      if (lv != null) {
         List list = manager.getClient().textRenderer.wrapLines(lv.getTitle(), 125);
         int i = lv.getFrame() == AdvancementFrame.CHALLENGE ? 16746751 : 16776960;
         if (list.size() == 1) {
            manager.getClient().textRenderer.draw(matrices, lv.getFrame().getToastText(), 30.0F, 7.0F, i | -16777216);
            manager.getClient().textRenderer.draw(matrices, (OrderedText)((OrderedText)list.get(0)), 30.0F, 18.0F, -1);
         } else {
            int j = true;
            float f = 300.0F;
            int k;
            if (startTime < 1500L) {
               k = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
               manager.getClient().textRenderer.draw(matrices, lv.getFrame().getToastText(), 30.0F, 11.0F, i | k);
            } else {
               k = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
               int var10000 = this.getHeight() / 2;
               int var10001 = list.size();
               Objects.requireNonNull(manager.getClient().textRenderer);
               int m = var10000 - var10001 * 9 / 2;

               for(Iterator var12 = list.iterator(); var12.hasNext(); m += 9) {
                  OrderedText lv2 = (OrderedText)var12.next();
                  manager.getClient().textRenderer.draw(matrices, lv2, 30.0F, (float)m, 16777215 | k);
                  Objects.requireNonNull(manager.getClient().textRenderer);
               }
            }
         }

         if (!this.soundPlayed && startTime > 0L) {
            this.soundPlayed = true;
            if (lv.getFrame() == AdvancementFrame.CHALLENGE) {
               manager.getClient().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
         }

         manager.getClient().getItemRenderer().renderInGui(matrices, lv.getIcon(), 8, 8);
         return (double)startTime >= 5000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
      } else {
         return Toast.Visibility.HIDE;
      }
   }
}
