package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.StorageDataObject;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class DataCommand {
   private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.merge.failed"));
   private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType((path) -> {
      return Text.translatable("commands.data.get.invalid", path);
   });
   private static final DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION = new DynamicCommandExceptionType((path) -> {
      return Text.translatable("commands.data.get.unknown", path);
   });
   private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.get.multiple"));
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_OBJECT_EXCEPTION = new DynamicCommandExceptionType((nbt) -> {
      return Text.translatable("commands.data.modify.expected_object", nbt);
   });
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_VALUE_EXCEPTION = new DynamicCommandExceptionType((nbt) -> {
      return Text.translatable("commands.data.modify.expected_value", nbt);
   });
   public static final List OBJECT_TYPE_FACTORIES;
   public static final List TARGET_OBJECT_TYPES;
   public static final List SOURCE_OBJECT_TYPES;

   public static void register(CommandDispatcher dispatcher) {
      LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("data").requires((source) -> {
         return source.hasPermissionLevel(2);
      });
      Iterator var2 = TARGET_OBJECT_TYPES.iterator();

      while(var2.hasNext()) {
         ObjectType lv = (ObjectType)var2.next();
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(lv.addArgumentsToBuilder(CommandManager.literal("merge"), (builder) -> {
            return builder.then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes((context) -> {
               return executeMerge((ServerCommandSource)context.getSource(), lv.getObject(context), NbtCompoundArgumentType.getNbtCompound(context, "nbt"));
            }));
         }))).then(lv.addArgumentsToBuilder(CommandManager.literal("get"), (builder) -> {
            return builder.executes((context) -> {
               return executeGet((ServerCommandSource)context.getSource(), lv.getObject(context));
            }).then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes((context) -> {
               return executeGet((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"));
            })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((context) -> {
               return executeGet((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"), DoubleArgumentType.getDouble(context, "scale"));
            })));
         }))).then(lv.addArgumentsToBuilder(CommandManager.literal("remove"), (builder) -> {
            return builder.then(CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes((context) -> {
               return executeRemove((ServerCommandSource)context.getSource(), lv.getObject(context), NbtPathArgumentType.getNbtPath(context, "path"));
            }));
         }))).then(addModifyArgument((builder, modifier) -> {
            builder.then(CommandManager.literal("insert").then(CommandManager.argument("index", IntegerArgumentType.integer()).then(modifier.create((context, sourceNbt, path, elements) -> {
               return path.insert(IntegerArgumentType.getInteger(context, "index"), sourceNbt, elements);
            })))).then(CommandManager.literal("prepend").then(modifier.create((context, arg, path, elements) -> {
               return path.insert(0, arg, elements);
            }))).then(CommandManager.literal("append").then(modifier.create((context, arg, path, elements) -> {
               return path.insert(-1, arg, elements);
            }))).then(CommandManager.literal("set").then(modifier.create((context, sourceNbt, path, elements) -> {
               return path.put(sourceNbt, (NbtElement)Iterables.getLast(elements));
            }))).then(CommandManager.literal("merge").then(modifier.create((context, element, path, elements) -> {
               NbtCompound lv = new NbtCompound();
               Iterator var5 = elements.iterator();

               while(var5.hasNext()) {
                  NbtElement lv2 = (NbtElement)var5.next();
                  if (NbtPathArgumentType.NbtPath.isTooDeep(lv2, 0)) {
                     throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
                  }

                  if (!(lv2 instanceof NbtCompound)) {
                     throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(lv2);
                  }

                  NbtCompound lv3 = (NbtCompound)lv2;
                  lv.copyFrom(lv3);
               }

               Collection collection = path.getOrInit(element, NbtCompound::new);
               int i = 0;

               NbtCompound lv5;
               NbtCompound lv6;
               for(Iterator var13 = collection.iterator(); var13.hasNext(); i += lv6.equals(lv5) ? 0 : 1) {
                  NbtElement lv4 = (NbtElement)var13.next();
                  if (!(lv4 instanceof NbtCompound)) {
                     throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(lv4);
                  }

                  lv5 = (NbtCompound)lv4;
                  lv6 = lv5.copy();
                  lv5.copyFrom(lv);
               }

               return i;
            })));
         }));
      }

      dispatcher.register(literalArgumentBuilder);
   }

   private static String asString(NbtElement nbt) throws CommandSyntaxException {
      if (nbt.getNbtType().isImmutable()) {
         return nbt.asString();
      } else {
         throw MODIFY_EXPECTED_VALUE_EXCEPTION.create(nbt);
      }
   }

   private static List mapValues(List list, Function function) throws CommandSyntaxException {
      List list2 = new ArrayList(list.size());
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         NbtElement lv = (NbtElement)var3.next();
         String string = asString(lv);
         list2.add(NbtString.of((String)function.apply(string)));
      }

      return list2;
   }

   private static ArgumentBuilder addModifyArgument(BiConsumer subArgumentAdder) {
      LiteralArgumentBuilder literalArgumentBuilder = CommandManager.literal("modify");
      Iterator var2 = TARGET_OBJECT_TYPES.iterator();

      while(var2.hasNext()) {
         ObjectType lv = (ObjectType)var2.next();
         lv.addArgumentsToBuilder(literalArgumentBuilder, (builder) -> {
            ArgumentBuilder argumentBuilder2 = CommandManager.argument("targetPath", NbtPathArgumentType.nbtPath());
            Iterator var4 = SOURCE_OBJECT_TYPES.iterator();

            while(var4.hasNext()) {
               ObjectType lvx = (ObjectType)var4.next();
               subArgumentAdder.accept(argumentBuilder2, (operation) -> {
                  return lvx.addArgumentsToBuilder(CommandManager.literal("from"), (builder) -> {
                     return builder.executes((context) -> {
                        return executeModify(context, lv, operation, getValues(context, lvx));
                     }).then(CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes((context) -> {
                        return executeModify(context, lv, operation, getValuesByPath(context, lvx));
                     }));
                  });
               });
               subArgumentAdder.accept(argumentBuilder2, (operation) -> {
                  return lvx.addArgumentsToBuilder(CommandManager.literal("string"), (builder) -> {
                     return builder.executes((context) -> {
                        return executeModify(context, lv, operation, mapValues(getValues(context, lvx), (value) -> {
                           return value;
                        }));
                     }).then(((RequiredArgumentBuilder)CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes((context) -> {
                        return executeModify(context, lv, operation, mapValues(getValuesByPath(context, lvx), (value) -> {
                           return value;
                        }));
                     })).then(((RequiredArgumentBuilder)CommandManager.argument("start", IntegerArgumentType.integer(0)).executes((context) -> {
                        return executeModify(context, lv, operation, mapValues(getValuesByPath(context, lvx), (value) -> {
                           return value.substring(IntegerArgumentType.getInteger(context, "start"));
                        }));
                     })).then(CommandManager.argument("end", IntegerArgumentType.integer(0)).executes((context) -> {
                        return executeModify(context, lv, operation, mapValues(getValuesByPath(context, lvx), (value) -> {
                           return value.substring(IntegerArgumentType.getInteger(context, "start"), IntegerArgumentType.getInteger(context, "end"));
                        }));
                     }))));
                  });
               });
            }

            subArgumentAdder.accept(argumentBuilder2, (modifier) -> {
               return CommandManager.literal("value").then(CommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes((context) -> {
                  List list = Collections.singletonList(NbtElementArgumentType.getNbtElement(context, "value"));
                  return executeModify(context, lv, modifier, list);
               }));
            });
            return builder.then(argumentBuilder2);
         });
      }

      return literalArgumentBuilder;
   }

   private static List getValues(CommandContext context, ObjectType objectType) throws CommandSyntaxException {
      DataCommandObject lv = objectType.getObject(context);
      return Collections.singletonList(lv.getNbt());
   }

   private static List getValuesByPath(CommandContext context, ObjectType objectType) throws CommandSyntaxException {
      DataCommandObject lv = objectType.getObject(context);
      NbtPathArgumentType.NbtPath lv2 = NbtPathArgumentType.getNbtPath(context, "sourcePath");
      return lv2.get(lv.getNbt());
   }

   private static int executeModify(CommandContext context, ObjectType objectType, ModifyOperation modifier, List elements) throws CommandSyntaxException {
      DataCommandObject lv = objectType.getObject(context);
      NbtPathArgumentType.NbtPath lv2 = NbtPathArgumentType.getNbtPath(context, "targetPath");
      NbtCompound lv3 = lv.getNbt();
      int i = modifier.modify(context, lv3, lv2, elements);
      if (i == 0) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         lv.setNbt(lv3);
         ((ServerCommandSource)context.getSource()).sendFeedback(lv.feedbackModify(), true);
         return i;
      }
   }

   private static int executeRemove(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      NbtCompound lv = object.getNbt();
      int i = path.remove(lv);
      if (i == 0) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         object.setNbt(lv);
         source.sendFeedback(object.feedbackModify(), true);
         return i;
      }
   }

   private static NbtElement getNbt(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
      Collection collection = path.get(object.getNbt());
      Iterator iterator = collection.iterator();
      NbtElement lv = (NbtElement)iterator.next();
      if (iterator.hasNext()) {
         throw GET_MULTIPLE_EXCEPTION.create();
      } else {
         return lv;
      }
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      NbtElement lv = getNbt(path, object);
      int i;
      if (lv instanceof AbstractNbtNumber) {
         i = MathHelper.floor(((AbstractNbtNumber)lv).doubleValue());
      } else if (lv instanceof AbstractNbtList) {
         i = ((AbstractNbtList)lv).size();
      } else if (lv instanceof NbtCompound) {
         i = ((NbtCompound)lv).getSize();
      } else {
         if (!(lv instanceof NbtString)) {
            throw GET_UNKNOWN_EXCEPTION.create(path.toString());
         }

         i = lv.asString().length();
      }

      source.sendFeedback(object.feedbackQuery(lv), false);
      return i;
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale) throws CommandSyntaxException {
      NbtElement lv = getNbt(path, object);
      if (!(lv instanceof AbstractNbtNumber)) {
         throw GET_INVALID_EXCEPTION.create(path.toString());
      } else {
         int i = MathHelper.floor(((AbstractNbtNumber)lv).doubleValue() * scale);
         source.sendFeedback(object.feedbackGet(path, scale, i), false);
         return i;
      }
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object) throws CommandSyntaxException {
      source.sendFeedback(object.feedbackQuery(object.getNbt()), false);
      return 1;
   }

   private static int executeMerge(ServerCommandSource source, DataCommandObject object, NbtCompound nbt) throws CommandSyntaxException {
      NbtCompound lv = object.getNbt();
      if (NbtPathArgumentType.NbtPath.isTooDeep(nbt, 0)) {
         throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
      } else {
         NbtCompound lv2 = lv.copy().copyFrom(nbt);
         if (lv.equals(lv2)) {
            throw MERGE_FAILED_EXCEPTION.create();
         } else {
            object.setNbt(lv2);
            source.sendFeedback(object.feedbackModify(), true);
            return 1;
         }
      }
   }

   static {
      OBJECT_TYPE_FACTORIES = ImmutableList.of(EntityDataObject.TYPE_FACTORY, BlockDataObject.TYPE_FACTORY, StorageDataObject.TYPE_FACTORY);
      TARGET_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map((factory) -> {
         return (ObjectType)factory.apply("target");
      }).collect(ImmutableList.toImmutableList());
      SOURCE_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map((factory) -> {
         return (ObjectType)factory.apply("source");
      }).collect(ImmutableList.toImmutableList());
   }

   public interface ObjectType {
      DataCommandObject getObject(CommandContext context) throws CommandSyntaxException;

      ArgumentBuilder addArgumentsToBuilder(ArgumentBuilder argument, Function argumentAdder);
   }

   private interface ModifyOperation {
      int modify(CommandContext context, NbtCompound sourceNbt, NbtPathArgumentType.NbtPath path, List elements) throws CommandSyntaxException;
   }

   interface ModifyArgumentCreator {
      ArgumentBuilder create(ModifyOperation modifier);
   }
}
