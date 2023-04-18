package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;

public class ResetChunksCommand {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("resetchunks").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).executes((context) -> {
         return executeResetChunks((ServerCommandSource)context.getSource(), 0, true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("range", IntegerArgumentType.integer(0, 5)).executes((context) -> {
         return executeResetChunks((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "range"), true);
      })).then(CommandManager.argument("skipOldChunks", BoolArgumentType.bool()).executes((context) -> {
         return executeResetChunks((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "range"), BoolArgumentType.getBool(context, "skipOldChunks"));
      }))));
   }

   private static int executeResetChunks(ServerCommandSource source, int radius, boolean skipOldChunks) {
      ServerWorld lv = source.getWorld();
      ServerChunkManager lv2 = lv.getChunkManager();
      lv2.threadedAnvilChunkStorage.verifyChunkGenerator();
      Vec3d lv3 = source.getPosition();
      ChunkPos lv4 = new ChunkPos(BlockPos.ofFloored(lv3));
      int j = lv4.z - radius;
      int k = lv4.z + radius;
      int l = lv4.x - radius;
      int m = lv4.x + radius;

      for(int n = j; n <= k; ++n) {
         for(int o = l; o <= m; ++o) {
            ChunkPos lv5 = new ChunkPos(o, n);
            WorldChunk lv6 = lv2.getWorldChunk(o, n, false);
            if (lv6 != null && (!skipOldChunks || !lv6.usesOldNoise())) {
               Iterator var15 = BlockPos.iterate(lv5.getStartX(), lv.getBottomY(), lv5.getStartZ(), lv5.getEndX(), lv.getTopY() - 1, lv5.getEndZ()).iterator();

               while(var15.hasNext()) {
                  BlockPos lv7 = (BlockPos)var15.next();
                  lv.setBlockState(lv7, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
               }
            }
         }
      }

      TaskExecutor lv8 = TaskExecutor.create(Util.getMainWorkerExecutor(), "worldgen-resetchunks");
      long p = System.currentTimeMillis();
      int q = (radius * 2 + 1) * (radius * 2 + 1);
      UnmodifiableIterator var33 = ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.LIQUID_CARVERS, ChunkStatus.FEATURES).iterator();

      long r;
      while(var33.hasNext()) {
         ChunkStatus lv9 = (ChunkStatus)var33.next();
         r = System.currentTimeMillis();
         Supplier var10000 = () -> {
            return Unit.INSTANCE;
         };
         Objects.requireNonNull(lv8);
         CompletableFuture completableFuture = CompletableFuture.supplyAsync(var10000, lv8::send);

         for(int s = lv4.z - radius; s <= lv4.z + radius; ++s) {
            for(int t = lv4.x - radius; t <= lv4.x + radius; ++t) {
               ChunkPos lv10 = new ChunkPos(t, s);
               WorldChunk lv11 = lv2.getWorldChunk(t, s, false);
               if (lv11 != null && (!skipOldChunks || !lv11.usesOldNoise())) {
                  List list = Lists.newArrayList();
                  int u = Math.max(1, lv9.getTaskMargin());

                  for(int v = lv10.z - u; v <= lv10.z + u; ++v) {
                     for(int w = lv10.x - u; w <= lv10.x + u; ++w) {
                        Chunk lv12 = lv2.getChunk(w, v, lv9.getPrevious(), true);
                        Object lv13;
                        if (lv12 instanceof ReadOnlyChunk) {
                           lv13 = new ReadOnlyChunk(((ReadOnlyChunk)lv12).getWrappedChunk(), true);
                        } else if (lv12 instanceof WorldChunk) {
                           lv13 = new ReadOnlyChunk((WorldChunk)lv12, true);
                        } else {
                           lv13 = lv12;
                        }

                        list.add(lv13);
                     }
                  }

                  Function var10001 = (unit) -> {
                     Objects.requireNonNull(lv8);
                     return lv9.runGenerationTask(lv8::send, lv, lv2.getChunkGenerator(), lv.getStructureTemplateManager(), lv2.getLightingProvider(), (chunk) -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                     }, list, true).thenApply((either) -> {
                        if (lv9 == ChunkStatus.NOISE) {
                           either.left().ifPresent((chunk) -> {
                              Heightmap.populateHeightmaps(chunk, ChunkStatus.POST_CARVER_HEIGHTMAPS);
                           });
                        }

                        return Unit.INSTANCE;
                     });
                  };
                  Objects.requireNonNull(lv8);
                  completableFuture = completableFuture.thenComposeAsync(var10001, lv8::send);
               }
            }
         }

         MinecraftServer var36 = source.getServer();
         Objects.requireNonNull(completableFuture);
         var36.runTasks(completableFuture::isDone);
         Logger var37 = LOGGER;
         String var39 = lv9.getId();
         var37.debug(var39 + " took " + (System.currentTimeMillis() - r) + " ms");
      }

      long x = System.currentTimeMillis();

      for(int y = lv4.z - radius; y <= lv4.z + radius; ++y) {
         for(int z = lv4.x - radius; z <= lv4.x + radius; ++z) {
            ChunkPos lv14 = new ChunkPos(z, y);
            WorldChunk lv15 = lv2.getWorldChunk(z, y, false);
            if (lv15 != null && (!skipOldChunks || !lv15.usesOldNoise())) {
               Iterator var42 = BlockPos.iterate(lv14.getStartX(), lv.getBottomY(), lv14.getStartZ(), lv14.getEndX(), lv.getTopY() - 1, lv14.getEndZ()).iterator();

               while(var42.hasNext()) {
                  BlockPos lv16 = (BlockPos)var42.next();
                  lv2.markForUpdate(lv16);
               }
            }
         }
      }

      LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - x) + " ms");
      r = System.currentTimeMillis() - p;
      source.sendFeedback(Text.literal(String.format(Locale.ROOT, "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", q, r, q, (float)r / (float)q)), true);
      return 1;
   }
}
