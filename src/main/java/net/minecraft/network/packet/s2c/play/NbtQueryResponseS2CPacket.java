package net.minecraft.network.packet.s2c.play;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public class NbtQueryResponseS2CPacket implements Packet {
   private final int transactionId;
   @Nullable
   private final NbtCompound nbt;

   public NbtQueryResponseS2CPacket(int transactionId, @Nullable NbtCompound nbt) {
      this.transactionId = transactionId;
      this.nbt = nbt;
   }

   public NbtQueryResponseS2CPacket(PacketByteBuf buf) {
      this.transactionId = buf.readVarInt();
      this.nbt = buf.readNbt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.transactionId);
      buf.writeNbt(this.nbt);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onNbtQueryResponse(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   @Nullable
   public NbtCompound getNbt() {
      return this.nbt;
   }

   public boolean isWritingErrorSkippable() {
      return true;
   }
}
