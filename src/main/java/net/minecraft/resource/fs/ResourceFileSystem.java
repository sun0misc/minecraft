/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource.fs;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resource.fs.ResourceFile;
import net.minecraft.resource.fs.ResourceFileStore;
import net.minecraft.resource.fs.ResourceFileSystemProvider;
import net.minecraft.resource.fs.ResourcePath;
import org.jetbrains.annotations.Nullable;

public class ResourceFileSystem
extends FileSystem {
    private static final Set<String> SUPPORTED_FILE_ATTRIBUTE_VIEWS = Set.of("basic");
    public static final String SEPARATOR = "/";
    private static final Splitter SEPARATOR_SPLITTER = Splitter.on('/');
    private final FileStore store;
    private final FileSystemProvider fileSystemProvider = new ResourceFileSystemProvider();
    private final ResourcePath root;

    ResourceFileSystem(String name, Directory root) {
        this.store = new ResourceFileStore(name);
        this.root = ResourceFileSystem.toResourcePath(root, this, "", null);
    }

    private static ResourcePath toResourcePath(Directory root, ResourceFileSystem fileSystem, String name, @Nullable ResourcePath parent) {
        Object2ObjectOpenHashMap<String, ResourcePath> object2ObjectOpenHashMap = new Object2ObjectOpenHashMap<String, ResourcePath>();
        ResourcePath lv = new ResourcePath(fileSystem, name, parent, new ResourceFile.Directory(object2ObjectOpenHashMap));
        root.files.forEach((fileName, path) -> object2ObjectOpenHashMap.put((String)fileName, new ResourcePath(fileSystem, (String)fileName, lv, new ResourceFile.File((Path)path))));
        root.children.forEach((directoryName, directory) -> object2ObjectOpenHashMap.put((String)directoryName, ResourceFileSystem.toResourcePath(directory, fileSystem, directoryName, lv)));
        object2ObjectOpenHashMap.trim();
        return lv;
    }

    @Override
    public FileSystemProvider provider() {
        return this.fileSystemProvider;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return List.of(this.root);
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return List.of(this.store);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return SUPPORTED_FILE_ATTRIBUTE_VIEWS;
    }

    @Override
    public Path getPath(String first, String ... more) {
        String string2;
        Stream<String> stream = Stream.of(first);
        if (more.length > 0) {
            stream = Stream.concat(stream, Stream.of(more));
        }
        if ((string2 = stream.collect(Collectors.joining(SEPARATOR))).equals(SEPARATOR)) {
            return this.root;
        }
        if (string2.startsWith(SEPARATOR)) {
            ResourcePath lv = this.root;
            for (String string3 : SEPARATOR_SPLITTER.split(string2.substring(1))) {
                if (string3.isEmpty()) {
                    throw new IllegalArgumentException("Empty paths not allowed");
                }
                lv = lv.get(string3);
            }
            return lv;
        }
        ResourcePath lv = null;
        for (String string3 : SEPARATOR_SPLITTER.split(string2)) {
            if (string3.isEmpty()) {
                throw new IllegalArgumentException("Empty paths not allowed");
            }
            lv = new ResourcePath(this, string3, lv, ResourceFile.RELATIVE);
        }
        if (lv == null) {
            throw new IllegalArgumentException("Empty paths not allowed");
        }
        return lv;
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException();
    }

    public FileStore getStore() {
        return this.store;
    }

    public ResourcePath getRoot() {
        return this.root;
    }

    public static Builder builder() {
        return new Builder();
    }

    record Directory(Map<String, Directory> children, Map<String, Path> files) {
        public Directory() {
            this(new HashMap<String, Directory>(), new HashMap<String, Path>());
        }
    }

    public static class Builder {
        private final Directory root = new Directory();

        public Builder withFile(List<String> directories, String name, Path path) {
            Directory lv = this.root;
            for (String string2 : directories) {
                lv = lv.children.computeIfAbsent(string2, directory -> new Directory());
            }
            lv.files.put(name, path);
            return this;
        }

        public Builder withFile(List<String> directories, Path path) {
            if (directories.isEmpty()) {
                throw new IllegalArgumentException("Path can't be empty");
            }
            int i = directories.size() - 1;
            return this.withFile(directories.subList(0, i), directories.get(i), path);
        }

        public FileSystem build(String name) {
            return new ResourceFileSystem(name, this.root);
        }
    }
}

