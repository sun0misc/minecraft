package net.minecraft.network.packet.s2c.play;

import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record ChatMessageS2CPacket(UUID sender, int index, @Nullable MessageSignatureData signature, MessageBody.Serialized body, @Nullable Text unsignedContent, FilterMask filterMask, MessageType.Serialized serializedParameters) implements Packet {
   public ChatMessageS2CPacket(PacketByteBuf buf) {
      this(buf.readUuid(), buf.readVarInt(), (MessageSignatureData)buf.readNullable(MessageSignatureData::fromBuf), new MessageBody.Serialized(buf), (Text)buf.readNullable(PacketByteBuf::readText), FilterMask.readMask(buf), new MessageType.Serialized(buf));
   }

   public ChatMessageS2CPacket(UUID uUID, int i, @Nullable MessageSignatureData arg, MessageBody.Serialized arg2, @Nullable Text arg3, FilterMask arg4, MessageType.Serialized arg5) {
      this.sender = uUID;
      this.index = i;
      this.signature = arg;
      this.body = arg2;
      this.unsignedContent = arg3;
      this.filterMask = arg4;
      this.serializedParameters = arg5;
   }

   public void write(PacketByteBuf buf) {
      buf.writeUuid(this.sender);
      buf.writeVarInt(this.index);
      buf.writeNullable(this.signature, MessageSignatureData::write);
      this.body.write(buf);
      buf.writeNullable(this.unsignedContent, PacketByteBuf::writeText);
      FilterMask.writeMask(buf, this.filterMask);
      this.serializedParameters.write(buf);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onChatMessage(this);
   }

   public boolean isWritingErrorSkippable() {
      return true;
   }

   public UUID sender() {
      return this.sender;
   }

   public int index() {
      return this.index;
   }

   @Nullable
   public MessageSignatureData signature() {
      return this.signature;
   }

   public MessageBody.Serialized body() {
      return this.body;
   }

   @Nullable
   public Text unsignedContent() {
      return this.unsignedContent;
   }

   public FilterMask filterMask() {
      return this.filterMask;
   }

   public MessageType.Serialized serializedParameters() {
      return this.serializedParameters;
   }
}
