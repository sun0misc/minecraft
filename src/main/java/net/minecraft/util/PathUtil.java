package net.minecraft.util;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FilenameUtils;

public class PathUtil {
   private static final Pattern FILE_NAME_WITH_COUNT = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
   private static final int MAX_NAME_LENGTH = 255;
   private static final Pattern RESERVED_WINDOWS_NAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
   private static final Pattern VALID_FILE_NAME = Pattern.compile("[-._a-z0-9]+");

   public static String getNextUniqueName(Path path, String name, String extension) throws IOException {
      char[] var3 = SharedConstants.INVALID_CHARS_LEVEL_NAME;
      int i = var3.length;

      for(int var5 = 0; var5 < i; ++var5) {
         char c = var3[var5];
         name = name.replace(c, '_');
      }

      name = name.replaceAll("[./\"]", "_");
      if (RESERVED_WINDOWS_NAMES.matcher(name).matches()) {
         name = "_" + name + "_";
      }

      Matcher matcher = FILE_NAME_WITH_COUNT.matcher(name);
      i = 0;
      if (matcher.matches()) {
         name = matcher.group("name");
         i = Integer.parseInt(matcher.group("count"));
      }

      if (name.length() > 255 - extension.length()) {
         name = name.substring(0, 255 - extension.length());
      }

      while(true) {
         String string3 = name;
         if (i != 0) {
            String string4 = " (" + i + ")";
            int j = 255 - string4.length();
            if (name.length() > j) {
               string3 = name.substring(0, j);
            }

            string3 = string3 + string4;
         }

         string3 = string3 + extension;
         Path path2 = path.resolve(string3);

         try {
            Path path3 = Files.createDirectory(path2);
            Files.deleteIfExists(path3);
            return path.relativize(path3).toString();
         } catch (FileAlreadyExistsException var8) {
            ++i;
         }
      }
   }

   public static boolean isNormal(Path path) {
      Path path2 = path.normalize();
      return path2.equals(path);
   }

   public static boolean isAllowedName(Path path) {
      Iterator var1 = path.iterator();

      Path path2;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         path2 = (Path)var1.next();
      } while(!RESERVED_WINDOWS_NAMES.matcher(path2.toString()).matches());

      return false;
   }

   public static Path getResourcePath(Path path, String resourceName, String extension) {
      String string3 = resourceName + extension;
      Path path2 = Paths.get(string3);
      if (path2.endsWith(extension)) {
         throw new InvalidPathException(string3, "empty resource name");
      } else {
         return path.resolve(path2);
      }
   }

   public static String getPosixFullPath(String path) {
      return FilenameUtils.getFullPath(path).replace(File.separator, "/");
   }

   public static String normalizeToPosix(String path) {
      return FilenameUtils.normalize(path).replace(File.separator, "/");
   }

   public static DataResult split(String path) {
      int i = path.indexOf(47);
      if (i == -1) {
         DataResult var10000;
         switch (path) {
            case "":
            case ".":
            case "..":
               var10000 = DataResult.error(() -> {
                  return "Invalid path '" + path + "'";
               });
               break;
            default:
               var10000 = !isFileNameValid(path) ? DataResult.error(() -> {
                  return "Invalid path '" + path + "'";
               }) : DataResult.success(List.of(path));
         }

         return var10000;
      } else {
         List list = new ArrayList();
         int j = 0;
         boolean bl = false;

         while(true) {
            switch (path.substring(j, i)) {
               case "":
               case ".":
               case "..":
                  return DataResult.error(() -> {
                     return "Invalid segment '" + string2 + "' in path '" + path + "'";
                  });
            }

            if (!isFileNameValid(string2)) {
               return DataResult.error(() -> {
                  return "Invalid segment '" + string2 + "' in path '" + path + "'";
               });
            }

            list.add(string2);
            if (bl) {
               return DataResult.success(list);
            }

            j = i + 1;
            i = path.indexOf(47, j);
            if (i == -1) {
               i = path.length();
               bl = true;
            }
         }
      }
   }

   public static Path getPath(Path root, List paths) {
      int i = paths.size();
      Path var10000;
      switch (i) {
         case 0:
            var10000 = root;
            break;
         case 1:
            var10000 = root.resolve((String)paths.get(0));
            break;
         default:
            String[] strings = new String[i - 1];

            for(int j = 1; j < i; ++j) {
               strings[j - 1] = (String)paths.get(j);
            }

            var10000 = root.resolve(root.getFileSystem().getPath((String)paths.get(0), strings));
      }

      return var10000;
   }

   public static boolean isFileNameValid(String name) {
      return VALID_FILE_NAME.matcher(name).matches();
   }

   public static void validatePath(String... paths) {
      if (paths.length == 0) {
         throw new IllegalArgumentException("Path must have at least one element");
      } else {
         String[] var1 = paths;
         int var2 = paths.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String string = var1[var3];
            if (string.equals("..") || string.equals(".") || !isFileNameValid(string)) {
               throw new IllegalArgumentException("Illegal segment " + string + " in path " + Arrays.toString(paths));
            }
         }

      }
   }

   public static void createDirectories(Path path) throws IOException {
      Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
   }
}
