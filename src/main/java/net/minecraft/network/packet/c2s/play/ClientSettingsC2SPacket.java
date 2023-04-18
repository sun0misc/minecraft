package net.minecraft.network.packet.c2s.play;

import net.minecraft.client.option.ChatVisibility;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Arm;

public record ClientSettingsC2SPacket(String language, int viewDistance, ChatVisibility chatVisibility, boolean chatColors, int playerModelBitMask, Arm mainArm, boolean filterText, boolean allowsListing) implements Packet {
   public static final int MAX_LANGUAGE_LENGTH = 16;

   public ClientSettingsC2SPacket(PacketByteBuf buf) {
      this(buf.readString(16), buf.readByte(), (ChatVisibility)buf.readEnumConstant(ChatVisibility.class), buf.readBoolean(), buf.readUnsignedByte(), (Arm)buf.readEnumConstant(Arm.class), buf.readBoolean(), buf.readBoolean());
   }

   public ClientSettingsC2SPacket(String language, int viewDistance, ChatVisibility chatVisibility, boolean chatColors, int modelBitMask, Arm mainArm, boolean filterText, boolean bl3) {
      this.language = language;
      this.viewDistance = viewDistance;
      this.chatVisibility = chatVisibility;
      this.chatColors = chatColors;
      this.playerModelBitMask = modelBitMask;
      this.mainArm = mainArm;
      this.filterText = filterText;
      this.allowsListing = bl3;
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.language);
      buf.writeByte(this.viewDistance);
      buf.writeEnumConstant(this.chatVisibility);
      buf.writeBoolean(this.chatColors);
      buf.writeByte(this.playerModelBitMask);
      buf.writeEnumConstant(this.mainArm);
      buf.writeBoolean(this.filterText);
      buf.writeBoolean(this.allowsListing);
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onClientSettings(this);
   }

   public String language() {
      return this.language;
   }

   public int viewDistance() {
      return this.viewDistance;
   }

   public ChatVisibility chatVisibility() {
      return this.chatVisibility;
   }

   public boolean chatColors() {
      return this.chatColors;
   }

   public int playerModelBitMask() {
      return this.playerModelBitMask;
   }

   public Arm mainArm() {
      return this.mainArm;
   }

   public boolean filterText() {
      return this.filterText;
   }

   public boolean allowsListing() {
      return this.allowsListing;
   }
}
