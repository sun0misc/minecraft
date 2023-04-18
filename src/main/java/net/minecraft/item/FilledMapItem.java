package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class FilledMapItem extends NetworkSyncedItem {
   public static final int field_30907 = 128;
   public static final int field_30908 = 128;
   private static final int DEFAULT_MAP_COLOR = -12173266;
   private static final String MAP_KEY = "map";
   public static final String MAP_SCALE_DIRECTION_KEY = "map_scale_direction";
   public static final String MAP_TO_LOCK_KEY = "map_to_lock";

   public FilledMapItem(Item.Settings arg) {
      super(arg);
   }

   public static ItemStack createMap(World world, int x, int z, byte scale, boolean showIcons, boolean unlimitedTracking) {
      ItemStack lv = new ItemStack(Items.FILLED_MAP);
      createMapState(lv, world, x, z, scale, showIcons, unlimitedTracking, world.getRegistryKey());
      return lv;
   }

   @Nullable
   public static MapState getMapState(@Nullable Integer id, World world) {
      return id == null ? null : world.getMapState(getMapName(id));
   }

   @Nullable
   public static MapState getMapState(ItemStack map, World world) {
      Integer integer = getMapId(map);
      return getMapState(integer, world);
   }

   @Nullable
   public static Integer getMapId(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      return lv != null && lv.contains("map", NbtElement.NUMBER_TYPE) ? lv.getInt("map") : null;
   }

   private static int allocateMapId(World world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey dimension) {
      MapState lv = MapState.of((double)x, (double)z, (byte)scale, showIcons, unlimitedTracking, dimension);
      int l = world.getNextMapId();
      world.putMapState(getMapName(l), lv);
      return l;
   }

   private static void setMapId(ItemStack stack, int id) {
      stack.getOrCreateNbt().putInt("map", id);
   }

   private static void createMapState(ItemStack stack, World world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey dimension) {
      int l = allocateMapId(world, x, z, scale, showIcons, unlimitedTracking, dimension);
      setMapId(stack, l);
   }

   public static String getMapName(int mapId) {
      return "map_" + mapId;
   }

   public void updateColors(World world, Entity entity, MapState state) {
      if (world.getRegistryKey() == state.dimension && entity instanceof PlayerEntity) {
         int i = 1 << state.scale;
         int j = state.centerX;
         int k = state.centerZ;
         int l = MathHelper.floor(entity.getX() - (double)j) / i + 64;
         int m = MathHelper.floor(entity.getZ() - (double)k) / i + 64;
         int n = 128 / i;
         if (world.getDimension().hasCeiling()) {
            n /= 2;
         }

         MapState.PlayerUpdateTracker lv = state.getPlayerSyncData((PlayerEntity)entity);
         ++lv.field_131;
         BlockPos.Mutable lv2 = new BlockPos.Mutable();
         BlockPos.Mutable lv3 = new BlockPos.Mutable();
         boolean bl = false;

         for(int o = l - n + 1; o < l + n; ++o) {
            if ((o & 15) == (lv.field_131 & 15) || bl) {
               bl = false;
               double d = 0.0;

               for(int p = m - n - 1; p < m + n; ++p) {
                  if (o >= 0 && p >= -1 && o < 128 && p < 128) {
                     int q = MathHelper.square(o - l) + MathHelper.square(p - m);
                     boolean bl2 = q > (n - 2) * (n - 2);
                     int r = (j / i + o - 64) * i;
                     int s = (k / i + p - 64) * i;
                     Multiset multiset = LinkedHashMultiset.create();
                     WorldChunk lv4 = world.getChunk(ChunkSectionPos.getSectionCoord(r), ChunkSectionPos.getSectionCoord(s));
                     if (!lv4.isEmpty()) {
                        int t = 0;
                        double e = 0.0;
                        int u;
                        if (world.getDimension().hasCeiling()) {
                           u = r + s * 231871;
                           u = u * u * 31287121 + u * 11;
                           if ((u >> 20 & 1) == 0) {
                              multiset.add(Blocks.DIRT.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 10);
                           } else {
                              multiset.add(Blocks.STONE.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 100);
                           }

                           e = 100.0;
                        } else {
                           for(u = 0; u < i; ++u) {
                              for(int v = 0; v < i; ++v) {
                                 lv2.set(r + u, 0, s + v);
                                 int w = lv4.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lv2.getX(), lv2.getZ()) + 1;
                                 BlockState lv5;
                                 if (w <= world.getBottomY() + 1) {
                                    lv5 = Blocks.BEDROCK.getDefaultState();
                                 } else {
                                    do {
                                       --w;
                                       lv2.setY(w);
                                       lv5 = lv4.getBlockState(lv2);
                                    } while(lv5.getMapColor(world, lv2) == MapColor.CLEAR && w > world.getBottomY());

                                    if (w > world.getBottomY() && !lv5.getFluidState().isEmpty()) {
                                       int x = w - 1;
                                       lv3.set(lv2);

                                       BlockState lv6;
                                       do {
                                          lv3.setY(x--);
                                          lv6 = lv4.getBlockState(lv3);
                                          ++t;
                                       } while(x > world.getBottomY() && !lv6.getFluidState().isEmpty());

                                       lv5 = this.getFluidStateIfVisible(world, lv5, lv2);
                                    }
                                 }

                                 state.removeBanner(world, lv2.getX(), lv2.getZ());
                                 e += (double)w / (double)(i * i);
                                 multiset.add(lv5.getMapColor(world, lv2));
                              }
                           }
                        }

                        t /= i * i;
                        MapColor lv7 = (MapColor)Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.CLEAR);
                        MapColor.Brightness lv8;
                        double f;
                        if (lv7 == MapColor.WATER_BLUE) {
                           f = (double)t * 0.1 + (double)(o + p & 1) * 0.2;
                           if (f < 0.5) {
                              lv8 = MapColor.Brightness.HIGH;
                           } else if (f > 0.9) {
                              lv8 = MapColor.Brightness.LOW;
                           } else {
                              lv8 = MapColor.Brightness.NORMAL;
                           }
                        } else {
                           f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4;
                           if (f > 0.6) {
                              lv8 = MapColor.Brightness.HIGH;
                           } else if (f < -0.6) {
                              lv8 = MapColor.Brightness.LOW;
                           } else {
                              lv8 = MapColor.Brightness.NORMAL;
                           }
                        }

                        d = e;
                        if (p >= 0 && q < n * n && (!bl2 || (o + p & 1) != 0)) {
                           bl |= state.putColor(o, p, lv7.getRenderColorByte(lv8));
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private BlockState getFluidStateIfVisible(World world, BlockState state, BlockPos pos) {
      FluidState lv = state.getFluidState();
      return !lv.isEmpty() && !state.isSideSolidFullSquare(world, pos, Direction.UP) ? lv.getBlockState() : state;
   }

   private static boolean isAquaticBiome(boolean[] biomes, int x, int z) {
      return biomes[z * 128 + x];
   }

   public static void fillExplorationMap(ServerWorld world, ItemStack map) {
      MapState lv = getMapState((ItemStack)map, world);
      if (lv != null) {
         if (world.getRegistryKey() == lv.dimension) {
            int i = 1 << lv.scale;
            int j = lv.centerX;
            int k = lv.centerZ;
            boolean[] bls = new boolean[16384];
            int l = j / i - 64;
            int m = k / i - 64;
            BlockPos.Mutable lv2 = new BlockPos.Mutable();

            int n;
            int o;
            for(n = 0; n < 128; ++n) {
               for(o = 0; o < 128; ++o) {
                  RegistryEntry lv3 = world.getBiome(lv2.set((l + o) * i, 0, (m + n) * i));
                  bls[n * 128 + o] = lv3.isIn(BiomeTags.WATER_ON_MAP_OUTLINES);
               }
            }

            for(n = 1; n < 127; ++n) {
               for(o = 1; o < 127; ++o) {
                  int p = 0;

                  for(int q = -1; q < 2; ++q) {
                     for(int r = -1; r < 2; ++r) {
                        if ((q != 0 || r != 0) && isAquaticBiome(bls, n + q, o + r)) {
                           ++p;
                        }
                     }
                  }

                  MapColor.Brightness lv4 = MapColor.Brightness.LOWEST;
                  MapColor lv5 = MapColor.CLEAR;
                  if (isAquaticBiome(bls, n, o)) {
                     lv5 = MapColor.ORANGE;
                     if (p > 7 && o % 2 == 0) {
                        switch ((n + (int)(MathHelper.sin((float)o + 0.0F) * 7.0F)) / 8 % 5) {
                           case 0:
                           case 4:
                              lv4 = MapColor.Brightness.LOW;
                              break;
                           case 1:
                           case 3:
                              lv4 = MapColor.Brightness.NORMAL;
                              break;
                           case 2:
                              lv4 = MapColor.Brightness.HIGH;
                        }
                     } else if (p > 7) {
                        lv5 = MapColor.CLEAR;
                     } else if (p > 5) {
                        lv4 = MapColor.Brightness.NORMAL;
                     } else if (p > 3) {
                        lv4 = MapColor.Brightness.LOW;
                     } else if (p > 1) {
                        lv4 = MapColor.Brightness.LOW;
                     }
                  } else if (p > 0) {
                     lv5 = MapColor.BROWN;
                     if (p > 3) {
                        lv4 = MapColor.Brightness.NORMAL;
                     } else {
                        lv4 = MapColor.Brightness.LOWEST;
                     }
                  }

                  if (lv5 != MapColor.CLEAR) {
                     lv.setColor(n, o, lv5.getRenderColorByte(lv4));
                  }
               }
            }

         }
      }
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      if (!world.isClient) {
         MapState lv = getMapState(stack, world);
         if (lv != null) {
            if (entity instanceof PlayerEntity) {
               PlayerEntity lv2 = (PlayerEntity)entity;
               lv.update(lv2, stack);
            }

            if (!lv.locked && (selected || entity instanceof PlayerEntity && ((PlayerEntity)entity).getOffHandStack() == stack)) {
               this.updateColors(world, entity, lv);
            }

         }
      }
   }

   @Nullable
   public Packet createSyncPacket(ItemStack stack, World world, PlayerEntity player) {
      Integer integer = getMapId(stack);
      MapState lv = getMapState(integer, world);
      return lv != null ? lv.getPlayerMarkerPacket(integer, player) : null;
   }

   public void onCraft(ItemStack stack, World world, PlayerEntity player) {
      NbtCompound lv = stack.getNbt();
      if (lv != null && lv.contains("map_scale_direction", NbtElement.NUMBER_TYPE)) {
         scale(stack, world, lv.getInt("map_scale_direction"));
         lv.remove("map_scale_direction");
      } else if (lv != null && lv.contains("map_to_lock", NbtElement.BYTE_TYPE) && lv.getBoolean("map_to_lock")) {
         copyMap(world, stack);
         lv.remove("map_to_lock");
      }

   }

   private static void scale(ItemStack map, World world, int amount) {
      MapState lv = getMapState(map, world);
      if (lv != null) {
         int j = world.getNextMapId();
         world.putMapState(getMapName(j), lv.zoomOut(amount));
         setMapId(map, j);
      }

   }

   public static void copyMap(World world, ItemStack stack) {
      MapState lv = getMapState(stack, world);
      if (lv != null) {
         int i = world.getNextMapId();
         String string = getMapName(i);
         MapState lv2 = lv.copy();
         world.putMapState(string, lv2);
         setMapId(stack, i);
      }

   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      Integer integer = getMapId(stack);
      MapState lv = world == null ? null : getMapState(integer, world);
      NbtCompound lv2 = stack.getNbt();
      boolean bl;
      byte b;
      if (lv2 != null) {
         bl = lv2.getBoolean("map_to_lock");
         b = lv2.getByte("map_scale_direction");
      } else {
         bl = false;
         b = 0;
      }

      if (lv != null && (lv.locked || bl)) {
         tooltip.add(Text.translatable("filled_map.locked", integer).formatted(Formatting.GRAY));
      }

      if (context.isAdvanced()) {
         if (lv != null) {
            if (!bl && b == 0) {
               tooltip.add(Text.translatable("filled_map.id", integer).formatted(Formatting.GRAY));
            }

            int i = Math.min(lv.scale + b, 4);
            tooltip.add(Text.translatable("filled_map.scale", 1 << i).formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("filled_map.level", i, 4).formatted(Formatting.GRAY));
         } else {
            tooltip.add(Text.translatable("filled_map.unknown").formatted(Formatting.GRAY));
         }
      }

   }

   public static int getMapColor(ItemStack stack) {
      NbtCompound lv = stack.getSubNbt("display");
      if (lv != null && lv.contains("MapColor", NbtElement.NUMBER_TYPE)) {
         int i = lv.getInt("MapColor");
         return -16777216 | i & 16777215;
      } else {
         return -12173266;
      }
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      BlockState lv = context.getWorld().getBlockState(context.getBlockPos());
      if (lv.isIn(BlockTags.BANNERS)) {
         if (!context.getWorld().isClient) {
            MapState lv2 = getMapState(context.getStack(), context.getWorld());
            if (lv2 != null && !lv2.addBanner(context.getWorld(), context.getBlockPos())) {
               return ActionResult.FAIL;
            }
         }

         return ActionResult.success(context.getWorld().isClient);
      } else {
         return super.useOnBlock(context);
      }
   }
}
