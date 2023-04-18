package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
   public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.unloaded"));
   private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maximum, specified) -> {
      return Text.translatable("commands.fillbiome.toobig", maximum, specified);
   });

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fillbiome").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then(CommandManager.argument("to", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("biome", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.BIOME)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "from"), BlockPosArgumentType.getLoadedBlockPos(context, "to"), RegistryEntryArgumentType.getRegistryEntry(context, "biome", RegistryKeys.BIOME), (arg) -> {
            return true;
         });
      })).then(CommandManager.literal("replace").then(CommandManager.argument("filter", RegistryEntryPredicateArgumentType.registryEntryPredicate(commandRegistryAccess, RegistryKeys.BIOME)).executes((context) -> {
         ServerCommandSource var10000 = (ServerCommandSource)context.getSource();
         BlockPos var10001 = BlockPosArgumentType.getLoadedBlockPos(context, "from");
         BlockPos var10002 = BlockPosArgumentType.getLoadedBlockPos(context, "to");
         RegistryEntry.Reference var10003 = RegistryEntryArgumentType.getRegistryEntry(context, "biome", RegistryKeys.BIOME);
         RegistryEntryPredicateArgumentType.EntryPredicate var10004 = RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "filter", RegistryKeys.BIOME);
         Objects.requireNonNull(var10004);
         return execute(var10000, var10001, var10002, var10003, var10004::test);
      })))))));
   }

   private static int convertCoordinate(int coordinate) {
      return BiomeCoords.toBlock(BiomeCoords.fromBlock(coordinate));
   }

   private static BlockPos convertPos(BlockPos pos) {
      return new BlockPos(convertCoordinate(pos.getX()), convertCoordinate(pos.getY()), convertCoordinate(pos.getZ()));
   }

   private static BiomeSupplier createBiomeSupplier(MutableInt counter, Chunk chunk, BlockBox box, RegistryEntry biome, Predicate filter) {
      return (x, y, z, noise) -> {
         int l = BiomeCoords.toBlock(x);
         int m = BiomeCoords.toBlock(y);
         int n = BiomeCoords.toBlock(z);
         RegistryEntry lv = chunk.getBiomeForNoiseGen(x, y, z);
         if (box.contains(l, m, n) && filter.test(lv)) {
            counter.increment();
            return biome;
         } else {
            return lv;
         }
      };
   }

   private static int execute(ServerCommandSource source, BlockPos from, BlockPos to, RegistryEntry.Reference biome, Predicate filter) throws CommandSyntaxException {
      BlockPos lv = convertPos(from);
      BlockPos lv2 = convertPos(to);
      BlockBox lv3 = BlockBox.create(lv, lv2);
      int i = lv3.getBlockCountX() * lv3.getBlockCountY() * lv3.getBlockCountZ();
      int j = source.getWorld().getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);
      if (i > j) {
         throw TOO_BIG_EXCEPTION.create(j, i);
      } else {
         ServerWorld lv4 = source.getWorld();
         List list = new ArrayList();

         Chunk lv5;
         for(int k = ChunkSectionPos.getSectionCoord(lv3.getMinZ()); k <= ChunkSectionPos.getSectionCoord(lv3.getMaxZ()); ++k) {
            for(int l = ChunkSectionPos.getSectionCoord(lv3.getMinX()); l <= ChunkSectionPos.getSectionCoord(lv3.getMaxX()); ++l) {
               lv5 = lv4.getChunk(l, k, ChunkStatus.FULL, false);
               if (lv5 == null) {
                  throw UNLOADED_EXCEPTION.create();
               }

               list.add(lv5);
            }
         }

         MutableInt mutableInt = new MutableInt(0);
         Iterator var16 = list.iterator();

         while(var16.hasNext()) {
            lv5 = (Chunk)var16.next();
            lv5.populateBiomes(createBiomeSupplier(mutableInt, lv5, lv3, biome, filter), lv4.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
            lv5.setNeedsSaving(true);
         }

         lv4.getChunkManager().threadedAnvilChunkStorage.sendChunkBiomePackets(list);
         source.sendFeedback(Text.translatable("commands.fillbiome.success.count", mutableInt.getValue(), lv3.getMinX(), lv3.getMinY(), lv3.getMinZ(), lv3.getMaxX(), lv3.getMaxY(), lv3.getMaxZ()), true);
         return mutableInt.getValue();
      }
   }
}
