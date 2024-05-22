/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;

public class DyedCarpetBlock
extends CarpetBlock
implements Equipment {
    public static final MapCodec<DyedCarpetBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DyeColor.CODEC.fieldOf("color")).forGetter(DyedCarpetBlock::getDyeColor), DyedCarpetBlock.createSettingsCodec()).apply((Applicative<DyedCarpetBlock, ?>)instance, DyedCarpetBlock::new));
    private final DyeColor dyeColor;

    public MapCodec<DyedCarpetBlock> getCodec() {
        return CODEC;
    }

    protected DyedCarpetBlock(DyeColor dyeColor, AbstractBlock.Settings settings) {
        super(settings);
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.BODY;
    }

    @Override
    public RegistryEntry<SoundEvent> getEquipSound() {
        return SoundEvents.ENTITY_LLAMA_SWAG;
    }
}

