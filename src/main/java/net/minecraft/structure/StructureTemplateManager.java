/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.PathUtil;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String STRUCTURES_DIRECTORY = "structure";
    private static final String NBT_FILE_EXTENSION = ".nbt";
    private static final String SNBT_FILE_EXTENSION = ".snbt";
    private final Map<Identifier, Optional<StructureTemplate>> templates = Maps.newConcurrentMap();
    private final DataFixer dataFixer;
    private ResourceManager resourceManager;
    private final Path generatedPath;
    private final List<Provider> providers;
    private final RegistryEntryLookup<Block> blockLookup;
    private static final ResourceFinder NBT_FINDER = new ResourceFinder("structure", ".nbt");

    public StructureTemplateManager(ResourceManager resourceManager, LevelStorage.Session session, DataFixer dataFixer, RegistryEntryLookup<Block> blockLookup) {
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
        Optional<StructureTemplate> optional = this.getTemplate(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        StructureTemplate lv = new StructureTemplate();
        this.templates.put(id, Optional.of(lv));
        return lv;
    }

    public Optional<StructureTemplate> getTemplate(Identifier id) {
        return this.templates.computeIfAbsent(id, this::loadTemplate);
    }

    public Stream<Identifier> streamTemplates() {
        return this.providers.stream().flatMap(provider -> provider.lister().get()).distinct();
    }

    private Optional<StructureTemplate> loadTemplate(Identifier id) {
        for (Provider lv : this.providers) {
            try {
                Optional<StructureTemplate> optional = lv.loader().apply(id);
                if (!optional.isPresent()) continue;
                return optional;
            } catch (Exception exception) {
            }
        }
        return Optional.empty();
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.templates.clear();
    }

    private Optional<StructureTemplate> loadTemplateFromResource(Identifier id) {
        Identifier lv = NBT_FINDER.toResourcePath(id);
        return this.loadTemplate(() -> this.resourceManager.open(lv), throwable -> LOGGER.error("Couldn't load structure {}", (Object)id, throwable));
    }

    private Stream<Identifier> streamTemplatesFromResource() {
        return NBT_FINDER.findResources(this.resourceManager).keySet().stream().map(NBT_FINDER::toResourceId);
    }

    private Optional<StructureTemplate> loadTemplateFromGameTestFile(Identifier id) {
        return this.loadTemplateFromSnbt(id, Paths.get(StructureTestUtil.testStructuresDirectoryName, new String[0]));
    }

    private Stream<Identifier> streamTemplatesFromGameTestFile() {
        return this.streamTemplates(Paths.get(StructureTestUtil.testStructuresDirectoryName, new String[0]), "minecraft", SNBT_FILE_EXTENSION);
    }

    private Optional<StructureTemplate> loadTemplateFromFile(Identifier id) {
        if (!Files.isDirectory(this.generatedPath, new LinkOption[0])) {
            return Optional.empty();
        }
        Path path = StructureTemplateManager.getAndCheckTemplatePath(this.generatedPath, id, NBT_FILE_EXTENSION);
        return this.loadTemplate(() -> new FileInputStream(path.toFile()), throwable -> LOGGER.error("Couldn't load structure from {}", (Object)path, throwable));
    }

    private Stream<Identifier> streamTemplatesFromFile() {
        if (!Files.isDirectory(this.generatedPath, new LinkOption[0])) {
            return Stream.empty();
        }
        try {
            return Files.list(this.generatedPath).filter(path -> Files.isDirectory(path, new LinkOption[0])).flatMap(path -> this.streamTemplates((Path)path));
        } catch (IOException iOException) {
            return Stream.empty();
        }
    }

    private Stream<Identifier> streamTemplates(Path namespaceDirectory) {
        Path path2 = namespaceDirectory.resolve(STRUCTURES_DIRECTORY);
        return this.streamTemplates(path2, namespaceDirectory.getFileName().toString(), NBT_FILE_EXTENSION);
    }

    private Stream<Identifier> streamTemplates(Path structuresDirectoryPath, String namespace, String extension) {
        if (!Files.isDirectory(structuresDirectoryPath, new LinkOption[0])) {
            return Stream.empty();
        }
        int i = extension.length();
        Function<String, String> function = filename -> filename.substring(0, filename.length() - i);
        try {
            return Files.walk(structuresDirectoryPath, new FileVisitOption[0]).filter(path -> path.toString().endsWith(extension)).mapMulti((path2, consumer) -> {
                try {
                    consumer.accept(Identifier.method_60655(namespace, (String)function.apply(this.toRelativePath(structuresDirectoryPath, (Path)path2))));
                } catch (InvalidIdentifierException lv) {
                    LOGGER.error("Invalid location while listing pack contents", lv);
                }
            });
        } catch (IOException iOException) {
            LOGGER.error("Failed to list folder contents", iOException);
            return Stream.empty();
        }
    }

    private String toRelativePath(Path root, Path path) {
        return root.relativize(path).toString().replace(File.separator, "/");
    }

    private Optional<StructureTemplate> loadTemplateFromSnbt(Identifier id, Path path) {
        Optional<StructureTemplate> optional;
        block10: {
            if (!Files.isDirectory(path, new LinkOption[0])) {
                return Optional.empty();
            }
            Path path2 = PathUtil.getResourcePath(path, id.getPath(), SNBT_FILE_EXTENSION);
            BufferedReader bufferedReader = Files.newBufferedReader(path2);
            try {
                String string = IOUtils.toString(bufferedReader);
                optional = Optional.of(this.createTemplate(NbtHelper.fromNbtProviderString(string)));
                if (bufferedReader == null) break block10;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (NoSuchFileException noSuchFileException) {
                    return Optional.empty();
                } catch (CommandSyntaxException | IOException exception) {
                    LOGGER.error("Couldn't load structure from {}", (Object)path2, (Object)exception);
                    return Optional.empty();
                }
            }
            bufferedReader.close();
        }
        return optional;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private Optional<StructureTemplate> loadTemplate(TemplateFileOpener opener, Consumer<Throwable> exceptionConsumer) {
        try (InputStream inputStream = opener.open();){
            Optional<StructureTemplate> optional;
            try (FixedBufferInputStream inputStream2 = new FixedBufferInputStream(inputStream);){
                optional = Optional.of(this.readTemplate(inputStream2));
            }
            return optional;
        } catch (FileNotFoundException fileNotFoundException) {
            return Optional.empty();
        } catch (Throwable throwable) {
            exceptionConsumer.accept(throwable);
            return Optional.empty();
        }
    }

    private StructureTemplate readTemplate(InputStream templateIInputStream) throws IOException {
        NbtCompound lv = NbtIo.readCompressed(templateIInputStream, NbtSizeTracker.ofUnlimitedBytes());
        return this.createTemplate(lv);
    }

    public StructureTemplate createTemplate(NbtCompound nbt) {
        StructureTemplate lv = new StructureTemplate();
        int i = NbtHelper.getDataVersion(nbt, 500);
        lv.readNbt(this.blockLookup, DataFixTypes.STRUCTURE.update(this.dataFixer, nbt, i));
        return lv;
    }

    public boolean saveTemplate(Identifier id) {
        Optional<StructureTemplate> optional = this.templates.get(id);
        if (optional.isEmpty()) {
            return false;
        }
        StructureTemplate lv = optional.get();
        Path path = StructureTemplateManager.getAndCheckTemplatePath(this.generatedPath, id, NBT_FILE_EXTENSION);
        Path path2 = path.getParent();
        if (path2 == null) {
            return false;
        }
        try {
            Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath(new LinkOption[0]) : path2, new FileAttribute[0]);
        } catch (IOException iOException) {
            LOGGER.error("Failed to create parent directory: {}", (Object)path2);
            return false;
        }
        NbtCompound lv2 = lv.writeNbt(new NbtCompound());
        try (FileOutputStream outputStream = new FileOutputStream(path.toFile());){
            NbtIo.writeCompressed(lv2, outputStream);
        } catch (Throwable throwable) {
            return false;
        }
        return true;
    }

    public Path getTemplatePath(Identifier id, String extension) {
        return StructureTemplateManager.getTemplatePath(this.generatedPath, id, extension);
    }

    public static Path getTemplatePath(Path path, Identifier id, String extension) {
        try {
            Path path2 = path.resolve(id.getNamespace());
            Path path3 = path2.resolve(STRUCTURES_DIRECTORY);
            return PathUtil.getResourcePath(path3, id.getPath(), extension);
        } catch (InvalidPathException invalidPathException) {
            throw new InvalidIdentifierException("Invalid resource path: " + String.valueOf(id), invalidPathException);
        }
    }

    private static Path getAndCheckTemplatePath(Path path, Identifier id, String extension) {
        if (id.getPath().contains("//")) {
            throw new InvalidIdentifierException("Invalid resource path: " + String.valueOf(id));
        }
        Path path2 = StructureTemplateManager.getTemplatePath(path, id, extension);
        if (!(path2.startsWith(path) && PathUtil.isNormal(path2) && PathUtil.isAllowedName(path2))) {
            throw new InvalidIdentifierException("Invalid resource path: " + String.valueOf(path2));
        }
        return path2;
    }

    public void unloadTemplate(Identifier id) {
        this.templates.remove(id);
    }

    record Provider(Function<Identifier, Optional<StructureTemplate>> loader, Supplier<Stream<Identifier>> lister) {
    }

    @FunctionalInterface
    static interface TemplateFileOpener {
        public InputStream open() throws IOException;
    }
}

