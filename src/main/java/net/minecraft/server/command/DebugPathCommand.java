package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DebugPathCommand {
   private static final SimpleCommandExceptionType SOURCE_NOT_MOB_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Source is not a mob"));
   private static final SimpleCommandExceptionType PATH_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Path not found"));
   private static final SimpleCommandExceptionType TARGET_NOT_REACHED_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Target not reached"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("debugpath").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("to", BlockPosArgumentType.blockPos()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "to"));
      })));
   }

   private static int execute(ServerCommandSource source, BlockPos pos) throws CommandSyntaxException {
      Entity lv = source.getEntity();
      if (!(lv instanceof MobEntity lv2)) {
         throw SOURCE_NOT_MOB_EXCEPTION.create();
      } else {
         EntityNavigation lv3 = new MobNavigation(lv2, source.getWorld());
         Path lv4 = lv3.findPathTo((BlockPos)pos, 0);
         DebugInfoSender.sendPathfindingData(source.getWorld(), lv2, lv4, lv3.getNodeReachProximity());
         if (lv4 == null) {
            throw PATH_NOT_FOUND_EXCEPTION.create();
         } else if (!lv4.reachesTarget()) {
            throw TARGET_NOT_REACHED_EXCEPTION.create();
         } else {
            source.sendFeedback(Text.literal("Made path"), true);
            return 1;
         }
      }
   }
}
