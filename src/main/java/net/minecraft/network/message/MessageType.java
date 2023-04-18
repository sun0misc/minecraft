package net.minecraft.network.message;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Decoration;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record MessageType(Decoration chat, Decoration narration) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Decoration.CODEC.fieldOf("chat").forGetter(MessageType::chat), Decoration.CODEC.fieldOf("narration").forGetter(MessageType::narration)).apply(instance, MessageType::new);
   });
   public static final Decoration CHAT_TEXT_DECORATION = Decoration.ofChat("chat.type.text");
   public static final RegistryKey CHAT = register("chat");
   public static final RegistryKey SAY_COMMAND = register("say_command");
   public static final RegistryKey MSG_COMMAND_INCOMING = register("msg_command_incoming");
   public static final RegistryKey MSG_COMMAND_OUTGOING = register("msg_command_outgoing");
   public static final RegistryKey TEAM_MSG_COMMAND_INCOMING = register("team_msg_command_incoming");
   public static final RegistryKey TEAM_MSG_COMMAND_OUTGOING = register("team_msg_command_outgoing");
   public static final RegistryKey EMOTE_COMMAND = register("emote_command");

   public MessageType(Decoration arg, Decoration arg2) {
      this.chat = arg;
      this.narration = arg2;
   }

   private static RegistryKey register(String id) {
      return RegistryKey.of(RegistryKeys.MESSAGE_TYPE, new Identifier(id));
   }

   public static void bootstrap(Registerable messageTypeRegisterable) {
      messageTypeRegisterable.register(CHAT, new MessageType(CHAT_TEXT_DECORATION, Decoration.ofChat("chat.type.text.narrate")));
      messageTypeRegisterable.register(SAY_COMMAND, new MessageType(Decoration.ofChat("chat.type.announcement"), Decoration.ofChat("chat.type.text.narrate")));
      messageTypeRegisterable.register(MSG_COMMAND_INCOMING, new MessageType(Decoration.ofIncomingMessage("commands.message.display.incoming"), Decoration.ofChat("chat.type.text.narrate")));
      messageTypeRegisterable.register(MSG_COMMAND_OUTGOING, new MessageType(Decoration.ofOutgoingMessage("commands.message.display.outgoing"), Decoration.ofChat("chat.type.text.narrate")));
      messageTypeRegisterable.register(TEAM_MSG_COMMAND_INCOMING, new MessageType(Decoration.ofTeamMessage("chat.type.team.text"), Decoration.ofChat("chat.type.text.narrate")));
      messageTypeRegisterable.register(TEAM_MSG_COMMAND_OUTGOING, new MessageType(Decoration.ofTeamMessage("chat.type.team.sent"), Decoration.ofChat("chat.type.text.narrate")));
      messageTypeRegisterable.register(EMOTE_COMMAND, new MessageType(Decoration.ofChat("chat.type.emote"), Decoration.ofChat("chat.type.emote")));
   }

   public static Parameters params(RegistryKey typeKey, Entity entity) {
      return params(typeKey, entity.world.getRegistryManager(), entity.getDisplayName());
   }

   public static Parameters params(RegistryKey typeKey, ServerCommandSource source) {
      return params(typeKey, source.getRegistryManager(), source.getDisplayName());
   }

   public static Parameters params(RegistryKey typeKey, DynamicRegistryManager registryManager, Text name) {
      Registry lv = registryManager.get(RegistryKeys.MESSAGE_TYPE);
      return ((MessageType)lv.getOrThrow(typeKey)).params(name);
   }

   public Parameters params(Text name) {
      return new Parameters(this, name);
   }

   public Decoration chat() {
      return this.chat;
   }

   public Decoration narration() {
      return this.narration;
   }

   public static record Parameters(MessageType type, Text name, @Nullable Text targetName) {
      Parameters(MessageType type, Text name) {
         this(type, name, (Text)null);
      }

      public Parameters(MessageType arg, Text arg2, @Nullable Text arg3) {
         this.type = arg;
         this.name = arg2;
         this.targetName = arg3;
      }

      public Text applyChatDecoration(Text content) {
         return this.type.chat().apply(content, this);
      }

      public Text applyNarrationDecoration(Text content) {
         return this.type.narration().apply(content, this);
      }

      public Parameters withTargetName(Text targetName) {
         return new Parameters(this.type, this.name, targetName);
      }

      public Serialized toSerialized(DynamicRegistryManager registryManager) {
         Registry lv = registryManager.get(RegistryKeys.MESSAGE_TYPE);
         return new Serialized(lv.getRawId(this.type), this.name, this.targetName);
      }

      public MessageType type() {
         return this.type;
      }

      public Text name() {
         return this.name;
      }

      @Nullable
      public Text targetName() {
         return this.targetName;
      }
   }

   public static record Serialized(int typeId, Text name, @Nullable Text targetName) {
      public Serialized(PacketByteBuf buf) {
         this(buf.readVarInt(), buf.readText(), (Text)buf.readNullable(PacketByteBuf::readText));
      }

      public Serialized(int i, Text arg, @Nullable Text arg2) {
         this.typeId = i;
         this.name = arg;
         this.targetName = arg2;
      }

      public void write(PacketByteBuf buf) {
         buf.writeVarInt(this.typeId);
         buf.writeText(this.name);
         buf.writeNullable(this.targetName, PacketByteBuf::writeText);
      }

      public Optional toParameters(DynamicRegistryManager registryManager) {
         Registry lv = registryManager.get(RegistryKeys.MESSAGE_TYPE);
         MessageType lv2 = (MessageType)lv.get(this.typeId);
         return Optional.ofNullable(lv2).map((type) -> {
            return new Parameters(type, this.name, this.targetName);
         });
      }

      public int typeId() {
         return this.typeId;
      }

      public Text name() {
         return this.name;
      }

      @Nullable
      public Text targetName() {
         return this.targetName;
      }
   }
}
