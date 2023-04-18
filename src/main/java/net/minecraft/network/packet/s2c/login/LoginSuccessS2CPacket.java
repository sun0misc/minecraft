package net.minecraft.network.packet.s2c.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.Packet;

public class LoginSuccessS2CPacket implements Packet {
   private final GameProfile profile;

   public LoginSuccessS2CPacket(GameProfile profile) {
      this.profile = profile;
   }

   public LoginSuccessS2CPacket(PacketByteBuf buf) {
      this.profile = buf.readGameProfile();
   }

   public void write(PacketByteBuf buf) {
      buf.writeGameProfile(this.profile);
   }

   public void apply(ClientLoginPacketListener arg) {
      arg.onSuccess(this);
   }

   public GameProfile getProfile() {
      return this.profile;
   }
}
