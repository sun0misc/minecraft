/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.validate;

import com.mojang.logging.LogUtils;
import net.minecraft.data.SnbtProvider;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.structure.StructureTemplate;
import org.slf4j.Logger;

public class StructureValidatorProvider
implements SnbtProvider.Tweaker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String field_52179 = ResourceType.SERVER_DATA.getDirectory() + "/minecraft/structure/";

    @Override
    public NbtCompound write(String name, NbtCompound nbt) {
        if (name.startsWith(field_52179)) {
            return StructureValidatorProvider.update(name, nbt);
        }
        return nbt;
    }

    public static NbtCompound update(String name, NbtCompound nbt) {
        StructureTemplate lv = new StructureTemplate();
        int i = NbtHelper.getDataVersion(nbt, 500);
        int j = 3937;
        if (i < 3937) {
            LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", i, 3937, name);
        }
        NbtCompound lv2 = DataFixTypes.STRUCTURE.update(Schemas.getFixer(), nbt, i);
        lv.readNbt(Registries.BLOCK.getReadOnlyWrapper(), lv2);
        return lv.writeNbt(new NbtCompound());
    }
}

