package net.minecraft.item.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MapState extends PersistentState {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_31832 = 128;
   private static final int field_31833 = 64;
   public static final int MAX_SCALE = 4;
   public static final int MAX_ICONS = 256;
   public final int centerX;
   public final int centerZ;
   public final RegistryKey dimension;
   private final boolean showIcons;
   private final boolean unlimitedTracking;
   public final byte scale;
   public byte[] colors = new byte[16384];
   public final boolean locked;
   private final List updateTrackers = Lists.newArrayList();
   private final Map updateTrackersByPlayer = Maps.newHashMap();
   private final Map banners = Maps.newHashMap();
   final Map icons = Maps.newLinkedHashMap();
   private final Map frames = Maps.newHashMap();
   private int iconCount;

   private MapState(int centerX, int centerZ, byte scale, boolean showIcons, boolean unlimitedTracking, boolean locked, RegistryKey dimension) {
      this.scale = scale;
      this.centerX = centerX;
      this.centerZ = centerZ;
      this.dimension = dimension;
      this.showIcons = showIcons;
      this.unlimitedTracking = unlimitedTracking;
      this.locked = locked;
      this.markDirty();
   }

   public static MapState of(double centerX, double centerZ, byte scale, boolean showIcons, boolean unlimitedTracking, RegistryKey dimension) {
      int i = 128 * (1 << scale);
      int j = MathHelper.floor((centerX + 64.0) / (double)i);
      int k = MathHelper.floor((centerZ + 64.0) / (double)i);
      int l = j * i + i / 2 - 64;
      int m = k * i + i / 2 - 64;
      return new MapState(l, m, scale, showIcons, unlimitedTracking, false, dimension);
   }

   public static MapState of(byte scale, boolean locked, RegistryKey dimension) {
      return new MapState(0, 0, scale, false, false, locked, dimension);
   }

   public static MapState fromNbt(NbtCompound nbt) {
      DataResult var10000 = DimensionType.worldFromDimensionNbt(new Dynamic(NbtOps.INSTANCE, nbt.get("dimension")));
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      RegistryKey lv = (RegistryKey)var10000.resultOrPartial(var10001::error).orElseThrow(() -> {
         return new IllegalArgumentException("Invalid map dimension: " + nbt.get("dimension"));
      });
      int i = nbt.getInt("xCenter");
      int j = nbt.getInt("zCenter");
      byte b = (byte)MathHelper.clamp(nbt.getByte("scale"), 0, 4);
      boolean bl = !nbt.contains("trackingPosition", NbtElement.BYTE_TYPE) || nbt.getBoolean("trackingPosition");
      boolean bl2 = nbt.getBoolean("unlimitedTracking");
      boolean bl3 = nbt.getBoolean("locked");
      MapState lv2 = new MapState(i, j, b, bl, bl2, bl3, lv);
      byte[] bs = nbt.getByteArray("colors");
      if (bs.length == 16384) {
         lv2.colors = bs;
      }

      NbtList lv3 = nbt.getList("banners", NbtElement.COMPOUND_TYPE);

      for(int k = 0; k < lv3.size(); ++k) {
         MapBannerMarker lv4 = MapBannerMarker.fromNbt(lv3.getCompound(k));
         lv2.banners.put(lv4.getKey(), lv4);
         lv2.addIcon(lv4.getIconType(), (WorldAccess)null, lv4.getKey(), (double)lv4.getPos().getX(), (double)lv4.getPos().getZ(), 180.0, lv4.getName());
      }

      NbtList lv5 = nbt.getList("frames", NbtElement.COMPOUND_TYPE);

      for(int l = 0; l < lv5.size(); ++l) {
         MapFrameMarker lv6 = MapFrameMarker.fromNbt(lv5.getCompound(l));
         lv2.frames.put(lv6.getKey(), lv6);
         lv2.addIcon(MapIcon.Type.FRAME, (WorldAccess)null, "frame-" + lv6.getEntityId(), (double)lv6.getPos().getX(), (double)lv6.getPos().getZ(), (double)lv6.getRotation(), (Text)null);
      }

      return lv2;
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      DataResult var10000 = Identifier.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.getValue());
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
         nbt.put("dimension", arg2);
      });
      nbt.putInt("xCenter", this.centerX);
      nbt.putInt("zCenter", this.centerZ);
      nbt.putByte("scale", this.scale);
      nbt.putByteArray("colors", this.colors);
      nbt.putBoolean("trackingPosition", this.showIcons);
      nbt.putBoolean("unlimitedTracking", this.unlimitedTracking);
      nbt.putBoolean("locked", this.locked);
      NbtList lv = new NbtList();
      Iterator var3 = this.banners.values().iterator();

      while(var3.hasNext()) {
         MapBannerMarker lv2 = (MapBannerMarker)var3.next();
         lv.add(lv2.getNbt());
      }

      nbt.put("banners", lv);
      NbtList lv3 = new NbtList();
      Iterator var7 = this.frames.values().iterator();

      while(var7.hasNext()) {
         MapFrameMarker lv4 = (MapFrameMarker)var7.next();
         lv3.add(lv4.toNbt());
      }

      nbt.put("frames", lv3);
      return nbt;
   }

   public MapState copy() {
      MapState lv = new MapState(this.centerX, this.centerZ, this.scale, this.showIcons, this.unlimitedTracking, true, this.dimension);
      lv.banners.putAll(this.banners);
      lv.icons.putAll(this.icons);
      lv.iconCount = this.iconCount;
      System.arraycopy(this.colors, 0, lv.colors, 0, this.colors.length);
      lv.markDirty();
      return lv;
   }

   public MapState zoomOut(int zoomOutScale) {
      return of((double)this.centerX, (double)this.centerZ, (byte)MathHelper.clamp(this.scale + zoomOutScale, 0, 4), this.showIcons, this.unlimitedTracking, this.dimension);
   }

   public void update(PlayerEntity player, ItemStack stack) {
      if (!this.updateTrackersByPlayer.containsKey(player)) {
         PlayerUpdateTracker lv = new PlayerUpdateTracker(player);
         this.updateTrackersByPlayer.put(player, lv);
         this.updateTrackers.add(lv);
      }

      if (!player.getInventory().contains(stack)) {
         this.removeIcon(player.getName().getString());
      }

      for(int i = 0; i < this.updateTrackers.size(); ++i) {
         PlayerUpdateTracker lv2 = (PlayerUpdateTracker)this.updateTrackers.get(i);
         String string = lv2.player.getName().getString();
         if (!lv2.player.isRemoved() && (lv2.player.getInventory().contains(stack) || stack.isInFrame())) {
            if (!stack.isInFrame() && lv2.player.world.getRegistryKey() == this.dimension && this.showIcons) {
               this.addIcon(MapIcon.Type.PLAYER, lv2.player.world, string, lv2.player.getX(), lv2.player.getZ(), (double)lv2.player.getYaw(), (Text)null);
            }
         } else {
            this.updateTrackersByPlayer.remove(lv2.player);
            this.updateTrackers.remove(lv2);
            this.removeIcon(string);
         }
      }

      if (stack.isInFrame() && this.showIcons) {
         ItemFrameEntity lv3 = stack.getFrame();
         BlockPos lv4 = lv3.getDecorationBlockPos();
         MapFrameMarker lv5 = (MapFrameMarker)this.frames.get(MapFrameMarker.getKey(lv4));
         if (lv5 != null && lv3.getId() != lv5.getEntityId() && this.frames.containsKey(lv5.getKey())) {
            this.removeIcon("frame-" + lv5.getEntityId());
         }

         MapFrameMarker lv6 = new MapFrameMarker(lv4, lv3.getHorizontalFacing().getHorizontal() * 90, lv3.getId());
         this.addIcon(MapIcon.Type.FRAME, player.world, "frame-" + lv3.getId(), (double)lv4.getX(), (double)lv4.getZ(), (double)(lv3.getHorizontalFacing().getHorizontal() * 90), (Text)null);
         this.frames.put(lv6.getKey(), lv6);
      }

      NbtCompound lv7 = stack.getNbt();
      if (lv7 != null && lv7.contains("Decorations", NbtElement.LIST_TYPE)) {
         NbtList lv8 = lv7.getList("Decorations", NbtElement.COMPOUND_TYPE);

         for(int j = 0; j < lv8.size(); ++j) {
            NbtCompound lv9 = lv8.getCompound(j);
            if (!this.icons.containsKey(lv9.getString("id"))) {
               this.addIcon(MapIcon.Type.byId(lv9.getByte("type")), player.world, lv9.getString("id"), lv9.getDouble("x"), lv9.getDouble("z"), lv9.getDouble("rot"), (Text)null);
            }
         }
      }

   }

   private void removeIcon(String id) {
      MapIcon lv = (MapIcon)this.icons.remove(id);
      if (lv != null && lv.getType().shouldUseIconCountLimit()) {
         --this.iconCount;
      }

      this.markIconsDirty();
   }

   public static void addDecorationsNbt(ItemStack stack, BlockPos pos, String id, MapIcon.Type type) {
      NbtList lv;
      if (stack.hasNbt() && stack.getNbt().contains("Decorations", NbtElement.LIST_TYPE)) {
         lv = stack.getNbt().getList("Decorations", NbtElement.COMPOUND_TYPE);
      } else {
         lv = new NbtList();
         stack.setSubNbt("Decorations", lv);
      }

      NbtCompound lv2 = new NbtCompound();
      lv2.putByte("type", type.getId());
      lv2.putString("id", id);
      lv2.putDouble("x", (double)pos.getX());
      lv2.putDouble("z", (double)pos.getZ());
      lv2.putDouble("rot", 180.0);
      lv.add(lv2);
      if (type.hasTintColor()) {
         NbtCompound lv3 = stack.getOrCreateSubNbt("display");
         lv3.putInt("MapColor", type.getTintColor());
      }

   }

   private void addIcon(MapIcon.Type type, @Nullable WorldAccess world, String key, double x, double z, double rotation, @Nullable Text text) {
      int i = 1 << this.scale;
      float g = (float)(x - (double)this.centerX) / (float)i;
      float h = (float)(z - (double)this.centerZ) / (float)i;
      byte b = (byte)((int)((double)(g * 2.0F) + 0.5));
      byte c = (byte)((int)((double)(h * 2.0F) + 0.5));
      int j = true;
      byte k;
      if (g >= -63.0F && h >= -63.0F && g <= 63.0F && h <= 63.0F) {
         rotation += rotation < 0.0 ? -8.0 : 8.0;
         k = (byte)((int)(rotation * 16.0 / 360.0));
         if (this.dimension == World.NETHER && world != null) {
            int l = (int)(world.getLevelProperties().getTimeOfDay() / 10L);
            k = (byte)(l * l * 34187121 + l * 121 >> 15 & 15);
         }
      } else {
         if (type != MapIcon.Type.PLAYER) {
            this.removeIcon(key);
            return;
         }

         int l = true;
         if (Math.abs(g) < 320.0F && Math.abs(h) < 320.0F) {
            type = MapIcon.Type.PLAYER_OFF_MAP;
         } else {
            if (!this.unlimitedTracking) {
               this.removeIcon(key);
               return;
            }

            type = MapIcon.Type.PLAYER_OFF_LIMITS;
         }

         k = 0;
         if (g <= -63.0F) {
            b = -128;
         }

         if (h <= -63.0F) {
            c = -128;
         }

         if (g >= 63.0F) {
            b = 127;
         }

         if (h >= 63.0F) {
            c = 127;
         }
      }

      MapIcon lv = new MapIcon(type, b, c, k, text);
      MapIcon lv2 = (MapIcon)this.icons.put(key, lv);
      if (!lv.equals(lv2)) {
         if (lv2 != null && lv2.getType().shouldUseIconCountLimit()) {
            --this.iconCount;
         }

         if (type.shouldUseIconCountLimit()) {
            ++this.iconCount;
         }

         this.markIconsDirty();
      }

   }

   @Nullable
   public Packet getPlayerMarkerPacket(int id, PlayerEntity player) {
      PlayerUpdateTracker lv = (PlayerUpdateTracker)this.updateTrackersByPlayer.get(player);
      return lv == null ? null : lv.getPacket(id);
   }

   private void markDirty(int x, int z) {
      this.markDirty();
      Iterator var3 = this.updateTrackers.iterator();

      while(var3.hasNext()) {
         PlayerUpdateTracker lv = (PlayerUpdateTracker)var3.next();
         lv.markDirty(x, z);
      }

   }

   private void markIconsDirty() {
      this.markDirty();
      this.updateTrackers.forEach(PlayerUpdateTracker::markIconsDirty);
   }

   public PlayerUpdateTracker getPlayerSyncData(PlayerEntity player) {
      PlayerUpdateTracker lv = (PlayerUpdateTracker)this.updateTrackersByPlayer.get(player);
      if (lv == null) {
         lv = new PlayerUpdateTracker(player);
         this.updateTrackersByPlayer.put(player, lv);
         this.updateTrackers.add(lv);
      }

      return lv;
   }

   public boolean addBanner(WorldAccess world, BlockPos pos) {
      double d = (double)pos.getX() + 0.5;
      double e = (double)pos.getZ() + 0.5;
      int i = 1 << this.scale;
      double f = (d - (double)this.centerX) / (double)i;
      double g = (e - (double)this.centerZ) / (double)i;
      int j = true;
      if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
         MapBannerMarker lv = MapBannerMarker.fromWorldBlock(world, pos);
         if (lv == null) {
            return false;
         }

         if (this.banners.remove(lv.getKey(), lv)) {
            this.removeIcon(lv.getKey());
            return true;
         }

         if (!this.iconCountNotLessThan(256)) {
            this.banners.put(lv.getKey(), lv);
            this.addIcon(lv.getIconType(), world, lv.getKey(), d, e, 180.0, lv.getName());
            return true;
         }
      }

      return false;
   }

   public void removeBanner(BlockView world, int x, int z) {
      Iterator iterator = this.banners.values().iterator();

      while(iterator.hasNext()) {
         MapBannerMarker lv = (MapBannerMarker)iterator.next();
         if (lv.getPos().getX() == x && lv.getPos().getZ() == z) {
            MapBannerMarker lv2 = MapBannerMarker.fromWorldBlock(world, lv.getPos());
            if (!lv.equals(lv2)) {
               iterator.remove();
               this.removeIcon(lv.getKey());
            }
         }
      }

   }

   public Collection getBanners() {
      return this.banners.values();
   }

   public void removeFrame(BlockPos pos, int id) {
      this.removeIcon("frame-" + id);
      this.frames.remove(MapFrameMarker.getKey(pos));
   }

   public boolean putColor(int x, int z, byte color) {
      byte c = this.colors[x + z * 128];
      if (c != color) {
         this.setColor(x, z, color);
         return true;
      } else {
         return false;
      }
   }

   public void setColor(int x, int z, byte color) {
      this.colors[x + z * 128] = color;
      this.markDirty(x, z);
   }

   public boolean hasMonumentIcon() {
      Iterator var1 = this.icons.values().iterator();

      MapIcon lv;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         lv = (MapIcon)var1.next();
      } while(lv.getType() != MapIcon.Type.MANSION && lv.getType() != MapIcon.Type.MONUMENT);

      return true;
   }

   public void replaceIcons(List icons) {
      this.icons.clear();
      this.iconCount = 0;

      for(int i = 0; i < icons.size(); ++i) {
         MapIcon lv = (MapIcon)icons.get(i);
         this.icons.put("icon-" + i, lv);
         if (lv.getType().shouldUseIconCountLimit()) {
            ++this.iconCount;
         }
      }

   }

   public Iterable getIcons() {
      return this.icons.values();
   }

   public boolean iconCountNotLessThan(int iconCount) {
      return this.iconCount >= iconCount;
   }

   public class PlayerUpdateTracker {
      public final PlayerEntity player;
      private boolean dirty = true;
      private int startX;
      private int startZ;
      private int endX = 127;
      private int endZ = 127;
      private boolean iconsDirty = true;
      private int emptyPacketsRequested;
      public int field_131;

      PlayerUpdateTracker(PlayerEntity player) {
         this.player = player;
      }

      private UpdateData getMapUpdateData() {
         int i = this.startX;
         int j = this.startZ;
         int k = this.endX + 1 - this.startX;
         int l = this.endZ + 1 - this.startZ;
         byte[] bs = new byte[k * l];

         for(int m = 0; m < k; ++m) {
            for(int n = 0; n < l; ++n) {
               bs[m + n * k] = MapState.this.colors[i + m + (j + n) * 128];
            }
         }

         return new UpdateData(i, j, k, l, bs);
      }

      @Nullable
      Packet getPacket(int mapId) {
         UpdateData lv;
         if (this.dirty) {
            this.dirty = false;
            lv = this.getMapUpdateData();
         } else {
            lv = null;
         }

         Collection collection;
         if (this.iconsDirty && this.emptyPacketsRequested++ % 5 == 0) {
            this.iconsDirty = false;
            collection = MapState.this.icons.values();
         } else {
            collection = null;
         }

         return collection == null && lv == null ? null : new MapUpdateS2CPacket(mapId, MapState.this.scale, MapState.this.locked, collection, lv);
      }

      void markDirty(int startX, int startZ) {
         if (this.dirty) {
            this.startX = Math.min(this.startX, startX);
            this.startZ = Math.min(this.startZ, startZ);
            this.endX = Math.max(this.endX, startX);
            this.endZ = Math.max(this.endZ, startZ);
         } else {
            this.dirty = true;
            this.startX = startX;
            this.startZ = startZ;
            this.endX = startX;
            this.endZ = startZ;
         }

      }

      private void markIconsDirty() {
         this.iconsDirty = true;
      }
   }

   public static class UpdateData {
      public final int startX;
      public final int startZ;
      public final int width;
      public final int height;
      public final byte[] colors;

      public UpdateData(int startX, int startZ, int width, int height, byte[] colors) {
         this.startX = startX;
         this.startZ = startZ;
         this.width = width;
         this.height = height;
         this.colors = colors;
      }

      public void setColorsTo(MapState mapState) {
         for(int i = 0; i < this.width; ++i) {
            for(int j = 0; j < this.height; ++j) {
               mapState.setColor(this.startX + i, this.startZ + j, this.colors[i + j * this.width]);
            }
         }

      }
   }
}
