/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.message;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.SignatureUpdatable;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;

public record LastSeenMessageList(List<MessageSignatureData> entries) {
    public static final Codec<LastSeenMessageList> CODEC = MessageSignatureData.CODEC.listOf().xmap(LastSeenMessageList::new, LastSeenMessageList::entries);
    public static LastSeenMessageList EMPTY = new LastSeenMessageList(List.of());
    public static final int MAX_ENTRIES = 20;

    public void updateSignatures(SignatureUpdatable.SignatureUpdater updater) throws SignatureException {
        updater.update(Ints.toByteArray(this.entries.size()));
        for (MessageSignatureData lv : this.entries) {
            updater.update(lv.data());
        }
    }

    public Indexed pack(MessageSignatureStorage storage) {
        return new Indexed(this.entries.stream().map(signature -> signature.pack(storage)).toList());
    }

    public record Indexed(List<MessageSignatureData.Indexed> buf) {
        public static final Indexed EMPTY = new Indexed(List.of());

        public Indexed(PacketByteBuf buf) {
            this(buf.readCollection(PacketByteBuf.getMaxValidator(ArrayList::new, 20), MessageSignatureData.Indexed::fromBuf));
        }

        public void write(PacketByteBuf buf) {
            buf.writeCollection(this.buf, MessageSignatureData.Indexed::write);
        }

        public Optional<LastSeenMessageList> unpack(MessageSignatureStorage storage) {
            ArrayList<MessageSignatureData> list = new ArrayList<MessageSignatureData>(this.buf.size());
            for (MessageSignatureData.Indexed lv : this.buf) {
                Optional<MessageSignatureData> optional = lv.getSignature(storage);
                if (optional.isEmpty()) {
                    return Optional.empty();
                }
                list.add(optional.get());
            }
            return Optional.of(new LastSeenMessageList(list));
        }
    }

    public record Acknowledgment(int offset, BitSet acknowledged) {
        public Acknowledgment(PacketByteBuf buf) {
            this(buf.readVarInt(), buf.readBitSet(20));
        }

        public void write(PacketByteBuf buf) {
            buf.writeVarInt(this.offset);
            buf.writeBitSet(this.acknowledged, 20);
        }
    }
}

