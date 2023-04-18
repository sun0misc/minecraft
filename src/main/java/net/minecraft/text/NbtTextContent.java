package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NbtTextContent implements TextContent {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final boolean interpret;
   private final Optional separator;
   private final String rawPath;
   private final NbtDataSource dataSource;
   @Nullable
   protected final NbtPathArgumentType.NbtPath path;

   public NbtTextContent(String rawPath, boolean interpret, Optional separator, NbtDataSource dataSource) {
      this(rawPath, parsePath(rawPath), interpret, separator, dataSource);
   }

   private NbtTextContent(String rawPath, @Nullable NbtPathArgumentType.NbtPath path, boolean interpret, Optional separator, NbtDataSource dataSource) {
      this.rawPath = rawPath;
      this.path = path;
      this.interpret = interpret;
      this.separator = separator;
      this.dataSource = dataSource;
   }

   @Nullable
   private static NbtPathArgumentType.NbtPath parsePath(String rawPath) {
      try {
         return (new NbtPathArgumentType()).parse(new StringReader(rawPath));
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   public String getPath() {
      return this.rawPath;
   }

   public boolean shouldInterpret() {
      return this.interpret;
   }

   public Optional getSeparator() {
      return this.separator;
   }

   public NbtDataSource getDataSource() {
      return this.dataSource;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         boolean var10000;
         if (o instanceof NbtTextContent) {
            NbtTextContent lv = (NbtTextContent)o;
            if (this.dataSource.equals(lv.dataSource) && this.separator.equals(lv.separator) && this.interpret == lv.interpret && this.rawPath.equals(lv.rawPath)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   public int hashCode() {
      int i = this.interpret ? 1 : 0;
      i = 31 * i + this.separator.hashCode();
      i = 31 * i + this.rawPath.hashCode();
      i = 31 * i + this.dataSource.hashCode();
      return i;
   }

   public String toString() {
      return "nbt{" + this.dataSource + ", interpreting=" + this.interpret + ", separator=" + this.separator + "}";
   }

   public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
      if (source != null && this.path != null) {
         Stream stream = this.dataSource.get(source).flatMap((nbt) -> {
            try {
               return this.path.get(nbt).stream();
            } catch (CommandSyntaxException var3) {
               return Stream.empty();
            }
         }).map(NbtElement::asString);
         if (this.interpret) {
            Text lv = (Text)DataFixUtils.orElse(Texts.parse(source, this.separator, sender, depth), Texts.DEFAULT_SEPARATOR_TEXT);
            return (MutableText)stream.flatMap((text) -> {
               try {
                  MutableText lv = Text.Serializer.fromJson(text);
                  return Stream.of(Texts.parse(source, (Text)lv, sender, depth));
               } catch (Exception var5) {
                  LOGGER.warn("Failed to parse component: {}", text, var5);
                  return Stream.of();
               }
            }).reduce((accumulator, current) -> {
               return accumulator.append(lv).append((Text)current);
            }).orElseGet(Text::empty);
         } else {
            return (MutableText)Texts.parse(source, this.separator, sender, depth).map((text) -> {
               return (MutableText)stream.map(Text::literal).reduce((accumulator, current) -> {
                  return accumulator.append((Text)text).append((Text)current);
               }).orElseGet(Text::empty);
            }).orElseGet(() -> {
               return Text.literal((String)stream.collect(Collectors.joining(", ")));
            });
         }
      } else {
         return Text.empty();
      }
   }
}
