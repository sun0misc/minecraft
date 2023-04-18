package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SummonCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.summon.failed"));
   private static final SimpleCommandExceptionType FAILED_UUID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.summon.failed.uuid"));
   private static final SimpleCommandExceptionType INVALID_POSITION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.summon.invalidPosition"));

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess registryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("summon").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("entity", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), RegistryEntryArgumentType.getSummonableEntityType(context, "entity"), ((ServerCommandSource)context.getSource()).getPosition(), new NbtCompound(), true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), RegistryEntryArgumentType.getSummonableEntityType(context, "entity"), Vec3ArgumentType.getVec3(context, "pos"), new NbtCompound(), true);
      })).then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), RegistryEntryArgumentType.getSummonableEntityType(context, "entity"), Vec3ArgumentType.getVec3(context, "pos"), NbtCompoundArgumentType.getNbtCompound(context, "nbt"), false);
      })))));
   }

   public static Entity summon(ServerCommandSource source, RegistryEntry.Reference entityType, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
      BlockPos lv = BlockPos.ofFloored(pos);
      if (!World.isValid(lv)) {
         throw INVALID_POSITION_EXCEPTION.create();
      } else {
         NbtCompound lv2 = nbt.copy();
         lv2.putString("id", entityType.registryKey().getValue().toString());
         ServerWorld lv3 = source.getWorld();
         Entity lv4 = EntityType.loadEntityWithPassengers(lv2, lv3, (entity) -> {
            entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, entity.getYaw(), entity.getPitch());
            return entity;
         });
         if (lv4 == null) {
            throw FAILED_EXCEPTION.create();
         } else {
            if (initialize && lv4 instanceof MobEntity) {
               ((MobEntity)lv4).initialize(source.getWorld(), source.getWorld().getLocalDifficulty(lv4.getBlockPos()), SpawnReason.COMMAND, (EntityData)null, (NbtCompound)null);
            }

            if (!lv3.spawnNewEntityAndPassengers(lv4)) {
               throw FAILED_UUID_EXCEPTION.create();
            } else {
               return lv4;
            }
         }
      }
   }

   private static int execute(ServerCommandSource source, RegistryEntry.Reference entityType, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
      Entity lv = summon(source, entityType, pos, nbt, initialize);
      source.sendFeedback(Text.translatable("commands.summon.success", lv.getDisplayName()), true);
      return 1;
   }
}
