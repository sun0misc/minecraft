/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.message;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MessageChain {
    static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    MessageLink link;
    Instant lastTimestamp = Instant.EPOCH;

    public MessageChain(UUID sender, UUID sessionId) {
        this.link = MessageLink.of(sender, sessionId);
    }

    public Packer getPacker(Signer signer) {
        return body -> {
            MessageLink lv = this.link;
            if (lv == null) {
                return null;
            }
            this.link = lv.next();
            return new MessageSignatureData(signer.sign(updatable -> SignedMessage.update(updatable, lv, body)));
        };
    }

    public Unpacker getUnpacker(final PlayerPublicKey playerPublicKey) {
        final SignatureVerifier lv = playerPublicKey.createSignatureInstance();
        return new Unpacker(){

            @Override
            public SignedMessage unpack(@Nullable MessageSignatureData arg, MessageBody arg2) throws MessageChainException {
                if (arg == null) {
                    throw new MessageChainException(MessageChainException.MISSING_PROFILE_KEY_EXCEPTION);
                }
                if (playerPublicKey.data().isExpired()) {
                    throw new MessageChainException(MessageChainException.EXPIRED_PROFILE_KEY_EXCEPTION);
                }
                MessageLink lv3 = MessageChain.this.link;
                if (lv3 == null) {
                    throw new MessageChainException(MessageChainException.CHAIN_BROKEN_EXCEPTION);
                }
                if (arg2.timestamp().isBefore(MessageChain.this.lastTimestamp)) {
                    this.setChainBroken();
                    throw new MessageChainException(MessageChainException.OUT_OF_ORDER_CHAT_EXCEPTION);
                }
                MessageChain.this.lastTimestamp = arg2.timestamp();
                SignedMessage lv2 = new SignedMessage(lv3, arg, arg2, null, FilterMask.PASS_THROUGH);
                if (!lv2.verify(lv)) {
                    this.setChainBroken();
                    throw new MessageChainException(MessageChainException.INVALID_SIGNATURE_EXCEPTION);
                }
                if (lv2.isExpiredOnServer(Instant.now())) {
                    LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)arg2.content());
                }
                MessageChain.this.link = lv3.next();
                return lv2;
            }

            @Override
            public void setChainBroken() {
                MessageChain.this.link = null;
            }
        };
    }

    @FunctionalInterface
    public static interface Packer {
        public static final Packer NONE = body -> null;

        @Nullable
        public MessageSignatureData pack(MessageBody var1);
    }

    public static class MessageChainException
    extends TextifiedException {
        static final Text MISSING_PROFILE_KEY_EXCEPTION = Text.translatable("chat.disabled.missingProfileKey");
        static final Text CHAIN_BROKEN_EXCEPTION = Text.translatable("chat.disabled.chain_broken");
        static final Text EXPIRED_PROFILE_KEY_EXCEPTION = Text.translatable("chat.disabled.expiredProfileKey");
        static final Text INVALID_SIGNATURE_EXCEPTION = Text.translatable("chat.disabled.invalid_signature");
        static final Text OUT_OF_ORDER_CHAT_EXCEPTION = Text.translatable("chat.disabled.out_of_order_chat");

        public MessageChainException(Text message) {
            super(message);
        }
    }

    @FunctionalInterface
    public static interface Unpacker {
        public static Unpacker unsigned(UUID sender, BooleanSupplier secureProfileEnforced) {
            return (signature, body) -> {
                if (secureProfileEnforced.getAsBoolean()) {
                    throw new MessageChainException(MessageChainException.MISSING_PROFILE_KEY_EXCEPTION);
                }
                return SignedMessage.ofUnsigned(sender, body.content());
            };
        }

        public SignedMessage unpack(@Nullable MessageSignatureData var1, MessageBody var2) throws MessageChainException;

        default public void setChainBroken() {
        }
    }
}

