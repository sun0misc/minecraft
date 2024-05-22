/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure.processor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class JigsawReplacementStructureProcessor
extends StructureProcessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<JigsawReplacementStructureProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    public static final JigsawReplacementStructureProcessor INSTANCE = new JigsawReplacementStructureProcessor();

    private JigsawReplacementStructureProcessor() {
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        BlockState lv3;
        BlockState lv = currentBlockInfo.state();
        if (!lv.isOf(Blocks.JIGSAW)) {
            return currentBlockInfo;
        }
        if (currentBlockInfo.nbt() == null) {
            LOGGER.warn("Jigsaw block at {} is missing nbt, will not replace", (Object)pos);
            return currentBlockInfo;
        }
        String string = currentBlockInfo.nbt().getString("final_state");
        try {
            BlockArgumentParser.BlockResult lv2 = BlockArgumentParser.block(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), string, true);
            lv3 = lv2.blockState();
        } catch (CommandSyntaxException commandSyntaxException) {
            LOGGER.error("Failed to parse jigsaw replacement state '{}' at {}: {}", string, pos, commandSyntaxException.getMessage());
            return null;
        }
        if (lv3.isOf(Blocks.STRUCTURE_VOID)) {
            return null;
        }
        return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), lv3, null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}

