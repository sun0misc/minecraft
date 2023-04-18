package net.minecraft.text;

import java.util.Optional;

public record LiteralTextContent(String string) implements TextContent {
   public LiteralTextContent(String string) {
      this.string = string;
   }

   public Optional visit(StringVisitable.Visitor visitor) {
      return visitor.accept(this.string);
   }

   public Optional visit(StringVisitable.StyledVisitor visitor, Style style) {
      return visitor.accept(style, this.string);
   }

   public String toString() {
      return "literal{" + this.string + "}";
   }

   public String string() {
      return this.string;
   }
}
