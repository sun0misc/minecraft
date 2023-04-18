package net.minecraft.data.report;

import com.mojang.brigadier.CommandDispatcher;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.argument.ArgumentHelper;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.server.command.CommandManager;

public class CommandSyntaxProvider implements DataProvider {
   private final DataOutput output;
   private final CompletableFuture registryLookupFuture;

   public CommandSyntaxProvider(DataOutput output, CompletableFuture registryLookupFuture) {
      this.output = output;
      this.registryLookupFuture = registryLookupFuture;
   }

   public CompletableFuture run(DataWriter writer) {
      Path path = this.output.resolvePath(DataOutput.OutputType.REPORTS).resolve("commands.json");
      return this.registryLookupFuture.thenCompose((lookup) -> {
         CommandDispatcher commandDispatcher = (new CommandManager(CommandManager.RegistrationEnvironment.ALL, CommandManager.createRegistryAccess(lookup))).getDispatcher();
         return DataProvider.writeToPath(writer, ArgumentHelper.toJson(commandDispatcher, commandDispatcher.getRoot()), path);
      });
   }

   public final String getName() {
      return "Command Syntax";
   }
}
