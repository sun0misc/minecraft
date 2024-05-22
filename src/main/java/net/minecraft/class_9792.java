/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.class_9791;
import net.minecraft.class_9793;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipAppender;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public record class_9792(class_9791<class_9793> song, boolean showInTooltip) implements TooltipAppender
{
    public static final Codec<class_9792> field_52025 = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)class_9791.method_60736(RegistryKeys.JUKEBOX_SONG, class_9793.field_52029).fieldOf("song")).forGetter(class_9792::song), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(class_9792::showInTooltip)).apply((Applicative<class_9792, ?>)instance, class_9792::new));
    public static final PacketCodec<RegistryByteBuf, class_9792> field_52026 = PacketCodec.tuple(class_9791.method_60737(RegistryKeys.JUKEBOX_SONG, class_9793.field_52030), class_9792::song, PacketCodecs.BOOL, class_9792::showInTooltip, class_9792::new);

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        RegistryWrapper.WrapperLookup lv = context.getRegistryLookup();
        if (this.showInTooltip && lv != null) {
            this.song.method_60739(lv).ifPresent(arg -> {
                MutableText lv = ((class_9793)arg.value()).description().copy();
                Texts.setStyleIfAbsent(lv, Style.EMPTY.withColor(Formatting.GRAY));
                tooltip.accept(lv);
            });
        }
    }

    public class_9792 method_60749(boolean bl) {
        return new class_9792(this.song, bl);
    }

    public static ItemActionResult method_60747(World arg, BlockPos arg2, ItemStack arg3, PlayerEntity arg4) {
        class_9792 lv = arg3.get(DataComponentTypes.JUKEBOX_PLAYABLE);
        if (lv == null) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BlockState lv2 = arg.getBlockState(arg2);
        if (!lv2.isOf(Blocks.JUKEBOX) || lv2.get(JukeboxBlock.HAS_RECORD).booleanValue()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!arg.isClient) {
            ItemStack lv3 = arg3.splitUnlessCreative(1, arg4);
            BlockEntity blockEntity = arg.getBlockEntity(arg2);
            if (blockEntity instanceof JukeboxBlockEntity) {
                JukeboxBlockEntity lv4 = (JukeboxBlockEntity)blockEntity;
                lv4.setStack(lv3);
                arg.emitGameEvent(GameEvent.BLOCK_CHANGE, arg2, GameEvent.Emitter.of(arg4, lv2));
            }
            arg4.incrementStat(Stats.PLAY_RECORD);
        }
        return ItemActionResult.success(arg.isClient);
    }
}

