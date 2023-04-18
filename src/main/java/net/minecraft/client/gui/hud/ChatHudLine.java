package net.minecraft.client.gui.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ChatHudLine(int creationTick, Text content, @Nullable MessageSignatureData signature, @Nullable MessageIndicator indicator) {
   public ChatHudLine(int creationTick, Text arg, @Nullable MessageSignatureData arg2, @Nullable MessageIndicator arg3) {
      this.creationTick = creationTick;
      this.content = arg;
      this.signature = arg2;
      this.indicator = arg3;
   }

   public int creationTick() {
      return this.creationTick;
   }

   public Text content() {
      return this.content;
   }

   @Nullable
   public MessageSignatureData signature() {
      return this.signature;
   }

   @Nullable
   public MessageIndicator indicator() {
      return this.indicator;
   }

   @Environment(EnvType.CLIENT)
   public static record Visible(int addedTime, OrderedText content, @Nullable MessageIndicator indicator, boolean endOfEntry) {
      public Visible(int i, OrderedText arg, @Nullable MessageIndicator arg2, boolean bl) {
         this.addedTime = i;
         this.content = arg;
         this.indicator = arg2;
         this.endOfEntry = bl;
      }

      public int addedTime() {
         return this.addedTime;
      }

      public OrderedText content() {
         return this.content;
      }

      @Nullable
      public MessageIndicator indicator() {
         return this.indicator;
      }

      public boolean endOfEntry() {
         return this.endOfEntry;
      }
   }
}
