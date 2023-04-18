package net.minecraft.client.font;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TextHandler {
   final WidthRetriever widthRetriever;

   public TextHandler(WidthRetriever widthRetriever) {
      this.widthRetriever = widthRetriever;
   }

   public float getWidth(@Nullable String text) {
      if (text == null) {
         return 0.0F;
      } else {
         MutableFloat mutableFloat = new MutableFloat();
         TextVisitFactory.visitFormatted(text, Style.EMPTY, (unused, style, codePoint) -> {
            mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
            return true;
         });
         return mutableFloat.floatValue();
      }
   }

   public float getWidth(StringVisitable text) {
      MutableFloat mutableFloat = new MutableFloat();
      TextVisitFactory.visitFormatted(text, Style.EMPTY, (unused, style, codePoint) -> {
         mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
         return true;
      });
      return mutableFloat.floatValue();
   }

   public float getWidth(OrderedText text) {
      MutableFloat mutableFloat = new MutableFloat();
      text.accept((index, style, codePoint) -> {
         mutableFloat.add(this.widthRetriever.getWidth(codePoint, style));
         return true;
      });
      return mutableFloat.floatValue();
   }

   public int getTrimmedLength(String text, int maxWidth, Style style) {
      WidthLimitingVisitor lv = new WidthLimitingVisitor((float)maxWidth);
      TextVisitFactory.visitForwards(text, style, lv);
      return lv.getLength();
   }

   public String trimToWidth(String text, int maxWidth, Style style) {
      return text.substring(0, this.getTrimmedLength(text, maxWidth, style));
   }

   public String trimToWidthBackwards(String text, int maxWidth, Style style) {
      MutableFloat mutableFloat = new MutableFloat();
      MutableInt mutableInt = new MutableInt(text.length());
      TextVisitFactory.visitBackwards(text, style, (index, stylex, codePoint) -> {
         float f = mutableFloat.addAndGet(this.widthRetriever.getWidth(codePoint, stylex));
         if (f > (float)maxWidth) {
            return false;
         } else {
            mutableInt.setValue(index);
            return true;
         }
      });
      return text.substring(mutableInt.intValue());
   }

   public int getLimitedStringLength(String text, int maxWidth, Style style) {
      WidthLimitingVisitor lv = new WidthLimitingVisitor((float)maxWidth);
      TextVisitFactory.visitFormatted((String)text, style, lv);
      return lv.getLength();
   }

   @Nullable
   public Style getStyleAt(StringVisitable text, int x) {
      WidthLimitingVisitor lv = new WidthLimitingVisitor((float)x);
      return (Style)text.visit((style, textx) -> {
         return TextVisitFactory.visitFormatted((String)textx, style, lv) ? Optional.empty() : Optional.of(style);
      }, Style.EMPTY).orElse((Object)null);
   }

   @Nullable
   public Style getStyleAt(OrderedText text, int x) {
      WidthLimitingVisitor lv = new WidthLimitingVisitor((float)x);
      MutableObject mutableObject = new MutableObject();
      text.accept((index, style, codePoint) -> {
         if (!lv.accept(index, style, codePoint)) {
            mutableObject.setValue(style);
            return false;
         } else {
            return true;
         }
      });
      return (Style)mutableObject.getValue();
   }

   public String limitString(String text, int maxWidth, Style style) {
      return text.substring(0, this.getLimitedStringLength(text, maxWidth, style));
   }

   public StringVisitable trimToWidth(StringVisitable text, int width, Style style) {
      final WidthLimitingVisitor lv = new WidthLimitingVisitor((float)width);
      return (StringVisitable)text.visit(new StringVisitable.StyledVisitor() {
         private final TextCollector collector = new TextCollector();

         public Optional accept(Style arg, String string) {
            lv.resetLength();
            if (!TextVisitFactory.visitFormatted((String)string, arg, lv)) {
               String string2 = string.substring(0, lv.getLength());
               if (!string2.isEmpty()) {
                  this.collector.add(StringVisitable.styled(string2, arg));
               }

               return Optional.of(this.collector.getCombined());
            } else {
               if (!string.isEmpty()) {
                  this.collector.add(StringVisitable.styled(string, arg));
               }

               return Optional.empty();
            }
         }
      }, style).orElse(text);
   }

   public int getEndingIndex(String text, int maxWidth, Style style) {
      LineBreakingVisitor lv = new LineBreakingVisitor((float)maxWidth);
      TextVisitFactory.visitFormatted((String)text, style, lv);
      return lv.getEndingIndex();
   }

   public static int moveCursorByWords(String text, int offset, int cursor, boolean consumeSpaceOrBreak) {
      int k = cursor;
      boolean bl2 = offset < 0;
      int l = Math.abs(offset);

      for(int m = 0; m < l; ++m) {
         if (bl2) {
            while(consumeSpaceOrBreak && k > 0 && (text.charAt(k - 1) == ' ' || text.charAt(k - 1) == '\n')) {
               --k;
            }

            while(k > 0 && text.charAt(k - 1) != ' ' && text.charAt(k - 1) != '\n') {
               --k;
            }
         } else {
            int n = text.length();
            int o = text.indexOf(32, k);
            int p = text.indexOf(10, k);
            if (o == -1 && p == -1) {
               k = -1;
            } else if (o != -1 && p != -1) {
               k = Math.min(o, p);
            } else if (o != -1) {
               k = o;
            } else {
               k = p;
            }

            if (k == -1) {
               k = n;
            } else {
               while(consumeSpaceOrBreak && k < n && (text.charAt(k) == ' ' || text.charAt(k) == '\n')) {
                  ++k;
               }
            }
         }
      }

      return k;
   }

   public void wrapLines(String text, int maxWidth, Style style, boolean retainTrailingWordSplit, LineWrappingConsumer consumer) {
      int j = 0;
      int k = text.length();

      LineBreakingVisitor lv2;
      for(Style lv = style; j < k; lv = lv2.getEndingStyle()) {
         lv2 = new LineBreakingVisitor((float)maxWidth);
         boolean bl2 = TextVisitFactory.visitFormatted(text, j, lv, style, lv2);
         if (bl2) {
            consumer.accept(lv, j, k);
            break;
         }

         int l = lv2.getEndingIndex();
         char c = text.charAt(l);
         int m = c != '\n' && c != ' ' ? l : l + 1;
         consumer.accept(lv, j, retainTrailingWordSplit ? m : l);
         j = m;
      }

   }

   public List wrapLines(String text, int maxWidth, Style style) {
      List list = Lists.newArrayList();
      this.wrapLines(text, maxWidth, style, false, (stylex, start, end) -> {
         list.add(StringVisitable.styled(text.substring(start, end), stylex));
      });
      return list;
   }

   public List wrapLines(StringVisitable text, int maxWidth, Style style) {
      List list = Lists.newArrayList();
      this.wrapLines(text, maxWidth, style, (textx, lastLineWrapped) -> {
         list.add(textx);
      });
      return list;
   }

   public List wrapLines(StringVisitable text, int maxWidth, Style style, StringVisitable wrappedLinePrefix) {
      List list = Lists.newArrayList();
      this.wrapLines(text, maxWidth, style, (textx, lastLineWrapped) -> {
         list.add(lastLineWrapped ? StringVisitable.concat(wrappedLinePrefix, textx) : textx);
      });
      return list;
   }

   public void wrapLines(StringVisitable text, int maxWidth, Style style, BiConsumer lineConsumer) {
      List list = Lists.newArrayList();
      text.visit((stylex, textx) -> {
         if (!textx.isEmpty()) {
            list.add(new StyledString(textx, stylex));
         }

         return Optional.empty();
      }, style);
      LineWrappingCollector lv = new LineWrappingCollector(list);
      boolean bl = true;
      boolean bl2 = false;
      boolean bl3 = false;

      while(true) {
         while(bl) {
            bl = false;
            LineBreakingVisitor lv2 = new LineBreakingVisitor((float)maxWidth);
            Iterator var11 = lv.parts.iterator();

            while(var11.hasNext()) {
               StyledString lv3 = (StyledString)var11.next();
               boolean bl4 = TextVisitFactory.visitFormatted(lv3.literal, 0, lv3.style, style, lv2);
               if (!bl4) {
                  int j = lv2.getEndingIndex();
                  Style lv4 = lv2.getEndingStyle();
                  char c = lv.charAt(j);
                  boolean bl5 = c == '\n';
                  boolean bl6 = bl5 || c == ' ';
                  bl2 = bl5;
                  StringVisitable lv5 = lv.collectLine(j, bl6 ? 1 : 0, lv4);
                  lineConsumer.accept(lv5, bl3);
                  bl3 = !bl5;
                  bl = true;
                  break;
               }

               lv2.offset(lv3.literal.length());
            }
         }

         StringVisitable lv6 = lv.collectRemainers();
         if (lv6 != null) {
            lineConsumer.accept(lv6, bl3);
         } else if (bl2) {
            lineConsumer.accept(StringVisitable.EMPTY, false);
         }

         return;
      }
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface WidthRetriever {
      float getWidth(int codePoint, Style style);
   }

   @Environment(EnvType.CLIENT)
   private class WidthLimitingVisitor implements CharacterVisitor {
      private float widthLeft;
      private int length;

      public WidthLimitingVisitor(float maxWidth) {
         this.widthLeft = maxWidth;
      }

      public boolean accept(int i, Style arg, int j) {
         this.widthLeft -= TextHandler.this.widthRetriever.getWidth(j, arg);
         if (this.widthLeft >= 0.0F) {
            this.length = i + Character.charCount(j);
            return true;
         } else {
            return false;
         }
      }

      public int getLength() {
         return this.length;
      }

      public void resetLength() {
         this.length = 0;
      }
   }

   @Environment(EnvType.CLIENT)
   class LineBreakingVisitor implements CharacterVisitor {
      private final float maxWidth;
      private int endIndex = -1;
      private Style endStyle;
      private boolean nonEmpty;
      private float totalWidth;
      private int lastSpaceBreak;
      private Style lastSpaceStyle;
      private int count;
      private int startOffset;

      public LineBreakingVisitor(float maxWidth) {
         this.endStyle = Style.EMPTY;
         this.lastSpaceBreak = -1;
         this.lastSpaceStyle = Style.EMPTY;
         this.maxWidth = Math.max(maxWidth, 1.0F);
      }

      public boolean accept(int i, Style arg, int j) {
         int k = i + this.startOffset;
         switch (j) {
            case 10:
               return this.breakLine(k, arg);
            case 32:
               this.lastSpaceBreak = k;
               this.lastSpaceStyle = arg;
            default:
               float f = TextHandler.this.widthRetriever.getWidth(j, arg);
               this.totalWidth += f;
               if (this.nonEmpty && this.totalWidth > this.maxWidth) {
                  return this.lastSpaceBreak != -1 ? this.breakLine(this.lastSpaceBreak, this.lastSpaceStyle) : this.breakLine(k, arg);
               } else {
                  this.nonEmpty |= f != 0.0F;
                  this.count = k + Character.charCount(j);
                  return true;
               }
         }
      }

      private boolean breakLine(int finishIndex, Style finishStyle) {
         this.endIndex = finishIndex;
         this.endStyle = finishStyle;
         return false;
      }

      private boolean hasLineBreak() {
         return this.endIndex != -1;
      }

      public int getEndingIndex() {
         return this.hasLineBreak() ? this.endIndex : this.count;
      }

      public Style getEndingStyle() {
         return this.endStyle;
      }

      public void offset(int extraOffset) {
         this.startOffset += extraOffset;
      }
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface LineWrappingConsumer {
      void accept(Style style, int start, int end);
   }

   @Environment(EnvType.CLIENT)
   private static class LineWrappingCollector {
      final List parts;
      private String joined;

      public LineWrappingCollector(List parts) {
         this.parts = parts;
         this.joined = (String)parts.stream().map((part) -> {
            return part.literal;
         }).collect(Collectors.joining());
      }

      public char charAt(int index) {
         return this.joined.charAt(index);
      }

      public StringVisitable collectLine(int lineLength, int skippedLength, Style style) {
         TextCollector lv = new TextCollector();
         ListIterator listIterator = this.parts.listIterator();
         int k = lineLength;
         boolean bl = false;

         while(listIterator.hasNext()) {
            StyledString lv2 = (StyledString)listIterator.next();
            String string = lv2.literal;
            int l = string.length();
            String string2;
            if (!bl) {
               if (k > l) {
                  lv.add(lv2);
                  listIterator.remove();
                  k -= l;
               } else {
                  string2 = string.substring(0, k);
                  if (!string2.isEmpty()) {
                     lv.add(StringVisitable.styled(string2, lv2.style));
                  }

                  k += skippedLength;
                  bl = true;
               }
            }

            if (bl) {
               if (k <= l) {
                  string2 = string.substring(k);
                  if (string2.isEmpty()) {
                     listIterator.remove();
                  } else {
                     listIterator.set(new StyledString(string2, style));
                  }
                  break;
               }

               listIterator.remove();
               k -= l;
            }
         }

         this.joined = this.joined.substring(lineLength + skippedLength);
         return lv.getCombined();
      }

      @Nullable
      public StringVisitable collectRemainers() {
         TextCollector lv = new TextCollector();
         List var10000 = this.parts;
         Objects.requireNonNull(lv);
         var10000.forEach(lv::add);
         this.parts.clear();
         return lv.getRawCombined();
      }
   }

   @Environment(EnvType.CLIENT)
   private static class StyledString implements StringVisitable {
      final String literal;
      final Style style;

      public StyledString(String literal, Style style) {
         this.literal = literal;
         this.style = style;
      }

      public Optional visit(StringVisitable.Visitor visitor) {
         return visitor.accept(this.literal);
      }

      public Optional visit(StringVisitable.StyledVisitor styledVisitor, Style style) {
         return styledVisitor.accept(this.style.withParent(style), this.literal);
      }
   }
}
