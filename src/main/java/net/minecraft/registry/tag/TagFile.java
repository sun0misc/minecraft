/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.tag;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.registry.tag.TagEntry;

public record TagFile(List<TagEntry> entries, boolean replace) {
    public static final Codec<TagFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TagEntry.CODEC.listOf().fieldOf("values")).forGetter(TagFile::entries), Codec.BOOL.optionalFieldOf("replace", false).forGetter(TagFile::replace)).apply((Applicative<TagFile, ?>)instance, TagFile::new));
}

