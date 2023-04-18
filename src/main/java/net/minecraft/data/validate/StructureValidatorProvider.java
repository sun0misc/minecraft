package net.minecraft.data.validate;

import com.mojang.logging.LogUtils;
import net.minecraft.data.SnbtProvider;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureTemplate;
import org.slf4j.Logger;

public class StructureValidatorProvider implements SnbtProvider.Tweaker {
   private static final Logger LOGGER = LogUtils.getLogger();

   public NbtCompound write(String name, NbtCompound nbt) {
      return name.startsWith("data/minecraft/structures/") ? update(name, nbt) : nbt;
   }

   public static NbtCompound update(String name, NbtCompound nbt) {
      StructureTemplate lv = new StructureTemplate();
      int i = NbtHelper.getDataVersion(nbt, 500);
      int j = true;
      if (i < 3437) {
         LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", new Object[]{i, 3437, name});
      }

      NbtCompound lv2 = DataFixTypes.STRUCTURE.update(Schemas.getFixer(), nbt, i);
      lv.readNbt(Registries.BLOCK.getReadOnlyWrapper(), lv2);
      return lv.writeNbt(new NbtCompound());
   }
}
