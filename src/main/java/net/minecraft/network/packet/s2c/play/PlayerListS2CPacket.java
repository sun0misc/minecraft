package net.minecraft.network.packet.s2c.play;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class PlayerListS2CPacket implements Packet {
   private final EnumSet actions;
   private final List entries;

   public PlayerListS2CPacket(EnumSet actions, Collection players) {
      this.actions = actions;
      this.entries = players.stream().map(Entry::new).toList();
   }

   public PlayerListS2CPacket(Action action, ServerPlayerEntity player) {
      this.actions = EnumSet.of(action);
      this.entries = List.of(new Entry(player));
   }

   public static PlayerListS2CPacket entryFromPlayer(Collection players) {
      EnumSet enumSet = EnumSet.of(PlayerListS2CPacket.Action.ADD_PLAYER, PlayerListS2CPacket.Action.INITIALIZE_CHAT, PlayerListS2CPacket.Action.UPDATE_GAME_MODE, PlayerListS2CPacket.Action.UPDATE_LISTED, PlayerListS2CPacket.Action.UPDATE_LATENCY, PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME);
      return new PlayerListS2CPacket(enumSet, players);
   }

   public PlayerListS2CPacket(PacketByteBuf buf) {
      this.actions = buf.readEnumSet(Action.class);
      this.entries = buf.readList((buf2) -> {
         Serialized lv = new Serialized(buf2.readUuid());
         Iterator var3 = this.actions.iterator();

         while(var3.hasNext()) {
            Action lv2 = (Action)var3.next();
            lv2.reader.read(lv, buf2);
         }

         return lv.toEntry();
      });
   }

   public void write(PacketByteBuf buf) {
      buf.writeEnumSet(this.actions, Action.class);
      buf.writeCollection(this.entries, (buf2, entry) -> {
         buf2.writeUuid(entry.profileId());
         Iterator var3 = this.actions.iterator();

         while(var3.hasNext()) {
            Action lv = (Action)var3.next();
            lv.writer.write(buf2, entry);
         }

      });
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onPlayerList(this);
   }

   public EnumSet getActions() {
      return this.actions;
   }

   public List getEntries() {
      return this.entries;
   }

   public List getPlayerAdditionEntries() {
      return this.actions.contains(PlayerListS2CPacket.Action.ADD_PLAYER) ? this.entries : List.of();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
   }

   public static record Entry(UUID profileId, GameProfile profile, boolean listed, int latency, GameMode gameMode, @Nullable Text displayName, @Nullable PublicPlayerSession.Serialized chatSession) {
      @Nullable
      final PublicPlayerSession.Serialized chatSession;

      Entry(ServerPlayerEntity player) {
         this(player.getUuid(), player.getGameProfile(), true, player.pingMilliseconds, player.interactionManager.getGameMode(), player.getPlayerListName(), (PublicPlayerSession.Serialized)Nullables.map(player.getSession(), PublicPlayerSession::toSerialized));
      }

      public Entry(UUID uUID, GameProfile gameProfile, boolean bl, int i, GameMode arg, @Nullable Text arg2, @Nullable PublicPlayerSession.Serialized arg3) {
         this.profileId = uUID;
         this.profile = gameProfile;
         this.listed = bl;
         this.latency = i;
         this.gameMode = arg;
         this.displayName = arg2;
         this.chatSession = arg3;
      }

      public UUID profileId() {
         return this.profileId;
      }

      public GameProfile profile() {
         return this.profile;
      }

      public boolean listed() {
         return this.listed;
      }

      public int latency() {
         return this.latency;
      }

      public GameMode gameMode() {
         return this.gameMode;
      }

      @Nullable
      public Text displayName() {
         return this.displayName;
      }

      @Nullable
      public PublicPlayerSession.Serialized chatSession() {
         return this.chatSession;
      }
   }

   public static enum Action {
      ADD_PLAYER((serialized, buf) -> {
         GameProfile gameProfile = new GameProfile(serialized.profileId, buf.readString(16));
         gameProfile.getProperties().putAll(buf.readPropertyMap());
         serialized.gameProfile = gameProfile;
      }, (buf, entry) -> {
         buf.writeString(entry.profile().getName(), 16);
         buf.writePropertyMap(entry.profile().getProperties());
      }),
      INITIALIZE_CHAT((serialized, buf) -> {
         serialized.session = (PublicPlayerSession.Serialized)buf.readNullable(PublicPlayerSession.Serialized::fromBuf);
      }, (buf, entry) -> {
         buf.writeNullable(entry.chatSession, PublicPlayerSession.Serialized::write);
      }),
      UPDATE_GAME_MODE((serialized, buf) -> {
         serialized.gameMode = GameMode.byId(buf.readVarInt());
      }, (buf, entry) -> {
         buf.writeVarInt(entry.gameMode().getId());
      }),
      UPDATE_LISTED((serialized, buf) -> {
         serialized.listed = buf.readBoolean();
      }, (buf, entry) -> {
         buf.writeBoolean(entry.listed());
      }),
      UPDATE_LATENCY((serialized, buf) -> {
         serialized.latency = buf.readVarInt();
      }, (buf, entry) -> {
         buf.writeVarInt(entry.latency());
      }),
      UPDATE_DISPLAY_NAME((serialized, buf) -> {
         serialized.displayName = (Text)buf.readNullable(PacketByteBuf::readText);
      }, (buf, entry) -> {
         buf.writeNullable(entry.displayName(), PacketByteBuf::writeText);
      });

      final Reader reader;
      final Writer writer;

      private Action(Reader reader, Writer writer) {
         this.reader = reader;
         this.writer = writer;
      }

      // $FF: synthetic method
      private static Action[] method_36951() {
         return new Action[]{ADD_PLAYER, INITIALIZE_CHAT, UPDATE_GAME_MODE, UPDATE_LISTED, UPDATE_LATENCY, UPDATE_DISPLAY_NAME};
      }

      public interface Reader {
         void read(Serialized serialized, PacketByteBuf buf);
      }

      public interface Writer {
         void write(PacketByteBuf buf, Entry entry);
      }
   }

   private static class Serialized {
      final UUID profileId;
      GameProfile gameProfile;
      boolean listed;
      int latency;
      GameMode gameMode;
      @Nullable
      Text displayName;
      @Nullable
      PublicPlayerSession.Serialized session;

      Serialized(UUID profileId) {
         this.gameMode = GameMode.DEFAULT;
         this.profileId = profileId;
         this.gameProfile = new GameProfile(profileId, (String)null);
      }

      Entry toEntry() {
         return new Entry(this.profileId, this.gameProfile, this.listed, this.latency, this.gameMode, this.displayName, this.session);
      }
   }
}
