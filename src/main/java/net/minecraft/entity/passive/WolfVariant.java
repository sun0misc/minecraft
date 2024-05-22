/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public final class WolfVariant {
    public static final Codec<WolfVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("wild_texture")).forGetter(wolfVariant -> wolfVariant.wildId), ((MapCodec)Identifier.CODEC.fieldOf("tame_texture")).forGetter(wolfVariant -> wolfVariant.tameId), ((MapCodec)Identifier.CODEC.fieldOf("angry_texture")).forGetter(wolfVariant -> wolfVariant.angryId), ((MapCodec)RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biomes")).forGetter(WolfVariant::getBiomes)).apply((Applicative<WolfVariant, ?>)instance, WolfVariant::new));
    public static final PacketCodec<RegistryByteBuf, WolfVariant> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, WolfVariant::getWildTextureId, Identifier.PACKET_CODEC, WolfVariant::getTameTextureId, Identifier.PACKET_CODEC, WolfVariant::getAngryTextureId, PacketCodecs.registryEntryList(RegistryKeys.BIOME), WolfVariant::getBiomes, WolfVariant::new);
    public static final Codec<RegistryEntry<WolfVariant>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.WOLF_VARIANT, CODEC);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<WolfVariant>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.WOLF_VARIANT, PACKET_CODEC);
    private final Identifier wildId;
    private final Identifier tameId;
    private final Identifier angryId;
    private final Identifier wildTextureId;
    private final Identifier tameTextureId;
    private final Identifier angryTextureId;
    private final RegistryEntryList<Biome> biomes;

    public WolfVariant(Identifier wildId, Identifier tameId, Identifier angryId, RegistryEntryList<Biome> biomes) {
        this.wildId = wildId;
        this.wildTextureId = WolfVariant.getTextureId(wildId);
        this.tameId = tameId;
        this.tameTextureId = WolfVariant.getTextureId(tameId);
        this.angryId = angryId;
        this.angryTextureId = WolfVariant.getTextureId(angryId);
        this.biomes = biomes;
    }

    private static Identifier getTextureId(Identifier id) {
        return id.withPath(oldPath -> "textures/" + oldPath + ".png");
    }

    public Identifier getWildTextureId() {
        return this.wildTextureId;
    }

    public Identifier getTameTextureId() {
        return this.tameTextureId;
    }

    public Identifier getAngryTextureId() {
        return this.angryTextureId;
    }

    public RegistryEntryList<Biome> getBiomes() {
        return this.biomes;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof WolfVariant) {
            WolfVariant lv = (WolfVariant)o;
            return Objects.equals(this.wildId, lv.wildId) && Objects.equals(this.tameId, lv.tameId) && Objects.equals(this.angryId, lv.angryId) && Objects.equals(this.biomes, lv.biomes);
        }
        return false;
    }

    public int hashCode() {
        int i = 1;
        i = 31 * i + this.wildId.hashCode();
        i = 31 * i + this.tameId.hashCode();
        i = 31 * i + this.angryId.hashCode();
        i = 31 * i + this.biomes.hashCode();
        return i;
    }
}

