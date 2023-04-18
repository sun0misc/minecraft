package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

public interface TextContent {
   TextContent EMPTY = new TextContent() {
      public String toString() {
         return "empty";
      }
   };

   default Optional visit(StringVisitable.StyledVisitor visitor, Style style) {
      return Optional.empty();
   }

   default Optional visit(StringVisitable.Visitor visitor) {
      return Optional.empty();
   }

   default MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
      return MutableText.of(this);
   }
}
