/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.encryption;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.PrivateKey;
import java.time.Instant;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.dynamic.Codecs;

public record PlayerKeyPair(PrivateKey privateKey, PlayerPublicKey publicKey, Instant refreshedAfter) {
    public static final Codec<PlayerKeyPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)NetworkEncryptionUtils.RSA_PRIVATE_KEY_CODEC.fieldOf("private_key")).forGetter(PlayerKeyPair::privateKey), ((MapCodec)PlayerPublicKey.CODEC.fieldOf("public_key")).forGetter(PlayerKeyPair::publicKey), ((MapCodec)Codecs.INSTANT.fieldOf("refreshed_after")).forGetter(PlayerKeyPair::refreshedAfter)).apply((Applicative<PlayerKeyPair, ?>)instance, PlayerKeyPair::new));

    public boolean isExpired() {
        return this.refreshedAfter.isBefore(Instant.now());
    }
}

