package net.minecraft.datafixer.fix;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;

public class BlockEntityIdFix extends DataFix {
   private static final Map RENAMED_BLOCK_ENTITIES = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      map.put("Airportal", "minecraft:end_portal");
      map.put("Banner", "minecraft:banner");
      map.put("Beacon", "minecraft:beacon");
      map.put("Cauldron", "minecraft:brewing_stand");
      map.put("Chest", "minecraft:chest");
      map.put("Comparator", "minecraft:comparator");
      map.put("Control", "minecraft:command_block");
      map.put("DLDetector", "minecraft:daylight_detector");
      map.put("Dropper", "minecraft:dropper");
      map.put("EnchantTable", "minecraft:enchanting_table");
      map.put("EndGateway", "minecraft:end_gateway");
      map.put("EnderChest", "minecraft:ender_chest");
      map.put("FlowerPot", "minecraft:flower_pot");
      map.put("Furnace", "minecraft:furnace");
      map.put("Hopper", "minecraft:hopper");
      map.put("MobSpawner", "minecraft:mob_spawner");
      map.put("Music", "minecraft:noteblock");
      map.put("Piston", "minecraft:piston");
      map.put("RecordPlayer", "minecraft:jukebox");
      map.put("Sign", "minecraft:sign");
      map.put("Skull", "minecraft:skull");
      map.put("Structure", "minecraft:structure_block");
      map.put("Trap", "minecraft:dispenser");
   });

   public BlockEntityIdFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
      Type type2 = this.getOutputSchema().getType(TypeReferences.ITEM_STACK);
      TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY);
      TaggedChoice.TaggedChoiceType taggedChoiceType2 = this.getOutputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY);
      return TypeRewriteRule.seq(this.convertUnchecked("item stack block entity name hook converter", type, type2), this.fixTypeEverywhere("BlockEntityIdFix", taggedChoiceType, taggedChoiceType2, (dynamicOps) -> {
         return (pair) -> {
            return pair.mapFirst((string) -> {
               return (String)RENAMED_BLOCK_ENTITIES.getOrDefault(string, string);
            });
         };
      }));
   }
}
