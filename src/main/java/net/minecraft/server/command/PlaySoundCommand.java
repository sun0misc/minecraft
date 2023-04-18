package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlaySoundCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.playsound.failed"));

   public static void register(CommandDispatcher dispatcher) {
      RequiredArgumentBuilder requiredArgumentBuilder = CommandManager.argument("sound", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.AVAILABLE_SOUNDS);
      SoundCategory[] var2 = SoundCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         SoundCategory lv = var2[var4];
         requiredArgumentBuilder.then(makeArgumentsForCategory(lv));
      }

      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("playsound").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(requiredArgumentBuilder));
   }

   private static LiteralArgumentBuilder makeArgumentsForCategory(SoundCategory category) {
      return (LiteralArgumentBuilder)CommandManager.literal(category.getName()).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, ((ServerCommandSource)context.getSource()).getPosition(), 1.0F, 1.0F, 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), 1.0F, 1.0F, 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), (Float)context.getArgument("volume", Float.class), 1.0F, 0.0F);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), (Float)context.getArgument("volume", Float.class), (Float)context.getArgument("pitch", Float.class), 0.0F);
      })).then(CommandManager.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), (Float)context.getArgument("volume", Float.class), (Float)context.getArgument("pitch", Float.class), (Float)context.getArgument("minVolume", Float.class));
      }))))));
   }

   private static int execute(ServerCommandSource source, Collection targets, Identifier sound, SoundCategory category, Vec3d pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {
      RegistryEntry lv = RegistryEntry.of(SoundEvent.of(sound));
      double d = (double)MathHelper.square(((SoundEvent)lv.value()).getDistanceToTravel(volume));
      int i = 0;
      long l = source.getWorld().getRandom().nextLong();
      Iterator var14 = targets.iterator();

      while(true) {
         ServerPlayerEntity lv2;
         Vec3d lv3;
         float n;
         while(true) {
            if (!var14.hasNext()) {
               if (i == 0) {
                  throw FAILED_EXCEPTION.create();
               }

               if (targets.size() == 1) {
                  source.sendFeedback(Text.translatable("commands.playsound.success.single", sound, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
               } else {
                  source.sendFeedback(Text.translatable("commands.playsound.success.multiple", sound, targets.size()), true);
               }

               return i;
            }

            lv2 = (ServerPlayerEntity)var14.next();
            double e = pos.x - lv2.getX();
            double j = pos.y - lv2.getY();
            double k = pos.z - lv2.getZ();
            double m = e * e + j * j + k * k;
            lv3 = pos;
            n = volume;
            if (!(m > d)) {
               break;
            }

            if (!(minVolume <= 0.0F)) {
               double o = Math.sqrt(m);
               lv3 = new Vec3d(lv2.getX() + e / o * 2.0, lv2.getY() + j / o * 2.0, lv2.getZ() + k / o * 2.0);
               n = minVolume;
               break;
            }
         }

         lv2.networkHandler.sendPacket(new PlaySoundS2CPacket(lv, category, lv3.getX(), lv3.getY(), lv3.getZ(), n, pitch, l));
         ++i;
      }
   }
}
