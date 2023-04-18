package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SelectorTextContent implements TextContent {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String pattern;
   @Nullable
   private final EntitySelector selector;
   protected final Optional separator;

   public SelectorTextContent(String pattern, Optional separator) {
      this.pattern = pattern;
      this.separator = separator;
      this.selector = readSelector(pattern);
   }

   @Nullable
   private static EntitySelector readSelector(String pattern) {
      EntitySelector lv = null;

      try {
         EntitySelectorReader lv2 = new EntitySelectorReader(new StringReader(pattern));
         lv = lv2.read();
      } catch (CommandSyntaxException var3) {
         LOGGER.warn("Invalid selector component: {}: {}", pattern, var3.getMessage());
      }

      return lv;
   }

   public String getPattern() {
      return this.pattern;
   }

   @Nullable
   public EntitySelector getSelector() {
      return this.selector;
   }

   public Optional getSeparator() {
      return this.separator;
   }

   public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
      if (source != null && this.selector != null) {
         Optional optional = Texts.parse(source, this.separator, sender, depth);
         return Texts.join(this.selector.getEntities(source), (Optional)optional, Entity::getDisplayName);
      } else {
         return Text.empty();
      }
   }

   public Optional visit(StringVisitable.StyledVisitor visitor, Style style) {
      return visitor.accept(style, this.pattern);
   }

   public Optional visit(StringVisitable.Visitor visitor) {
      return visitor.accept(this.pattern);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         boolean var10000;
         if (o instanceof SelectorTextContent) {
            SelectorTextContent lv = (SelectorTextContent)o;
            if (this.pattern.equals(lv.pattern) && this.separator.equals(lv.separator)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   public int hashCode() {
      int i = this.pattern.hashCode();
      i = 31 * i + this.separator.hashCode();
      return i;
   }

   public String toString() {
      return "pattern{" + this.pattern + "}";
   }
}
