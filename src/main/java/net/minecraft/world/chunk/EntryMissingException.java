/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

public class EntryMissingException
extends RuntimeException {
    public EntryMissingException(int index) {
        super("Missing Palette entry for index " + index + ".");
    }
}

