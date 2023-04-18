package net.minecraft.nbt.visitor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

public class NbtTextFormatter implements NbtElementVisitor {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_33271 = 8;
   private static final ByteCollection SINGLE_LINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList(1, 2, 3, 4, 5, 6));
   private static final Formatting NAME_COLOR;
   private static final Formatting STRING_COLOR;
   private static final Formatting NUMBER_COLOR;
   private static final Formatting TYPE_SUFFIX_COLOR;
   private static final Pattern SIMPLE_NAME;
   private static final String KEY_VALUE_SEPARATOR;
   private static final String ENTRY_SEPARATOR;
   private static final String SQUARE_OPEN_BRACKET = "[";
   private static final String SQUARE_CLOSE_BRACKET = "]";
   private static final String SEMICOLON = ";";
   private static final String SPACE = " ";
   private static final String CURLY_OPEN_BRACKET = "{";
   private static final String CURLY_CLOSE_BRACKET = "}";
   private static final String NEW_LINE = "\n";
   private final String prefix;
   private final int indentationLevel;
   private Text result;

   public NbtTextFormatter(String prefix, int indentationLevel) {
      this.result = ScreenTexts.EMPTY;
      this.prefix = prefix;
      this.indentationLevel = indentationLevel;
   }

   public Text apply(NbtElement element) {
      element.accept((NbtElementVisitor)this);
      return this.result;
   }

   public void visitString(NbtString element) {
      String string = NbtString.escape(element.asString());
      String string2 = string.substring(0, 1);
      Text lv = Text.literal(string.substring(1, string.length() - 1)).formatted(STRING_COLOR);
      this.result = Text.literal(string2).append((Text)lv).append(string2);
   }

   public void visitByte(NbtByte element) {
      Text lv = Text.literal("b").formatted(TYPE_SUFFIX_COLOR);
      this.result = Text.literal(String.valueOf(element.numberValue())).append((Text)lv).formatted(NUMBER_COLOR);
   }

   public void visitShort(NbtShort element) {
      Text lv = Text.literal("s").formatted(TYPE_SUFFIX_COLOR);
      this.result = Text.literal(String.valueOf(element.numberValue())).append((Text)lv).formatted(NUMBER_COLOR);
   }

   public void visitInt(NbtInt element) {
      this.result = Text.literal(String.valueOf(element.numberValue())).formatted(NUMBER_COLOR);
   }

   public void visitLong(NbtLong element) {
      Text lv = Text.literal("L").formatted(TYPE_SUFFIX_COLOR);
      this.result = Text.literal(String.valueOf(element.numberValue())).append((Text)lv).formatted(NUMBER_COLOR);
   }

   public void visitFloat(NbtFloat element) {
      Text lv = Text.literal("f").formatted(TYPE_SUFFIX_COLOR);
      this.result = Text.literal(String.valueOf(element.floatValue())).append((Text)lv).formatted(NUMBER_COLOR);
   }

   public void visitDouble(NbtDouble element) {
      Text lv = Text.literal("d").formatted(TYPE_SUFFIX_COLOR);
      this.result = Text.literal(String.valueOf(element.doubleValue())).append((Text)lv).formatted(NUMBER_COLOR);
   }

   public void visitByteArray(NbtByteArray element) {
      Text lv = Text.literal("B").formatted(TYPE_SUFFIX_COLOR);
      MutableText lv2 = Text.literal("[").append((Text)lv).append(";");
      byte[] bs = element.getByteArray();

      for(int i = 0; i < bs.length; ++i) {
         MutableText lv3 = Text.literal(String.valueOf(bs[i])).formatted(NUMBER_COLOR);
         lv2.append(" ").append((Text)lv3).append((Text)lv);
         if (i != bs.length - 1) {
            lv2.append(ENTRY_SEPARATOR);
         }
      }

      lv2.append("]");
      this.result = lv2;
   }

   public void visitIntArray(NbtIntArray element) {
      Text lv = Text.literal("I").formatted(TYPE_SUFFIX_COLOR);
      MutableText lv2 = Text.literal("[").append((Text)lv).append(";");
      int[] is = element.getIntArray();

      for(int i = 0; i < is.length; ++i) {
         lv2.append(" ").append((Text)Text.literal(String.valueOf(is[i])).formatted(NUMBER_COLOR));
         if (i != is.length - 1) {
            lv2.append(ENTRY_SEPARATOR);
         }
      }

      lv2.append("]");
      this.result = lv2;
   }

   public void visitLongArray(NbtLongArray element) {
      Text lv = Text.literal("L").formatted(TYPE_SUFFIX_COLOR);
      MutableText lv2 = Text.literal("[").append((Text)lv).append(";");
      long[] ls = element.getLongArray();

      for(int i = 0; i < ls.length; ++i) {
         Text lv3 = Text.literal(String.valueOf(ls[i])).formatted(NUMBER_COLOR);
         lv2.append(" ").append((Text)lv3).append((Text)lv);
         if (i != ls.length - 1) {
            lv2.append(ENTRY_SEPARATOR);
         }
      }

      lv2.append("]");
      this.result = lv2;
   }

   public void visitList(NbtList element) {
      if (element.isEmpty()) {
         this.result = Text.literal("[]");
      } else if (SINGLE_LINE_ELEMENT_TYPES.contains(element.getHeldType()) && element.size() <= 8) {
         String string = ENTRY_SEPARATOR + " ";
         MutableText lv = Text.literal("[");

         for(int i = 0; i < element.size(); ++i) {
            if (i != 0) {
               lv.append(string);
            }

            lv.append((new NbtTextFormatter(this.prefix, this.indentationLevel)).apply(element.get(i)));
         }

         lv.append("]");
         this.result = lv;
      } else {
         MutableText lv2 = Text.literal("[");
         if (!this.prefix.isEmpty()) {
            lv2.append("\n");
         }

         for(int j = 0; j < element.size(); ++j) {
            MutableText lv3 = Text.literal(Strings.repeat(this.prefix, this.indentationLevel + 1));
            lv3.append((new NbtTextFormatter(this.prefix, this.indentationLevel + 1)).apply(element.get(j)));
            if (j != element.size() - 1) {
               lv3.append(ENTRY_SEPARATOR).append(this.prefix.isEmpty() ? " " : "\n");
            }

            lv2.append((Text)lv3);
         }

         if (!this.prefix.isEmpty()) {
            lv2.append("\n").append(Strings.repeat(this.prefix, this.indentationLevel));
         }

         lv2.append("]");
         this.result = lv2;
      }
   }

   public void visitCompound(NbtCompound compound) {
      if (compound.isEmpty()) {
         this.result = Text.literal("{}");
      } else {
         MutableText lv = Text.literal("{");
         Collection collection = compound.getKeys();
         if (LOGGER.isDebugEnabled()) {
            List list = Lists.newArrayList(compound.getKeys());
            Collections.sort(list);
            collection = list;
         }

         if (!this.prefix.isEmpty()) {
            lv.append("\n");
         }

         MutableText lv2;
         for(Iterator iterator = ((Collection)collection).iterator(); iterator.hasNext(); lv.append((Text)lv2)) {
            String string = (String)iterator.next();
            lv2 = Text.literal(Strings.repeat(this.prefix, this.indentationLevel + 1)).append(escapeName(string)).append(KEY_VALUE_SEPARATOR).append(" ").append((new NbtTextFormatter(this.prefix, this.indentationLevel + 1)).apply(compound.get(string)));
            if (iterator.hasNext()) {
               lv2.append(ENTRY_SEPARATOR).append(this.prefix.isEmpty() ? " " : "\n");
            }
         }

         if (!this.prefix.isEmpty()) {
            lv.append("\n").append(Strings.repeat(this.prefix, this.indentationLevel));
         }

         lv.append("}");
         this.result = lv;
      }
   }

   protected static Text escapeName(String name) {
      if (SIMPLE_NAME.matcher(name).matches()) {
         return Text.literal(name).formatted(NAME_COLOR);
      } else {
         String string2 = NbtString.escape(name);
         String string3 = string2.substring(0, 1);
         Text lv = Text.literal(string2.substring(1, string2.length() - 1)).formatted(NAME_COLOR);
         return Text.literal(string3).append((Text)lv).append(string3);
      }
   }

   public void visitEnd(NbtEnd element) {
      this.result = ScreenTexts.EMPTY;
   }

   static {
      NAME_COLOR = Formatting.AQUA;
      STRING_COLOR = Formatting.GREEN;
      NUMBER_COLOR = Formatting.GOLD;
      TYPE_SUFFIX_COLOR = Formatting.RED;
      SIMPLE_NAME = Pattern.compile("[A-Za-z0-9._+-]+");
      KEY_VALUE_SEPARATOR = String.valueOf(':');
      ENTRY_SEPARATOR = String.valueOf(',');
   }
}
