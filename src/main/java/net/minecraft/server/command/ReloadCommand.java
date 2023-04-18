package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import org.slf4j.Logger;

public class ReloadCommand {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void tryReloadDataPacks(Collection dataPacks, ServerCommandSource source) {
      source.getServer().reloadResources(dataPacks).exceptionally((throwable) -> {
         LOGGER.warn("Failed to execute reload", throwable);
         source.sendError(Text.translatable("commands.reload.failure"));
         return null;
      });
   }

   private static Collection findNewDataPacks(ResourcePackManager dataPackManager, SaveProperties saveProperties, Collection enabledDataPacks) {
      dataPackManager.scanPacks();
      Collection collection2 = Lists.newArrayList(enabledDataPacks);
      Collection collection3 = saveProperties.getDataConfiguration().dataPacks().getDisabled();
      Iterator var5 = dataPackManager.getNames().iterator();

      while(var5.hasNext()) {
         String string = (String)var5.next();
         if (!collection3.contains(string) && !collection2.contains(string)) {
            collection2.add(string);
         }
      }

      return collection2;
   }

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("reload").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).executes((context) -> {
         ServerCommandSource lv = (ServerCommandSource)context.getSource();
         MinecraftServer minecraftServer = lv.getServer();
         ResourcePackManager lv2 = minecraftServer.getDataPackManager();
         SaveProperties lv3 = minecraftServer.getSaveProperties();
         Collection collection = lv2.getEnabledNames();
         Collection collection2 = findNewDataPacks(lv2, lv3, collection);
         lv.sendFeedback(Text.translatable("commands.reload.success"), true);
         tryReloadDataPacks(collection2, lv);
         return 0;
      }));
   }
}
