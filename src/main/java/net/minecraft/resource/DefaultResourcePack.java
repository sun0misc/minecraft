/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class DefaultResourcePack
implements ResourcePack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourcePackInfo info;
    private final ResourceMetadataMap metadata;
    private final Set<String> namespaces;
    private final List<Path> rootPaths;
    private final Map<ResourceType, List<Path>> namespacePaths;

    DefaultResourcePack(ResourcePackInfo info, ResourceMetadataMap metadata, Set<String> namespaces, List<Path> rootPaths, Map<ResourceType, List<Path>> namespacePaths) {
        this.info = info;
        this.metadata = metadata;
        this.namespaces = namespaces;
        this.rootPaths = rootPaths;
        this.namespacePaths = namespacePaths;
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> openRoot(String ... segments) {
        PathUtil.validatePath(segments);
        List<String> list = List.of(segments);
        for (Path path : this.rootPaths) {
            Path path2 = PathUtil.getPath(path, list);
            if (!Files.exists(path2, new LinkOption[0]) || !DirectoryResourcePack.isValidPath(path2)) continue;
            return InputSupplier.create(path2);
        }
        return null;
    }

    public void forEachNamespacedPath(ResourceType type, Identifier path, Consumer<Path> consumer) {
        PathUtil.split(path.getPath()).ifSuccess(segments -> {
            String string = path.getNamespace();
            for (Path path : this.namespacePaths.get((Object)type)) {
                Path path2 = path.resolve(string);
                consumer.accept(PathUtil.getPath(path2, segments));
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)path, (Object)error.message()));
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResourcePack.ResultConsumer consumer) {
        PathUtil.split(prefix).ifSuccess(segments -> {
            List<Path> list2 = this.namespacePaths.get((Object)type);
            int i = list2.size();
            if (i == 1) {
                DefaultResourcePack.collectIdentifiers(consumer, namespace, list2.get(0), segments);
            } else if (i > 1) {
                HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
                for (int j = 0; j < i - 1; ++j) {
                    DefaultResourcePack.collectIdentifiers(map::putIfAbsent, namespace, list2.get(j), segments);
                }
                Path path = list2.get(i - 1);
                if (map.isEmpty()) {
                    DefaultResourcePack.collectIdentifiers(consumer, namespace, path, segments);
                } else {
                    DefaultResourcePack.collectIdentifiers(map::putIfAbsent, namespace, path, segments);
                    map.forEach(consumer);
                }
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)prefix, (Object)error.message()));
    }

    private static void collectIdentifiers(ResourcePack.ResultConsumer consumer, String namespace, Path root, List<String> prefixSegments) {
        Path path2 = root.resolve(namespace);
        DirectoryResourcePack.findResources(namespace, path2, prefixSegments, consumer);
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        return PathUtil.split(id.getPath()).mapOrElse(segments -> {
            String string = id.getNamespace();
            for (Path path : this.namespacePaths.get((Object)type)) {
                Path path2 = PathUtil.getPath(path.resolve(string), segments);
                if (!Files.exists(path2, new LinkOption[0]) || !DirectoryResourcePack.isValidPath(path2)) continue;
                return InputSupplier.create(path2);
            }
            return null;
        }, error -> {
            LOGGER.error("Invalid path {}: {}", (Object)id, (Object)error.message());
            return null;
        });
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return this.namespaces;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    @Nullable
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
        InputSupplier<InputStream> lv = this.openRoot("pack.mcmeta");
        if (lv == null) return this.metadata.get(metaReader);
        try (InputStream inputStream = lv.get();){
            T object = AbstractFileResourcePack.parseMetadata(metaReader, inputStream);
            if (object == null) return this.metadata.get(metaReader);
            T t = object;
            return t;
        } catch (IOException iOException) {
            // empty catch block
        }
        return this.metadata.get(metaReader);
    }

    @Override
    public ResourcePackInfo getInfo() {
        return this.info;
    }

    @Override
    public void close() {
    }

    public ResourceFactory getFactory() {
        return id -> Optional.ofNullable(this.open(ResourceType.CLIENT_RESOURCES, id)).map(stream -> new Resource(this, (InputSupplier<InputStream>)stream));
    }
}

