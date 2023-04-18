package net.minecraft.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BlockDataObject implements DataCommandObject {
   static final SimpleCommandExceptionType INVALID_BLOCK_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.block.invalid"));
   public static final Function TYPE_FACTORY = (argumentName) -> {
      return new DataCommand.ObjectType() {
         public DataCommandObject getObject(CommandContext context) throws CommandSyntaxException {
            BlockPos lv = BlockPosArgumentType.getLoadedBlockPos(context, argumentName + "Pos");
            BlockEntity lv2 = ((ServerCommandSource)context.getSource()).getWorld().getBlockEntity(lv);
            if (lv2 == null) {
               throw BlockDataObject.INVALID_BLOCK_EXCEPTION.create();
            } else {
               return new BlockDataObject(lv2, lv);
            }
         }

         public ArgumentBuilder addArgumentsToBuilder(ArgumentBuilder argument, Function argumentAdder) {
            return argument.then(CommandManager.literal("block").then((ArgumentBuilder)argumentAdder.apply(CommandManager.argument(argumentName + "Pos", BlockPosArgumentType.blockPos()))));
         }
      };
   };
   private final BlockEntity blockEntity;
   private final BlockPos pos;

   public BlockDataObject(BlockEntity blockEntity, BlockPos pos) {
      this.blockEntity = blockEntity;
      this.pos = pos;
   }

   public void setNbt(NbtCompound nbt) {
      BlockState lv = this.blockEntity.getWorld().getBlockState(this.pos);
      this.blockEntity.readNbt(nbt);
      this.blockEntity.markDirty();
      this.blockEntity.getWorld().updateListeners(this.pos, lv, lv, Block.NOTIFY_ALL);
   }

   public NbtCompound getNbt() {
      return this.blockEntity.createNbtWithIdentifyingData();
   }

   public Text feedbackModify() {
      return Text.translatable("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
   }

   public Text feedbackQuery(NbtElement element) {
      return Text.translatable("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtHelper.toPrettyPrintedText(element));
   }

   public Text feedbackGet(NbtPathArgumentType.NbtPath path, double scale, int result) {
      return Text.translatable("commands.data.block.get", path, this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", scale), result);
   }
}
