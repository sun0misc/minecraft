package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.visitor.NbtOrderedStringFormatter;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class NbtHelper {
   private static final Comparator BLOCK_POS_COMPARATOR = Comparator.comparingInt((nbt) -> {
      return nbt.getInt(1);
   }).thenComparingInt((nbt) -> {
      return nbt.getInt(0);
   }).thenComparingInt((nbt) -> {
      return nbt.getInt(2);
   });
   private static final Comparator ENTITY_POS_COMPARATOR = Comparator.comparingDouble((nbt) -> {
      return nbt.getDouble(1);
   }).thenComparingDouble((nbt) -> {
      return nbt.getDouble(0);
   }).thenComparingDouble((nbt) -> {
      return nbt.getDouble(2);
   });
   public static final String DATA_KEY = "data";
   private static final char LEFT_CURLY_BRACKET = '{';
   private static final char RIGHT_CURLY_BRACKET = '}';
   private static final String COMMA = ",";
   private static final char COLON = ':';
   private static final Splitter COMMA_SPLITTER = Splitter.on(",");
   private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_33229 = 2;
   private static final int field_33230 = -1;

   private NbtHelper() {
   }

   @Nullable
   public static GameProfile toGameProfile(NbtCompound nbt) {
      String string = null;
      UUID uUID = null;
      if (nbt.contains("Name", NbtElement.STRING_TYPE)) {
         string = nbt.getString("Name");
      }

      if (nbt.containsUuid("Id")) {
         uUID = nbt.getUuid("Id");
      }

      try {
         GameProfile gameProfile = new GameProfile(uUID, string);
         if (nbt.contains("Properties", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv = nbt.getCompound("Properties");
            Iterator var5 = lv.getKeys().iterator();

            while(var5.hasNext()) {
               String string2 = (String)var5.next();
               NbtList lv2 = lv.getList(string2, NbtElement.COMPOUND_TYPE);

               for(int i = 0; i < lv2.size(); ++i) {
                  NbtCompound lv3 = lv2.getCompound(i);
                  String string3 = lv3.getString("Value");
                  if (lv3.contains("Signature", NbtElement.STRING_TYPE)) {
                     gameProfile.getProperties().put(string2, new Property(string2, string3, lv3.getString("Signature")));
                  } else {
                     gameProfile.getProperties().put(string2, new Property(string2, string3));
                  }
               }
            }
         }

         return gameProfile;
      } catch (Throwable var11) {
         return null;
      }
   }

   public static NbtCompound writeGameProfile(NbtCompound nbt, GameProfile profile) {
      if (!StringHelper.isEmpty(profile.getName())) {
         nbt.putString("Name", profile.getName());
      }

      if (profile.getId() != null) {
         nbt.putUuid("Id", profile.getId());
      }

      if (!profile.getProperties().isEmpty()) {
         NbtCompound lv = new NbtCompound();
         Iterator var3 = profile.getProperties().keySet().iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            NbtList lv2 = new NbtList();

            NbtCompound lv3;
            for(Iterator var6 = profile.getProperties().get(string).iterator(); var6.hasNext(); lv2.add(lv3)) {
               Property property = (Property)var6.next();
               lv3 = new NbtCompound();
               lv3.putString("Value", property.getValue());
               if (property.hasSignature()) {
                  lv3.putString("Signature", property.getSignature());
               }
            }

            lv.put(string, lv2);
         }

         nbt.put("Properties", lv);
      }

      return nbt;
   }

   @VisibleForTesting
   public static boolean matches(@Nullable NbtElement standard, @Nullable NbtElement subject, boolean ignoreListOrder) {
      if (standard == subject) {
         return true;
      } else if (standard == null) {
         return true;
      } else if (subject == null) {
         return false;
      } else if (!standard.getClass().equals(subject.getClass())) {
         return false;
      } else if (standard instanceof NbtCompound) {
         NbtCompound lv = (NbtCompound)standard;
         NbtCompound lv2 = (NbtCompound)subject;
         Iterator var11 = lv.getKeys().iterator();

         String string;
         NbtElement lv3;
         do {
            if (!var11.hasNext()) {
               return true;
            }

            string = (String)var11.next();
            lv3 = lv.get(string);
         } while(matches(lv3, lv2.get(string), ignoreListOrder));

         return false;
      } else if (standard instanceof NbtList && ignoreListOrder) {
         NbtList lv4 = (NbtList)standard;
         NbtList lv5 = (NbtList)subject;
         if (lv4.isEmpty()) {
            return lv5.isEmpty();
         } else {
            for(int i = 0; i < lv4.size(); ++i) {
               NbtElement lv6 = lv4.get(i);
               boolean bl2 = false;

               for(int j = 0; j < lv5.size(); ++j) {
                  if (matches(lv6, lv5.get(j), ignoreListOrder)) {
                     bl2 = true;
                     break;
                  }
               }

               if (!bl2) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return standard.equals(subject);
      }
   }

   public static NbtIntArray fromUuid(UUID uuid) {
      return new NbtIntArray(Uuids.toIntArray(uuid));
   }

   public static UUID toUuid(NbtElement element) {
      if (element.getNbtType() != NbtIntArray.TYPE) {
         String var10002 = NbtIntArray.TYPE.getCrashReportName();
         throw new IllegalArgumentException("Expected UUID-Tag to be of type " + var10002 + ", but found " + element.getNbtType().getCrashReportName() + ".");
      } else {
         int[] is = ((NbtIntArray)element).getIntArray();
         if (is.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
         } else {
            return Uuids.toUuid(is);
         }
      }
   }

   public static BlockPos toBlockPos(NbtCompound nbt) {
      return new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
   }

   public static NbtCompound fromBlockPos(BlockPos pos) {
      NbtCompound lv = new NbtCompound();
      lv.putInt("X", pos.getX());
      lv.putInt("Y", pos.getY());
      lv.putInt("Z", pos.getZ());
      return lv;
   }

   public static BlockState toBlockState(RegistryEntryLookup blockLookup, NbtCompound nbt) {
      if (!nbt.contains("Name", NbtElement.STRING_TYPE)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Identifier lv = new Identifier(nbt.getString("Name"));
         Optional optional = blockLookup.getOptional(RegistryKey.of(RegistryKeys.BLOCK, lv));
         if (optional.isEmpty()) {
            return Blocks.AIR.getDefaultState();
         } else {
            Block lv2 = (Block)((RegistryEntry)optional.get()).value();
            BlockState lv3 = lv2.getDefaultState();
            if (nbt.contains("Properties", NbtElement.COMPOUND_TYPE)) {
               NbtCompound lv4 = nbt.getCompound("Properties");
               StateManager lv5 = lv2.getStateManager();
               Iterator var8 = lv4.getKeys().iterator();

               while(var8.hasNext()) {
                  String string = (String)var8.next();
                  net.minecraft.state.property.Property lv6 = lv5.getProperty(string);
                  if (lv6 != null) {
                     lv3 = (BlockState)withProperty(lv3, lv6, string, lv4, nbt);
                  }
               }
            }

            return lv3;
         }
      }
   }

   private static State withProperty(State state, net.minecraft.state.property.Property property, String key, NbtCompound properties, NbtCompound root) {
      Optional optional = property.parse(properties.getString(key));
      if (optional.isPresent()) {
         return (State)state.with(property, (Comparable)optional.get());
      } else {
         LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{key, properties.getString(key), root.toString()});
         return state;
      }
   }

   public static NbtCompound fromBlockState(BlockState state) {
      NbtCompound lv = new NbtCompound();
      lv.putString("Name", Registries.BLOCK.getId(state.getBlock()).toString());
      ImmutableMap immutableMap = state.getEntries();
      if (!immutableMap.isEmpty()) {
         NbtCompound lv2 = new NbtCompound();
         UnmodifiableIterator var4 = immutableMap.entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry entry = (Map.Entry)var4.next();
            net.minecraft.state.property.Property lv3 = (net.minecraft.state.property.Property)entry.getKey();
            lv2.putString(lv3.getName(), nameValue(lv3, (Comparable)entry.getValue()));
         }

         lv.put("Properties", lv2);
      }

      return lv;
   }

   public static NbtCompound fromFluidState(FluidState state) {
      NbtCompound lv = new NbtCompound();
      lv.putString("Name", Registries.FLUID.getId(state.getFluid()).toString());
      ImmutableMap immutableMap = state.getEntries();
      if (!immutableMap.isEmpty()) {
         NbtCompound lv2 = new NbtCompound();
         UnmodifiableIterator var4 = immutableMap.entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry entry = (Map.Entry)var4.next();
            net.minecraft.state.property.Property lv3 = (net.minecraft.state.property.Property)entry.getKey();
            lv2.putString(lv3.getName(), nameValue(lv3, (Comparable)entry.getValue()));
         }

         lv.put("Properties", lv2);
      }

      return lv;
   }

   private static String nameValue(net.minecraft.state.property.Property property, Comparable value) {
      return property.name(value);
   }

   public static String toFormattedString(NbtElement nbt) {
      return toFormattedString(nbt, false);
   }

   public static String toFormattedString(NbtElement nbt, boolean withArrayContents) {
      return appendFormattedString(new StringBuilder(), nbt, 0, withArrayContents).toString();
   }

   public static StringBuilder appendFormattedString(StringBuilder stringBuilder, NbtElement nbt, int depth, boolean withArrayContents) {
      int var9;
      int n;
      int j;
      int k;
      String string;
      int m;
      switch (nbt.getType()) {
         case 0:
            break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 8:
            stringBuilder.append(nbt);
            break;
         case 7:
            NbtByteArray lv = (NbtByteArray)nbt;
            byte[] bs = lv.getByteArray();
            j = bs.length;
            appendIndent(depth, stringBuilder).append("byte[").append(j).append("] {\n");
            if (!withArrayContents) {
               appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
            } else {
               appendIndent(depth + 1, stringBuilder);

               for(k = 0; k < bs.length; ++k) {
                  if (k != 0) {
                     stringBuilder.append(',');
                  }

                  if (k % 16 == 0 && k / 16 > 0) {
                     stringBuilder.append('\n');
                     if (k < bs.length) {
                        appendIndent(depth + 1, stringBuilder);
                     }
                  } else if (k != 0) {
                     stringBuilder.append(' ');
                  }

                  stringBuilder.append(String.format(Locale.ROOT, "0x%02X", bs[k] & 255));
               }
            }

            stringBuilder.append('\n');
            appendIndent(depth, stringBuilder).append('}');
            break;
         case 9:
            NbtList lv2 = (NbtList)nbt;
            int l = lv2.size();
            int j = lv2.getHeldType();
            string = j == 0 ? "undefined" : NbtTypes.byId(j).getCommandFeedbackName();
            appendIndent(depth, stringBuilder).append("list<").append(string).append(">[").append(l).append("] [");
            if (l != 0) {
               stringBuilder.append('\n');
            }

            for(m = 0; m < l; ++m) {
               if (m != 0) {
                  stringBuilder.append(",\n");
               }

               appendIndent(depth + 1, stringBuilder);
               appendFormattedString(stringBuilder, lv2.get(m), depth + 1, withArrayContents);
            }

            if (l != 0) {
               stringBuilder.append('\n');
            }

            appendIndent(depth, stringBuilder).append(']');
            break;
         case 10:
            NbtCompound lv4 = (NbtCompound)nbt;
            List list = Lists.newArrayList(lv4.getKeys());
            Collections.sort(list);
            appendIndent(depth, stringBuilder).append('{');
            if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (depth + 1)) {
               stringBuilder.append('\n');
               appendIndent(depth + 1, stringBuilder);
            }

            j = list.stream().mapToInt(String::length).max().orElse(0);
            string = Strings.repeat(" ", j);

            for(m = 0; m < list.size(); ++m) {
               if (m != 0) {
                  stringBuilder.append(",\n");
               }

               String string2 = (String)list.get(m);
               appendIndent(depth + 1, stringBuilder).append('"').append(string2).append('"').append(string, 0, string.length() - string2.length()).append(": ");
               appendFormattedString(stringBuilder, lv4.get(string2), depth + 1, withArrayContents);
            }

            if (!list.isEmpty()) {
               stringBuilder.append('\n');
            }

            appendIndent(depth, stringBuilder).append('}');
            break;
         case 11:
            NbtIntArray lv3 = (NbtIntArray)nbt;
            int[] is = lv3.getIntArray();
            j = 0;
            int[] var7 = is;
            m = is.length;

            for(var9 = 0; var9 < m; ++var9) {
               n = var7[var9];
               j = Math.max(j, String.format(Locale.ROOT, "%X", n).length());
            }

            k = is.length;
            appendIndent(depth, stringBuilder).append("int[").append(k).append("] {\n");
            if (!withArrayContents) {
               appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
            } else {
               appendIndent(depth + 1, stringBuilder);

               for(m = 0; m < is.length; ++m) {
                  if (m != 0) {
                     stringBuilder.append(',');
                  }

                  if (m % 16 == 0 && m / 16 > 0) {
                     stringBuilder.append('\n');
                     if (m < is.length) {
                        appendIndent(depth + 1, stringBuilder);
                     }
                  } else if (m != 0) {
                     stringBuilder.append(' ');
                  }

                  stringBuilder.append(String.format(Locale.ROOT, "0x%0" + j + "X", is[m]));
               }
            }

            stringBuilder.append('\n');
            appendIndent(depth, stringBuilder).append('}');
            break;
         case 12:
            NbtLongArray lv5 = (NbtLongArray)nbt;
            long[] ls = lv5.getLongArray();
            long o = 0L;
            long[] var8 = ls;
            var9 = ls.length;

            for(n = 0; n < var9; ++n) {
               long p = var8[n];
               o = Math.max(o, (long)String.format(Locale.ROOT, "%X", p).length());
            }

            long q = (long)ls.length;
            appendIndent(depth, stringBuilder).append("long[").append(q).append("] {\n");
            if (!withArrayContents) {
               appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
            } else {
               appendIndent(depth + 1, stringBuilder);

               for(n = 0; n < ls.length; ++n) {
                  if (n != 0) {
                     stringBuilder.append(',');
                  }

                  if (n % 16 == 0 && n / 16 > 0) {
                     stringBuilder.append('\n');
                     if (n < ls.length) {
                        appendIndent(depth + 1, stringBuilder);
                     }
                  } else if (n != 0) {
                     stringBuilder.append(' ');
                  }

                  stringBuilder.append(String.format(Locale.ROOT, "0x%0" + o + "X", ls[n]));
               }
            }

            stringBuilder.append('\n');
            appendIndent(depth, stringBuilder).append('}');
            break;
         default:
            stringBuilder.append("<UNKNOWN :(>");
      }

      return stringBuilder;
   }

   private static StringBuilder appendIndent(int depth, StringBuilder stringBuilder) {
      int j = stringBuilder.lastIndexOf("\n") + 1;
      int k = stringBuilder.length() - j;

      for(int l = 0; l < 2 * depth - k; ++l) {
         stringBuilder.append(' ');
      }

      return stringBuilder;
   }

   public static Text toPrettyPrintedText(NbtElement element) {
      return (new NbtTextFormatter("", 0)).apply(element);
   }

   public static String toNbtProviderString(NbtCompound compound) {
      return (new NbtOrderedStringFormatter()).apply(toNbtProviderFormat(compound));
   }

   public static NbtCompound fromNbtProviderString(String string) throws CommandSyntaxException {
      return fromNbtProviderFormat(StringNbtReader.parse(string));
   }

   @VisibleForTesting
   static NbtCompound toNbtProviderFormat(NbtCompound compound) {
      boolean bl = compound.contains("palettes", NbtElement.LIST_TYPE);
      NbtList lv;
      if (bl) {
         lv = compound.getList("palettes", NbtElement.LIST_TYPE).getList(0);
      } else {
         lv = compound.getList("palette", NbtElement.COMPOUND_TYPE);
      }

      Stream var10000 = lv.stream();
      Objects.requireNonNull(NbtCompound.class);
      NbtList lv2 = (NbtList)var10000.map(NbtCompound.class::cast).map(NbtHelper::toNbtProviderFormattedPalette).map(NbtString::of).collect(Collectors.toCollection(NbtList::new));
      compound.put("palette", lv2);
      NbtList lv3;
      NbtList lv4;
      if (bl) {
         lv3 = new NbtList();
         lv4 = compound.getList("palettes", NbtElement.LIST_TYPE);
         var10000 = lv4.stream();
         Objects.requireNonNull(NbtList.class);
         var10000.map(NbtList.class::cast).forEach((nbt) -> {
            NbtCompound lv = new NbtCompound();

            for(int i = 0; i < nbt.size(); ++i) {
               lv.putString(lv2.getString(i), toNbtProviderFormattedPalette(nbt.getCompound(i)));
            }

            lv3.add(lv);
         });
         compound.put("palettes", lv3);
      }

      if (compound.contains("entities", NbtElement.LIST_TYPE)) {
         lv3 = compound.getList("entities", NbtElement.COMPOUND_TYPE);
         var10000 = lv3.stream();
         Objects.requireNonNull(NbtCompound.class);
         lv4 = (NbtList)var10000.map(NbtCompound.class::cast).sorted(Comparator.comparing((nbt) -> {
            return nbt.getList("pos", NbtElement.DOUBLE_TYPE);
         }, ENTITY_POS_COMPARATOR)).collect(Collectors.toCollection(NbtList::new));
         compound.put("entities", lv4);
      }

      var10000 = compound.getList("blocks", NbtElement.COMPOUND_TYPE).stream();
      Objects.requireNonNull(NbtCompound.class);
      lv3 = (NbtList)var10000.map(NbtCompound.class::cast).sorted(Comparator.comparing((nbt) -> {
         return nbt.getList("pos", NbtElement.INT_TYPE);
      }, BLOCK_POS_COMPARATOR)).peek((nbt) -> {
         nbt.putString("state", lv2.getString(nbt.getInt("state")));
      }).collect(Collectors.toCollection(NbtList::new));
      compound.put("data", lv3);
      compound.remove("blocks");
      return compound;
   }

   @VisibleForTesting
   static NbtCompound fromNbtProviderFormat(NbtCompound compound) {
      NbtList lv = compound.getList("palette", NbtElement.STRING_TYPE);
      Stream var10000 = lv.stream();
      Objects.requireNonNull(NbtString.class);
      Map map = (Map)var10000.map(NbtString.class::cast).map(NbtString::asString).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtHelper::fromNbtProviderFormattedPalette));
      if (compound.contains("palettes", NbtElement.LIST_TYPE)) {
         Stream var10002 = compound.getList("palettes", NbtElement.COMPOUND_TYPE).stream();
         Objects.requireNonNull(NbtCompound.class);
         compound.put("palettes", (NbtElement)var10002.map(NbtCompound.class::cast).map((nbt) -> {
            Stream var10000 = map.keySet().stream();
            Objects.requireNonNull(nbt);
            return (NbtList)var10000.map(nbt::getString).map(NbtHelper::fromNbtProviderFormattedPalette).collect(Collectors.toCollection(NbtList::new));
         }).collect(Collectors.toCollection(NbtList::new)));
         compound.remove("palette");
      } else {
         compound.put("palette", (NbtElement)map.values().stream().collect(Collectors.toCollection(NbtList::new)));
      }

      if (compound.contains("data", NbtElement.LIST_TYPE)) {
         Object2IntMap object2IntMap = new Object2IntOpenHashMap();
         object2IntMap.defaultReturnValue(-1);

         for(int i = 0; i < lv.size(); ++i) {
            object2IntMap.put(lv.getString(i), i);
         }

         NbtList lv2 = compound.getList("data", NbtElement.COMPOUND_TYPE);

         for(int j = 0; j < lv2.size(); ++j) {
            NbtCompound lv3 = lv2.getCompound(j);
            String string = lv3.getString("state");
            int k = object2IntMap.getInt(string);
            if (k == -1) {
               throw new IllegalStateException("Entry " + string + " missing from palette");
            }

            lv3.putInt("state", k);
         }

         compound.put("blocks", lv2);
         compound.remove("data");
      }

      return compound;
   }

   @VisibleForTesting
   static String toNbtProviderFormattedPalette(NbtCompound compound) {
      StringBuilder stringBuilder = new StringBuilder(compound.getString("Name"));
      if (compound.contains("Properties", NbtElement.COMPOUND_TYPE)) {
         NbtCompound lv = compound.getCompound("Properties");
         String string = (String)lv.getKeys().stream().sorted().map((key) -> {
            return key + ":" + lv.get(key).asString();
         }).collect(Collectors.joining(","));
         stringBuilder.append('{').append(string).append('}');
      }

      return stringBuilder.toString();
   }

   @VisibleForTesting
   static NbtCompound fromNbtProviderFormattedPalette(String string) {
      NbtCompound lv = new NbtCompound();
      int i = string.indexOf(123);
      String string2;
      if (i >= 0) {
         string2 = string.substring(0, i);
         NbtCompound lv2 = new NbtCompound();
         if (i + 2 <= string.length()) {
            String string3 = string.substring(i + 1, string.indexOf(125, i));
            COMMA_SPLITTER.split(string3).forEach((property) -> {
               List list = COLON_SPLITTER.splitToList(property);
               if (list.size() == 2) {
                  lv2.putString((String)list.get(0), (String)list.get(1));
               } else {
                  LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", string);
               }

            });
            lv.put("Properties", lv2);
         }
      } else {
         string2 = string;
      }

      lv.putString("Name", string2);
      return lv;
   }

   public static NbtCompound putDataVersion(NbtCompound nbt) {
      int i = SharedConstants.getGameVersion().getSaveVersion().getId();
      return putDataVersion(nbt, i);
   }

   public static NbtCompound putDataVersion(NbtCompound nbt, int dataVersion) {
      nbt.putInt("DataVersion", dataVersion);
      return nbt;
   }

   public static int getDataVersion(NbtCompound nbt, int fallback) {
      return nbt.contains("DataVersion", NbtElement.NUMBER_TYPE) ? nbt.getInt("DataVersion") : fallback;
   }
}
