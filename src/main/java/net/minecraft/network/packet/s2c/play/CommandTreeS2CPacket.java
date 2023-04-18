package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CommandTreeS2CPacket implements Packet {
   private static final byte field_33317 = 3;
   private static final byte field_33318 = 4;
   private static final byte field_33319 = 8;
   private static final byte field_33320 = 16;
   private static final byte field_33321 = 0;
   private static final byte field_33322 = 1;
   private static final byte field_33323 = 2;
   private final int rootSize;
   private final List nodes;

   public CommandTreeS2CPacket(RootCommandNode rootNode) {
      Object2IntMap object2IntMap = traverse(rootNode);
      this.nodes = collectNodes(object2IntMap);
      this.rootSize = object2IntMap.getInt(rootNode);
   }

   public CommandTreeS2CPacket(PacketByteBuf buf) {
      this.nodes = buf.readList(CommandTreeS2CPacket::readCommandNode);
      this.rootSize = buf.readVarInt();
      validate(this.nodes);
   }

   public void write(PacketByteBuf buf) {
      buf.writeCollection(this.nodes, (buf2, node) -> {
         node.write(buf2);
      });
      buf.writeVarInt(this.rootSize);
   }

   private static void validate(List nodeDatas, BiPredicate validator) {
      IntSet intSet = new IntOpenHashSet(IntSets.fromTo(0, nodeDatas.size()));

      boolean bl;
      do {
         if (intSet.isEmpty()) {
            return;
         }

         bl = intSet.removeIf((i) -> {
            return validator.test((CommandNodeData)nodeDatas.get(i), intSet);
         });
      } while(bl);

      throw new IllegalStateException("Server sent an impossible command tree");
   }

   private static void validate(List nodeDatas) {
      validate(nodeDatas, CommandNodeData::validateRedirectNodeIndex);
      validate(nodeDatas, CommandNodeData::validateChildNodeIndices);
   }

   private static Object2IntMap traverse(RootCommandNode commandTree) {
      Object2IntMap object2IntMap = new Object2IntOpenHashMap();
      Queue queue = Queues.newArrayDeque();
      queue.add(commandTree);

      CommandNode commandNode;
      while((commandNode = (CommandNode)queue.poll()) != null) {
         if (!object2IntMap.containsKey(commandNode)) {
            int i = object2IntMap.size();
            object2IntMap.put(commandNode, i);
            queue.addAll(commandNode.getChildren());
            if (commandNode.getRedirect() != null) {
               queue.add(commandNode.getRedirect());
            }
         }
      }

      return object2IntMap;
   }

   private static List collectNodes(Object2IntMap nodes) {
      ObjectArrayList objectArrayList = new ObjectArrayList(nodes.size());
      objectArrayList.size(nodes.size());
      ObjectIterator var2 = Object2IntMaps.fastIterable(nodes).iterator();

      while(var2.hasNext()) {
         Object2IntMap.Entry entry = (Object2IntMap.Entry)var2.next();
         objectArrayList.set(entry.getIntValue(), createNodeData((CommandNode)entry.getKey(), nodes));
      }

      return objectArrayList;
   }

   private static CommandNodeData readCommandNode(PacketByteBuf buf) {
      byte b = buf.readByte();
      int[] is = buf.readIntArray();
      int i = (b & 8) != 0 ? buf.readVarInt() : 0;
      SuggestableNode lv = readArgumentBuilder(buf, b);
      return new CommandNodeData(lv, b, i, is);
   }

   @Nullable
   private static SuggestableNode readArgumentBuilder(PacketByteBuf buf, byte flags) {
      int i = flags & 3;
      String string;
      if (i == 2) {
         string = buf.readString();
         int j = buf.readVarInt();
         ArgumentSerializer lv = (ArgumentSerializer)Registries.COMMAND_ARGUMENT_TYPE.get(j);
         if (lv == null) {
            return null;
         } else {
            ArgumentSerializer.ArgumentTypeProperties lv2 = lv.fromPacket(buf);
            Identifier lv3 = (flags & 16) != 0 ? buf.readIdentifier() : null;
            return new ArgumentNode(string, lv2, lv3);
         }
      } else if (i == 1) {
         string = buf.readString();
         return new LiteralNode(string);
      } else {
         return null;
      }
   }

   private static CommandNodeData createNodeData(CommandNode node, Object2IntMap nodes) {
      int i = 0;
      int j;
      if (node.getRedirect() != null) {
         i |= 8;
         j = nodes.getInt(node.getRedirect());
      } else {
         j = 0;
      }

      if (node.getCommand() != null) {
         i |= 4;
      }

      Object lv;
      if (node instanceof RootCommandNode) {
         i |= 0;
         lv = null;
      } else if (node instanceof ArgumentCommandNode) {
         ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)node;
         lv = new ArgumentNode(argumentCommandNode);
         i |= 2;
         if (argumentCommandNode.getCustomSuggestions() != null) {
            i |= 16;
         }
      } else {
         if (!(node instanceof LiteralCommandNode)) {
            throw new UnsupportedOperationException("Unknown node type " + node);
         }

         LiteralCommandNode literalCommandNode = (LiteralCommandNode)node;
         lv = new LiteralNode(literalCommandNode.getLiteral());
         i |= 1;
      }

      Stream var10000 = node.getChildren().stream();
      Objects.requireNonNull(nodes);
      int[] is = var10000.mapToInt(nodes::getInt).toArray();
      return new CommandNodeData((SuggestableNode)lv, i, j, is);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onCommandTree(this);
   }

   public RootCommandNode getCommandTree(CommandRegistryAccess commandRegistryAccess) {
      return (RootCommandNode)(new CommandTree(commandRegistryAccess, this.nodes)).getNode(this.rootSize);
   }

   private static class CommandNodeData {
      @Nullable
      final SuggestableNode suggestableNode;
      final int flags;
      final int redirectNodeIndex;
      final int[] childNodeIndices;

      CommandNodeData(@Nullable SuggestableNode suggestableNode, int flags, int redirectNodeIndex, int[] childNodeIndices) {
         this.suggestableNode = suggestableNode;
         this.flags = flags;
         this.redirectNodeIndex = redirectNodeIndex;
         this.childNodeIndices = childNodeIndices;
      }

      public void write(PacketByteBuf buf) {
         buf.writeByte(this.flags);
         buf.writeIntArray(this.childNodeIndices);
         if ((this.flags & 8) != 0) {
            buf.writeVarInt(this.redirectNodeIndex);
         }

         if (this.suggestableNode != null) {
            this.suggestableNode.write(buf);
         }

      }

      public boolean validateRedirectNodeIndex(IntSet indices) {
         if ((this.flags & 8) != 0) {
            return !indices.contains(this.redirectNodeIndex);
         } else {
            return true;
         }
      }

      public boolean validateChildNodeIndices(IntSet indices) {
         int[] var2 = this.childNodeIndices;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int i = var2[var4];
            if (indices.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   private interface SuggestableNode {
      ArgumentBuilder createArgumentBuilder(CommandRegistryAccess commandRegistryAccess);

      void write(PacketByteBuf buf);
   }

   private static class ArgumentNode implements SuggestableNode {
      private final String name;
      private final ArgumentSerializer.ArgumentTypeProperties properties;
      @Nullable
      private final Identifier id;

      @Nullable
      private static Identifier computeId(@Nullable SuggestionProvider provider) {
         return provider != null ? SuggestionProviders.computeId(provider) : null;
      }

      ArgumentNode(String name, ArgumentSerializer.ArgumentTypeProperties properties, @Nullable Identifier id) {
         this.name = name;
         this.properties = properties;
         this.id = id;
      }

      public ArgumentNode(ArgumentCommandNode node) {
         this(node.getName(), ArgumentTypes.getArgumentTypeProperties(node.getType()), computeId(node.getCustomSuggestions()));
      }

      public ArgumentBuilder createArgumentBuilder(CommandRegistryAccess commandRegistryAccess) {
         ArgumentType argumentType = this.properties.createType(commandRegistryAccess);
         RequiredArgumentBuilder requiredArgumentBuilder = RequiredArgumentBuilder.argument(this.name, argumentType);
         if (this.id != null) {
            requiredArgumentBuilder.suggests(SuggestionProviders.byId(this.id));
         }

         return requiredArgumentBuilder;
      }

      public void write(PacketByteBuf buf) {
         buf.writeString(this.name);
         write(buf, this.properties);
         if (this.id != null) {
            buf.writeIdentifier(this.id);
         }

      }

      private static void write(PacketByteBuf buf, ArgumentSerializer.ArgumentTypeProperties properties) {
         write(buf, properties.getSerializer(), properties);
      }

      private static void write(PacketByteBuf buf, ArgumentSerializer serializer, ArgumentSerializer.ArgumentTypeProperties properties) {
         buf.writeVarInt(Registries.COMMAND_ARGUMENT_TYPE.getRawId(serializer));
         serializer.writePacket(properties, buf);
      }
   }

   private static class LiteralNode implements SuggestableNode {
      private final String literal;

      LiteralNode(String literal) {
         this.literal = literal;
      }

      public ArgumentBuilder createArgumentBuilder(CommandRegistryAccess commandRegistryAccess) {
         return LiteralArgumentBuilder.literal(this.literal);
      }

      public void write(PacketByteBuf buf) {
         buf.writeString(this.literal);
      }
   }

   private static class CommandTree {
      private final CommandRegistryAccess commandRegistryAccess;
      private final List nodeDatas;
      private final List nodes;

      CommandTree(CommandRegistryAccess commandRegistryAccess, List nodeDatas) {
         this.commandRegistryAccess = commandRegistryAccess;
         this.nodeDatas = nodeDatas;
         ObjectArrayList objectArrayList = new ObjectArrayList();
         objectArrayList.size(nodeDatas.size());
         this.nodes = objectArrayList;
      }

      public CommandNode getNode(int index) {
         CommandNode commandNode = (CommandNode)this.nodes.get(index);
         if (commandNode != null) {
            return commandNode;
         } else {
            CommandNodeData lv = (CommandNodeData)this.nodeDatas.get(index);
            Object commandNode2;
            if (lv.suggestableNode == null) {
               commandNode2 = new RootCommandNode();
            } else {
               ArgumentBuilder argumentBuilder = lv.suggestableNode.createArgumentBuilder(this.commandRegistryAccess);
               if ((lv.flags & 8) != 0) {
                  argumentBuilder.redirect(this.getNode(lv.redirectNodeIndex));
               }

               if ((lv.flags & 4) != 0) {
                  argumentBuilder.executes((context) -> {
                     return 0;
                  });
               }

               commandNode2 = argumentBuilder.build();
            }

            this.nodes.set(index, commandNode2);
            int[] var10 = lv.childNodeIndices;
            int var6 = var10.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               int j = var10[var7];
               CommandNode commandNode3 = this.getNode(j);
               if (!(commandNode3 instanceof RootCommandNode)) {
                  ((CommandNode)commandNode2).addChild(commandNode3);
               }
            }

            return (CommandNode)commandNode2;
         }
      }
   }
}
