package net.minecraft.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public class SignText {
   private static final Codec MESSAGES_CODEC;
   public static final Codec CODEC;
   public static final int field_43299 = 4;
   private final Text[] messages;
   private final Text[] filteredMessages;
   private final DyeColor color;
   private final boolean glowing;
   @Nullable
   private OrderedText[] orderedMessages;
   private boolean filtered;

   public SignText() {
      this(getDefaultText(), getDefaultText(), DyeColor.BLACK, false);
   }

   public SignText(Text[] messages, Text[] filteredMessages, DyeColor color, boolean glowing) {
      this.messages = messages;
      this.filteredMessages = filteredMessages;
      this.color = color;
      this.glowing = glowing;
   }

   private static Text[] getDefaultText() {
      return new Text[]{ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY};
   }

   private static SignText create(Text[] messages, Optional filteredMessages, DyeColor color, boolean glowing) {
      Text[] lvs = (Text[])filteredMessages.orElseGet(SignText::getDefaultText);
      copyMessages(messages, lvs);
      return new SignText(messages, lvs, color, glowing);
   }

   private static void copyMessages(Text[] from, Text[] to) {
      for(int i = 0; i < 4; ++i) {
         if (to[i].equals(ScreenTexts.EMPTY)) {
            to[i] = from[i];
         }
      }

   }

   public boolean isGlowing() {
      return this.glowing;
   }

   public SignText withGlowing(boolean glowing) {
      return glowing == this.glowing ? this : new SignText(this.messages, this.filteredMessages, this.color, glowing);
   }

   public DyeColor getColor() {
      return this.color;
   }

   public SignText withColor(DyeColor color) {
      return color == this.getColor() ? this : new SignText(this.messages, this.filteredMessages, color, this.glowing);
   }

   public Text getMessage(int line, boolean filtered) {
      return this.getMessages(filtered)[line];
   }

   public SignText withMessage(int line, Text message) {
      return this.withMessage(line, message, message);
   }

   public SignText withMessage(int line, Text message, Text filteredMessage) {
      Text[] lvs = (Text[])Arrays.copyOf(this.messages, this.messages.length);
      Text[] lvs2 = (Text[])Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);
      lvs[line] = message;
      lvs2[line] = filteredMessage;
      return new SignText(lvs, lvs2, this.color, this.glowing);
   }

   public boolean hasText(PlayerEntity player) {
      return Arrays.stream(this.getMessages(player.shouldFilterText())).anyMatch((text) -> {
         return !text.getString().isEmpty();
      });
   }

   public Text[] getMessages(boolean filtered) {
      return filtered ? this.filteredMessages : this.messages;
   }

   public OrderedText[] getOrderedMessages(boolean filtered, Function messageOrderer) {
      if (this.orderedMessages == null || this.filtered != filtered) {
         this.filtered = filtered;
         this.orderedMessages = new OrderedText[4];

         for(int i = 0; i < 4; ++i) {
            this.orderedMessages[i] = (OrderedText)messageOrderer.apply(this.getMessage(i, filtered));
         }
      }

      return this.orderedMessages;
   }

   private Optional getFilteredMessages() {
      Text[] lvs = new Text[4];
      boolean bl = false;

      for(int i = 0; i < 4; ++i) {
         Text lv = this.filteredMessages[i];
         if (!lv.equals(this.messages[i])) {
            lvs[i] = lv;
            bl = true;
         } else {
            lvs[i] = ScreenTexts.EMPTY;
         }
      }

      return bl ? Optional.of(lvs) : Optional.empty();
   }

   public boolean hasRunCommandClickEvent(PlayerEntity player) {
      Text[] var2 = this.getMessages(player.shouldFilterText());
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Text lv = var2[var4];
         Style lv2 = lv.getStyle();
         ClickEvent lv3 = lv2.getClickEvent();
         if (lv3 != null && lv3.getAction() == ClickEvent.Action.RUN_COMMAND) {
            return true;
         }
      }

      return false;
   }

   static {
      MESSAGES_CODEC = Codecs.STRINGIFIED_TEXT.listOf().comapFlatMap((messages) -> {
         return Util.toArray((List)messages, 4).map((list) -> {
            return new Text[]{(Text)list.get(0), (Text)list.get(1), (Text)list.get(2), (Text)list.get(3)};
         });
      }, (messages) -> {
         return List.of(messages[0], messages[1], messages[2], messages[3]);
      });
      CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(MESSAGES_CODEC.fieldOf("messages").forGetter((signText) -> {
            return signText.messages;
         }), MESSAGES_CODEC.optionalFieldOf("filtered_messages").forGetter(SignText::getFilteredMessages), DyeColor.CODEC.fieldOf("color").orElse(DyeColor.BLACK).forGetter((signText) -> {
            return signText.color;
         }), Codec.BOOL.fieldOf("has_glowing_text").orElse(false).forGetter((signText) -> {
            return signText.glowing;
         })).apply(instance, SignText::create);
      });
   }
}
