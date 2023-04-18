package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class FillCommand {
   private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> {
      return Text.translatable("commands.fill.toobig", maxCount, count);
   });
   static final BlockStateArgument AIR_BLOCK_ARGUMENT;
   private static final SimpleCommandExceptionType FAILED_EXCEPTION;

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fill").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then(CommandManager.argument("to", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), FillCommand.Mode.REPLACE, (Predicate)null);
      })).then(((LiteralArgumentBuilder)CommandManager.literal("replace").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), FillCommand.Mode.REPLACE, (Predicate)null);
      })).then(CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), FillCommand.Mode.REPLACE, BlockPredicateArgumentType.getBlockPredicate(context, "filter"));
      })))).then(CommandManager.literal("keep").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), FillCommand.Mode.REPLACE, (pos) -> {
            return pos.getWorld().isAir(pos.getBlockPos());
         });
      }))).then(CommandManager.literal("outline").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), FillCommand.Mode.OUTLINE, (Predicate)null);
      }))).then(CommandManager.literal("hollow").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), FillCommand.Mode.HOLLOW, (Predicate)null);
      }))).then(CommandManager.literal("destroy").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to")), BlockStateArgumentType.getBlockState(context, "block"), FillCommand.Mode.DESTROY, (Predicate)null);
      }))))));
   }

   private static int execute(ServerCommandSource source, BlockBox range, BlockStateArgument block, Mode mode, @Nullable Predicate filter) throws CommandSyntaxException {
      int i = range.getBlockCountX() * range.getBlockCountY() * range.getBlockCountZ();
      int j = source.getWorld().getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);
      if (i > j) {
         throw TOO_BIG_EXCEPTION.create(j, i);
      } else {
         List list = Lists.newArrayList();
         ServerWorld lv = source.getWorld();
         int k = 0;
         Iterator var10 = BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ()).iterator();

         while(true) {
            BlockPos lv2;
            do {
               if (!var10.hasNext()) {
                  var10 = list.iterator();

                  while(var10.hasNext()) {
                     lv2 = (BlockPos)var10.next();
                     Block lv5 = lv.getBlockState(lv2).getBlock();
                     lv.updateNeighbors(lv2, lv5);
                  }

                  if (k == 0) {
                     throw FAILED_EXCEPTION.create();
                  }

                  source.sendFeedback(Text.translatable("commands.fill.success", k), true);
                  return k;
               }

               lv2 = (BlockPos)var10.next();
            } while(filter != null && !filter.test(new CachedBlockPosition(lv, lv2, true)));

            BlockStateArgument lv3 = mode.filter.filter(range, lv2, block, lv);
            if (lv3 != null) {
               BlockEntity lv4 = lv.getBlockEntity(lv2);
               Clearable.clear(lv4);
               if (lv3.setBlockState(lv, lv2, Block.NOTIFY_LISTENERS)) {
                  list.add(lv2.toImmutable());
                  ++k;
               }
            }
         }
      }
   }

   static {
      AIR_BLOCK_ARGUMENT = new BlockStateArgument(Blocks.AIR.getDefaultState(), Collections.emptySet(), (NbtCompound)null);
      FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.fill.failed"));
   }

   private static enum Mode {
      REPLACE((range, pos, block, world) -> {
         return block;
      }),
      OUTLINE((range, pos, block, world) -> {
         return pos.getX() != range.getMinX() && pos.getX() != range.getMaxX() && pos.getY() != range.getMinY() && pos.getY() != range.getMaxY() && pos.getZ() != range.getMinZ() && pos.getZ() != range.getMaxZ() ? null : block;
      }),
      HOLLOW((range, pos, block, world) -> {
         return pos.getX() != range.getMinX() && pos.getX() != range.getMaxX() && pos.getY() != range.getMinY() && pos.getY() != range.getMaxY() && pos.getZ() != range.getMinZ() && pos.getZ() != range.getMaxZ() ? FillCommand.AIR_BLOCK_ARGUMENT : block;
      }),
      DESTROY((range, pos, block, world) -> {
         world.breakBlock(pos, true);
         return block;
      });

      public final SetBlockCommand.Filter filter;

      private Mode(SetBlockCommand.Filter filter) {
         this.filter = filter;
      }

      // $FF: synthetic method
      private static Mode[] method_36968() {
         return new Mode[]{REPLACE, OUTLINE, HOLLOW, DESTROY};
      }
   }
}
