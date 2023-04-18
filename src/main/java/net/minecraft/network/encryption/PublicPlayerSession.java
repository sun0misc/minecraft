package net.minecraft.network.encryption;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageVerifier;

public record PublicPlayerSession(UUID sessionId, PlayerPublicKey publicKeyData) {
   public PublicPlayerSession(UUID uUID, PlayerPublicKey arg) {
      this.sessionId = uUID;
      this.publicKeyData = arg;
   }

   public MessageVerifier createVerifier() {
      return new MessageVerifier.Impl(this.publicKeyData.createSignatureInstance());
   }

   public MessageChain.Unpacker createUnpacker(UUID sender) {
      return (new MessageChain(sender, this.sessionId)).getUnpacker(this.publicKeyData);
   }

   public Serialized toSerialized() {
      return new Serialized(this.sessionId, this.publicKeyData.data());
   }

   public UUID sessionId() {
      return this.sessionId;
   }

   public PlayerPublicKey publicKeyData() {
      return this.publicKeyData;
   }

   public static record Serialized(UUID sessionId, PlayerPublicKey.PublicKeyData publicKeyData) {
      public Serialized(UUID uUID, PlayerPublicKey.PublicKeyData arg) {
         this.sessionId = uUID;
         this.publicKeyData = arg;
      }

      public static Serialized fromBuf(PacketByteBuf buf) {
         return new Serialized(buf.readUuid(), new PlayerPublicKey.PublicKeyData(buf));
      }

      public static void write(PacketByteBuf buf, Serialized serialized) {
         buf.writeUuid(serialized.sessionId);
         serialized.publicKeyData.write(buf);
      }

      public PublicPlayerSession toSession(GameProfile gameProfile, SignatureVerifier servicesSignatureVerifier, Duration gracePeriod) throws PlayerPublicKey.PublicKeyException {
         return new PublicPlayerSession(this.sessionId, PlayerPublicKey.verifyAndDecode(servicesSignatureVerifier, gameProfile.getId(), this.publicKeyData, gracePeriod));
      }

      public UUID sessionId() {
         return this.sessionId;
      }

      public PlayerPublicKey.PublicKeyData publicKeyData() {
         return this.publicKeyData;
      }
   }
}
