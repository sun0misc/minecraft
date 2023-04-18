package net.minecraft.network.message;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.SignatureUpdatable;

public record LastSeenMessageList(List entries) {
   public static final Codec CODEC;
   public static LastSeenMessageList EMPTY;
   public static final int MAX_ENTRIES = 20;

   public LastSeenMessageList(List list) {
      this.entries = list;
   }

   public void updateSignatures(SignatureUpdatable.SignatureUpdater updater) throws SignatureException {
      updater.update(Ints.toByteArray(this.entries.size()));
      Iterator var2 = this.entries.iterator();

      while(var2.hasNext()) {
         MessageSignatureData lv = (MessageSignatureData)var2.next();
         updater.update(lv.data());
      }

   }

   public Indexed pack(MessageSignatureStorage storage) {
      return new Indexed(this.entries.stream().map((signature) -> {
         return signature.pack(storage);
      }).toList());
   }

   public List entries() {
      return this.entries;
   }

   static {
      CODEC = MessageSignatureData.CODEC.listOf().xmap(LastSeenMessageList::new, LastSeenMessageList::entries);
      EMPTY = new LastSeenMessageList(List.of());
   }

   public static record Indexed(List buf) {
      public static final Indexed EMPTY = new Indexed(List.of());

      public Indexed(PacketByteBuf buf) {
         this((List)buf.readCollection(PacketByteBuf.getMaxValidator(ArrayList::new, 20), MessageSignatureData.Indexed::fromBuf));
      }

      public Indexed(List list) {
         this.buf = list;
      }

      public void write(PacketByteBuf buf) {
         buf.writeCollection(this.buf, MessageSignatureData.Indexed::write);
      }

      public Optional unpack(MessageSignatureStorage storage) {
         List list = new ArrayList(this.buf.size());
         Iterator var3 = this.buf.iterator();

         while(var3.hasNext()) {
            MessageSignatureData.Indexed lv = (MessageSignatureData.Indexed)var3.next();
            Optional optional = lv.getSignature(storage);
            if (optional.isEmpty()) {
               return Optional.empty();
            }

            list.add((MessageSignatureData)optional.get());
         }

         return Optional.of(new LastSeenMessageList(list));
      }

      public List buf() {
         return this.buf;
      }
   }

   public static record Acknowledgment(int offset, BitSet acknowledged) {
      public Acknowledgment(PacketByteBuf buf) {
         this(buf.readVarInt(), buf.readBitSet(20));
      }

      public Acknowledgment(int i, BitSet bitSet) {
         this.offset = i;
         this.acknowledged = bitSet;
      }

      public void write(PacketByteBuf buf) {
         buf.writeVarInt(this.offset);
         buf.writeBitSet(this.acknowledged, 20);
      }

      public int offset() {
         return this.offset;
      }

      public BitSet acknowledged() {
         return this.acknowledged;
      }
   }
}
