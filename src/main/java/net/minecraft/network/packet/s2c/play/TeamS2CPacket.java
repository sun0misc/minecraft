package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class TeamS2CPacket implements Packet {
   private static final int ADD = 0;
   private static final int REMOVE = 1;
   private static final int UPDATE = 2;
   private static final int ADD_PLAYERS = 3;
   private static final int REMOVE_PLAYERS = 4;
   private static final int FIRST_MAX_VISIBILITY_OR_COLLISION_RULE_LENGTH = 40;
   private static final int SECOND_MAX_VISIBILITY_OR_COLLISION_RULE_LENGTH = 40;
   private final int packetType;
   private final String teamName;
   private final Collection playerNames;
   private final Optional team;

   private TeamS2CPacket(String teamName, int packetType, Optional team, Collection playerNames) {
      this.teamName = teamName;
      this.packetType = packetType;
      this.team = team;
      this.playerNames = ImmutableList.copyOf(playerNames);
   }

   public static TeamS2CPacket updateTeam(Team team, boolean updatePlayers) {
      return new TeamS2CPacket(team.getName(), updatePlayers ? ADD : UPDATE, Optional.of(new SerializableTeam(team)), (Collection)(updatePlayers ? team.getPlayerList() : ImmutableList.of()));
   }

   public static TeamS2CPacket updateRemovedTeam(Team team) {
      return new TeamS2CPacket(team.getName(), REMOVE, Optional.empty(), ImmutableList.of());
   }

   public static TeamS2CPacket changePlayerTeam(Team team, String playerName, Operation operation) {
      return new TeamS2CPacket(team.getName(), operation == TeamS2CPacket.Operation.ADD ? ADD_PLAYERS : REMOVE_PLAYERS, Optional.empty(), ImmutableList.of(playerName));
   }

   public TeamS2CPacket(PacketByteBuf buf) {
      this.teamName = buf.readString();
      this.packetType = buf.readByte();
      if (containsTeamInfo(this.packetType)) {
         this.team = Optional.of(new SerializableTeam(buf));
      } else {
         this.team = Optional.empty();
      }

      if (containsPlayers(this.packetType)) {
         this.playerNames = buf.readList(PacketByteBuf::readString);
      } else {
         this.playerNames = ImmutableList.of();
      }

   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.teamName);
      buf.writeByte(this.packetType);
      if (containsTeamInfo(this.packetType)) {
         ((SerializableTeam)this.team.orElseThrow(() -> {
            return new IllegalStateException("Parameters not present, but method is" + this.packetType);
         })).write(buf);
      }

      if (containsPlayers(this.packetType)) {
         buf.writeCollection(this.playerNames, PacketByteBuf::writeString);
      }

   }

   private static boolean containsPlayers(int packetType) {
      return packetType == 0 || packetType == ADD_PLAYERS || packetType == REMOVE_PLAYERS;
   }

   private static boolean containsTeamInfo(int packetType) {
      return packetType == 0 || packetType == UPDATE;
   }

   @Nullable
   public Operation getPlayerListOperation() {
      switch (this.packetType) {
         case 0:
         case 3:
            return TeamS2CPacket.Operation.ADD;
         case 1:
         case 2:
         default:
            return null;
         case 4:
            return TeamS2CPacket.Operation.REMOVE;
      }
   }

   @Nullable
   public Operation getTeamOperation() {
      switch (this.packetType) {
         case 0:
            return TeamS2CPacket.Operation.ADD;
         case 1:
            return TeamS2CPacket.Operation.REMOVE;
         default:
            return null;
      }
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onTeam(this);
   }

   public String getTeamName() {
      return this.teamName;
   }

   public Collection getPlayerNames() {
      return this.playerNames;
   }

   public Optional getTeam() {
      return this.team;
   }

   public static class SerializableTeam {
      private final Text displayName;
      private final Text prefix;
      private final Text suffix;
      private final String nameTagVisibilityRule;
      private final String collisionRule;
      private final Formatting color;
      private final int friendlyFlags;

      public SerializableTeam(Team team) {
         this.displayName = team.getDisplayName();
         this.friendlyFlags = team.getFriendlyFlagsBitwise();
         this.nameTagVisibilityRule = team.getNameTagVisibilityRule().name;
         this.collisionRule = team.getCollisionRule().name;
         this.color = team.getColor();
         this.prefix = team.getPrefix();
         this.suffix = team.getSuffix();
      }

      public SerializableTeam(PacketByteBuf buf) {
         this.displayName = buf.readText();
         this.friendlyFlags = buf.readByte();
         this.nameTagVisibilityRule = buf.readString(40);
         this.collisionRule = buf.readString(40);
         this.color = (Formatting)buf.readEnumConstant(Formatting.class);
         this.prefix = buf.readText();
         this.suffix = buf.readText();
      }

      public Text getDisplayName() {
         return this.displayName;
      }

      public int getFriendlyFlagsBitwise() {
         return this.friendlyFlags;
      }

      public Formatting getColor() {
         return this.color;
      }

      public String getNameTagVisibilityRule() {
         return this.nameTagVisibilityRule;
      }

      public String getCollisionRule() {
         return this.collisionRule;
      }

      public Text getPrefix() {
         return this.prefix;
      }

      public Text getSuffix() {
         return this.suffix;
      }

      public void write(PacketByteBuf buf) {
         buf.writeText(this.displayName);
         buf.writeByte(this.friendlyFlags);
         buf.writeString(this.nameTagVisibilityRule);
         buf.writeString(this.collisionRule);
         buf.writeEnumConstant(this.color);
         buf.writeText(this.prefix);
         buf.writeText(this.suffix);
      }
   }

   public static enum Operation {
      ADD,
      REMOVE;

      // $FF: synthetic method
      private static Operation[] method_36954() {
         return new Operation[]{ADD, REMOVE};
      }
   }
}
