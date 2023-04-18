package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BannerBlockEntity extends BlockEntity implements Nameable {
   public static final int MAX_PATTERN_COUNT = 6;
   public static final String PATTERNS_KEY = "Patterns";
   public static final String PATTERN_KEY = "Pattern";
   public static final String COLOR_KEY = "Color";
   @Nullable
   private Text customName;
   private DyeColor baseColor;
   @Nullable
   private NbtList patternListNbt;
   @Nullable
   private List patterns;

   public BannerBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.BANNER, pos, state);
      this.baseColor = ((AbstractBannerBlock)state.getBlock()).getColor();
   }

   public BannerBlockEntity(BlockPos pos, BlockState state, DyeColor baseColor) {
      this(pos, state);
      this.baseColor = baseColor;
   }

   @Nullable
   public static NbtList getPatternListNbt(ItemStack stack) {
      NbtList lv = null;
      NbtCompound lv2 = BlockItem.getBlockEntityNbt(stack);
      if (lv2 != null && lv2.contains("Patterns", NbtElement.LIST_TYPE)) {
         lv = lv2.getList("Patterns", NbtElement.COMPOUND_TYPE).copy();
      }

      return lv;
   }

   public void readFrom(ItemStack stack, DyeColor baseColor) {
      this.baseColor = baseColor;
      this.readFrom(stack);
   }

   public void readFrom(ItemStack stack) {
      this.patternListNbt = getPatternListNbt(stack);
      this.patterns = null;
      this.customName = stack.hasCustomName() ? stack.getName() : null;
   }

   public Text getName() {
      return (Text)(this.customName != null ? this.customName : Text.translatable("block.minecraft.banner"));
   }

   @Nullable
   public Text getCustomName() {
      return this.customName;
   }

   public void setCustomName(Text customName) {
      this.customName = customName;
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.patternListNbt != null) {
         nbt.put("Patterns", this.patternListNbt);
      }

      if (this.customName != null) {
         nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
      }

   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
         this.customName = Text.Serializer.fromJson(nbt.getString("CustomName"));
      }

      this.patternListNbt = nbt.getList("Patterns", NbtElement.COMPOUND_TYPE);
      this.patterns = null;
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public static int getPatternCount(ItemStack stack) {
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      return lv != null && lv.contains("Patterns") ? lv.getList("Patterns", NbtElement.COMPOUND_TYPE).size() : 0;
   }

   public List getPatterns() {
      if (this.patterns == null) {
         this.patterns = getPatternsFromNbt(this.baseColor, this.patternListNbt);
      }

      return this.patterns;
   }

   public static List getPatternsFromNbt(DyeColor baseColor, @Nullable NbtList patternListNbt) {
      List list = Lists.newArrayList();
      list.add(Pair.of(Registries.BANNER_PATTERN.entryOf(BannerPatterns.BASE), baseColor));
      if (patternListNbt != null) {
         for(int i = 0; i < patternListNbt.size(); ++i) {
            NbtCompound lv = patternListNbt.getCompound(i);
            RegistryEntry lv2 = BannerPattern.byId(lv.getString("Pattern"));
            if (lv2 != null) {
               int j = lv.getInt("Color");
               list.add(Pair.of(lv2, DyeColor.byId(j)));
            }
         }
      }

      return list;
   }

   public static void loadFromItemStack(ItemStack stack) {
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      if (lv != null && lv.contains("Patterns", NbtElement.LIST_TYPE)) {
         NbtList lv2 = lv.getList("Patterns", NbtElement.COMPOUND_TYPE);
         if (!lv2.isEmpty()) {
            lv2.remove(lv2.size() - 1);
            if (lv2.isEmpty()) {
               lv.remove("Patterns");
            }

            BlockItem.setBlockEntityNbt(stack, BlockEntityType.BANNER, lv);
         }
      }
   }

   public ItemStack getPickStack() {
      ItemStack lv = new ItemStack(BannerBlock.getForColor(this.baseColor));
      if (this.patternListNbt != null && !this.patternListNbt.isEmpty()) {
         NbtCompound lv2 = new NbtCompound();
         lv2.put("Patterns", this.patternListNbt.copy());
         BlockItem.setBlockEntityNbt(lv, this.getType(), lv2);
      }

      if (this.customName != null) {
         lv.setCustomName(this.customName);
      }

      return lv;
   }

   public DyeColor getColorForState() {
      return this.baseColor;
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
