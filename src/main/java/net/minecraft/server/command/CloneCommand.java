package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class CloneCommand {
   private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.overlap"));
   private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> {
      return Text.translatable("commands.clone.toobig", maxCount, count);
   });
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.clone.failed"));
   public static final Predicate IS_AIR_PREDICATE = (pos) -> {
      return !pos.getBlockState().isAir();
   };

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("clone").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(createSourceArgs(commandRegistryAccess, (context) -> {
         return ((ServerCommandSource)context.getSource()).getWorld();
      }))).then(CommandManager.literal("from").then(CommandManager.argument("sourceDimension", DimensionArgumentType.dimension()).then(createSourceArgs(commandRegistryAccess, (context) -> {
         return DimensionArgumentType.getDimensionArgument(context, "sourceDimension");
      })))));
   }

   private static ArgumentBuilder createSourceArgs(CommandRegistryAccess commandRegistryAccess, ArgumentGetter worldGetter) {
      return CommandManager.argument("begin", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(createDestinationArgs(commandRegistryAccess, worldGetter, (context) -> {
         return ((ServerCommandSource)context.getSource()).getWorld();
      }))).then(CommandManager.literal("to").then(CommandManager.argument("targetDimension", DimensionArgumentType.dimension()).then(createDestinationArgs(commandRegistryAccess, worldGetter, (context) -> {
         return DimensionArgumentType.getDimensionArgument(context, "targetDimension");
      })))));
   }

   private static DimensionalPos createDimensionalPos(CommandContext context, ServerWorld world, String name) throws CommandSyntaxException {
      BlockPos lv = BlockPosArgumentType.getLoadedBlockPos(context, world, name);
      return new DimensionalPos(world, lv);
   }

   private static ArgumentBuilder createDestinationArgs(CommandRegistryAccess commandRegistryAccess, ArgumentGetter sourceWorldGetter, ArgumentGetter targetWorldGetter) {
      ArgumentGetter lv = (context) -> {
         return createDimensionalPos(context, (ServerWorld)sourceWorldGetter.apply(context), "begin");
      };
      ArgumentGetter lv2 = (context) -> {
         return createDimensionalPos(context, (ServerWorld)sourceWorldGetter.apply(context), "end");
      };
      ArgumentGetter lv3 = (context) -> {
         return createDimensionalPos(context, (ServerWorld)targetWorldGetter.apply(context), "destination");
      };
      return ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), (arg) -> {
            return true;
         }, CloneCommand.Mode.NORMAL);
      })).then(createModeArgs(lv, lv2, lv3, (context) -> {
         return (arg) -> {
            return true;
         };
      }, CommandManager.literal("replace").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), (arg) -> {
            return true;
         }, CloneCommand.Mode.NORMAL);
      })))).then(createModeArgs(lv, lv2, lv3, (context) -> {
         return IS_AIR_PREDICATE;
      }, CommandManager.literal("masked").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), IS_AIR_PREDICATE, CloneCommand.Mode.NORMAL);
      })))).then(CommandManager.literal("filtered").then(createModeArgs(lv, lv2, lv3, (context) -> {
         return BlockPredicateArgumentType.getBlockPredicate(context, "filter");
      }, CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (DimensionalPos)lv.apply(context), (DimensionalPos)lv2.apply(context), (DimensionalPos)lv3.apply(context), BlockPredicateArgumentType.getBlockPredicate(context, "filter"), CloneCommand.Mode.NORMAL);
      }))));
   }

   private static ArgumentBuilder createModeArgs(ArgumentGetter beginPosGetter, ArgumentGetter endPosGetter, ArgumentGetter destinationPosGetter, ArgumentGetter filterGetter, ArgumentBuilder builder) {
      return builder.then(CommandManager.literal("force").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), CloneCommand.Mode.FORCE);
      })).then(CommandManager.literal("move").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), CloneCommand.Mode.MOVE);
      })).then(CommandManager.literal("normal").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), CloneCommand.Mode.NORMAL);
      }));
   }

   private static int execute(ServerCommandSource source, DimensionalPos begin, DimensionalPos end, DimensionalPos destination, Predicate filter, Mode mode) throws CommandSyntaxException {
      BlockPos lv = begin.position();
      BlockPos lv2 = end.position();
      BlockBox lv3 = BlockBox.create(lv, lv2);
      BlockPos lv4 = destination.position();
      BlockPos lv5 = lv4.add(lv3.getDimensions());
      BlockBox lv6 = BlockBox.create(lv4, lv5);
      ServerWorld lv7 = begin.dimension();
      ServerWorld lv8 = destination.dimension();
      if (!mode.allowsOverlap() && lv7 == lv8 && lv6.intersects(lv3)) {
         throw OVERLAP_EXCEPTION.create();
      } else {
         int i = lv3.getBlockCountX() * lv3.getBlockCountY() * lv3.getBlockCountZ();
         int j = source.getWorld().getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);
         if (i > j) {
            throw TOO_BIG_EXCEPTION.create(j, i);
         } else if (lv7.isRegionLoaded(lv, lv2) && lv8.isRegionLoaded(lv4, lv5)) {
            List list = Lists.newArrayList();
            List list2 = Lists.newArrayList();
            List list3 = Lists.newArrayList();
            Deque deque = Lists.newLinkedList();
            BlockPos lv9 = new BlockPos(lv6.getMinX() - lv3.getMinX(), lv6.getMinY() - lv3.getMinY(), lv6.getMinZ() - lv3.getMinZ());

            int m;
            for(int k = lv3.getMinZ(); k <= lv3.getMaxZ(); ++k) {
               for(int l = lv3.getMinY(); l <= lv3.getMaxY(); ++l) {
                  for(m = lv3.getMinX(); m <= lv3.getMaxX(); ++m) {
                     BlockPos lv10 = new BlockPos(m, l, k);
                     BlockPos lv11 = lv10.add(lv9);
                     CachedBlockPosition lv12 = new CachedBlockPosition(lv7, lv10, false);
                     BlockState lv13 = lv12.getBlockState();
                     if (filter.test(lv12)) {
                        BlockEntity lv14 = lv7.getBlockEntity(lv10);
                        if (lv14 != null) {
                           NbtCompound lv15 = lv14.createNbt();
                           list2.add(new BlockInfo(lv11, lv13, lv15));
                           deque.addLast(lv10);
                        } else if (!lv13.isOpaqueFullCube(lv7, lv10) && !lv13.isFullCube(lv7, lv10)) {
                           list3.add(new BlockInfo(lv11, lv13, (NbtCompound)null));
                           deque.addFirst(lv10);
                        } else {
                           list.add(new BlockInfo(lv11, lv13, (NbtCompound)null));
                           deque.addLast(lv10);
                        }
                     }
                  }
               }
            }

            if (mode == CloneCommand.Mode.MOVE) {
               Iterator var30 = deque.iterator();

               BlockPos lv16;
               while(var30.hasNext()) {
                  lv16 = (BlockPos)var30.next();
                  BlockEntity lv17 = lv7.getBlockEntity(lv16);
                  Clearable.clear(lv17);
                  lv7.setBlockState(lv16, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
               }

               var30 = deque.iterator();

               while(var30.hasNext()) {
                  lv16 = (BlockPos)var30.next();
                  lv7.setBlockState(lv16, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
               }
            }

            List list4 = Lists.newArrayList();
            list4.addAll(list);
            list4.addAll(list2);
            list4.addAll(list3);
            List list5 = Lists.reverse(list4);
            Iterator var35 = list5.iterator();

            while(var35.hasNext()) {
               BlockInfo lv18 = (BlockInfo)var35.next();
               BlockEntity lv19 = lv8.getBlockEntity(lv18.pos);
               Clearable.clear(lv19);
               lv8.setBlockState(lv18.pos, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
            }

            m = 0;
            Iterator var37 = list4.iterator();

            BlockInfo lv20;
            while(var37.hasNext()) {
               lv20 = (BlockInfo)var37.next();
               if (lv8.setBlockState(lv20.pos, lv20.state, Block.NOTIFY_LISTENERS)) {
                  ++m;
               }
            }

            for(var37 = list2.iterator(); var37.hasNext(); lv8.setBlockState(lv20.pos, lv20.state, Block.NOTIFY_LISTENERS)) {
               lv20 = (BlockInfo)var37.next();
               BlockEntity lv21 = lv8.getBlockEntity(lv20.pos);
               if (lv20.blockEntityNbt != null && lv21 != null) {
                  lv21.readNbt(lv20.blockEntityNbt);
                  lv21.markDirty();
               }
            }

            var37 = list5.iterator();

            while(var37.hasNext()) {
               lv20 = (BlockInfo)var37.next();
               lv8.updateNeighbors(lv20.pos, lv20.state.getBlock());
            }

            lv8.getBlockTickScheduler().scheduleTicks(lv7.getBlockTickScheduler(), lv3, lv9);
            if (m == 0) {
               throw FAILED_EXCEPTION.create();
            } else {
               source.sendFeedback(Text.translatable("commands.clone.success", m), true);
               return m;
            }
         } else {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
         }
      }
   }

   @FunctionalInterface
   interface ArgumentGetter {
      Object apply(Object value) throws CommandSyntaxException;
   }

   private static record DimensionalPos(ServerWorld dimension, BlockPos position) {
      DimensionalPos(ServerWorld arg, BlockPos arg2) {
         this.dimension = arg;
         this.position = arg2;
      }

      public ServerWorld dimension() {
         return this.dimension;
      }

      public BlockPos position() {
         return this.position;
      }
   }

   static enum Mode {
      FORCE(true),
      MOVE(true),
      NORMAL(false);

      private final boolean allowsOverlap;

      private Mode(boolean allowsOverlap) {
         this.allowsOverlap = allowsOverlap;
      }

      public boolean allowsOverlap() {
         return this.allowsOverlap;
      }

      // $FF: synthetic method
      private static Mode[] method_36966() {
         return new Mode[]{FORCE, MOVE, NORMAL};
      }
   }

   static class BlockInfo {
      public final BlockPos pos;
      public final BlockState state;
      @Nullable
      public final NbtCompound blockEntityNbt;

      public BlockInfo(BlockPos pos, BlockState state, @Nullable NbtCompound blockEntityNbt) {
         this.pos = pos;
         this.state = state;
         this.blockEntityNbt = blockEntityNbt;
      }
   }
}
