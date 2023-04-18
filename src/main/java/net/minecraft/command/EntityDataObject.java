package net.minecraft.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.text.Text;

public class EntityDataObject implements DataCommandObject {
   private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.entity.invalid"));
   public static final Function TYPE_FACTORY = (argumentName) -> {
      return new DataCommand.ObjectType() {
         public DataCommandObject getObject(CommandContext context) throws CommandSyntaxException {
            return new EntityDataObject(EntityArgumentType.getEntity(context, argumentName));
         }

         public ArgumentBuilder addArgumentsToBuilder(ArgumentBuilder argument, Function argumentAdder) {
            return argument.then(CommandManager.literal("entity").then((ArgumentBuilder)argumentAdder.apply(CommandManager.argument(argumentName, EntityArgumentType.entity()))));
         }
      };
   };
   private final Entity entity;

   public EntityDataObject(Entity entity) {
      this.entity = entity;
   }

   public void setNbt(NbtCompound nbt) throws CommandSyntaxException {
      if (this.entity instanceof PlayerEntity) {
         throw INVALID_ENTITY_EXCEPTION.create();
      } else {
         UUID uUID = this.entity.getUuid();
         this.entity.readNbt(nbt);
         this.entity.setUuid(uUID);
      }
   }

   public NbtCompound getNbt() {
      return NbtPredicate.entityToNbt(this.entity);
   }

   public Text feedbackModify() {
      return Text.translatable("commands.data.entity.modified", this.entity.getDisplayName());
   }

   public Text feedbackQuery(NbtElement element) {
      return Text.translatable("commands.data.entity.query", this.entity.getDisplayName(), NbtHelper.toPrettyPrintedText(element));
   }

   public Text feedbackGet(NbtPathArgumentType.NbtPath path, double scale, int result) {
      return Text.translatable("commands.data.entity.get", path, this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", scale), result);
   }
}
