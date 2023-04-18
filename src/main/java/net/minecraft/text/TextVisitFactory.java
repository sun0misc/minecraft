package net.minecraft.text;

import java.util.Optional;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;

public class TextVisitFactory {
   private static final char REPLACEMENT_CHARACTER = '�';
   private static final Optional VISIT_TERMINATED;

   private static boolean visitRegularCharacter(Style style, CharacterVisitor visitor, int index, char c) {
      return Character.isSurrogate(c) ? visitor.accept(index, style, 65533) : visitor.accept(index, style, c);
   }

   public static boolean visitForwards(String text, Style style, CharacterVisitor visitor) {
      int i = text.length();

      for(int j = 0; j < i; ++j) {
         char c = text.charAt(j);
         if (Character.isHighSurrogate(c)) {
            if (j + 1 >= i) {
               if (!visitor.accept(j, style, 65533)) {
                  return false;
               }
               break;
            }

            char d = text.charAt(j + 1);
            if (Character.isLowSurrogate(d)) {
               if (!visitor.accept(j, style, Character.toCodePoint(c, d))) {
                  return false;
               }

               ++j;
            } else if (!visitor.accept(j, style, 65533)) {
               return false;
            }
         } else if (!visitRegularCharacter(style, visitor, j, c)) {
            return false;
         }
      }

      return true;
   }

   public static boolean visitBackwards(String text, Style style, CharacterVisitor visitor) {
      int i = text.length();

      for(int j = i - 1; j >= 0; --j) {
         char c = text.charAt(j);
         if (Character.isLowSurrogate(c)) {
            if (j - 1 < 0) {
               if (!visitor.accept(0, style, 65533)) {
                  return false;
               }
               break;
            }

            char d = text.charAt(j - 1);
            if (Character.isHighSurrogate(d)) {
               --j;
               if (!visitor.accept(j, style, Character.toCodePoint(d, c))) {
                  return false;
               }
            } else if (!visitor.accept(j, style, 65533)) {
               return false;
            }
         } else if (!visitRegularCharacter(style, visitor, j, c)) {
            return false;
         }
      }

      return true;
   }

   public static boolean visitFormatted(String text, Style style, CharacterVisitor visitor) {
      return visitFormatted(text, 0, style, visitor);
   }

   public static boolean visitFormatted(String text, int startIndex, Style style, CharacterVisitor visitor) {
      return visitFormatted(text, startIndex, style, style, visitor);
   }

   public static boolean visitFormatted(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor) {
      int j = text.length();
      Style lv = startingStyle;

      for(int k = startIndex; k < j; ++k) {
         char c = text.charAt(k);
         char d;
         if (c == 167) {
            if (k + 1 >= j) {
               break;
            }

            d = text.charAt(k + 1);
            Formatting lv2 = Formatting.byCode(d);
            if (lv2 != null) {
               lv = lv2 == Formatting.RESET ? resetStyle : lv.withExclusiveFormatting(lv2);
            }

            ++k;
         } else if (Character.isHighSurrogate(c)) {
            if (k + 1 >= j) {
               if (!visitor.accept(k, lv, 65533)) {
                  return false;
               }
               break;
            }

            d = text.charAt(k + 1);
            if (Character.isLowSurrogate(d)) {
               if (!visitor.accept(k, lv, Character.toCodePoint(c, d))) {
                  return false;
               }

               ++k;
            } else if (!visitor.accept(k, lv, 65533)) {
               return false;
            }
         } else if (!visitRegularCharacter(lv, visitor, k, c)) {
            return false;
         }
      }

      return true;
   }

   public static boolean visitFormatted(StringVisitable text, Style style, CharacterVisitor visitor) {
      return !text.visit((stylex, string) -> {
         return visitFormatted(string, 0, stylex, visitor) ? Optional.empty() : VISIT_TERMINATED;
      }, style).isPresent();
   }

   public static String validateSurrogates(String text) {
      StringBuilder stringBuilder = new StringBuilder();
      visitForwards(text, Style.EMPTY, (index, style, codePoint) -> {
         stringBuilder.appendCodePoint(codePoint);
         return true;
      });
      return stringBuilder.toString();
   }

   public static String removeFormattingCodes(StringVisitable text) {
      StringBuilder stringBuilder = new StringBuilder();
      visitFormatted(text, Style.EMPTY, (index, style, codePoint) -> {
         stringBuilder.appendCodePoint(codePoint);
         return true;
      });
      return stringBuilder.toString();
   }

   static {
      VISIT_TERMINATED = Optional.of(Unit.INSTANCE);
   }
}
