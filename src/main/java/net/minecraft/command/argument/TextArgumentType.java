package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.text.Text;

public class TextArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
   public static final DynamicCommandExceptionType INVALID_COMPONENT_EXCEPTION = new DynamicCommandExceptionType((text) -> {
      return Text.translatable("argument.component.invalid", text);
   });

   private TextArgumentType() {
   }

   public static Text getTextArgument(CommandContext context, String name) {
      return (Text)context.getArgument(name, Text.class);
   }

   public static TextArgumentType text() {
      return new TextArgumentType();
   }

   public Text parse(StringReader stringReader) throws CommandSyntaxException {
      try {
         Text lv = Text.Serializer.fromJson(stringReader);
         if (lv == null) {
            throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, "empty");
         } else {
            return lv;
         }
      } catch (Exception var4) {
         String string = var4.getCause() != null ? var4.getCause().getMessage() : var4.getMessage();
         throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, string);
      }
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
