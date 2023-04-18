package net.minecraft.server.function;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class CommandFunction {
   private final Element[] elements;
   final Identifier id;

   public CommandFunction(Identifier id, Element[] elements) {
      this.id = id;
      this.elements = elements;
   }

   public Identifier getId() {
      return this.id;
   }

   public Element[] getElements() {
      return this.elements;
   }

   public static CommandFunction create(Identifier id, CommandDispatcher dispatcher, ServerCommandSource source, List lines) {
      List list2 = Lists.newArrayListWithCapacity(lines.size());

      for(int i = 0; i < lines.size(); ++i) {
         int j = i + 1;
         String string = ((String)lines.get(i)).trim();
         StringReader stringReader = new StringReader(string);
         if (stringReader.canRead() && stringReader.peek() != '#') {
            if (stringReader.peek() == '/') {
               stringReader.skip();
               if (stringReader.peek() == '/') {
                  throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
               }

               String string2 = stringReader.readUnquotedString();
               throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)");
            }

            try {
               ParseResults parseResults = dispatcher.parse(stringReader, source);
               if (parseResults.getReader().canRead()) {
                  throw CommandManager.getException(parseResults);
               }

               list2.add(new CommandElement(parseResults));
            } catch (CommandSyntaxException var10) {
               throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var10.getMessage());
            }
         }
      }

      return new CommandFunction(id, (Element[])list2.toArray(new Element[0]));
   }

   @FunctionalInterface
   public interface Element {
      void execute(CommandFunctionManager manager, ServerCommandSource source, Deque entries, int maxChainLength, int depth, @Nullable CommandFunctionManager.Tracer tracer) throws CommandSyntaxException;
   }

   public static class CommandElement implements Element {
      private final ParseResults parsed;

      public CommandElement(ParseResults parsed) {
         this.parsed = parsed;
      }

      public void execute(CommandFunctionManager manager, ServerCommandSource source, Deque entries, int maxChainLength, int depth, @Nullable CommandFunctionManager.Tracer tracer) throws CommandSyntaxException {
         if (tracer != null) {
            String string = this.parsed.getReader().getString();
            tracer.traceCommandStart(depth, string);
            int k = this.execute(manager, source);
            tracer.traceCommandEnd(depth, string, k);
         } else {
            this.execute(manager, source);
         }

      }

      private int execute(CommandFunctionManager manager, ServerCommandSource source) throws CommandSyntaxException {
         return manager.getDispatcher().execute(CommandManager.withCommandSource(this.parsed, (currentSource) -> {
            return source;
         }));
      }

      public String toString() {
         return this.parsed.getReader().getString();
      }
   }

   public static class LazyContainer {
      public static final LazyContainer EMPTY = new LazyContainer((Identifier)null);
      @Nullable
      private final Identifier id;
      private boolean initialized;
      private Optional function = Optional.empty();

      public LazyContainer(@Nullable Identifier id) {
         this.id = id;
      }

      public LazyContainer(CommandFunction function) {
         this.initialized = true;
         this.id = null;
         this.function = Optional.of(function);
      }

      public Optional get(CommandFunctionManager manager) {
         if (!this.initialized) {
            if (this.id != null) {
               this.function = manager.getFunction(this.id);
            }

            this.initialized = true;
         }

         return this.function;
      }

      @Nullable
      public Identifier getId() {
         return (Identifier)this.function.map((f) -> {
            return f.id;
         }).orElse(this.id);
      }
   }

   public static class FunctionElement implements Element {
      private final LazyContainer function;

      public FunctionElement(CommandFunction function) {
         this.function = new LazyContainer(function);
      }

      public void execute(CommandFunctionManager manager, ServerCommandSource source, Deque entries, int maxChainLength, int depth, @Nullable CommandFunctionManager.Tracer tracer) {
         Util.ifPresentOrElse(this.function.get(manager), (f) -> {
            Element[] lvs = f.getElements();
            if (tracer != null) {
               tracer.traceFunctionCall(depth, f.getId(), lvs.length);
            }

            int k = maxChainLength - entries.size();
            int l = Math.min(lvs.length, k);

            for(int m = l - 1; m >= 0; --m) {
               entries.addFirst(new CommandFunctionManager.Entry(source, depth + 1, lvs[m]));
            }

         }, () -> {
            if (tracer != null) {
               tracer.traceFunctionCall(depth, this.function.getId(), -1);
            }

         });
      }

      public String toString() {
         return "function " + this.function.getId();
      }
   }
}
