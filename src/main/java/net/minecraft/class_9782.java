/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.function.ValueLists;

public record class_9782(List<class_9783> entries) {
    public static final class_9782 field_51977 = new class_9782(List.of());
    public static final PacketCodec<ByteBuf, class_9782> field_51978 = PacketCodec.tuple(class_9783.field_51980.collect(PacketCodecs.toList()), class_9782::entries, class_9782::new);

    public boolean method_60657() {
        return this.entries.isEmpty();
    }

    public Optional<class_9783> method_60658(class_9784 arg) {
        return this.entries.stream().filter(arg22 -> arg22.type.map(arg2 -> arg2 == arg, arg -> false)).findFirst();
    }

    public static enum class_9784 {
        BUG_REPORT(0, "report_bug");

        private static final IntFunction<class_9784> field_51983;
        public static final PacketCodec<ByteBuf, class_9784> field_51982;
        private final int field_51984;
        private final String field_51985;

        private class_9784(int j, String string2) {
            this.field_51984 = j;
            this.field_51985 = string2;
        }

        private Text method_60666() {
            return Text.translatable("known_server_link." + this.field_51985);
        }

        public class_9783 method_60668(String string) {
            return class_9783.method_60663(this, string);
        }

        static {
            field_51983 = ValueLists.createIdToValueFunction(arg -> arg.field_51984, class_9784.values(), ValueLists.OutOfBoundsHandling.ZERO);
            field_51982 = PacketCodecs.indexed(field_51983, arg -> arg.field_51984);
        }
    }

    public record class_9783(Either<class_9784, Text> type, String url) {
        public static final PacketCodec<ByteBuf, Either<class_9784, Text>> field_51979 = PacketCodecs.either(class_9784.field_51982, TextCodecs.PACKET_CODEC);
        public static final PacketCodec<ByteBuf, class_9783> field_51980 = PacketCodec.tuple(field_51979, class_9783::type, PacketCodecs.STRING, class_9783::url, class_9783::new);

        public static class_9783 method_60663(class_9784 arg, String string) {
            return new class_9783(Either.left(arg), string);
        }

        public static class_9783 method_60665(Text arg, String string) {
            return new class_9783(Either.right(arg), string);
        }

        public Text method_60662() {
            return this.type.map(class_9784::method_60666, arg -> arg);
        }
    }
}

