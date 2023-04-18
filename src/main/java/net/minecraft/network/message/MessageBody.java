package net.minecraft.network.message;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.SignatureUpdatable;
import net.minecraft.util.dynamic.Codecs;

public record MessageBody(String content, Instant timestamp, long salt, LastSeenMessageList lastSeenMessages) {
   public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      return instance.group(Codec.STRING.fieldOf("content").forGetter(MessageBody::content), Codecs.INSTANT.fieldOf("time_stamp").forGetter(MessageBody::timestamp), Codec.LONG.fieldOf("salt").forGetter(MessageBody::salt), LastSeenMessageList.CODEC.optionalFieldOf("last_seen", LastSeenMessageList.EMPTY).forGetter(MessageBody::lastSeenMessages)).apply(instance, MessageBody::new);
   });

   public MessageBody(String string, Instant instant, long l, LastSeenMessageList arg) {
      this.content = string;
      this.timestamp = instant;
      this.salt = l;
      this.lastSeenMessages = arg;
   }

   public static MessageBody ofUnsigned(String content) {
      return new MessageBody(content, Instant.now(), 0L, LastSeenMessageList.EMPTY);
   }

   public void update(SignatureUpdatable.SignatureUpdater updater) throws SignatureException {
      updater.update(Longs.toByteArray(this.salt));
      updater.update(Longs.toByteArray(this.timestamp.getEpochSecond()));
      byte[] bs = this.content.getBytes(StandardCharsets.UTF_8);
      updater.update(Ints.toByteArray(bs.length));
      updater.update(bs);
      this.lastSeenMessages.updateSignatures(updater);
   }

   public Serialized toSerialized(MessageSignatureStorage storage) {
      return new Serialized(this.content, this.timestamp, this.salt, this.lastSeenMessages.pack(storage));
   }

   public String content() {
      return this.content;
   }

   public Instant timestamp() {
      return this.timestamp;
   }

   public long salt() {
      return this.salt;
   }

   public LastSeenMessageList lastSeenMessages() {
      return this.lastSeenMessages;
   }

   public static record Serialized(String content, Instant timestamp, long salt, LastSeenMessageList.Indexed lastSeen) {
      public Serialized(PacketByteBuf buf) {
         this(buf.readString(256), buf.readInstant(), buf.readLong(), new LastSeenMessageList.Indexed(buf));
      }

      public Serialized(String string, Instant instant, long l, LastSeenMessageList.Indexed arg) {
         this.content = string;
         this.timestamp = instant;
         this.salt = l;
         this.lastSeen = arg;
      }

      public void write(PacketByteBuf buf) {
         buf.writeString(this.content, 256);
         buf.writeInstant(this.timestamp);
         buf.writeLong(this.salt);
         this.lastSeen.write(buf);
      }

      public Optional toBody(MessageSignatureStorage storage) {
         return this.lastSeen.unpack(storage).map((lastSeenMessages) -> {
            return new MessageBody(this.content, this.timestamp, this.salt, lastSeenMessages);
         });
      }

      public String content() {
         return this.content;
      }

      public Instant timestamp() {
         return this.timestamp;
      }

      public long salt() {
         return this.salt;
      }

      public LastSeenMessageList.Indexed lastSeen() {
         return this.lastSeen;
      }
   }
}
