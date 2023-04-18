package net.minecraft.network.packet.c2s.play;

import java.time.Instant;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.Packet;

public record CommandExecutionC2SPacket(String command, Instant timestamp, long salt, ArgumentSignatureDataMap argumentSignatures, LastSeenMessageList.Acknowledgment acknowledgment) implements Packet {
   public CommandExecutionC2SPacket(PacketByteBuf buf) {
      this(buf.readString(256), buf.readInstant(), buf.readLong(), new ArgumentSignatureDataMap(buf), new LastSeenMessageList.Acknowledgment(buf));
   }

   public CommandExecutionC2SPacket(String string, Instant timestamp, long salt, ArgumentSignatureDataMap argumentSignatures, LastSeenMessageList.Acknowledgment arg2) {
      this.command = string;
      this.timestamp = timestamp;
      this.salt = salt;
      this.argumentSignatures = argumentSignatures;
      this.acknowledgment = arg2;
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.command, 256);
      buf.writeInstant(this.timestamp);
      buf.writeLong(this.salt);
      this.argumentSignatures.write(buf);
      this.acknowledgment.write(buf);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onCommandExecution(this);
   }

   public String command() {
      return this.command;
   }

   public Instant timestamp() {
      return this.timestamp;
   }

   public long salt() {
      return this.salt;
   }

   public ArgumentSignatureDataMap argumentSignatures() {
      return this.argumentSignatures;
   }

   public LastSeenMessageList.Acknowledgment acknowledgment() {
      return this.acknowledgment;
   }
}
