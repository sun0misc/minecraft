package net.minecraft.client.gl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.PathUtil;
import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class GlImportProcessor {
   private static final String MULTI_LINE_COMMENT_PATTERN = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
   private static final String SINGLE_LINE_COMMENT_PATTERN = "//[^\\v]*";
   private static final Pattern MOJ_IMPORT_PATTERN = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
   private static final Pattern IMPORT_VERSION_PATTERN = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b");
   private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

   public List readSource(String source) {
      Context lv = new Context();
      List list = this.parseImports(source, lv, "");
      list.set(0, this.readImport((String)list.get(0), lv.column));
      return list;
   }

   private List parseImports(String source, Context context, String path) {
      int i = context.line;
      int j = 0;
      String string3 = "";
      List list = Lists.newArrayList();
      Matcher matcher = MOJ_IMPORT_PATTERN.matcher(source);

      String string4;
      while(matcher.find()) {
         if (!hasBogusString(source, matcher, j)) {
            string4 = matcher.group(2);
            boolean bl = string4 != null;
            if (!bl) {
               string4 = matcher.group(3);
            }

            if (string4 != null) {
               String string5 = source.substring(j, matcher.start(1));
               String string6 = path + string4;
               String string7 = this.loadImport(bl, string6);
               int k;
               if (!Strings.isNullOrEmpty(string7)) {
                  if (!StringHelper.endsWithLineBreak(string7)) {
                     string7 = string7 + System.lineSeparator();
                  }

                  ++context.line;
                  k = context.line;
                  List list2 = this.parseImports(string7, context, bl ? PathUtil.getPosixFullPath(string6) : "");
                  list2.set(0, String.format(Locale.ROOT, "#line %d %d\n%s", 0, k, this.extractVersion((String)list2.get(0), context)));
                  if (!StringUtils.isBlank(string5)) {
                     list.add(string5);
                  }

                  list.addAll(list2);
               } else {
                  String string8 = bl ? String.format(Locale.ROOT, "/*#moj_import \"%s\"*/", string4) : String.format(Locale.ROOT, "/*#moj_import <%s>*/", string4);
                  list.add(string3 + string5 + string8);
               }

               k = StringHelper.countLines(source.substring(0, matcher.end(1)));
               string3 = String.format(Locale.ROOT, "#line %d %d", k, i);
               j = matcher.end(1);
            }
         }
      }

      string4 = source.substring(j);
      if (!StringUtils.isBlank(string4)) {
         list.add(string3 + string4);
      }

      return list;
   }

   private String extractVersion(String line, Context context) {
      Matcher matcher = IMPORT_VERSION_PATTERN.matcher(line);
      if (matcher.find() && isLineValid(line, matcher)) {
         context.column = Math.max(context.column, Integer.parseInt(matcher.group(2)));
         String var10000 = line.substring(0, matcher.start(1));
         return var10000 + "/*" + line.substring(matcher.start(1), matcher.end(1)) + "*/" + line.substring(matcher.end(1));
      } else {
         return line;
      }
   }

   private String readImport(String line, int start) {
      Matcher matcher = IMPORT_VERSION_PATTERN.matcher(line);
      if (matcher.find() && isLineValid(line, matcher)) {
         String var10000 = line.substring(0, matcher.start(2));
         return var10000 + Math.max(start, Integer.parseInt(matcher.group(2))) + line.substring(matcher.end(2));
      } else {
         return line;
      }
   }

   private static boolean isLineValid(String line, Matcher matcher) {
      return !hasBogusString(line, matcher, 0);
   }

   private static boolean hasBogusString(String string, Matcher matcher, int matchEnd) {
      int j = matcher.start() - matchEnd;
      if (j == 0) {
         return false;
      } else {
         Matcher matcher2 = TRAILING_WHITESPACE_PATTERN.matcher(string.substring(matchEnd, matcher.start()));
         if (!matcher2.find()) {
            return true;
         } else {
            int k = matcher2.end(1);
            return k == matcher.start();
         }
      }
   }

   @Nullable
   public abstract String loadImport(boolean inline, String name);

   @Environment(EnvType.CLIENT)
   static final class Context {
      int column;
      int line;
   }
}
