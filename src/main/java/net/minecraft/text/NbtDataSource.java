package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.server.command.ServerCommandSource;

@FunctionalInterface
public interface NbtDataSource {
   Stream get(ServerCommandSource source) throws CommandSyntaxException;
}
