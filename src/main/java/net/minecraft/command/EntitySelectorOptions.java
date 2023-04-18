package net.minecraft.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

public class EntitySelectorOptions {
   private static final Map OPTIONS = Maps.newHashMap();
   public static final DynamicCommandExceptionType UNKNOWN_OPTION_EXCEPTION = new DynamicCommandExceptionType((option) -> {
      return Text.translatable("argument.entity.options.unknown", option);
   });
   public static final DynamicCommandExceptionType INAPPLICABLE_OPTION_EXCEPTION = new DynamicCommandExceptionType((option) -> {
      return Text.translatable("argument.entity.options.inapplicable", option);
   });
   public static final SimpleCommandExceptionType NEGATIVE_DISTANCE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.options.distance.negative"));
   public static final SimpleCommandExceptionType NEGATIVE_LEVEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.options.level.negative"));
   public static final SimpleCommandExceptionType TOO_SMALL_LEVEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.options.limit.toosmall"));
   public static final DynamicCommandExceptionType IRREVERSIBLE_SORT_EXCEPTION = new DynamicCommandExceptionType((sortType) -> {
      return Text.translatable("argument.entity.options.sort.irreversible", sortType);
   });
   public static final DynamicCommandExceptionType INVALID_MODE_EXCEPTION = new DynamicCommandExceptionType((gameMode) -> {
      return Text.translatable("argument.entity.options.mode.invalid", gameMode);
   });
   public static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType((entity) -> {
      return Text.translatable("argument.entity.options.type.invalid", entity);
   });

   private static void putOption(String id, SelectorHandler handler, Predicate condition, Text description) {
      OPTIONS.put(id, new SelectorOption(handler, condition, description));
   }

   public static void register() {
      if (OPTIONS.isEmpty()) {
         putOption("name", (reader) -> {
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readString();
            if (reader.excludesName() && !bl) {
               reader.getReader().setCursor(i);
               throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "name");
            } else {
               if (bl) {
                  reader.setExcludesName(true);
               } else {
                  reader.setSelectsName(true);
               }

               reader.setPredicate((readerx) -> {
                  return readerx.getName().getString().equals(string) != bl;
               });
            }
         }, (reader) -> {
            return !reader.selectsName();
         }, Text.translatable("argument.entity.options.name.description"));
         putOption("distance", (reader) -> {
            int i = reader.getReader().getCursor();
            NumberRange.FloatRange lv = NumberRange.FloatRange.parse(reader.getReader());
            if ((lv.getMin() == null || !((Double)lv.getMin() < 0.0)) && (lv.getMax() == null || !((Double)lv.getMax() < 0.0))) {
               reader.setDistance(lv);
               reader.setLocalWorldOnly();
            } else {
               reader.getReader().setCursor(i);
               throw NEGATIVE_DISTANCE_EXCEPTION.createWithContext(reader.getReader());
            }
         }, (reader) -> {
            return reader.getDistance().isDummy();
         }, Text.translatable("argument.entity.options.distance.description"));
         putOption("level", (reader) -> {
            int i = reader.getReader().getCursor();
            NumberRange.IntRange lv = NumberRange.IntRange.parse(reader.getReader());
            if ((lv.getMin() == null || (Integer)lv.getMin() >= 0) && (lv.getMax() == null || (Integer)lv.getMax() >= 0)) {
               reader.setLevelRange(lv);
               reader.setIncludesNonPlayers(false);
            } else {
               reader.getReader().setCursor(i);
               throw NEGATIVE_LEVEL_EXCEPTION.createWithContext(reader.getReader());
            }
         }, (reader) -> {
            return reader.getLevelRange().isDummy();
         }, Text.translatable("argument.entity.options.level.description"));
         putOption("x", (reader) -> {
            reader.setLocalWorldOnly();
            reader.setX(reader.getReader().readDouble());
         }, (reader) -> {
            return reader.getX() == null;
         }, Text.translatable("argument.entity.options.x.description"));
         putOption("y", (reader) -> {
            reader.setLocalWorldOnly();
            reader.setY(reader.getReader().readDouble());
         }, (reader) -> {
            return reader.getY() == null;
         }, Text.translatable("argument.entity.options.y.description"));
         putOption("z", (reader) -> {
            reader.setLocalWorldOnly();
            reader.setZ(reader.getReader().readDouble());
         }, (reader) -> {
            return reader.getZ() == null;
         }, Text.translatable("argument.entity.options.z.description"));
         putOption("dx", (reader) -> {
            reader.setLocalWorldOnly();
            reader.setDx(reader.getReader().readDouble());
         }, (reader) -> {
            return reader.getDx() == null;
         }, Text.translatable("argument.entity.options.dx.description"));
         putOption("dy", (reader) -> {
            reader.setLocalWorldOnly();
            reader.setDy(reader.getReader().readDouble());
         }, (reader) -> {
            return reader.getDy() == null;
         }, Text.translatable("argument.entity.options.dy.description"));
         putOption("dz", (reader) -> {
            reader.setLocalWorldOnly();
            reader.setDz(reader.getReader().readDouble());
         }, (reader) -> {
            return reader.getDz() == null;
         }, Text.translatable("argument.entity.options.dz.description"));
         putOption("x_rotation", (reader) -> {
            reader.setPitchRange(FloatRangeArgument.parse(reader.getReader(), true, MathHelper::wrapDegrees));
         }, (reader) -> {
            return reader.getPitchRange() == FloatRangeArgument.ANY;
         }, Text.translatable("argument.entity.options.x_rotation.description"));
         putOption("y_rotation", (reader) -> {
            reader.setYawRange(FloatRangeArgument.parse(reader.getReader(), true, MathHelper::wrapDegrees));
         }, (reader) -> {
            return reader.getYawRange() == FloatRangeArgument.ANY;
         }, Text.translatable("argument.entity.options.y_rotation.description"));
         putOption("limit", (reader) -> {
            int i = reader.getReader().getCursor();
            int j = reader.getReader().readInt();
            if (j < 1) {
               reader.getReader().setCursor(i);
               throw TOO_SMALL_LEVEL_EXCEPTION.createWithContext(reader.getReader());
            } else {
               reader.setLimit(j);
               reader.setHasLimit(true);
            }
         }, (reader) -> {
            return !reader.isSenderOnly() && !reader.hasLimit();
         }, Text.translatable("argument.entity.options.limit.description"));
         putOption("sort", (reader) -> {
            int i = reader.getReader().getCursor();
            String string = reader.getReader().readUnquotedString();
            reader.setSuggestionProvider((builder, consumer) -> {
               return CommandSource.suggestMatching((Iterable)Arrays.asList("nearest", "furthest", "random", "arbitrary"), builder);
            });
            BiConsumer var10001;
            switch (string) {
               case "nearest":
                  var10001 = EntitySelectorReader.NEAREST;
                  break;
               case "furthest":
                  var10001 = EntitySelectorReader.FURTHEST;
                  break;
               case "random":
                  var10001 = EntitySelectorReader.RANDOM;
                  break;
               case "arbitrary":
                  var10001 = EntitySelector.ARBITRARY;
                  break;
               default:
                  reader.getReader().setCursor(i);
                  throw IRREVERSIBLE_SORT_EXCEPTION.createWithContext(reader.getReader(), string);
            }

            reader.setSorter(var10001);
            reader.setHasSorter(true);
         }, (reader) -> {
            return !reader.isSenderOnly() && !reader.hasSorter();
         }, Text.translatable("argument.entity.options.sort.description"));
         putOption("gamemode", (reader) -> {
            reader.setSuggestionProvider((builder, consumer) -> {
               String string = builder.getRemaining().toLowerCase(Locale.ROOT);
               boolean bl = !reader.excludesGameMode();
               boolean bl2 = true;
               if (!string.isEmpty()) {
                  if (string.charAt(0) == '!') {
                     bl = false;
                     string = string.substring(1);
                  } else {
                     bl2 = false;
                  }
               }

               GameMode[] var6 = GameMode.values();
               int var7 = var6.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  GameMode lv = var6[var8];
                  if (lv.getName().toLowerCase(Locale.ROOT).startsWith(string)) {
                     if (bl2) {
                        builder.suggest("!" + lv.getName());
                     }

                     if (bl) {
                        builder.suggest(lv.getName());
                     }
                  }
               }

               return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            if (reader.excludesGameMode() && !bl) {
               reader.getReader().setCursor(i);
               throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "gamemode");
            } else {
               String string = reader.getReader().readUnquotedString();
               GameMode lv = GameMode.byName(string, (GameMode)null);
               if (lv == null) {
                  reader.getReader().setCursor(i);
                  throw INVALID_MODE_EXCEPTION.createWithContext(reader.getReader(), string);
               } else {
                  reader.setIncludesNonPlayers(false);
                  reader.setPredicate((entity) -> {
                     if (!(entity instanceof ServerPlayerEntity)) {
                        return false;
                     } else {
                        GameMode lvx = ((ServerPlayerEntity)entity).interactionManager.getGameMode();
                        return bl ? lvx != lv : lvx == lv;
                     }
                  });
                  if (bl) {
                     reader.setExcludesGameMode(true);
                  } else {
                     reader.setSelectsGameMode(true);
                  }

               }
            }
         }, (reader) -> {
            return !reader.selectsGameMode();
         }, Text.translatable("argument.entity.options.gamemode.description"));
         putOption("team", (reader) -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            reader.setPredicate((entity) -> {
               if (!(entity instanceof LivingEntity)) {
                  return false;
               } else {
                  AbstractTeam lv = entity.getScoreboardTeam();
                  String string2 = lv == null ? "" : lv.getName();
                  return string2.equals(string) != bl;
               }
            });
            if (bl) {
               reader.setExcludesTeam(true);
            } else {
               reader.setSelectsTeam(true);
            }

         }, (reader) -> {
            return !reader.selectsTeam();
         }, Text.translatable("argument.entity.options.team.description"));
         putOption("type", (reader) -> {
            reader.setSuggestionProvider((builder, consumer) -> {
               CommandSource.suggestIdentifiers((Iterable)Registries.ENTITY_TYPE.getIds(), (SuggestionsBuilder)builder, (String)String.valueOf('!'));
               CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(TagKey::id), builder, "!#");
               if (!reader.excludesEntityType()) {
                  CommandSource.suggestIdentifiers((Iterable)Registries.ENTITY_TYPE.getIds(), builder);
                  CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(TagKey::id), builder, String.valueOf('#'));
               }

               return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            if (reader.excludesEntityType() && !bl) {
               reader.getReader().setCursor(i);
               throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "type");
            } else {
               if (bl) {
                  reader.setExcludesEntityType();
               }

               if (reader.readTagCharacter()) {
                  TagKey lv = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.fromCommandInput(reader.getReader()));
                  reader.setPredicate((entity) -> {
                     return entity.getType().isIn(lv) != bl;
                  });
               } else {
                  Identifier lv2 = Identifier.fromCommandInput(reader.getReader());
                  EntityType lv3 = (EntityType)Registries.ENTITY_TYPE.getOrEmpty(lv2).orElseThrow(() -> {
                     reader.getReader().setCursor(i);
                     return INVALID_TYPE_EXCEPTION.createWithContext(reader.getReader(), lv2.toString());
                  });
                  if (Objects.equals(EntityType.PLAYER, lv3) && !bl) {
                     reader.setIncludesNonPlayers(false);
                  }

                  reader.setPredicate((entity) -> {
                     return Objects.equals(lv3, entity.getType()) != bl;
                  });
                  if (!bl) {
                     reader.setEntityType(lv3);
                  }
               }

            }
         }, (reader) -> {
            return !reader.selectsEntityType();
         }, Text.translatable("argument.entity.options.type.description"));
         putOption("tag", (reader) -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            reader.setPredicate((entity) -> {
               if ("".equals(string)) {
                  return entity.getCommandTags().isEmpty() != bl;
               } else {
                  return entity.getCommandTags().contains(string) != bl;
               }
            });
         }, (reader) -> {
            return true;
         }, Text.translatable("argument.entity.options.tag.description"));
         putOption("nbt", (reader) -> {
            boolean bl = reader.readNegationCharacter();
            NbtCompound lv = (new StringNbtReader(reader.getReader())).parseCompound();
            reader.setPredicate((entity) -> {
               NbtCompound lvx = entity.writeNbt(new NbtCompound());
               if (entity instanceof ServerPlayerEntity) {
                  ItemStack lv2 = ((ServerPlayerEntity)entity).getInventory().getMainHandStack();
                  if (!lv2.isEmpty()) {
                     lvx.put("SelectedItem", lv2.writeNbt(new NbtCompound()));
                  }
               }

               return NbtHelper.matches(lv, lvx, true) != bl;
            });
         }, (reader) -> {
            return true;
         }, Text.translatable("argument.entity.options.nbt.description"));
         putOption("scores", (reader) -> {
            StringReader stringReader = reader.getReader();
            Map map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();

            while(stringReader.canRead() && stringReader.peek() != '}') {
               stringReader.skipWhitespace();
               String string = stringReader.readUnquotedString();
               stringReader.skipWhitespace();
               stringReader.expect('=');
               stringReader.skipWhitespace();
               NumberRange.IntRange lv = NumberRange.IntRange.parse(stringReader);
               map.put(string, lv);
               stringReader.skipWhitespace();
               if (stringReader.canRead() && stringReader.peek() == ',') {
                  stringReader.skip();
               }
            }

            stringReader.expect('}');
            if (!map.isEmpty()) {
               reader.setPredicate((entity) -> {
                  Scoreboard lv = entity.getServer().getScoreboard();
                  String string = entity.getEntityName();
                  Iterator var4 = map.entrySet().iterator();

                  Map.Entry entry;
                  int i;
                  do {
                     if (!var4.hasNext()) {
                        return true;
                     }

                     entry = (Map.Entry)var4.next();
                     ScoreboardObjective lv2 = lv.getNullableObjective((String)entry.getKey());
                     if (lv2 == null) {
                        return false;
                     }

                     if (!lv.playerHasObjective(string, lv2)) {
                        return false;
                     }

                     ScoreboardPlayerScore lv3 = lv.getPlayerScore(string, lv2);
                     i = lv3.getScore();
                  } while(((NumberRange.IntRange)entry.getValue()).test(i));

                  return false;
               });
            }

            reader.setSelectsScores(true);
         }, (reader) -> {
            return !reader.selectsScores();
         }, Text.translatable("argument.entity.options.scores.description"));
         putOption("advancements", (reader) -> {
            StringReader stringReader = reader.getReader();
            Map map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();

            while(stringReader.canRead() && stringReader.peek() != '}') {
               stringReader.skipWhitespace();
               Identifier lv = Identifier.fromCommandInput(stringReader);
               stringReader.skipWhitespace();
               stringReader.expect('=');
               stringReader.skipWhitespace();
               if (stringReader.canRead() && stringReader.peek() == '{') {
                  Map map2 = Maps.newHashMap();
                  stringReader.skipWhitespace();
                  stringReader.expect('{');
                  stringReader.skipWhitespace();

                  while(stringReader.canRead() && stringReader.peek() != '}') {
                     stringReader.skipWhitespace();
                     String string = stringReader.readUnquotedString();
                     stringReader.skipWhitespace();
                     stringReader.expect('=');
                     stringReader.skipWhitespace();
                     boolean bl = stringReader.readBoolean();
                     map2.put(string, (criterionProgress) -> {
                        return criterionProgress.isObtained() == bl;
                     });
                     stringReader.skipWhitespace();
                     if (stringReader.canRead() && stringReader.peek() == ',') {
                        stringReader.skip();
                     }
                  }

                  stringReader.skipWhitespace();
                  stringReader.expect('}');
                  stringReader.skipWhitespace();
                  map.put(lv, (advancementProgress) -> {
                     Iterator var2 = map2.entrySet().iterator();

                     Map.Entry entry;
                     CriterionProgress lv;
                     do {
                        if (!var2.hasNext()) {
                           return true;
                        }

                        entry = (Map.Entry)var2.next();
                        lv = advancementProgress.getCriterionProgress((String)entry.getKey());
                     } while(lv != null && ((Predicate)entry.getValue()).test(lv));

                     return false;
                  });
               } else {
                  boolean bl2 = stringReader.readBoolean();
                  map.put(lv, (advancementProgress) -> {
                     return advancementProgress.isDone() == bl2;
                  });
               }

               stringReader.skipWhitespace();
               if (stringReader.canRead() && stringReader.peek() == ',') {
                  stringReader.skip();
               }
            }

            stringReader.expect('}');
            if (!map.isEmpty()) {
               reader.setPredicate((entity) -> {
                  if (!(entity instanceof ServerPlayerEntity lv)) {
                     return false;
                  } else {
                     PlayerAdvancementTracker lv2 = lv.getAdvancementTracker();
                     ServerAdvancementLoader lv3 = lv.getServer().getAdvancementLoader();
                     Iterator var5 = map.entrySet().iterator();

                     Map.Entry entry;
                     Advancement lv4;
                     do {
                        if (!var5.hasNext()) {
                           return true;
                        }

                        entry = (Map.Entry)var5.next();
                        lv4 = lv3.get((Identifier)entry.getKey());
                     } while(lv4 != null && ((Predicate)entry.getValue()).test(lv2.getProgress(lv4)));

                     return false;
                  }
               });
               reader.setIncludesNonPlayers(false);
            }

            reader.setSelectsAdvancements(true);
         }, (reader) -> {
            return !reader.selectsAdvancements();
         }, Text.translatable("argument.entity.options.advancements.description"));
         putOption("predicate", (reader) -> {
            boolean bl = reader.readNegationCharacter();
            Identifier lv = Identifier.fromCommandInput(reader.getReader());
            reader.setPredicate((entity) -> {
               if (!(entity.world instanceof ServerWorld)) {
                  return false;
               } else {
                  ServerWorld lvx = (ServerWorld)entity.world;
                  LootCondition lv2 = (LootCondition)lvx.getServer().getLootManager().getElement(LootDataType.PREDICATES, lv);
                  if (lv2 == null) {
                     return false;
                  } else {
                     LootContext lv3 = (new LootContext.Builder(lvx)).parameter(LootContextParameters.THIS_ENTITY, entity).parameter(LootContextParameters.ORIGIN, entity.getPos()).build(LootContextTypes.SELECTOR);
                     lv3.markActive(LootContext.predicate(lv2));
                     return bl ^ lv2.test(lv3);
                  }
               }
            });
         }, (reader) -> {
            return true;
         }, Text.translatable("argument.entity.options.predicate.description"));
      }
   }

   public static SelectorHandler getHandler(EntitySelectorReader reader, String option, int restoreCursor) throws CommandSyntaxException {
      SelectorOption lv = (SelectorOption)OPTIONS.get(option);
      if (lv != null) {
         if (lv.condition.test(reader)) {
            return lv.handler;
         } else {
            throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
         }
      } else {
         reader.getReader().setCursor(restoreCursor);
         throw UNKNOWN_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
      }
   }

   public static void suggestOptions(EntitySelectorReader reader, SuggestionsBuilder suggestionBuilder) {
      String string = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
      Iterator var3 = OPTIONS.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         if (((SelectorOption)entry.getValue()).condition.test(reader) && ((String)entry.getKey()).toLowerCase(Locale.ROOT).startsWith(string)) {
            suggestionBuilder.suggest((String)entry.getKey() + "=", ((SelectorOption)entry.getValue()).description);
         }
      }

   }

   static record SelectorOption(SelectorHandler handler, Predicate condition, Text description) {
      final SelectorHandler handler;
      final Predicate condition;
      final Text description;

      SelectorOption(SelectorHandler handler, Predicate condition, Text description) {
         this.handler = handler;
         this.condition = condition;
         this.description = description;
      }

      public SelectorHandler handler() {
         return this.handler;
      }

      public Predicate condition() {
         return this.condition;
      }

      public Text description() {
         return this.description;
      }
   }

   public interface SelectorHandler {
      void handle(EntitySelectorReader reader) throws CommandSyntaxException;
   }
}
