package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AdvancementCommand {
   private static final SuggestionProvider SUGGESTION_PROVIDER = (context, builder) -> {
      Collection collection = ((ServerCommandSource)context.getSource()).getServer().getAdvancementLoader().getAdvancements();
      return CommandSource.suggestIdentifiers(collection.stream().map(Advancement::getId), builder);
   };

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("advancement").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("grant").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("only").then(((RequiredArgumentBuilder)CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.ONLY));
      })).then(CommandManager.argument("criterion", StringArgumentType.greedyString()).suggests((context, builder) -> {
         return CommandSource.suggestMatching((Iterable)IdentifierArgumentType.getAdvancementArgument(context, "advancement").getCriteria().keySet(), builder);
      }).executes((context) -> {
         return executeCriterion((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.GRANT, IdentifierArgumentType.getAdvancementArgument(context, "advancement"), StringArgumentType.getString(context, "criterion"));
      }))))).then(CommandManager.literal("from").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.FROM));
      })))).then(CommandManager.literal("until").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.UNTIL));
      })))).then(CommandManager.literal("through").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.THROUGH));
      })))).then(CommandManager.literal("everything").executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.GRANT, ((ServerCommandSource)context.getSource()).getServer().getAdvancementLoader().getAdvancements());
      }))))).then(CommandManager.literal("revoke").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("only").then(((RequiredArgumentBuilder)CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.ONLY));
      })).then(CommandManager.argument("criterion", StringArgumentType.greedyString()).suggests((context, builder) -> {
         return CommandSource.suggestMatching((Iterable)IdentifierArgumentType.getAdvancementArgument(context, "advancement").getCriteria().keySet(), builder);
      }).executes((context) -> {
         return executeCriterion((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.REVOKE, IdentifierArgumentType.getAdvancementArgument(context, "advancement"), StringArgumentType.getString(context, "criterion"));
      }))))).then(CommandManager.literal("from").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.FROM));
      })))).then(CommandManager.literal("until").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.UNTIL));
      })))).then(CommandManager.literal("through").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(context, "advancement"), AdvancementCommand.Selection.THROUGH));
      })))).then(CommandManager.literal("everything").executes((context) -> {
         return executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), AdvancementCommand.Operation.REVOKE, ((ServerCommandSource)context.getSource()).getServer().getAdvancementLoader().getAdvancements());
      })))));
   }

   private static int executeAdvancement(ServerCommandSource source, Collection targets, Operation operation, Collection selection) {
      int i = 0;

      ServerPlayerEntity lv;
      for(Iterator var5 = targets.iterator(); var5.hasNext(); i += operation.processAll(lv, selection)) {
         lv = (ServerPlayerEntity)var5.next();
      }

      if (i == 0) {
         if (selection.size() == 1) {
            if (targets.size() == 1) {
               throw new CommandException(Text.translatable(operation.getCommandPrefix() + ".one.to.one.failure", ((Advancement)selection.iterator().next()).toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()));
            } else {
               throw new CommandException(Text.translatable(operation.getCommandPrefix() + ".one.to.many.failure", ((Advancement)selection.iterator().next()).toHoverableText(), targets.size()));
            }
         } else if (targets.size() == 1) {
            throw new CommandException(Text.translatable(operation.getCommandPrefix() + ".many.to.one.failure", selection.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()));
         } else {
            throw new CommandException(Text.translatable(operation.getCommandPrefix() + ".many.to.many.failure", selection.size(), targets.size()));
         }
      } else {
         if (selection.size() == 1) {
            if (targets.size() == 1) {
               source.sendFeedback(Text.translatable(operation.getCommandPrefix() + ".one.to.one.success", ((Advancement)selection.iterator().next()).toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
            } else {
               source.sendFeedback(Text.translatable(operation.getCommandPrefix() + ".one.to.many.success", ((Advancement)selection.iterator().next()).toHoverableText(), targets.size()), true);
            }
         } else if (targets.size() == 1) {
            source.sendFeedback(Text.translatable(operation.getCommandPrefix() + ".many.to.one.success", selection.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
         } else {
            source.sendFeedback(Text.translatable(operation.getCommandPrefix() + ".many.to.many.success", selection.size(), targets.size()), true);
         }

         return i;
      }
   }

   private static int executeCriterion(ServerCommandSource source, Collection targets, Operation operation, Advancement advancement, String criterion) {
      int i = 0;
      if (!advancement.getCriteria().containsKey(criterion)) {
         throw new CommandException(Text.translatable("commands.advancement.criterionNotFound", advancement.toHoverableText(), criterion));
      } else {
         Iterator var6 = targets.iterator();

         while(var6.hasNext()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var6.next();
            if (operation.processEachCriterion(lv, advancement, criterion)) {
               ++i;
            }
         }

         if (i == 0) {
            if (targets.size() == 1) {
               throw new CommandException(Text.translatable(operation.getCommandPrefix() + ".criterion.to.one.failure", criterion, advancement.toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()));
            } else {
               throw new CommandException(Text.translatable(operation.getCommandPrefix() + ".criterion.to.many.failure", criterion, advancement.toHoverableText(), targets.size()));
            }
         } else {
            if (targets.size() == 1) {
               source.sendFeedback(Text.translatable(operation.getCommandPrefix() + ".criterion.to.one.success", criterion, advancement.toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
            } else {
               source.sendFeedback(Text.translatable(operation.getCommandPrefix() + ".criterion.to.many.success", criterion, advancement.toHoverableText(), targets.size()), true);
            }

            return i;
         }
      }
   }

   private static List select(Advancement advancement, Selection selection) {
      List list = Lists.newArrayList();
      if (selection.before) {
         for(Advancement lv = advancement.getParent(); lv != null; lv = lv.getParent()) {
            list.add(lv);
         }
      }

      list.add(advancement);
      if (selection.after) {
         addChildrenRecursivelyToList(advancement, list);
      }

      return list;
   }

   private static void addChildrenRecursivelyToList(Advancement parent, List childList) {
      Iterator var2 = parent.getChildren().iterator();

      while(var2.hasNext()) {
         Advancement lv = (Advancement)var2.next();
         childList.add(lv);
         addChildrenRecursivelyToList(lv, childList);
      }

   }

   private static enum Operation {
      GRANT("grant") {
         protected boolean processEach(ServerPlayerEntity player, Advancement advancement) {
            AdvancementProgress lv = player.getAdvancementTracker().getProgress(advancement);
            if (lv.isDone()) {
               return false;
            } else {
               Iterator var4 = lv.getUnobtainedCriteria().iterator();

               while(var4.hasNext()) {
                  String string = (String)var4.next();
                  player.getAdvancementTracker().grantCriterion(advancement, string);
               }

               return true;
            }
         }

         protected boolean processEachCriterion(ServerPlayerEntity player, Advancement advancement, String criterion) {
            return player.getAdvancementTracker().grantCriterion(advancement, criterion);
         }
      },
      REVOKE("revoke") {
         protected boolean processEach(ServerPlayerEntity player, Advancement advancement) {
            AdvancementProgress lv = player.getAdvancementTracker().getProgress(advancement);
            if (!lv.isAnyObtained()) {
               return false;
            } else {
               Iterator var4 = lv.getObtainedCriteria().iterator();

               while(var4.hasNext()) {
                  String string = (String)var4.next();
                  player.getAdvancementTracker().revokeCriterion(advancement, string);
               }

               return true;
            }
         }

         protected boolean processEachCriterion(ServerPlayerEntity player, Advancement advancement, String criterion) {
            return player.getAdvancementTracker().revokeCriterion(advancement, criterion);
         }
      };

      private final String commandPrefix;

      Operation(String name) {
         this.commandPrefix = "commands.advancement." + name;
      }

      public int processAll(ServerPlayerEntity player, Iterable advancements) {
         int i = 0;
         Iterator var4 = advancements.iterator();

         while(var4.hasNext()) {
            Advancement lv = (Advancement)var4.next();
            if (this.processEach(player, lv)) {
               ++i;
            }
         }

         return i;
      }

      protected abstract boolean processEach(ServerPlayerEntity player, Advancement advancement);

      protected abstract boolean processEachCriterion(ServerPlayerEntity player, Advancement advancement, String criterion);

      protected String getCommandPrefix() {
         return this.commandPrefix;
      }

      // $FF: synthetic method
      private static Operation[] method_36964() {
         return new Operation[]{GRANT, REVOKE};
      }
   }

   private static enum Selection {
      ONLY(false, false),
      THROUGH(true, true),
      FROM(false, true),
      UNTIL(true, false),
      EVERYTHING(true, true);

      final boolean before;
      final boolean after;

      private Selection(boolean before, boolean after) {
         this.before = before;
         this.after = after;
      }

      // $FF: synthetic method
      private static Selection[] method_36965() {
         return new Selection[]{ONLY, THROUGH, FROM, UNTIL, EVERYTHING};
      }
   }
}
