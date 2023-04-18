package net.minecraft.server.command;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.command.argument.RegistryPredicateArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.slf4j.Logger;

public class LocateCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final DynamicCommandExceptionType STRUCTURE_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("commands.locate.structure.not_found", id);
   });
   private static final DynamicCommandExceptionType STRUCTURE_INVALID_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("commands.locate.structure.invalid", id);
   });
   private static final DynamicCommandExceptionType BIOME_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("commands.locate.biome.not_found", id);
   });
   private static final DynamicCommandExceptionType POI_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("commands.locate.poi.not_found", id);
   });
   private static final int LOCATE_STRUCTURE_RADIUS = 100;
   private static final int LOCATE_BIOME_RADIUS = 6400;
   private static final int LOCATE_BIOME_HORIZONTAL_BLOCK_CHECK_INTERVAL = 32;
   private static final int LOCATE_BIOME_VERTICAL_BLOCK_CHECK_INTERVAL = 64;
   private static final int LOCATE_POI_RADIUS = 256;

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess registryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("locate").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("structure").then(CommandManager.argument("structure", RegistryPredicateArgumentType.registryPredicate(RegistryKeys.STRUCTURE)).executes((context) -> {
         return executeLocateStructure((ServerCommandSource)context.getSource(), RegistryPredicateArgumentType.getPredicate(context, "structure", RegistryKeys.STRUCTURE, STRUCTURE_INVALID_EXCEPTION));
      })))).then(CommandManager.literal("biome").then(CommandManager.argument("biome", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.BIOME)).executes((context) -> {
         return executeLocateBiome((ServerCommandSource)context.getSource(), RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "biome", RegistryKeys.BIOME));
      })))).then(CommandManager.literal("poi").then(CommandManager.argument("poi", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.POINT_OF_INTEREST_TYPE)).executes((context) -> {
         return executeLocatePoi((ServerCommandSource)context.getSource(), RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "poi", RegistryKeys.POINT_OF_INTEREST_TYPE));
      }))));
   }

   private static Optional getStructureListForPredicate(RegistryPredicateArgumentType.RegistryPredicate predicate, Registry structureRegistry) {
      Either var10000 = predicate.getKey();
      Function var10001 = (key) -> {
         return structureRegistry.getEntry(key).map((entry) -> {
            return RegistryEntryList.of(entry);
         });
      };
      Objects.requireNonNull(structureRegistry);
      return (Optional)var10000.map(var10001, structureRegistry::getEntryList);
   }

   private static int executeLocateStructure(ServerCommandSource source, RegistryPredicateArgumentType.RegistryPredicate predicate) throws CommandSyntaxException {
      Registry lv = source.getWorld().getRegistryManager().get(RegistryKeys.STRUCTURE);
      RegistryEntryList lv2 = (RegistryEntryList)getStructureListForPredicate(predicate, lv).orElseThrow(() -> {
         return STRUCTURE_INVALID_EXCEPTION.create(predicate.asString());
      });
      BlockPos lv3 = BlockPos.ofFloored(source.getPosition());
      ServerWorld lv4 = source.getWorld();
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Pair pair = lv4.getChunkManager().getChunkGenerator().locateStructure(lv4, lv2, lv3, 100, false);
      stopwatch.stop();
      if (pair == null) {
         throw STRUCTURE_NOT_FOUND_EXCEPTION.create(predicate.asString());
      } else {
         return sendCoordinates(source, predicate, lv3, pair, "commands.locate.structure.success", false, stopwatch.elapsed());
      }
   }

   private static int executeLocateBiome(ServerCommandSource source, RegistryEntryPredicateArgumentType.EntryPredicate predicate) throws CommandSyntaxException {
      BlockPos lv = BlockPos.ofFloored(source.getPosition());
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Pair pair = source.getWorld().locateBiome(predicate, lv, 6400, 32, 64);
      stopwatch.stop();
      if (pair == null) {
         throw BIOME_NOT_FOUND_EXCEPTION.create(predicate.asString());
      } else {
         return sendCoordinates(source, predicate, lv, pair, "commands.locate.biome.success", true, stopwatch.elapsed());
      }
   }

   private static int executeLocatePoi(ServerCommandSource source, RegistryEntryPredicateArgumentType.EntryPredicate predicate) throws CommandSyntaxException {
      BlockPos lv = BlockPos.ofFloored(source.getPosition());
      ServerWorld lv2 = source.getWorld();
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Optional optional = lv2.getPointOfInterestStorage().getNearestTypeAndPosition(predicate, lv, 256, PointOfInterestStorage.OccupationStatus.ANY);
      stopwatch.stop();
      if (optional.isEmpty()) {
         throw POI_NOT_FOUND_EXCEPTION.create(predicate.asString());
      } else {
         return sendCoordinates(source, predicate, lv, ((Pair)optional.get()).swap(), "commands.locate.poi.success", false, stopwatch.elapsed());
      }
   }

   private static String getKeyString(Pair result) {
      return (String)((RegistryEntry)result.getSecond()).getKey().map((key) -> {
         return key.getValue().toString();
      }).orElse("[unregistered]");
   }

   public static int sendCoordinates(ServerCommandSource source, RegistryEntryPredicateArgumentType.EntryPredicate predicate, BlockPos currentPos, Pair result, String successMessage, boolean includeY, Duration timeTaken) {
      String string2 = (String)predicate.getEntry().map((entry) -> {
         return predicate.asString();
      }, (tag) -> {
         String var10000 = predicate.asString();
         return var10000 + " (" + getKeyString(result) + ")";
      });
      return sendCoordinates(source, currentPos, result, successMessage, includeY, string2, timeTaken);
   }

   public static int sendCoordinates(ServerCommandSource source, RegistryPredicateArgumentType.RegistryPredicate structure, BlockPos currentPos, Pair result, String successMessage, boolean includeY, Duration timeTaken) {
      String string2 = (String)structure.getKey().map((key) -> {
         return key.getValue().toString();
      }, (key) -> {
         Identifier var10000 = key.id();
         return "#" + var10000 + " (" + getKeyString(result) + ")";
      });
      return sendCoordinates(source, currentPos, result, successMessage, includeY, string2, timeTaken);
   }

   private static int sendCoordinates(ServerCommandSource source, BlockPos currentPos, Pair result, String successMessage, boolean includeY, String entryString, Duration timeTaken) {
      BlockPos lv = (BlockPos)result.getFirst();
      int i = includeY ? MathHelper.floor(MathHelper.sqrt((float)currentPos.getSquaredDistance(lv))) : MathHelper.floor(getDistance(currentPos.getX(), currentPos.getZ(), lv.getX(), lv.getZ()));
      String string3 = includeY ? String.valueOf(lv.getY()) : "~";
      Text lv2 = Texts.bracketed(Text.translatable("chat.coordinates", lv.getX(), string3, lv.getZ())).styled((style) -> {
         return style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + lv.getX() + " " + string3 + " " + lv.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip")));
      });
      source.sendFeedback(Text.translatable(successMessage, entryString, lv2, i), false);
      LOGGER.info("Locating element " + entryString + " took " + timeTaken.toMillis() + " ms");
      return i;
   }

   private static float getDistance(int x1, int y1, int x2, int y2) {
      int m = x2 - x1;
      int n = y2 - y1;
      return MathHelper.sqrt((float)(m * m + n * n));
   }
}
