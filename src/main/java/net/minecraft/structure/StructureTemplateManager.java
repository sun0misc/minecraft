package net.minecraft.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.PathUtil;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String STRUCTURES_DIRECTORY = "structures";
   private static final String GAME_TEST_STRUCTURES_DIRECTORY = "gameteststructures";
   private static final String NBT_FILE_EXTENSION = ".nbt";
   private static final String SNBT_FILE_EXTENSION = ".snbt";
   private final Map templates = Maps.newConcurrentMap();
   private final DataFixer dataFixer;
   private ResourceManager resourceManager;
   private final Path generatedPath;
   private final List providers;
   private final RegistryEntryLookup blockLookup;
   private static final ResourceFinder NBT_FINDER = new ResourceFinder("structures", ".nbt");

   public StructureTemplateManager(ResourceManager resourceManager, LevelStorage.Session session, DataFixer dataFixer, RegistryEntryLookup blockLookup) {
      this.resourceManager = resourceManager;
      this.dataFixer = dataFixer;
      this.generatedPath = session.getDirectory(WorldSavePath.GENERATED).normalize();
      this.blockLookup = blockLookup;
      ImmutableList.Builder builder = ImmutableList.builder();
      builder.add(new Provider(this::loadTemplateFromFile, this::streamTemplatesFromFile));
      if (SharedConstants.isDevelopment) {
         builder.add(new Provider(this::loadTemplateFromGameTestFile, this::streamTemplatesFromGameTestFile));
      }

      builder.add(new Provider(this::loadTemplateFromResource, this::streamTemplatesFromResource));
      this.providers = builder.build();
   }

   public StructureTemplate getTemplateOrBlank(Identifier id) {
      Optional optional = this.getTemplate(id);
      if (optional.isPresent()) {
         return (StructureTemplate)optional.get();
      } else {
         StructureTemplate lv = new StructureTemplate();
         this.templates.put(id, Optional.of(lv));
         return lv;
      }
   }

   public Optional getTemplate(Identifier id) {
      return (Optional)this.templates.computeIfAbsent(id, this::loadTemplate);
   }

   public Stream streamTemplates() {
      return this.providers.stream().flatMap((provider) -> {
         return (Stream)provider.lister().get();
      }).distinct();
   }

   private Optional loadTemplate(Identifier id) {
      Iterator var2 = this.providers.iterator();

      while(var2.hasNext()) {
         Provider lv = (Provider)var2.next();

         try {
            Optional optional = (Optional)lv.loader().apply(id);
            if (optional.isPresent()) {
               return optional;
            }
         } catch (Exception var5) {
         }
      }

      return Optional.empty();
   }

   public void setResourceManager(ResourceManager resourceManager) {
      this.resourceManager = resourceManager;
      this.templates.clear();
   }

   private Optional loadTemplateFromResource(Identifier id) {
      Identifier lv = NBT_FINDER.toResourcePath(id);
      return this.loadTemplate(() -> {
         return this.resourceManager.open(lv);
      }, (throwable) -> {
         LOGGER.error("Couldn't load structure {}", id, throwable);
      });
   }

   private Stream streamTemplatesFromResource() {
      Stream var10000 = NBT_FINDER.findResources(this.resourceManager).keySet().stream();
      ResourceFinder var10001 = NBT_FINDER;
      Objects.requireNonNull(var10001);
      return var10000.map(var10001::toResourceId);
   }

   private Optional loadTemplateFromGameTestFile(Identifier id) {
      return this.loadTemplateFromSnbt(id, Paths.get("gameteststructures"));
   }

   private Stream streamTemplatesFromGameTestFile() {
      return this.streamTemplates(Paths.get("gameteststructures"), "minecraft", ".snbt");
   }

   private Optional loadTemplateFromFile(Identifier id) {
      if (!Files.isDirectory(this.generatedPath, new LinkOption[0])) {
         return Optional.empty();
      } else {
         Path path = getAndCheckTemplatePath(this.generatedPath, id, ".nbt");
         return this.loadTemplate(() -> {
            return new FileInputStream(path.toFile());
         }, (throwable) -> {
            LOGGER.error("Couldn't load structure from {}", path, throwable);
         });
      }
   }

   private Stream streamTemplatesFromFile() {
      if (!Files.isDirectory(this.generatedPath, new LinkOption[0])) {
         return Stream.empty();
      } else {
         try {
            return Files.list(this.generatedPath).filter((path) -> {
               return Files.isDirectory(path, new LinkOption[0]);
            }).flatMap((path) -> {
               return this.streamTemplates(path);
            });
         } catch (IOException var2) {
            return Stream.empty();
         }
      }
   }

   private Stream streamTemplates(Path namespaceDirectory) {
      Path path2 = namespaceDirectory.resolve("structures");
      return this.streamTemplates(path2, namespaceDirectory.getFileName().toString(), ".nbt");
   }

   private Stream streamTemplates(Path structuresDirectoryPath, String namespace, String extension) {
      if (!Files.isDirectory(structuresDirectoryPath, new LinkOption[0])) {
         return Stream.empty();
      } else {
         int i = extension.length();
         Function function = (filename) -> {
            return filename.substring(0, filename.length() - i);
         };

         try {
            return Files.walk(structuresDirectoryPath).filter((path) -> {
               return path.toString().endsWith(extension);
            }).mapMulti((path, consumer) -> {
               try {
                  consumer.accept(new Identifier(namespace, (String)function.apply(this.toRelativePath(structuresDirectoryPath, path))));
               } catch (InvalidIdentifierException var7) {
                  LOGGER.error("Invalid location while listing pack contents", var7);
               }

            });
         } catch (IOException var7) {
            LOGGER.error("Failed to list folder contents", var7);
            return Stream.empty();
         }
      }
   }

   private String toRelativePath(Path root, Path path) {
      return root.relativize(path).toString().replace(File.separator, "/");
   }

   private Optional loadTemplateFromSnbt(Identifier id, Path path) {
      if (!Files.isDirectory(path, new LinkOption[0])) {
         return Optional.empty();
      } else {
         Path path2 = PathUtil.getResourcePath(path, id.getPath(), ".snbt");

         try {
            BufferedReader bufferedReader = Files.newBufferedReader(path2);

            Optional var6;
            try {
               String string = IOUtils.toString(bufferedReader);
               var6 = Optional.of(this.createTemplate(NbtHelper.fromNbtProviderString(string)));
            } catch (Throwable var8) {
               if (bufferedReader != null) {
                  try {
                     bufferedReader.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (bufferedReader != null) {
               bufferedReader.close();
            }

            return var6;
         } catch (NoSuchFileException var9) {
            return Optional.empty();
         } catch (CommandSyntaxException | IOException var10) {
            LOGGER.error("Couldn't load structure from {}", path2, var10);
            return Optional.empty();
         }
      }
   }

   private Optional loadTemplate(TemplateFileOpener opener, Consumer exceptionConsumer) {
      try {
         InputStream inputStream = opener.open();

         Optional var4;
         try {
            var4 = Optional.of(this.readTemplate(inputStream));
         } catch (Throwable var7) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var4;
      } catch (FileNotFoundException var8) {
         return Optional.empty();
      } catch (Throwable var9) {
         exceptionConsumer.accept(var9);
         return Optional.empty();
      }
   }

   private StructureTemplate readTemplate(InputStream templateIInputStream) throws IOException {
      NbtCompound lv = NbtIo.readCompressed(templateIInputStream);
      return this.createTemplate(lv);
   }

   public StructureTemplate createTemplate(NbtCompound nbt) {
      StructureTemplate lv = new StructureTemplate();
      int i = NbtHelper.getDataVersion(nbt, 500);
      lv.readNbt(this.blockLookup, DataFixTypes.STRUCTURE.update(this.dataFixer, nbt, i));
      return lv;
   }

   public boolean saveTemplate(Identifier id) {
      Optional optional = (Optional)this.templates.get(id);
      if (!optional.isPresent()) {
         return false;
      } else {
         StructureTemplate lv = (StructureTemplate)optional.get();
         Path path = getAndCheckTemplatePath(this.generatedPath, id, ".nbt");
         Path path2 = path.getParent();
         if (path2 == null) {
            return false;
         } else {
            try {
               Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath() : path2);
            } catch (IOException var13) {
               LOGGER.error("Failed to create parent directory: {}", path2);
               return false;
            }

            NbtCompound lv2 = lv.writeNbt(new NbtCompound());

            try {
               OutputStream outputStream = new FileOutputStream(path.toFile());

               try {
                  NbtIo.writeCompressed(lv2, (OutputStream)outputStream);
               } catch (Throwable var11) {
                  try {
                     outputStream.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }

                  throw var11;
               }

               outputStream.close();
               return true;
            } catch (Throwable var12) {
               return false;
            }
         }
      }
   }

   public Path getTemplatePath(Identifier id, String extension) {
      return getTemplatePath(this.generatedPath, id, extension);
   }

   public static Path getTemplatePath(Path path, Identifier id, String extension) {
      try {
         Path path2 = path.resolve(id.getNamespace());
         Path path3 = path2.resolve("structures");
         return PathUtil.getResourcePath(path3, id.getPath(), extension);
      } catch (InvalidPathException var5) {
         throw new InvalidIdentifierException("Invalid resource path: " + id, var5);
      }
   }

   private static Path getAndCheckTemplatePath(Path path, Identifier id, String extension) {
      if (id.getPath().contains("//")) {
         throw new InvalidIdentifierException("Invalid resource path: " + id);
      } else {
         Path path2 = getTemplatePath(path, id, extension);
         if (path2.startsWith(path) && PathUtil.isNormal(path2) && PathUtil.isAllowedName(path2)) {
            return path2;
         } else {
            throw new InvalidIdentifierException("Invalid resource path: " + path2);
         }
      }
   }

   public void unloadTemplate(Identifier id) {
      this.templates.remove(id);
   }

   static record Provider(Function loader, Supplier lister) {
      Provider(Function function, Supplier supplier) {
         this.loader = function;
         this.lister = supplier;
      }

      public Function loader() {
         return this.loader;
      }

      public Supplier lister() {
         return this.lister;
      }
   }

   @FunctionalInterface
   interface TemplateFileOpener {
      InputStream open() throws IOException;
   }
}
