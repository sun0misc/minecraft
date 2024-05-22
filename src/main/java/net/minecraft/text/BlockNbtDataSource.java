/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.NbtDataSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public record BlockNbtDataSource(String rawPos, @Nullable PosArgument pos) implements NbtDataSource
{
    public static final MapCodec<BlockNbtDataSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("block")).forGetter(BlockNbtDataSource::rawPos)).apply((Applicative<BlockNbtDataSource, ?>)instance, BlockNbtDataSource::new));
    public static final NbtDataSource.Type<BlockNbtDataSource> TYPE = new NbtDataSource.Type<BlockNbtDataSource>(CODEC, "block");

    public BlockNbtDataSource(String rawPath) {
        this(rawPath, BlockNbtDataSource.parsePos(rawPath));
    }

    @Nullable
    private static PosArgument parsePos(String string) {
        try {
            return BlockPosArgumentType.blockPos().parse(new StringReader(string));
        } catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    public Stream<NbtCompound> get(ServerCommandSource source) {
        BlockEntity lv3;
        BlockPos lv2;
        ServerWorld lv;
        if (this.pos != null && (lv = source.getWorld()).canSetBlock(lv2 = this.pos.toAbsoluteBlockPos(source)) && (lv3 = lv.getBlockEntity(lv2)) != null) {
            return Stream.of(lv3.createNbtWithIdentifyingData(source.getRegistryManager()));
        }
        return Stream.empty();
    }

    @Override
    public NbtDataSource.Type<?> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "block=" + this.rawPos;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockNbtDataSource)) return false;
        BlockNbtDataSource lv = (BlockNbtDataSource)o;
        if (!this.rawPos.equals(lv.rawPos)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.rawPos.hashCode();
    }

    @Nullable
    public PosArgument pos() {
        return this.pos;
    }
}

