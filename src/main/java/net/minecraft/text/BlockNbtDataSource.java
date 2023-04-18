package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public record BlockNbtDataSource(String rawPos, @Nullable PosArgument pos) implements NbtDataSource {
   public BlockNbtDataSource(String rawPath) {
      this(rawPath, parsePos(rawPath));
   }

   public BlockNbtDataSource(String rawPath, @Nullable PosArgument arg) {
      this.rawPos = rawPath;
      this.pos = arg;
   }

   @Nullable
   private static PosArgument parsePos(String string) {
      try {
         return BlockPosArgumentType.blockPos().parse(new StringReader(string));
      } catch (CommandSyntaxException var2) {
         return null;
      }
   }

   public Stream get(ServerCommandSource source) {
      if (this.pos != null) {
         ServerWorld lv = source.getWorld();
         BlockPos lv2 = this.pos.toAbsoluteBlockPos(source);
         if (lv.canSetBlock(lv2)) {
            BlockEntity lv3 = lv.getBlockEntity(lv2);
            if (lv3 != null) {
               return Stream.of(lv3.createNbtWithIdentifyingData());
            }
         }
      }

      return Stream.empty();
   }

   public String toString() {
      return "block=" + this.rawPos;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         boolean var10000;
         if (o instanceof BlockNbtDataSource) {
            BlockNbtDataSource lv = (BlockNbtDataSource)o;
            if (this.rawPos.equals(lv.rawPos)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   public int hashCode() {
      return this.rawPos.hashCode();
   }

   public String rawPos() {
      return this.rawPos;
   }

   @Nullable
   public PosArgument pos() {
      return this.pos;
   }
}
