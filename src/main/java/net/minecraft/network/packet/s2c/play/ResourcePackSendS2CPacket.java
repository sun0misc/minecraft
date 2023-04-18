package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ResourcePackSendS2CPacket implements Packet {
   public static final int MAX_HASH_LENGTH = 40;
   private final String url;
   private final String hash;
   private final boolean required;
   @Nullable
   private final Text prompt;

   public ResourcePackSendS2CPacket(String url, String hash, boolean required, @Nullable Text prompt) {
      if (hash.length() > 40) {
         throw new IllegalArgumentException("Hash is too long (max 40, was " + hash.length() + ")");
      } else {
         this.url = url;
         this.hash = hash;
         this.required = required;
         this.prompt = prompt;
      }
   }

   public ResourcePackSendS2CPacket(PacketByteBuf buf) {
      this.url = buf.readString();
      this.hash = buf.readString(40);
      this.required = buf.readBoolean();
      this.prompt = (Text)buf.readNullable(PacketByteBuf::readText);
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.url);
      buf.writeString(this.hash);
      buf.writeBoolean(this.required);
      buf.writeNullable(this.prompt, PacketByteBuf::writeText);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onResourcePackSend(this);
   }

   public String getURL() {
      return this.url;
   }

   public String getSHA1() {
      return this.hash;
   }

   public boolean isRequired() {
      return this.required;
   }

   @Nullable
   public Text getPrompt() {
      return this.prompt;
   }
}
