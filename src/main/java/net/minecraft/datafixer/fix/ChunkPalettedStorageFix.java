package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.math.WordPackedArray;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkPalettedStorageFix extends DataFix {
   private static final int field_29871 = 128;
   private static final int field_29872 = 64;
   private static final int field_29873 = 32;
   private static final int field_29874 = 16;
   private static final int field_29875 = 8;
   private static final int field_29876 = 4;
   private static final int field_29877 = 2;
   private static final int field_29878 = 1;
   static final Logger LOGGER = LogUtils.getLogger();
   static final BitSet BLOCKS_NEEDING_SIDE_UPDATE = new BitSet(256);
   static final BitSet BLOCKS_NEEDING_IN_PLACE_UPDATE = new BitSet(256);
   static final Dynamic PUMPKIN = BlockStateFlattening.parseState("{Name:'minecraft:pumpkin'}");
   static final Dynamic PODZOL = BlockStateFlattening.parseState("{Name:'minecraft:podzol',Properties:{snowy:'true'}}");
   static final Dynamic SNOWY_GRASS = BlockStateFlattening.parseState("{Name:'minecraft:grass_block',Properties:{snowy:'true'}}");
   static final Dynamic SNOWY_MYCELIUM = BlockStateFlattening.parseState("{Name:'minecraft:mycelium',Properties:{snowy:'true'}}");
   static final Dynamic SUNFLOWER_UPPER = BlockStateFlattening.parseState("{Name:'minecraft:sunflower',Properties:{half:'upper'}}");
   static final Dynamic LILAC_UPPER = BlockStateFlattening.parseState("{Name:'minecraft:lilac',Properties:{half:'upper'}}");
   static final Dynamic GRASS_UPPER = BlockStateFlattening.parseState("{Name:'minecraft:tall_grass',Properties:{half:'upper'}}");
   static final Dynamic FERN_UPPER = BlockStateFlattening.parseState("{Name:'minecraft:large_fern',Properties:{half:'upper'}}");
   static final Dynamic ROSE_UPPER = BlockStateFlattening.parseState("{Name:'minecraft:rose_bush',Properties:{half:'upper'}}");
   static final Dynamic PEONY_UPPER = BlockStateFlattening.parseState("{Name:'minecraft:peony',Properties:{half:'upper'}}");
   static final Map FLOWER_POT = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      map.put("minecraft:air0", BlockStateFlattening.parseState("{Name:'minecraft:flower_pot'}"));
      map.put("minecraft:red_flower0", BlockStateFlattening.parseState("{Name:'minecraft:potted_poppy'}"));
      map.put("minecraft:red_flower1", BlockStateFlattening.parseState("{Name:'minecraft:potted_blue_orchid'}"));
      map.put("minecraft:red_flower2", BlockStateFlattening.parseState("{Name:'minecraft:potted_allium'}"));
      map.put("minecraft:red_flower3", BlockStateFlattening.parseState("{Name:'minecraft:potted_azure_bluet'}"));
      map.put("minecraft:red_flower4", BlockStateFlattening.parseState("{Name:'minecraft:potted_red_tulip'}"));
      map.put("minecraft:red_flower5", BlockStateFlattening.parseState("{Name:'minecraft:potted_orange_tulip'}"));
      map.put("minecraft:red_flower6", BlockStateFlattening.parseState("{Name:'minecraft:potted_white_tulip'}"));
      map.put("minecraft:red_flower7", BlockStateFlattening.parseState("{Name:'minecraft:potted_pink_tulip'}"));
      map.put("minecraft:red_flower8", BlockStateFlattening.parseState("{Name:'minecraft:potted_oxeye_daisy'}"));
      map.put("minecraft:yellow_flower0", BlockStateFlattening.parseState("{Name:'minecraft:potted_dandelion'}"));
      map.put("minecraft:sapling0", BlockStateFlattening.parseState("{Name:'minecraft:potted_oak_sapling'}"));
      map.put("minecraft:sapling1", BlockStateFlattening.parseState("{Name:'minecraft:potted_spruce_sapling'}"));
      map.put("minecraft:sapling2", BlockStateFlattening.parseState("{Name:'minecraft:potted_birch_sapling'}"));
      map.put("minecraft:sapling3", BlockStateFlattening.parseState("{Name:'minecraft:potted_jungle_sapling'}"));
      map.put("minecraft:sapling4", BlockStateFlattening.parseState("{Name:'minecraft:potted_acacia_sapling'}"));
      map.put("minecraft:sapling5", BlockStateFlattening.parseState("{Name:'minecraft:potted_dark_oak_sapling'}"));
      map.put("minecraft:red_mushroom0", BlockStateFlattening.parseState("{Name:'minecraft:potted_red_mushroom'}"));
      map.put("minecraft:brown_mushroom0", BlockStateFlattening.parseState("{Name:'minecraft:potted_brown_mushroom'}"));
      map.put("minecraft:deadbush0", BlockStateFlattening.parseState("{Name:'minecraft:potted_dead_bush'}"));
      map.put("minecraft:tallgrass2", BlockStateFlattening.parseState("{Name:'minecraft:potted_fern'}"));
      map.put("minecraft:cactus0", BlockStateFlattening.lookupState(2240));
   });
   static final Map SKULL = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      buildSkull(map, 0, "skeleton", "skull");
      buildSkull(map, 1, "wither_skeleton", "skull");
      buildSkull(map, 2, "zombie", "head");
      buildSkull(map, 3, "player", "head");
      buildSkull(map, 4, "creeper", "head");
      buildSkull(map, 5, "dragon", "head");
   });
   static final Map DOOR = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      buildDoor(map, "oak_door", 1024);
      buildDoor(map, "iron_door", 1136);
      buildDoor(map, "spruce_door", 3088);
      buildDoor(map, "birch_door", 3104);
      buildDoor(map, "jungle_door", 3120);
      buildDoor(map, "acacia_door", 3136);
      buildDoor(map, "dark_oak_door", 3152);
   });
   static final Map NOTE_BLOCK = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      for(int i = 0; i < 26; ++i) {
         map.put("true" + i, BlockStateFlattening.parseState("{Name:'minecraft:note_block',Properties:{powered:'true',note:'" + i + "'}}"));
         map.put("false" + i, BlockStateFlattening.parseState("{Name:'minecraft:note_block',Properties:{powered:'false',note:'" + i + "'}}"));
      }

   });
   private static final Int2ObjectMap COLORS = (Int2ObjectMap)DataFixUtils.make(new Int2ObjectOpenHashMap(), (map) -> {
      map.put(0, "white");
      map.put(1, "orange");
      map.put(2, "magenta");
      map.put(3, "light_blue");
      map.put(4, "yellow");
      map.put(5, "lime");
      map.put(6, "pink");
      map.put(7, "gray");
      map.put(8, "light_gray");
      map.put(9, "cyan");
      map.put(10, "purple");
      map.put(11, "blue");
      map.put(12, "brown");
      map.put(13, "green");
      map.put(14, "red");
      map.put(15, "black");
   });
   static final Map BED = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      ObjectIterator var1 = COLORS.int2ObjectEntrySet().iterator();

      while(var1.hasNext()) {
         Int2ObjectMap.Entry entry = (Int2ObjectMap.Entry)var1.next();
         if (!Objects.equals(entry.getValue(), "red")) {
            buildBed(map, entry.getIntKey(), (String)entry.getValue());
         }
      }

   });
   static final Map BANNER = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      ObjectIterator var1 = COLORS.int2ObjectEntrySet().iterator();

      while(var1.hasNext()) {
         Int2ObjectMap.Entry entry = (Int2ObjectMap.Entry)var1.next();
         if (!Objects.equals(entry.getValue(), "white")) {
            buildBanner(map, 15 - entry.getIntKey(), (String)entry.getValue());
         }
      }

   });
   static final Dynamic AIR;
   private static final int field_29870 = 4096;

   public ChunkPalettedStorageFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   private static void buildSkull(Map out, int i, String mob, String block) {
      out.put("" + i + "north", BlockStateFlattening.parseState("{Name:'minecraft:" + mob + "_wall_" + block + "',Properties:{facing:'north'}}"));
      out.put("" + i + "east", BlockStateFlattening.parseState("{Name:'minecraft:" + mob + "_wall_" + block + "',Properties:{facing:'east'}}"));
      out.put("" + i + "south", BlockStateFlattening.parseState("{Name:'minecraft:" + mob + "_wall_" + block + "',Properties:{facing:'south'}}"));
      out.put("" + i + "west", BlockStateFlattening.parseState("{Name:'minecraft:" + mob + "_wall_" + block + "',Properties:{facing:'west'}}"));

      for(int j = 0; j < 16; ++j) {
         out.put("" + i + j, BlockStateFlattening.parseState("{Name:'minecraft:" + mob + "_" + block + "',Properties:{rotation:'" + j + "'}}"));
      }

   }

   private static void buildDoor(Map out, String name, int i) {
      out.put("minecraft:" + name + "eastlowerleftfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "eastlowerleftfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "eastlowerlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "eastlowerlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "eastlowerrightfalsefalse", BlockStateFlattening.lookupState(i));
      out.put("minecraft:" + name + "eastlowerrightfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "eastlowerrighttruefalse", BlockStateFlattening.lookupState(i + 4));
      out.put("minecraft:" + name + "eastlowerrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "eastupperleftfalsefalse", BlockStateFlattening.lookupState(i + 8));
      out.put("minecraft:" + name + "eastupperleftfalsetrue", BlockStateFlattening.lookupState(i + 10));
      out.put("minecraft:" + name + "eastupperlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "eastupperlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "eastupperrightfalsefalse", BlockStateFlattening.lookupState(i + 9));
      out.put("minecraft:" + name + "eastupperrightfalsetrue", BlockStateFlattening.lookupState(i + 11));
      out.put("minecraft:" + name + "eastupperrighttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "eastupperrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "northlowerleftfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "northlowerleftfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "northlowerlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "northlowerlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "northlowerrightfalsefalse", BlockStateFlattening.lookupState(i + 3));
      out.put("minecraft:" + name + "northlowerrightfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "northlowerrighttruefalse", BlockStateFlattening.lookupState(i + 7));
      out.put("minecraft:" + name + "northlowerrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "northupperleftfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "northupperleftfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "northupperlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "northupperlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "northupperrightfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "northupperrightfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "northupperrighttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "northupperrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "southlowerleftfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "southlowerleftfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "southlowerlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "southlowerlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "southlowerrightfalsefalse", BlockStateFlattening.lookupState(i + 1));
      out.put("minecraft:" + name + "southlowerrightfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "southlowerrighttruefalse", BlockStateFlattening.lookupState(i + 5));
      out.put("minecraft:" + name + "southlowerrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "southupperleftfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "southupperleftfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "southupperlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "southupperlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "southupperrightfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "southupperrightfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "southupperrighttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "southupperrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "westlowerleftfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "westlowerleftfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "westlowerlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "westlowerlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "westlowerrightfalsefalse", BlockStateFlattening.lookupState(i + 2));
      out.put("minecraft:" + name + "westlowerrightfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "westlowerrighttruefalse", BlockStateFlattening.lookupState(i + 6));
      out.put("minecraft:" + name + "westlowerrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "westupperleftfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "westupperleftfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "westupperlefttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "westupperlefttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
      out.put("minecraft:" + name + "westupperrightfalsefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
      out.put("minecraft:" + name + "westupperrightfalsetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
      out.put("minecraft:" + name + "westupperrighttruefalse", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
      out.put("minecraft:" + name + "westupperrighttruetrue", BlockStateFlattening.parseState("{Name:'minecraft:" + name + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
   }

   private static void buildBed(Map out, int i, String color) {
      out.put("southfalsefoot" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'south',occupied:'false',part:'foot'}}"));
      out.put("westfalsefoot" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'west',occupied:'false',part:'foot'}}"));
      out.put("northfalsefoot" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'north',occupied:'false',part:'foot'}}"));
      out.put("eastfalsefoot" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'east',occupied:'false',part:'foot'}}"));
      out.put("southfalsehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'south',occupied:'false',part:'head'}}"));
      out.put("westfalsehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'west',occupied:'false',part:'head'}}"));
      out.put("northfalsehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'north',occupied:'false',part:'head'}}"));
      out.put("eastfalsehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'east',occupied:'false',part:'head'}}"));
      out.put("southtruehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'south',occupied:'true',part:'head'}}"));
      out.put("westtruehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'west',occupied:'true',part:'head'}}"));
      out.put("northtruehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'north',occupied:'true',part:'head'}}"));
      out.put("easttruehead" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_bed',Properties:{facing:'east',occupied:'true',part:'head'}}"));
   }

   private static void buildBanner(Map out, int i, String color) {
      for(int j = 0; j < 16; ++j) {
         out.put("" + j + "_" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_banner',Properties:{rotation:'" + j + "'}}"));
      }

      out.put("north_" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_wall_banner',Properties:{facing:'north'}}"));
      out.put("south_" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_wall_banner',Properties:{facing:'south'}}"));
      out.put("west_" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_wall_banner',Properties:{facing:'west'}}"));
      out.put("east_" + i, BlockStateFlattening.parseState("{Name:'minecraft:" + color + "_wall_banner',Properties:{facing:'east'}}"));
   }

   public static String getName(Dynamic dynamic) {
      return dynamic.get("Name").asString("");
   }

   public static String getProperty(Dynamic dynamic, String string) {
      return dynamic.get("Properties").get(string).asString("");
   }

   public static int addTo(Int2ObjectBiMap arg, Dynamic dynamic) {
      int i = arg.getRawId(dynamic);
      if (i == -1) {
         i = arg.add(dynamic);
      }

      return i;
   }

   private Dynamic fixChunk(Dynamic dynamic) {
      Optional optional = dynamic.get("Level").result();
      return optional.isPresent() && ((Dynamic)optional.get()).get("Sections").asStreamOpt().result().isPresent() ? dynamic.set("Level", (new Level((Dynamic)optional.get())).transform()) : dynamic;
   }

   public TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
      Type type2 = this.getOutputSchema().getType(TypeReferences.CHUNK);
      return this.writeFixAndRead("ChunkPalettedStorageFix", type, type2, this::fixChunk);
   }

   public static int getSideToUpgradeFlag(boolean west, boolean east, boolean north, boolean south) {
      int i = 0;
      if (north) {
         if (east) {
            i |= 2;
         } else if (west) {
            i |= 128;
         } else {
            i |= 1;
         }
      } else if (south) {
         if (west) {
            i |= 32;
         } else if (east) {
            i |= 8;
         } else {
            i |= 16;
         }
      } else if (east) {
         i |= 4;
      } else if (west) {
         i |= 64;
      }

      return i;
   }

   static {
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(2);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(3);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(110);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(140);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(144);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(25);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(86);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(26);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(176);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(177);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(175);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(64);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(71);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(193);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(194);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(195);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(196);
      BLOCKS_NEEDING_IN_PLACE_UPDATE.set(197);
      BLOCKS_NEEDING_SIDE_UPDATE.set(54);
      BLOCKS_NEEDING_SIDE_UPDATE.set(146);
      BLOCKS_NEEDING_SIDE_UPDATE.set(25);
      BLOCKS_NEEDING_SIDE_UPDATE.set(26);
      BLOCKS_NEEDING_SIDE_UPDATE.set(51);
      BLOCKS_NEEDING_SIDE_UPDATE.set(53);
      BLOCKS_NEEDING_SIDE_UPDATE.set(67);
      BLOCKS_NEEDING_SIDE_UPDATE.set(108);
      BLOCKS_NEEDING_SIDE_UPDATE.set(109);
      BLOCKS_NEEDING_SIDE_UPDATE.set(114);
      BLOCKS_NEEDING_SIDE_UPDATE.set(128);
      BLOCKS_NEEDING_SIDE_UPDATE.set(134);
      BLOCKS_NEEDING_SIDE_UPDATE.set(135);
      BLOCKS_NEEDING_SIDE_UPDATE.set(136);
      BLOCKS_NEEDING_SIDE_UPDATE.set(156);
      BLOCKS_NEEDING_SIDE_UPDATE.set(163);
      BLOCKS_NEEDING_SIDE_UPDATE.set(164);
      BLOCKS_NEEDING_SIDE_UPDATE.set(180);
      BLOCKS_NEEDING_SIDE_UPDATE.set(203);
      BLOCKS_NEEDING_SIDE_UPDATE.set(55);
      BLOCKS_NEEDING_SIDE_UPDATE.set(85);
      BLOCKS_NEEDING_SIDE_UPDATE.set(113);
      BLOCKS_NEEDING_SIDE_UPDATE.set(188);
      BLOCKS_NEEDING_SIDE_UPDATE.set(189);
      BLOCKS_NEEDING_SIDE_UPDATE.set(190);
      BLOCKS_NEEDING_SIDE_UPDATE.set(191);
      BLOCKS_NEEDING_SIDE_UPDATE.set(192);
      BLOCKS_NEEDING_SIDE_UPDATE.set(93);
      BLOCKS_NEEDING_SIDE_UPDATE.set(94);
      BLOCKS_NEEDING_SIDE_UPDATE.set(101);
      BLOCKS_NEEDING_SIDE_UPDATE.set(102);
      BLOCKS_NEEDING_SIDE_UPDATE.set(160);
      BLOCKS_NEEDING_SIDE_UPDATE.set(106);
      BLOCKS_NEEDING_SIDE_UPDATE.set(107);
      BLOCKS_NEEDING_SIDE_UPDATE.set(183);
      BLOCKS_NEEDING_SIDE_UPDATE.set(184);
      BLOCKS_NEEDING_SIDE_UPDATE.set(185);
      BLOCKS_NEEDING_SIDE_UPDATE.set(186);
      BLOCKS_NEEDING_SIDE_UPDATE.set(187);
      BLOCKS_NEEDING_SIDE_UPDATE.set(132);
      BLOCKS_NEEDING_SIDE_UPDATE.set(139);
      BLOCKS_NEEDING_SIDE_UPDATE.set(199);
      AIR = BlockStateFlattening.lookupState(0);
   }

   private static final class Level {
      private int sidesToUpgrade;
      private final Section[] sections = new Section[16];
      private final Dynamic level;
      private final int x;
      private final int z;
      private final Int2ObjectMap blockEntities = new Int2ObjectLinkedOpenHashMap(16);

      public Level(Dynamic dynamic) {
         this.level = dynamic;
         this.x = dynamic.get("xPos").asInt(0) << 4;
         this.z = dynamic.get("zPos").asInt(0) << 4;
         dynamic.get("TileEntities").asStreamOpt().result().ifPresent((stream) -> {
            stream.forEach((dynamic) -> {
               int i = dynamic.get("x").asInt(0) - this.x & 15;
               int j = dynamic.get("y").asInt(0);
               int k = dynamic.get("z").asInt(0) - this.z & 15;
               int l = j << 8 | k << 4 | i;
               if (this.blockEntities.put(l, dynamic) != null) {
                  ChunkPalettedStorageFix.LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", new Object[]{this.x, this.z, i, j, k});
               }

            });
         });
         boolean bl = dynamic.get("convertedFromAlphaFormat").asBoolean(false);
         dynamic.get("Sections").asStreamOpt().result().ifPresent((stream) -> {
            stream.forEach((dynamic) -> {
               Section lv = new Section(dynamic);
               this.sidesToUpgrade = lv.visit(this.sidesToUpgrade);
               this.sections[lv.y] = lv;
            });
         });
         Section[] var3 = this.sections;
         int var4 = var3.length;

         label261:
         for(int var5 = 0; var5 < var4; ++var5) {
            Section lv = var3[var5];
            if (lv != null) {
               ObjectIterator var7 = lv.inPlaceUpdates.entrySet().iterator();

               while(true) {
                  label251:
                  while(true) {
                     if (!var7.hasNext()) {
                        continue label261;
                     }

                     Map.Entry entry = (Map.Entry)var7.next();
                     int i = lv.y << 12;
                     IntListIterator var10;
                     int j;
                     Dynamic dynamic2;
                     Dynamic dynamic3;
                     int k;
                     String string2;
                     String var10000;
                     String string;
                     String string3;
                     switch ((Integer)entry.getKey()) {
                        case 2:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              do {
                                 do {
                                    if (!var10.hasNext()) {
                                       continue label251;
                                    }

                                    j = (Integer)var10.next();
                                    j |= i;
                                    dynamic2 = this.getBlock(j);
                                 } while(!"minecraft:grass_block".equals(ChunkPalettedStorageFix.getName(dynamic2)));

                                 string = ChunkPalettedStorageFix.getName(this.getBlock(adjacentTo(j, ChunkPalettedStorageFix.Facing.UP)));
                              } while(!"minecraft:snow".equals(string) && !"minecraft:snow_layer".equals(string));

                              this.setBlock(j, ChunkPalettedStorageFix.SNOWY_GRASS);
                           }
                        case 3:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              do {
                                 do {
                                    if (!var10.hasNext()) {
                                       continue label251;
                                    }

                                    j = (Integer)var10.next();
                                    j |= i;
                                    dynamic2 = this.getBlock(j);
                                 } while(!"minecraft:podzol".equals(ChunkPalettedStorageFix.getName(dynamic2)));

                                 string = ChunkPalettedStorageFix.getName(this.getBlock(adjacentTo(j, ChunkPalettedStorageFix.Facing.UP)));
                              } while(!"minecraft:snow".equals(string) && !"minecraft:snow_layer".equals(string));

                              this.setBlock(j, ChunkPalettedStorageFix.PODZOL);
                           }
                        case 25:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              if (!var10.hasNext()) {
                                 continue label251;
                              }

                              j = (Integer)var10.next();
                              j |= i;
                              dynamic2 = this.removeBlockEntity(j);
                              if (dynamic2 != null) {
                                 var10000 = Boolean.toString(dynamic2.get("powered").asBoolean(false));
                                 string = var10000 + (byte)Math.min(Math.max(dynamic2.get("note").asInt(0), 0), 24);
                                 this.setBlock(j, (Dynamic)ChunkPalettedStorageFix.NOTE_BLOCK.getOrDefault(string, (Dynamic)ChunkPalettedStorageFix.NOTE_BLOCK.get("false0")));
                              }
                           }
                        case 26:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              if (!var10.hasNext()) {
                                 continue label251;
                              }

                              j = (Integer)var10.next();
                              j |= i;
                              dynamic2 = this.getBlockEntity(j);
                              dynamic3 = this.getBlock(j);
                              if (dynamic2 != null) {
                                 k = dynamic2.get("color").asInt(0);
                                 if (k != 14 && k >= 0 && k < 16) {
                                    var10000 = ChunkPalettedStorageFix.getProperty(dynamic3, "facing");
                                    string2 = var10000 + ChunkPalettedStorageFix.getProperty(dynamic3, "occupied") + ChunkPalettedStorageFix.getProperty(dynamic3, "part") + k;
                                    if (ChunkPalettedStorageFix.BED.containsKey(string2)) {
                                       this.setBlock(j, (Dynamic)ChunkPalettedStorageFix.BED.get(string2));
                                    }
                                 }
                              }
                           }
                        case 64:
                        case 71:
                        case 193:
                        case 194:
                        case 195:
                        case 196:
                        case 197:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              if (!var10.hasNext()) {
                                 continue label251;
                              }

                              j = (Integer)var10.next();
                              j |= i;
                              dynamic2 = this.getBlock(j);
                              if (ChunkPalettedStorageFix.getName(dynamic2).endsWith("_door")) {
                                 dynamic3 = this.getBlock(j);
                                 if ("lower".equals(ChunkPalettedStorageFix.getProperty(dynamic3, "half"))) {
                                    k = adjacentTo(j, ChunkPalettedStorageFix.Facing.UP);
                                    Dynamic dynamic4 = this.getBlock(k);
                                    String string4 = ChunkPalettedStorageFix.getName(dynamic3);
                                    if (string4.equals(ChunkPalettedStorageFix.getName(dynamic4))) {
                                       String string5 = ChunkPalettedStorageFix.getProperty(dynamic3, "facing");
                                       String string6 = ChunkPalettedStorageFix.getProperty(dynamic3, "open");
                                       String string7 = bl ? "left" : ChunkPalettedStorageFix.getProperty(dynamic4, "hinge");
                                       String string8 = bl ? "false" : ChunkPalettedStorageFix.getProperty(dynamic4, "powered");
                                       this.setBlock(j, (Dynamic)ChunkPalettedStorageFix.DOOR.get(string4 + string5 + "lower" + string7 + string6 + string8));
                                       this.setBlock(k, (Dynamic)ChunkPalettedStorageFix.DOOR.get(string4 + string5 + "upper" + string7 + string6 + string8));
                                    }
                                 }
                              }
                           }
                        case 86:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              do {
                                 do {
                                    if (!var10.hasNext()) {
                                       continue label251;
                                    }

                                    j = (Integer)var10.next();
                                    j |= i;
                                    dynamic2 = this.getBlock(j);
                                 } while(!"minecraft:carved_pumpkin".equals(ChunkPalettedStorageFix.getName(dynamic2)));

                                 string = ChunkPalettedStorageFix.getName(this.getBlock(adjacentTo(j, ChunkPalettedStorageFix.Facing.DOWN)));
                              } while(!"minecraft:grass_block".equals(string) && !"minecraft:dirt".equals(string));

                              this.setBlock(j, ChunkPalettedStorageFix.PUMPKIN);
                           }
                        case 110:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              do {
                                 do {
                                    if (!var10.hasNext()) {
                                       continue label251;
                                    }

                                    j = (Integer)var10.next();
                                    j |= i;
                                    dynamic2 = this.getBlock(j);
                                 } while(!"minecraft:mycelium".equals(ChunkPalettedStorageFix.getName(dynamic2)));

                                 string = ChunkPalettedStorageFix.getName(this.getBlock(adjacentTo(j, ChunkPalettedStorageFix.Facing.UP)));
                              } while(!"minecraft:snow".equals(string) && !"minecraft:snow_layer".equals(string));

                              this.setBlock(j, ChunkPalettedStorageFix.SNOWY_MYCELIUM);
                           }
                        case 140:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              if (!var10.hasNext()) {
                                 continue label251;
                              }

                              j = (Integer)var10.next();
                              j |= i;
                              dynamic2 = this.removeBlockEntity(j);
                              if (dynamic2 != null) {
                                 var10000 = dynamic2.get("Item").asString("");
                                 string = var10000 + dynamic2.get("Data").asInt(0);
                                 this.setBlock(j, (Dynamic)ChunkPalettedStorageFix.FLOWER_POT.getOrDefault(string, (Dynamic)ChunkPalettedStorageFix.FLOWER_POT.get("minecraft:air0")));
                              }
                           }
                        case 144:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              do {
                                 if (!var10.hasNext()) {
                                    continue label251;
                                 }

                                 j = (Integer)var10.next();
                                 j |= i;
                                 dynamic2 = this.getBlockEntity(j);
                              } while(dynamic2 == null);

                              string = String.valueOf(dynamic2.get("SkullType").asInt(0));
                              string3 = ChunkPalettedStorageFix.getProperty(this.getBlock(j), "facing");
                              if (!"up".equals(string3) && !"down".equals(string3)) {
                                 string2 = string + string3;
                              } else {
                                 string2 = string + String.valueOf(dynamic2.get("Rot").asInt(0));
                              }

                              dynamic2.remove("SkullType");
                              dynamic2.remove("facing");
                              dynamic2.remove("Rot");
                              this.setBlock(j, (Dynamic)ChunkPalettedStorageFix.SKULL.getOrDefault(string2, (Dynamic)ChunkPalettedStorageFix.SKULL.get("0north")));
                           }
                        case 175:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(true) {
                              if (!var10.hasNext()) {
                                 continue label251;
                              }

                              j = (Integer)var10.next();
                              j |= i;
                              dynamic2 = this.getBlock(j);
                              if ("upper".equals(ChunkPalettedStorageFix.getProperty(dynamic2, "half"))) {
                                 dynamic3 = this.getBlock(adjacentTo(j, ChunkPalettedStorageFix.Facing.DOWN));
                                 string3 = ChunkPalettedStorageFix.getName(dynamic3);
                                 if ("minecraft:sunflower".equals(string3)) {
                                    this.setBlock(j, ChunkPalettedStorageFix.SUNFLOWER_UPPER);
                                 } else if ("minecraft:lilac".equals(string3)) {
                                    this.setBlock(j, ChunkPalettedStorageFix.LILAC_UPPER);
                                 } else if ("minecraft:tall_grass".equals(string3)) {
                                    this.setBlock(j, ChunkPalettedStorageFix.GRASS_UPPER);
                                 } else if ("minecraft:large_fern".equals(string3)) {
                                    this.setBlock(j, ChunkPalettedStorageFix.FERN_UPPER);
                                 } else if ("minecraft:rose_bush".equals(string3)) {
                                    this.setBlock(j, ChunkPalettedStorageFix.ROSE_UPPER);
                                 } else if ("minecraft:peony".equals(string3)) {
                                    this.setBlock(j, ChunkPalettedStorageFix.PEONY_UPPER);
                                 }
                              }
                           }
                        case 176:
                        case 177:
                           var10 = ((IntList)entry.getValue()).iterator();

                           while(var10.hasNext()) {
                              j = (Integer)var10.next();
                              j |= i;
                              dynamic2 = this.getBlockEntity(j);
                              dynamic3 = this.getBlock(j);
                              if (dynamic2 != null) {
                                 k = dynamic2.get("Base").asInt(0);
                                 if (k != 15 && k >= 0 && k < 16) {
                                    var10000 = ChunkPalettedStorageFix.getProperty(dynamic3, (Integer)entry.getKey() == 176 ? "rotation" : "facing");
                                    string2 = var10000 + "_" + k;
                                    if (ChunkPalettedStorageFix.BANNER.containsKey(string2)) {
                                       this.setBlock(j, (Dynamic)ChunkPalettedStorageFix.BANNER.get(string2));
                                    }
                                 }
                              }
                           }
                     }
                  }
               }
            }
         }

      }

      @Nullable
      private Dynamic getBlockEntity(int i) {
         return (Dynamic)this.blockEntities.get(i);
      }

      @Nullable
      private Dynamic removeBlockEntity(int i) {
         return (Dynamic)this.blockEntities.remove(i);
      }

      public static int adjacentTo(int i, Facing direction) {
         switch (direction.getAxis()) {
            case X:
               int j = (i & 15) + direction.getDirection().getOffset();
               return j >= 0 && j <= 15 ? i & -16 | j : -1;
            case Y:
               int k = (i >> 8) + direction.getDirection().getOffset();
               return k >= 0 && k <= 255 ? i & 255 | k << 8 : -1;
            case Z:
               int l = (i >> 4 & 15) + direction.getDirection().getOffset();
               return l >= 0 && l <= 15 ? i & -241 | l << 4 : -1;
            default:
               return -1;
         }
      }

      private void setBlock(int i, Dynamic dynamic) {
         if (i >= 0 && i <= 65535) {
            Section lv = this.getSection(i);
            if (lv != null) {
               lv.setBlock(i & 4095, dynamic);
            }
         }
      }

      @Nullable
      private Section getSection(int i) {
         int j = i >> 12;
         return j < this.sections.length ? this.sections[j] : null;
      }

      public Dynamic getBlock(int i) {
         if (i >= 0 && i <= 65535) {
            Section lv = this.getSection(i);
            return lv == null ? ChunkPalettedStorageFix.AIR : lv.getBlock(i & 4095);
         } else {
            return ChunkPalettedStorageFix.AIR;
         }
      }

      public Dynamic transform() {
         Dynamic dynamic = this.level;
         if (this.blockEntities.isEmpty()) {
            dynamic = dynamic.remove("TileEntities");
         } else {
            dynamic = dynamic.set("TileEntities", dynamic.createList(this.blockEntities.values().stream()));
         }

         Dynamic dynamic2 = dynamic.emptyMap();
         List list = Lists.newArrayList();
         Section[] var4 = this.sections;
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Section lv = var4[var6];
            if (lv != null) {
               list.add(lv.transform());
               dynamic2 = dynamic2.set(String.valueOf(lv.y), dynamic2.createIntList(Arrays.stream(lv.innerPositions.toIntArray())));
            }
         }

         Dynamic dynamic3 = dynamic.emptyMap();
         dynamic3 = dynamic3.set("Sides", dynamic3.createByte((byte)this.sidesToUpgrade));
         dynamic3 = dynamic3.set("Indices", dynamic2);
         return dynamic.set("UpgradeData", dynamic3).set("Sections", dynamic3.createList(list.stream()));
      }
   }

   public static enum Facing {
      DOWN(ChunkPalettedStorageFix.Facing.Direction.NEGATIVE, ChunkPalettedStorageFix.Facing.Axis.Y),
      UP(ChunkPalettedStorageFix.Facing.Direction.POSITIVE, ChunkPalettedStorageFix.Facing.Axis.Y),
      NORTH(ChunkPalettedStorageFix.Facing.Direction.NEGATIVE, ChunkPalettedStorageFix.Facing.Axis.Z),
      SOUTH(ChunkPalettedStorageFix.Facing.Direction.POSITIVE, ChunkPalettedStorageFix.Facing.Axis.Z),
      WEST(ChunkPalettedStorageFix.Facing.Direction.NEGATIVE, ChunkPalettedStorageFix.Facing.Axis.X),
      EAST(ChunkPalettedStorageFix.Facing.Direction.POSITIVE, ChunkPalettedStorageFix.Facing.Axis.X);

      private final Axis axis;
      private final Direction direction;

      private Facing(Direction direction, Axis arg2) {
         this.axis = arg2;
         this.direction = direction;
      }

      public Direction getDirection() {
         return this.direction;
      }

      public Axis getAxis() {
         return this.axis;
      }

      // $FF: synthetic method
      private static Facing[] method_36590() {
         return new Facing[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
      }

      public static enum Axis {
         X,
         Y,
         Z;

         // $FF: synthetic method
         private static Axis[] method_36591() {
            return new Axis[]{X, Y, Z};
         }
      }

      public static enum Direction {
         POSITIVE(1),
         NEGATIVE(-1);

         private final int offset;

         private Direction(int j) {
            this.offset = j;
         }

         public int getOffset() {
            return this.offset;
         }

         // $FF: synthetic method
         private static Direction[] method_36592() {
            return new Direction[]{POSITIVE, NEGATIVE};
         }
      }
   }

   private static class ChunkNibbleArray {
      private static final int field_29879 = 2048;
      private static final int field_29880 = 4;
      private final byte[] contents;

      public ChunkNibbleArray() {
         this.contents = new byte[2048];
      }

      public ChunkNibbleArray(byte[] bs) {
         this.contents = bs;
         if (bs.length != 2048) {
            throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + bs.length);
         }
      }

      public int get(int x, int y, int k) {
         int l = this.getRawIndex(y << 8 | k << 4 | x);
         return this.usesLowNibble(y << 8 | k << 4 | x) ? this.contents[l] & 15 : this.contents[l] >> 4 & 15;
      }

      private boolean usesLowNibble(int index) {
         return (index & 1) == 0;
      }

      private int getRawIndex(int index) {
         return index >> 1;
      }
   }

   private static class Section {
      private final Int2ObjectBiMap paletteMap = Int2ObjectBiMap.create(32);
      private final List paletteData = Lists.newArrayList();
      private final Dynamic section;
      private final boolean hasBlocks;
      final Int2ObjectMap inPlaceUpdates = new Int2ObjectLinkedOpenHashMap();
      final IntList innerPositions = new IntArrayList();
      public final int y;
      private final Set seenStates = Sets.newIdentityHashSet();
      private final int[] states = new int[4096];

      public Section(Dynamic dynamic) {
         this.section = dynamic;
         this.y = dynamic.get("Y").asInt(0);
         this.hasBlocks = dynamic.get("Blocks").result().isPresent();
      }

      public Dynamic getBlock(int index) {
         if (index >= 0 && index <= 4095) {
            Dynamic dynamic = (Dynamic)this.paletteMap.get(this.states[index]);
            return dynamic == null ? ChunkPalettedStorageFix.AIR : dynamic;
         } else {
            return ChunkPalettedStorageFix.AIR;
         }
      }

      public void setBlock(int pos, Dynamic dynamic) {
         if (this.seenStates.add(dynamic)) {
            this.paletteData.add("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName(dynamic)) ? ChunkPalettedStorageFix.AIR : dynamic);
         }

         this.states[pos] = ChunkPalettedStorageFix.addTo(this.paletteMap, dynamic);
      }

      public int visit(int sidesToUpgrade) {
         if (!this.hasBlocks) {
            return sidesToUpgrade;
         } else {
            ByteBuffer byteBuffer = (ByteBuffer)this.section.get("Blocks").asByteBufferOpt().result().get();
            ChunkNibbleArray lv = (ChunkNibbleArray)this.section.get("Data").asByteBufferOpt().map((byteBufferx) -> {
               return new ChunkNibbleArray(DataFixUtils.toArray(byteBufferx));
            }).result().orElseGet(ChunkNibbleArray::new);
            ChunkNibbleArray lv2 = (ChunkNibbleArray)this.section.get("Add").asByteBufferOpt().map((byteBufferx) -> {
               return new ChunkNibbleArray(DataFixUtils.toArray(byteBufferx));
            }).result().orElseGet(ChunkNibbleArray::new);
            this.seenStates.add(ChunkPalettedStorageFix.AIR);
            ChunkPalettedStorageFix.addTo(this.paletteMap, ChunkPalettedStorageFix.AIR);
            this.paletteData.add(ChunkPalettedStorageFix.AIR);

            for(int j = 0; j < 4096; ++j) {
               int k = j & 15;
               int l = j >> 8 & 15;
               int m = j >> 4 & 15;
               int n = lv2.get(k, l, m) << 12 | (byteBuffer.get(j) & 255) << 4 | lv.get(k, l, m);
               if (ChunkPalettedStorageFix.BLOCKS_NEEDING_IN_PLACE_UPDATE.get(n >> 4)) {
                  this.addInPlaceUpdate(n >> 4, j);
               }

               if (ChunkPalettedStorageFix.BLOCKS_NEEDING_SIDE_UPDATE.get(n >> 4)) {
                  int o = ChunkPalettedStorageFix.getSideToUpgradeFlag(k == 0, k == 15, m == 0, m == 15);
                  if (o == 0) {
                     this.innerPositions.add(j);
                  } else {
                     sidesToUpgrade |= o;
                  }
               }

               this.setBlock(j, BlockStateFlattening.lookupState(n));
            }

            return sidesToUpgrade;
         }
      }

      private void addInPlaceUpdate(int section, int index) {
         IntList intList = (IntList)this.inPlaceUpdates.get(section);
         if (intList == null) {
            intList = new IntArrayList();
            this.inPlaceUpdates.put(section, intList);
         }

         ((IntList)intList).add(index);
      }

      public Dynamic transform() {
         Dynamic dynamic = this.section;
         if (!this.hasBlocks) {
            return dynamic;
         } else {
            dynamic = dynamic.set("Palette", dynamic.createList(this.paletteData.stream()));
            int i = Math.max(4, DataFixUtils.ceillog2(this.seenStates.size()));
            WordPackedArray lv = new WordPackedArray(i, 4096);

            for(int j = 0; j < this.states.length; ++j) {
               lv.set(j, this.states[j]);
            }

            dynamic = dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(lv.getAlignedArray())));
            dynamic = dynamic.remove("Blocks");
            dynamic = dynamic.remove("Data");
            dynamic = dynamic.remove("Add");
            return dynamic;
         }
      }
   }
}
