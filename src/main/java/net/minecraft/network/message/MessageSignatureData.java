/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.SignatureUpdatable;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public record MessageSignatureData(byte[] data) {
    public static final Codec<MessageSignatureData> CODEC = Codecs.BASE_64.xmap(MessageSignatureData::new, MessageSignatureData::data);
    public static final int SIZE = 256;

    public MessageSignatureData {
        Preconditions.checkState(bs.length == 256, "Invalid message signature size");
    }

    public static MessageSignatureData fromBuf(PacketByteBuf buf) {
        byte[] bs = new byte[256];
        buf.readBytes(bs);
        return new MessageSignatureData(bs);
    }

    public static void write(PacketByteBuf buf, MessageSignatureData signature) {
        buf.writeBytes(signature.data);
    }

    public boolean verify(SignatureVerifier verifier, SignatureUpdatable updatable) {
        return verifier.validate(updatable, this.data);
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(this.data);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageSignatureData)) return false;
        MessageSignatureData lv = (MessageSignatureData)o;
        if (!Arrays.equals(this.data, lv.data)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.data);
    }

    public Indexed pack(MessageSignatureStorage storage) {
        int i = storage.indexOf(this);
        return i != -1 ? new Indexed(i) : new Indexed(this);
    }

    public record Indexed(int id, @Nullable MessageSignatureData fullSignature) {
        public static final int MISSING_ID = -1;

        public Indexed(MessageSignatureData signature) {
            this(-1, signature);
        }

        public Indexed(int id) {
            this(id, null);
        }

        public static Indexed fromBuf(PacketByteBuf buf) {
            int i = buf.readVarInt() - 1;
            if (i == -1) {
                return new Indexed(MessageSignatureData.fromBuf(buf));
            }
            return new Indexed(i);
        }

        public static void write(PacketByteBuf buf, Indexed indexed) {
            buf.writeVarInt(indexed.id() + 1);
            if (indexed.fullSignature() != null) {
                MessageSignatureData.write(buf, indexed.fullSignature());
            }
        }

        public Optional<MessageSignatureData> getSignature(MessageSignatureStorage storage) {
            if (this.fullSignature != null) {
                return Optional.of(this.fullSignature);
            }
            return Optional.ofNullable(storage.get(this.id));
        }

        @Nullable
        public MessageSignatureData fullSignature() {
            return this.fullSignature;
        }
    }
}

