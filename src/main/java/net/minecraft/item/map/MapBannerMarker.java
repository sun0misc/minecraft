package net.minecraft.item.map;

import java.util.Objects;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class MapBannerMarker {
   private final BlockPos pos;
   private final DyeColor color;
   @Nullable
   private final Text name;

   public MapBannerMarker(BlockPos pos, DyeColor dyeColor, @Nullable Text name) {
      this.pos = pos;
      this.color = dyeColor;
      this.name = name;
   }

   public static MapBannerMarker fromNbt(NbtCompound nbt) {
      BlockPos lv = NbtHelper.toBlockPos(nbt.getCompound("Pos"));
      DyeColor lv2 = DyeColor.byName(nbt.getString("Color"), DyeColor.WHITE);
      Text lv3 = nbt.contains("Name") ? Text.Serializer.fromJson(nbt.getString("Name")) : null;
      return new MapBannerMarker(lv, lv2, lv3);
   }

   @Nullable
   public static MapBannerMarker fromWorldBlock(BlockView blockView, BlockPos blockPos) {
      BlockEntity lv = blockView.getBlockEntity(blockPos);
      if (lv instanceof BannerBlockEntity lv2) {
         DyeColor lv3 = lv2.getColorForState();
         Text lv4 = lv2.hasCustomName() ? lv2.getCustomName() : null;
         return new MapBannerMarker(blockPos, lv3, lv4);
      } else {
         return null;
      }
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public MapIcon.Type getIconType() {
      switch (this.color) {
         case WHITE:
            return MapIcon.Type.BANNER_WHITE;
         case ORANGE:
            return MapIcon.Type.BANNER_ORANGE;
         case MAGENTA:
            return MapIcon.Type.BANNER_MAGENTA;
         case LIGHT_BLUE:
            return MapIcon.Type.BANNER_LIGHT_BLUE;
         case YELLOW:
            return MapIcon.Type.BANNER_YELLOW;
         case LIME:
            return MapIcon.Type.BANNER_LIME;
         case PINK:
            return MapIcon.Type.BANNER_PINK;
         case GRAY:
            return MapIcon.Type.BANNER_GRAY;
         case LIGHT_GRAY:
            return MapIcon.Type.BANNER_LIGHT_GRAY;
         case CYAN:
            return MapIcon.Type.BANNER_CYAN;
         case PURPLE:
            return MapIcon.Type.BANNER_PURPLE;
         case BLUE:
            return MapIcon.Type.BANNER_BLUE;
         case BROWN:
            return MapIcon.Type.BANNER_BROWN;
         case GREEN:
            return MapIcon.Type.BANNER_GREEN;
         case RED:
            return MapIcon.Type.BANNER_RED;
         case BLACK:
         default:
            return MapIcon.Type.BANNER_BLACK;
      }
   }

   @Nullable
   public Text getName() {
      return this.name;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MapBannerMarker lv = (MapBannerMarker)o;
         return Objects.equals(this.pos, lv.pos) && this.color == lv.color && Objects.equals(this.name, lv.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.pos, this.color, this.name});
   }

   public NbtCompound getNbt() {
      NbtCompound lv = new NbtCompound();
      lv.put("Pos", NbtHelper.fromBlockPos(this.pos));
      lv.putString("Color", this.color.getName());
      if (this.name != null) {
         lv.putString("Name", Text.Serializer.toJson(this.name));
      }

      return lv;
   }

   public String getKey() {
      int var10000 = this.pos.getX();
      return "banner-" + var10000 + "," + this.pos.getY() + "," + this.pos.getZ();
   }
}
