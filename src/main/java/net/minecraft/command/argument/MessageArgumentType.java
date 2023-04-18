package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class MessageArgumentType implements SignedArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

   public static MessageArgumentType message() {
      return new MessageArgumentType();
   }

   public static Text getMessage(CommandContext context, String name) throws CommandSyntaxException {
      MessageFormat lv = (MessageFormat)context.getArgument(name, MessageFormat.class);
      return lv.format((ServerCommandSource)context.getSource());
   }

   public static void getSignedMessage(CommandContext context, String name, Consumer callback) throws CommandSyntaxException {
      MessageFormat lv = (MessageFormat)context.getArgument(name, MessageFormat.class);
      ServerCommandSource lv2 = (ServerCommandSource)context.getSource();
      Text lv3 = lv.format(lv2);
      SignedCommandArguments lv4 = lv2.getSignedArguments();
      SignedMessage lv5 = lv4.getMessage(name);
      if (lv5 != null) {
         chain(callback, lv2, lv5.withUnsignedContent(lv3));
      } else {
         chainUnsigned(callback, lv2, SignedMessage.ofUnsigned(lv.contents).withUnsignedContent(lv3));
      }

   }

   private static void chain(Consumer callback, ServerCommandSource source, SignedMessage message) {
      MinecraftServer minecraftServer = source.getServer();
      CompletableFuture completableFuture = filterText(source, message);
      CompletableFuture completableFuture2 = minecraftServer.getMessageDecorator().decorate(source.getPlayer(), message.getContent());
      source.getMessageChainTaskQueue().append((executor) -> {
         return CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync((void_) -> {
            SignedMessage lv = message.withUnsignedContent((Text)completableFuture2.join()).withFilterMask(((FilteredMessage)completableFuture.join()).mask());
            callback.accept(lv);
         }, executor);
      });
   }

   private static void chainUnsigned(Consumer callback, ServerCommandSource source, SignedMessage message) {
      MinecraftServer minecraftServer = source.getServer();
      CompletableFuture completableFuture = minecraftServer.getMessageDecorator().decorate(source.getPlayer(), message.getContent());
      source.getMessageChainTaskQueue().append((executor) -> {
         return completableFuture.thenAcceptAsync((content) -> {
            callback.accept(message.withUnsignedContent(content));
         }, executor);
      });
   }

   private static CompletableFuture filterText(ServerCommandSource source, SignedMessage message) {
      ServerPlayerEntity lv = source.getPlayer();
      return lv != null && message.canVerifyFrom(lv.getUuid()) ? lv.getTextStream().filterText(message.getSignedContent()) : CompletableFuture.completedFuture(FilteredMessage.permitted(message.getSignedContent()));
   }

   public MessageFormat parse(StringReader stringReader) throws CommandSyntaxException {
      return MessageArgumentType.MessageFormat.parse(stringReader, true);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }

   public static class MessageFormat {
      final String contents;
      private final MessageSelector[] selectors;

      public MessageFormat(String contents, MessageSelector[] selectors) {
         this.contents = contents;
         this.selectors = selectors;
      }

      public String getContents() {
         return this.contents;
      }

      public MessageSelector[] getSelectors() {
         return this.selectors;
      }

      Text format(ServerCommandSource source) throws CommandSyntaxException {
         return this.format(source, source.hasPermissionLevel(2));
      }

      public Text format(ServerCommandSource source, boolean canUseSelectors) throws CommandSyntaxException {
         if (this.selectors.length != 0 && canUseSelectors) {
            MutableText lv = Text.literal(this.contents.substring(0, this.selectors[0].getStart()));
            int i = this.selectors[0].getStart();
            MessageSelector[] var5 = this.selectors;
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               MessageSelector lv2 = var5[var7];
               Text lv3 = lv2.format(source);
               if (i < lv2.getStart()) {
                  lv.append(this.contents.substring(i, lv2.getStart()));
               }

               if (lv3 != null) {
                  lv.append(lv3);
               }

               i = lv2.getEnd();
            }

            if (i < this.contents.length()) {
               lv.append(this.contents.substring(i));
            }

            return lv;
         } else {
            return Text.literal(this.contents);
         }
      }

      public static MessageFormat parse(StringReader reader, boolean canUseSelectors) throws CommandSyntaxException {
         String string = reader.getString().substring(reader.getCursor(), reader.getTotalLength());
         if (!canUseSelectors) {
            reader.setCursor(reader.getTotalLength());
            return new MessageFormat(string, new MessageSelector[0]);
         } else {
            List list = Lists.newArrayList();
            int i = reader.getCursor();

            while(true) {
               int j;
               EntitySelector lv2;
               label38:
               while(true) {
                  while(reader.canRead()) {
                     if (reader.peek() == '@') {
                        j = reader.getCursor();

                        try {
                           EntitySelectorReader lv = new EntitySelectorReader(reader);
                           lv2 = lv.read();
                           break label38;
                        } catch (CommandSyntaxException var8) {
                           if (var8.getType() != EntitySelectorReader.MISSING_EXCEPTION && var8.getType() != EntitySelectorReader.UNKNOWN_SELECTOR_EXCEPTION) {
                              throw var8;
                           }

                           reader.setCursor(j + 1);
                        }
                     } else {
                        reader.skip();
                     }
                  }

                  return new MessageFormat(string, (MessageSelector[])list.toArray(new MessageSelector[0]));
               }

               list.add(new MessageSelector(j - i, reader.getCursor() - i, lv2));
            }
         }
      }
   }

   public static class MessageSelector {
      private final int start;
      private final int end;
      private final EntitySelector selector;

      public MessageSelector(int start, int end, EntitySelector selector) {
         this.start = start;
         this.end = end;
         this.selector = selector;
      }

      public int getStart() {
         return this.start;
      }

      public int getEnd() {
         return this.end;
      }

      public EntitySelector getSelector() {
         return this.selector;
      }

      @Nullable
      public Text format(ServerCommandSource source) throws CommandSyntaxException {
         return EntitySelector.getNames(this.selector.getEntities(source));
      }
   }
}
