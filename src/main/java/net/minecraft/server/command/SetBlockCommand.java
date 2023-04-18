package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SetBlockCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.setblock.failed"));

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setblock").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), SetBlockCommand.Mode.REPLACE, (Predicate)null);
      })).then(CommandManager.literal("destroy").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), SetBlockCommand.Mode.DESTROY, (Predicate)null);
      }))).then(CommandManager.literal("keep").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), SetBlockCommand.Mode.REPLACE, (pos) -> {
            return pos.getWorld().isAir(pos.getBlockPos());
         });
      }))).then(CommandManager.literal("replace").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), BlockStateArgumentType.getBlockState(context, "block"), SetBlockCommand.Mode.REPLACE, (Predicate)null);
      })))));
   }

   private static int execute(ServerCommandSource source, BlockPos pos, BlockStateArgument block, Mode mode, @Nullable Predicate condition) throws CommandSyntaxException {
      ServerWorld lv = source.getWorld();
      if (condition != null && !condition.test(new CachedBlockPosition(lv, pos, true))) {
         throw FAILED_EXCEPTION.create();
      } else {
         boolean bl;
         if (mode == SetBlockCommand.Mode.DESTROY) {
            lv.breakBlock(pos, true);
            bl = !block.getBlockState().isAir() || !lv.getBlockState(pos).isAir();
         } else {
            BlockEntity lv2 = lv.getBlockEntity(pos);
            Clearable.clear(lv2);
            bl = true;
         }

         if (bl && !block.setBlockState(lv, pos, Block.NOTIFY_LISTENERS)) {
            throw FAILED_EXCEPTION.create();
         } else {
            lv.updateNeighbors(pos, block.getBlockState().getBlock());
            source.sendFeedback(Text.translatable("commands.setblock.success", pos.getX(), pos.getY(), pos.getZ()), true);
            return 1;
         }
      }
   }

   public static enum Mode {
      REPLACE,
      DESTROY;

      // $FF: synthetic method
      private static Mode[] method_36969() {
         return new Mode[]{REPLACE, DESTROY};
      }
   }

   public interface Filter {
      @Nullable
      BlockStateArgument filter(BlockBox box, BlockPos pos, BlockStateArgument block, ServerWorld world);
   }
}
