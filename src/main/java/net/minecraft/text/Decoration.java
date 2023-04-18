package net.minecraft.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.message.MessageType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public record Decoration(String translationKey, List parameters, Style style) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.STRING.fieldOf("translation_key").forGetter(Decoration::translationKey), Decoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(Decoration::parameters), Style.CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(Decoration::style)).apply(instance, Decoration::new);
   });

   public Decoration(String string, List list, Style arg) {
      this.translationKey = string;
      this.parameters = list;
      this.style = arg;
   }

   public static Decoration ofChat(String translationKey) {
      return new Decoration(translationKey, List.of(Decoration.Parameter.SENDER, Decoration.Parameter.CONTENT), Style.EMPTY);
   }

   public static Decoration ofIncomingMessage(String translationKey) {
      Style lv = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
      return new Decoration(translationKey, List.of(Decoration.Parameter.SENDER, Decoration.Parameter.CONTENT), lv);
   }

   public static Decoration ofOutgoingMessage(String translationKey) {
      Style lv = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);
      return new Decoration(translationKey, List.of(Decoration.Parameter.TARGET, Decoration.Parameter.CONTENT), lv);
   }

   public static Decoration ofTeamMessage(String translationKey) {
      return new Decoration(translationKey, List.of(Decoration.Parameter.TARGET, Decoration.Parameter.SENDER, Decoration.Parameter.CONTENT), Style.EMPTY);
   }

   public Text apply(Text content, MessageType.Parameters params) {
      Object[] objects = this.collectArguments(content, params);
      return Text.translatable(this.translationKey, objects).fillStyle(this.style);
   }

   private Text[] collectArguments(Text content, MessageType.Parameters params) {
      Text[] lvs = new Text[this.parameters.size()];

      for(int i = 0; i < lvs.length; ++i) {
         Parameter lv = (Parameter)this.parameters.get(i);
         lvs[i] = lv.apply(content, params);
      }

      return lvs;
   }

   public String translationKey() {
      return this.translationKey;
   }

   public List parameters() {
      return this.parameters;
   }

   public Style style() {
      return this.style;
   }

   public static enum Parameter implements StringIdentifiable {
      SENDER("sender", (content, params) -> {
         return params.name();
      }),
      TARGET("target", (content, params) -> {
         return params.targetName();
      }),
      CONTENT("content", (content, params) -> {
         return content;
      });

      public static final Codec CODEC = StringIdentifiable.createCodec(Parameter::values);
      private final String name;
      private final Selector selector;

      private Parameter(String name, Selector selector) {
         this.name = name;
         this.selector = selector;
      }

      public Text apply(Text content, MessageType.Parameters params) {
         Text lv = this.selector.select(content, params);
         return (Text)Objects.requireNonNullElse(lv, ScreenTexts.EMPTY);
      }

      public String asString() {
         return this.name;
      }

      // $FF: synthetic method
      private static Parameter[] method_43836() {
         return new Parameter[]{SENDER, TARGET, CONTENT};
      }

      public interface Selector {
         @Nullable
         Text select(Text content, MessageType.Parameters params);
      }
   }
}
