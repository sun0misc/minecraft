/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource.fs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import org.jetbrains.annotations.Nullable;

abstract class ResourceFileAttributes
implements BasicFileAttributes {
    private static final FileTime EPOCH = FileTime.fromMillis(0L);

    ResourceFileAttributes() {
    }

    @Override
    public FileTime lastModifiedTime() {
        return EPOCH;
    }

    @Override
    public FileTime lastAccessTime() {
        return EPOCH;
    }

    @Override
    public FileTime creationTime() {
        return EPOCH;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return 0L;
    }

    @Override
    @Nullable
    public Object fileKey() {
        return null;
    }
}

