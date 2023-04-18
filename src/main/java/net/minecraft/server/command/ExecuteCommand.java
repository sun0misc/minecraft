package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.HeightmapArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.SwizzleArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class ExecuteCommand {
   private static final int MAX_BLOCKS = 32768;
   private static final Dynamic2CommandExceptionType BLOCKS_TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> {
      return Text.translatable("commands.execute.blocks.toobig", maxCount, count);
   });
   private static final SimpleCommandExceptionType CONDITIONAL_FAIL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.execute.conditional.fail"));
   private static final DynamicCommandExceptionType CONDITIONAL_FAIL_COUNT_EXCEPTION = new DynamicCommandExceptionType((count) -> {
      return Text.translatable("commands.execute.conditional.fail_count", count);
   });
   private static final BinaryOperator BINARY_RESULT_CONSUMER = (consumer, consumer2) -> {
      return (context, success, result) -> {
         consumer.onCommandComplete(context, success, result);
         consumer2.onCommandComplete(context, success, result);
      };
   };
   private static final SuggestionProvider LOOT_CONDITIONS = (context, builder) -> {
      LootManager lv = ((ServerCommandSource)context.getSource()).getServer().getLootManager();
      return CommandSource.suggestIdentifiers((Iterable)lv.getIds(LootDataType.PREDICATES), builder);
   };

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      LiteralCommandNode literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("execute").requires((source) -> {
         return source.hasPermissionLevel(2);
      }));
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("execute").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("run").redirect(dispatcher.getRoot()))).then(addConditionArguments(literalCommandNode, CommandManager.literal("if"), true, commandRegistryAccess))).then(addConditionArguments(literalCommandNode, CommandManager.literal("unless"), false, commandRegistryAccess))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (context) -> {
         List list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(context, "targets").iterator();

         while(var2.hasNext()) {
            Entity lv = (Entity)var2.next();
            list.add(((ServerCommandSource)context.getSource()).withEntity(lv));
         }

         return list;
      })))).then(CommandManager.literal("at").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (context) -> {
         List list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(context, "targets").iterator();

         while(var2.hasNext()) {
            Entity lv = (Entity)var2.next();
            list.add(((ServerCommandSource)context.getSource()).withWorld((ServerWorld)lv.world).withPosition(lv.getPos()).withRotation(lv.getRotationClient()));
         }

         return list;
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("store").then(addStoreArguments(literalCommandNode, CommandManager.literal("result"), true))).then(addStoreArguments(literalCommandNode, CommandManager.literal("success"), false)))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("positioned").then(CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect(literalCommandNode, (context) -> {
         return ((ServerCommandSource)context.getSource()).withPosition(Vec3ArgumentType.getVec3(context, "pos")).withEntityAnchor(EntityAnchorArgumentType.EntityAnchor.FEET);
      }))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (context) -> {
         List list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(context, "targets").iterator();

         while(var2.hasNext()) {
            Entity lv = (Entity)var2.next();
            list.add(((ServerCommandSource)context.getSource()).withPosition(lv.getPos()));
         }

         return list;
      })))).then(CommandManager.literal("over").then(CommandManager.argument("heightmap", HeightmapArgumentType.heightmap()).redirect(literalCommandNode, (context) -> {
         Vec3d lv = ((ServerCommandSource)context.getSource()).getPosition();
         ServerWorld lv2 = ((ServerCommandSource)context.getSource()).getWorld();
         double d = lv.getX();
         double e = lv.getZ();
         if (!lv2.isChunkLoaded(ChunkSectionPos.getSectionCoordFloored(d), ChunkSectionPos.getSectionCoordFloored(e))) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
         } else {
            int i = lv2.getTopY(HeightmapArgumentType.getHeightmap(context, "heightmap"), MathHelper.floor(d), MathHelper.floor(e));
            return ((ServerCommandSource)context.getSource()).withPosition(new Vec3d(d, (double)i, e));
         }
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("rotated").then(CommandManager.argument("rot", RotationArgumentType.rotation()).redirect(literalCommandNode, (context) -> {
         return ((ServerCommandSource)context.getSource()).withRotation(RotationArgumentType.getRotation(context, "rot").toAbsoluteRotation((ServerCommandSource)context.getSource()));
      }))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (context) -> {
         List list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(context, "targets").iterator();

         while(var2.hasNext()) {
            Entity lv = (Entity)var2.next();
            list.add(((ServerCommandSource)context.getSource()).withRotation(lv.getRotationClient()));
         }

         return list;
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("facing").then(CommandManager.literal("entity").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).fork(literalCommandNode, (context) -> {
         List list = Lists.newArrayList();
         EntityAnchorArgumentType.EntityAnchor lv = EntityAnchorArgumentType.getEntityAnchor(context, "anchor");
         Iterator var3 = EntityArgumentType.getOptionalEntities(context, "targets").iterator();

         while(var3.hasNext()) {
            Entity lv2 = (Entity)var3.next();
            list.add(((ServerCommandSource)context.getSource()).withLookingAt(lv2, lv));
         }

         return list;
      }))))).then(CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect(literalCommandNode, (context) -> {
         return ((ServerCommandSource)context.getSource()).withLookingAt(Vec3ArgumentType.getVec3(context, "pos"));
      })))).then(CommandManager.literal("align").then(CommandManager.argument("axes", SwizzleArgumentType.swizzle()).redirect(literalCommandNode, (context) -> {
         return ((ServerCommandSource)context.getSource()).withPosition(((ServerCommandSource)context.getSource()).getPosition().floorAlongAxes(SwizzleArgumentType.getSwizzle(context, "axes")));
      })))).then(CommandManager.literal("anchored").then(CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).redirect(literalCommandNode, (context) -> {
         return ((ServerCommandSource)context.getSource()).withEntityAnchor(EntityAnchorArgumentType.getEntityAnchor(context, "anchor"));
      })))).then(CommandManager.literal("in").then(CommandManager.argument("dimension", DimensionArgumentType.dimension()).redirect(literalCommandNode, (context) -> {
         return ((ServerCommandSource)context.getSource()).withWorld(DimensionArgumentType.getDimensionArgument(context, "dimension"));
      })))).then(CommandManager.literal("summon").then(CommandManager.argument("entity", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).redirect(literalCommandNode, (context) -> {
         return summon((ServerCommandSource)context.getSource(), RegistryEntryArgumentType.getSummonableEntityType(context, "entity"));
      })))).then(addOnArguments(literalCommandNode, CommandManager.literal("on"))));
   }

   private static ArgumentBuilder addStoreArguments(LiteralCommandNode node, LiteralArgumentBuilder builder, boolean requestResult) {
      builder.then(CommandManager.literal("score").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).redirect(node, (context) -> {
         return executeStoreScore((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getObjective(context, "objective"), requestResult);
      }))));
      builder.then(CommandManager.literal("bossbar").then(((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(BossBarCommand.SUGGESTION_PROVIDER).then(CommandManager.literal("value").redirect(node, (context) -> {
         return executeStoreBossbar((ServerCommandSource)context.getSource(), BossBarCommand.getBossBar(context), true, requestResult);
      }))).then(CommandManager.literal("max").redirect(node, (context) -> {
         return executeStoreBossbar((ServerCommandSource)context.getSource(), BossBarCommand.getBossBar(context), false, requestResult);
      }))));
      Iterator var3 = DataCommand.TARGET_OBJECT_TYPES.iterator();

      while(var3.hasNext()) {
         DataCommand.ObjectType lv = (DataCommand.ObjectType)var3.next();
         lv.addArgumentsToBuilder(builder, (builderx) -> {
            return builderx.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).then(CommandManager.literal("int").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (context) -> {
               return executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), (result) -> {
                  return NbtInt.of((int)((double)result * DoubleArgumentType.getDouble(context, "scale")));
               }, requestResult);
            })))).then(CommandManager.literal("float").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (context) -> {
               return executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), (result) -> {
                  return NbtFloat.of((float)((double)result * DoubleArgumentType.getDouble(context, "scale")));
               }, requestResult);
            })))).then(CommandManager.literal("short").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (context) -> {
               return executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), (result) -> {
                  return NbtShort.of((short)((int)((double)result * DoubleArgumentType.getDouble(context, "scale"))));
               }, requestResult);
            })))).then(CommandManager.literal("long").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (context) -> {
               return executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), (result) -> {
                  return NbtLong.of((long)((double)result * DoubleArgumentType.getDouble(context, "scale")));
               }, requestResult);
            })))).then(CommandManager.literal("double").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (context) -> {
               return executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), (result) -> {
                  return NbtDouble.of((double)result * DoubleArgumentType.getDouble(context, "scale"));
               }, requestResult);
            })))).then(CommandManager.literal("byte").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (context) -> {
               return executeStoreData((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), (result) -> {
                  return NbtByte.of((byte)((int)((double)result * DoubleArgumentType.getDouble(context, "scale"))));
               }, requestResult);
            }))));
         });
      }

      return builder;
   }

   private static ServerCommandSource executeStoreScore(ServerCommandSource source, Collection targets, ScoreboardObjective objective, boolean requestResult) {
      Scoreboard lv = source.getServer().getScoreboard();
      return source.mergeConsumers((context, success, result) -> {
         Iterator var7 = targets.iterator();

         while(var7.hasNext()) {
            String string = (String)var7.next();
            ScoreboardPlayerScore lvx = lv.getPlayerScore(string, objective);
            int j = requestResult ? result : (success ? 1 : 0);
            lvx.setScore(j);
         }

      }, BINARY_RESULT_CONSUMER);
   }

   private static ServerCommandSource executeStoreBossbar(ServerCommandSource source, CommandBossBar bossBar, boolean storeInValue, boolean requestResult) {
      return source.mergeConsumers((context, success, result) -> {
         int j = requestResult ? result : (success ? 1 : 0);
         if (storeInValue) {
            bossBar.setValue(j);
         } else {
            bossBar.setMaxValue(j);
         }

      }, BINARY_RESULT_CONSUMER);
   }

   private static ServerCommandSource executeStoreData(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, IntFunction nbtSetter, boolean requestResult) {
      return source.mergeConsumers((context, success, result) -> {
         try {
            NbtCompound lv = object.getNbt();
            int j = requestResult ? result : (success ? 1 : 0);
            path.put(lv, (NbtElement)nbtSetter.apply(j));
            object.setNbt(lv);
         } catch (CommandSyntaxException var9) {
         }

      }, BINARY_RESULT_CONSUMER);
   }

   private static boolean isLoaded(ServerWorld world, BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX());
      int j = ChunkSectionPos.getSectionCoord(pos.getZ());
      WorldChunk lv = world.getChunkManager().getWorldChunk(i, j);
      if (lv != null) {
         return lv.getLevelType() == ChunkHolder.LevelType.ENTITY_TICKING;
      } else {
         return false;
      }
   }

   private static ArgumentBuilder addConditionArguments(CommandNode root, LiteralArgumentBuilder argumentBuilder, boolean positive, CommandRegistryAccess commandRegistryAccess) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)argumentBuilder.then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(addConditionLogic(root, CommandManager.argument("block", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)), positive, (context) -> {
         return BlockPredicateArgumentType.getBlockPredicate(context, "block").test(new CachedBlockPosition(((ServerCommandSource)context.getSource()).getWorld(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), true));
      }))))).then(CommandManager.literal("biome").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(addConditionLogic(root, CommandManager.argument("biome", RegistryEntryPredicateArgumentType.registryEntryPredicate(commandRegistryAccess, RegistryKeys.BIOME)), positive, (context) -> {
         return RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "biome", RegistryKeys.BIOME).test(((ServerCommandSource)context.getSource()).getWorld().getBiome(BlockPosArgumentType.getLoadedBlockPos(context, "pos")));
      }))))).then(CommandManager.literal("loaded").then(addConditionLogic(root, CommandManager.argument("pos", BlockPosArgumentType.blockPos()), positive, (commandContext) -> {
         return isLoaded(((ServerCommandSource)commandContext.getSource()).getWorld(), BlockPosArgumentType.getBlockPos(commandContext, "pos"));
      })))).then(CommandManager.literal("dimension").then(addConditionLogic(root, CommandManager.argument("dimension", DimensionArgumentType.dimension()), positive, (context) -> {
         return DimensionArgumentType.getDimensionArgument(context, "dimension") == ((ServerCommandSource)context.getSource()).getWorld();
      })))).then(CommandManager.literal("score").then(CommandManager.argument("target", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targetObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.literal("=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (context) -> {
         return testScoreCondition(context, Integer::equals);
      }))))).then(CommandManager.literal("<").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (context) -> {
         return testScoreCondition(context, (a, b) -> {
            return a < b;
         });
      }))))).then(CommandManager.literal("<=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (context) -> {
         return testScoreCondition(context, (a, b) -> {
            return a <= b;
         });
      }))))).then(CommandManager.literal(">").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (context) -> {
         return testScoreCondition(context, (a, b) -> {
            return a > b;
         });
      }))))).then(CommandManager.literal(">=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (context) -> {
         return testScoreCondition(context, (a, b) -> {
            return a >= b;
         });
      }))))).then(CommandManager.literal("matches").then(addConditionLogic(root, CommandManager.argument("range", NumberRangeArgumentType.intRange()), positive, (context) -> {
         return testScoreMatch(context, NumberRangeArgumentType.IntRangeArgumentType.getRangeArgument(context, "range"));
      }))))))).then(CommandManager.literal("blocks").then(CommandManager.argument("start", BlockPosArgumentType.blockPos()).then(CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).then(addBlocksConditionLogic(root, CommandManager.literal("all"), positive, false))).then(addBlocksConditionLogic(root, CommandManager.literal("masked"), positive, true))))))).then(CommandManager.literal("entity").then(((RequiredArgumentBuilder)CommandManager.argument("entities", EntityArgumentType.entities()).fork(root, (context) -> {
         return getSourceOrEmptyForConditionFork(context, positive, !EntityArgumentType.getOptionalEntities(context, "entities").isEmpty());
      })).executes(getExistsConditionExecute(positive, (context) -> {
         return EntityArgumentType.getOptionalEntities(context, "entities").size();
      }))))).then(CommandManager.literal("predicate").then(addConditionLogic(root, CommandManager.argument("predicate", IdentifierArgumentType.identifier()).suggests(LOOT_CONDITIONS), positive, (context) -> {
         return testLootCondition((ServerCommandSource)context.getSource(), IdentifierArgumentType.getPredicateArgument(context, "predicate"));
      })));
      Iterator var4 = DataCommand.SOURCE_OBJECT_TYPES.iterator();

      while(var4.hasNext()) {
         DataCommand.ObjectType lv = (DataCommand.ObjectType)var4.next();
         argumentBuilder.then(lv.addArgumentsToBuilder(CommandManager.literal("data"), (builder) -> {
            return builder.then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).fork(root, (context) -> {
               return getSourceOrEmptyForConditionFork(context, positive, countPathMatches(lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path")) > 0);
            })).executes(getExistsConditionExecute(positive, (context) -> {
               return countPathMatches(lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"));
            })));
         }));
      }

      return argumentBuilder;
   }

   private static Command getExistsConditionExecute(boolean positive, ExistsCondition condition) {
      return positive ? (context) -> {
         int i = condition.test(context);
         if (i > 0) {
            ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass_count", i), false);
            return i;
         } else {
            throw CONDITIONAL_FAIL_EXCEPTION.create();
         }
      } : (context) -> {
         int i = condition.test(context);
         if (i == 0) {
            ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create(i);
         }
      };
   }

   private static int countPathMatches(DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      return path.count(object.getNbt());
   }

   private static boolean testScoreCondition(CommandContext context, BiPredicate condition) throws CommandSyntaxException {
      String string = ScoreHolderArgumentType.getScoreHolder(context, "target");
      ScoreboardObjective lv = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
      String string2 = ScoreHolderArgumentType.getScoreHolder(context, "source");
      ScoreboardObjective lv2 = ScoreboardObjectiveArgumentType.getObjective(context, "sourceObjective");
      Scoreboard lv3 = ((ServerCommandSource)context.getSource()).getServer().getScoreboard();
      if (lv3.playerHasObjective(string, lv) && lv3.playerHasObjective(string2, lv2)) {
         ScoreboardPlayerScore lv4 = lv3.getPlayerScore(string, lv);
         ScoreboardPlayerScore lv5 = lv3.getPlayerScore(string2, lv2);
         return condition.test(lv4.getScore(), lv5.getScore());
      } else {
         return false;
      }
   }

   private static boolean testScoreMatch(CommandContext context, NumberRange.IntRange range) throws CommandSyntaxException {
      String string = ScoreHolderArgumentType.getScoreHolder(context, "target");
      ScoreboardObjective lv = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
      Scoreboard lv2 = ((ServerCommandSource)context.getSource()).getServer().getScoreboard();
      return !lv2.playerHasObjective(string, lv) ? false : range.test(lv2.getPlayerScore(string, lv).getScore());
   }

   private static boolean testLootCondition(ServerCommandSource source, LootCondition condition) {
      ServerWorld lv = source.getWorld();
      LootContext lv2 = (new LootContext.Builder(lv)).parameter(LootContextParameters.ORIGIN, source.getPosition()).optionalParameter(LootContextParameters.THIS_ENTITY, source.getEntity()).build(LootContextTypes.COMMAND);
      lv2.markActive(LootContext.predicate(condition));
      return condition.test(lv2);
   }

   private static Collection getSourceOrEmptyForConditionFork(CommandContext context, boolean positive, boolean value) {
      return (Collection)(value == positive ? Collections.singleton((ServerCommandSource)context.getSource()) : Collections.emptyList());
   }

   private static ArgumentBuilder addConditionLogic(CommandNode root, ArgumentBuilder builder, boolean positive, Condition condition) {
      return builder.fork(root, (context) -> {
         return getSourceOrEmptyForConditionFork(context, positive, condition.test(context));
      }).executes((context) -> {
         if (positive == condition.test(context)) {
            ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw CONDITIONAL_FAIL_EXCEPTION.create();
         }
      });
   }

   private static ArgumentBuilder addBlocksConditionLogic(CommandNode root, ArgumentBuilder builder, boolean positive, boolean masked) {
      return builder.fork(root, (context) -> {
         return getSourceOrEmptyForConditionFork(context, positive, testBlocksCondition(context, masked).isPresent());
      }).executes(positive ? (context) -> {
         return executePositiveBlockCondition(context, masked);
      } : (context) -> {
         return executeNegativeBlockCondition(context, masked);
      });
   }

   private static int executePositiveBlockCondition(CommandContext context, boolean masked) throws CommandSyntaxException {
      OptionalInt optionalInt = testBlocksCondition(context, masked);
      if (optionalInt.isPresent()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass_count", optionalInt.getAsInt()), false);
         return optionalInt.getAsInt();
      } else {
         throw CONDITIONAL_FAIL_EXCEPTION.create();
      }
   }

   private static int executeNegativeBlockCondition(CommandContext context, boolean masked) throws CommandSyntaxException {
      OptionalInt optionalInt = testBlocksCondition(context, masked);
      if (optionalInt.isPresent()) {
         throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create(optionalInt.getAsInt());
      } else {
         ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.execute.conditional.pass"), false);
         return 1;
      }
   }

   private static OptionalInt testBlocksCondition(CommandContext context, boolean masked) throws CommandSyntaxException {
      return testBlocksCondition(((ServerCommandSource)context.getSource()).getWorld(), BlockPosArgumentType.getLoadedBlockPos(context, "start"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), masked);
   }

   private static OptionalInt testBlocksCondition(ServerWorld world, BlockPos start, BlockPos end, BlockPos destination, boolean masked) throws CommandSyntaxException {
      BlockBox lv = BlockBox.create(start, end);
      BlockBox lv2 = BlockBox.create(destination, destination.add(lv.getDimensions()));
      BlockPos lv3 = new BlockPos(lv2.getMinX() - lv.getMinX(), lv2.getMinY() - lv.getMinY(), lv2.getMinZ() - lv.getMinZ());
      int i = lv.getBlockCountX() * lv.getBlockCountY() * lv.getBlockCountZ();
      if (i > 32768) {
         throw BLOCKS_TOOBIG_EXCEPTION.create(32768, i);
      } else {
         int j = 0;

         for(int k = lv.getMinZ(); k <= lv.getMaxZ(); ++k) {
            for(int l = lv.getMinY(); l <= lv.getMaxY(); ++l) {
               for(int m = lv.getMinX(); m <= lv.getMaxX(); ++m) {
                  BlockPos lv4 = new BlockPos(m, l, k);
                  BlockPos lv5 = lv4.add(lv3);
                  BlockState lv6 = world.getBlockState(lv4);
                  if (!masked || !lv6.isOf(Blocks.AIR)) {
                     if (lv6 != world.getBlockState(lv5)) {
                        return OptionalInt.empty();
                     }

                     BlockEntity lv7 = world.getBlockEntity(lv4);
                     BlockEntity lv8 = world.getBlockEntity(lv5);
                     if (lv7 != null) {
                        if (lv8 == null) {
                           return OptionalInt.empty();
                        }

                        if (lv8.getType() != lv7.getType()) {
                           return OptionalInt.empty();
                        }

                        NbtCompound lv9 = lv7.createNbt();
                        NbtCompound lv10 = lv8.createNbt();
                        if (!lv9.equals(lv10)) {
                           return OptionalInt.empty();
                        }
                     }

                     ++j;
                  }
               }
            }
         }

         return OptionalInt.of(j);
      }
   }

   private static RedirectModifier createEntityModifier(Function function) {
      return (context) -> {
         ServerCommandSource lv = (ServerCommandSource)context.getSource();
         Entity lv2 = lv.getEntity();
         return (Collection)(lv2 == null ? List.of() : (Collection)((Optional)function.apply(lv2)).filter((entity) -> {
            return !entity.isRemoved();
         }).map((entity) -> {
            return List.of(lv.withEntity(entity));
         }).orElse(List.of()));
      };
   }

   private static RedirectModifier createMultiEntityModifier(Function function) {
      return (context) -> {
         ServerCommandSource lv = (ServerCommandSource)context.getSource();
         Entity lv2 = lv.getEntity();
         if (lv2 == null) {
            return List.of();
         } else {
            Stream var10000 = ((Stream)function.apply(lv2)).filter((entity) -> {
               return !entity.isRemoved();
            });
            Objects.requireNonNull(lv);
            return var10000.map(lv::withEntity).toList();
         }
      };
   }

   private static LiteralArgumentBuilder addOnArguments(CommandNode node, LiteralArgumentBuilder builder) {
      return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(CommandManager.literal("owner").fork(node, createEntityModifier((entity) -> {
         Optional var10000;
         if (entity instanceof Tameable lv) {
            var10000 = Optional.ofNullable(lv.getOwner());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      })))).then(CommandManager.literal("leasher").fork(node, createEntityModifier((entity) -> {
         Optional var10000;
         if (entity instanceof MobEntity lv) {
            var10000 = Optional.ofNullable(lv.getHoldingEntity());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      })))).then(CommandManager.literal("target").fork(node, createEntityModifier((entity) -> {
         Optional var10000;
         if (entity instanceof Targeter lv) {
            var10000 = Optional.ofNullable(lv.getTarget());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      })))).then(CommandManager.literal("attacker").fork(node, createEntityModifier((entity) -> {
         Optional var10000;
         if (entity instanceof Attackable lv) {
            var10000 = Optional.ofNullable(lv.getLastAttacker());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      })))).then(CommandManager.literal("vehicle").fork(node, createEntityModifier((entity) -> {
         return Optional.ofNullable(entity.getVehicle());
      })))).then(CommandManager.literal("controller").fork(node, createEntityModifier((entity) -> {
         return Optional.ofNullable(entity.getControllingPassenger());
      })))).then(CommandManager.literal("origin").fork(node, createEntityModifier((entity) -> {
         Optional var10000;
         if (entity instanceof Ownable lv) {
            var10000 = Optional.ofNullable(lv.getOwner());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      })))).then(CommandManager.literal("passengers").fork(node, createMultiEntityModifier((entity) -> {
         return entity.getPassengerList().stream();
      })));
   }

   private static ServerCommandSource summon(ServerCommandSource source, RegistryEntry.Reference entityType) throws CommandSyntaxException {
      Entity lv = SummonCommand.summon(source, entityType, source.getPosition(), new NbtCompound(), true);
      return source.withEntity(lv);
   }

   @FunctionalInterface
   private interface Condition {
      boolean test(CommandContext context) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface ExistsCondition {
      int test(CommandContext context) throws CommandSyntaxException;
   }
}
