package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public interface StructureHolder {
   @Nullable
   StructureStart getStructureStart(Structure structure);

   void setStructureStart(Structure structure, StructureStart start);

   LongSet getStructureReferences(Structure structure);

   void addStructureReference(Structure structure, long reference);

   Map getStructureReferences();

   void setStructureReferences(Map structureReferences);
}
