package net.minecraft.server.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class CommandFunctionManager {
   private static final Text NO_TRACE_IN_FUNCTION_TEXT = Text.translatable("commands.debug.function.noRecursion");
   private static final Identifier TICK_TAG_ID = new Identifier("tick");
   private static final Identifier LOAD_TAG_ID = new Identifier("load");
   final MinecraftServer server;
   @Nullable
   private Execution execution;
   private List tickFunctions = ImmutableList.of();
   private boolean justLoaded;
   private FunctionLoader loader;

   public CommandFunctionManager(MinecraftServer server, FunctionLoader loader) {
      this.server = server;
      this.loader = loader;
      this.load(loader);
   }

   public int getMaxCommandChainLength() {
      return this.server.getGameRules().getInt(GameRules.MAX_COMMAND_CHAIN_LENGTH);
   }

   public CommandDispatcher getDispatcher() {
      return this.server.getCommandManager().getDispatcher();
   }

   public void tick() {
      if (this.justLoaded) {
         this.justLoaded = false;
         Collection collection = this.loader.getTagOrEmpty(LOAD_TAG_ID);
         this.executeAll(collection, LOAD_TAG_ID);
      }

      this.executeAll(this.tickFunctions, TICK_TAG_ID);
   }

   private void executeAll(Collection functions, Identifier label) {
      Profiler var10000 = this.server.getProfiler();
      Objects.requireNonNull(label);
      var10000.push(label::toString);
      Iterator var3 = functions.iterator();

      while(var3.hasNext()) {
         CommandFunction lv = (CommandFunction)var3.next();
         this.execute(lv, this.getScheduledCommandSource());
      }

      this.server.getProfiler().pop();
   }

   public int execute(CommandFunction function, ServerCommandSource source) {
      return this.execute(function, source, (Tracer)null);
   }

   public int execute(CommandFunction function, ServerCommandSource source, @Nullable Tracer tracer) {
      if (this.execution != null) {
         if (tracer != null) {
            this.execution.reportError(NO_TRACE_IN_FUNCTION_TEXT.getString());
            return 0;
         } else {
            this.execution.recursiveRun(function, source);
            return 0;
         }
      } else {
         int var4;
         try {
            this.execution = new Execution(tracer);
            var4 = this.execution.run(function, source);
         } finally {
            this.execution = null;
         }

         return var4;
      }
   }

   public void setFunctions(FunctionLoader loader) {
      this.loader = loader;
      this.load(loader);
   }

   private void load(FunctionLoader loader) {
      this.tickFunctions = ImmutableList.copyOf(loader.getTagOrEmpty(TICK_TAG_ID));
      this.justLoaded = true;
   }

   public ServerCommandSource getScheduledCommandSource() {
      return this.server.getCommandSource().withLevel(2).withSilent();
   }

   public Optional getFunction(Identifier id) {
      return this.loader.get(id);
   }

   public Collection getTag(Identifier id) {
      return this.loader.getTagOrEmpty(id);
   }

   public Iterable getAllFunctions() {
      return this.loader.getFunctions().keySet();
   }

   public Iterable getFunctionTags() {
      return this.loader.getTags();
   }

   public interface Tracer {
      void traceCommandStart(int depth, String command);

      void traceCommandEnd(int depth, String command, int result);

      void traceError(int depth, String message);

      void traceFunctionCall(int depth, Identifier function, int size);
   }

   private class Execution {
      private int depth;
      @Nullable
      private final Tracer tracer;
      private final Deque queue = Queues.newArrayDeque();
      private final List waitlist = Lists.newArrayList();

      Execution(@Nullable Tracer tracer) {
         this.tracer = tracer;
      }

      void recursiveRun(CommandFunction function, ServerCommandSource source) {
         int i = CommandFunctionManager.this.getMaxCommandChainLength();
         if (this.queue.size() + this.waitlist.size() < i) {
            this.waitlist.add(new Entry(source, this.depth, new CommandFunction.FunctionElement(function)));
         }

      }

      int run(CommandFunction function, ServerCommandSource source) {
         int i = CommandFunctionManager.this.getMaxCommandChainLength();
         int j = 0;
         CommandFunction.Element[] lvs = function.getElements();

         for(int k = lvs.length - 1; k >= 0; --k) {
            this.queue.push(new Entry(source, 0, lvs[k]));
         }

         do {
            if (this.queue.isEmpty()) {
               return j;
            }

            try {
               Entry lv = (Entry)this.queue.removeFirst();
               Profiler var10000 = CommandFunctionManager.this.server.getProfiler();
               Objects.requireNonNull(lv);
               var10000.push(lv::toString);
               this.depth = lv.depth;
               lv.execute(CommandFunctionManager.this, this.queue, i, this.tracer);
               if (!this.waitlist.isEmpty()) {
                  List var10 = Lists.reverse(this.waitlist);
                  Deque var10001 = this.queue;
                  Objects.requireNonNull(var10001);
                  var10.forEach(var10001::addFirst);
                  this.waitlist.clear();
               }
            } finally {
               CommandFunctionManager.this.server.getProfiler().pop();
            }

            ++j;
         } while(j < i);

         return j;
      }

      public void reportError(String message) {
         if (this.tracer != null) {
            this.tracer.traceError(this.depth, message);
         }

      }
   }

   public static class Entry {
      private final ServerCommandSource source;
      final int depth;
      private final CommandFunction.Element element;

      public Entry(ServerCommandSource source, int depth, CommandFunction.Element element) {
         this.source = source;
         this.depth = depth;
         this.element = element;
      }

      public void execute(CommandFunctionManager manager, Deque entries, int maxChainLength, @Nullable Tracer tracer) {
         try {
            this.element.execute(manager, this.source, entries, maxChainLength, this.depth, tracer);
         } catch (CommandSyntaxException var6) {
            if (tracer != null) {
               tracer.traceError(this.depth, var6.getRawMessage().getString());
            }
         } catch (Exception var7) {
            if (tracer != null) {
               tracer.traceError(this.depth, var7.getMessage());
            }
         }

      }

      public String toString() {
         return this.element.toString();
      }
   }
}
