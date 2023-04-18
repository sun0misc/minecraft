package net.minecraft.client.util;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

@Environment(EnvType.CLIENT)
public class ChatMessages {
   private static final OrderedText SPACES;

   private static String getRenderedChatMessage(String message) {
      return (Boolean)MinecraftClient.getInstance().options.getChatColors().getValue() ? message : Formatting.strip(message);
   }

   public static List breakRenderedChatMessageLines(StringVisitable message, int width, TextRenderer textRenderer) {
      TextCollector lv = new TextCollector();
      message.visit((style, messagex) -> {
         lv.add(StringVisitable.styled(getRenderedChatMessage(messagex), style));
         return Optional.empty();
      }, Style.EMPTY);
      List list = Lists.newArrayList();
      textRenderer.getTextHandler().wrapLines(lv.getCombined(), width, Style.EMPTY, (text, lastLineWrapped) -> {
         OrderedText lv = Language.getInstance().reorder(text);
         list.add(lastLineWrapped ? OrderedText.concat(SPACES, lv) : lv);
      });
      return list.isEmpty() ? Lists.newArrayList(new OrderedText[]{OrderedText.EMPTY}) : list;
   }

   static {
      SPACES = OrderedText.styled(32, Style.EMPTY);
   }
}
