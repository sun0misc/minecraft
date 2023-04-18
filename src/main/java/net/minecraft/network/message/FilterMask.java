package net.minecraft.network.message;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class FilterMask {
   public static final Codec CODEC = StringIdentifiable.createCodec(FilterStatus::values).dispatch(FilterMask::getStatus, FilterStatus::getCodec);
   public static final FilterMask FULLY_FILTERED;
   public static final FilterMask PASS_THROUGH;
   public static final Style FILTERED_STYLE;
   static final Codec PASS_THROUGH_CODEC;
   static final Codec FULLY_FILTERED_CODEC;
   static final Codec PARTIALLY_FILTERED_CODEC;
   private static final char FILTERED = '#';
   private final BitSet mask;
   private final FilterStatus status;

   private FilterMask(BitSet mask, FilterStatus status) {
      this.mask = mask;
      this.status = status;
   }

   private FilterMask(BitSet mask) {
      this.mask = mask;
      this.status = FilterMask.FilterStatus.PARTIALLY_FILTERED;
   }

   public FilterMask(int length) {
      this(new BitSet(length), FilterMask.FilterStatus.PARTIALLY_FILTERED);
   }

   private FilterStatus getStatus() {
      return this.status;
   }

   private BitSet getMask() {
      return this.mask;
   }

   public static FilterMask readMask(PacketByteBuf buf) {
      FilterStatus lv = (FilterStatus)buf.readEnumConstant(FilterStatus.class);
      FilterMask var10000;
      switch (lv) {
         case PASS_THROUGH:
            var10000 = PASS_THROUGH;
            break;
         case FULLY_FILTERED:
            var10000 = FULLY_FILTERED;
            break;
         case PARTIALLY_FILTERED:
            var10000 = new FilterMask(buf.readBitSet(), FilterMask.FilterStatus.PARTIALLY_FILTERED);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static void writeMask(PacketByteBuf buf, FilterMask mask) {
      buf.writeEnumConstant(mask.status);
      if (mask.status == FilterMask.FilterStatus.PARTIALLY_FILTERED) {
         buf.writeBitSet(mask.mask);
      }

   }

   public void markFiltered(int index) {
      this.mask.set(index);
   }

   @Nullable
   public String filter(String raw) {
      String var10000;
      switch (this.status) {
         case PASS_THROUGH:
            var10000 = raw;
            break;
         case FULLY_FILTERED:
            var10000 = null;
            break;
         case PARTIALLY_FILTERED:
            char[] cs = raw.toCharArray();

            for(int i = 0; i < cs.length && i < this.mask.length(); ++i) {
               if (this.mask.get(i)) {
                  cs[i] = '#';
               }
            }

            var10000 = new String(cs);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   @Nullable
   public Text getFilteredText(String message) {
      MutableText var10000;
      switch (this.status) {
         case PASS_THROUGH:
            var10000 = Text.literal(message);
            break;
         case FULLY_FILTERED:
            var10000 = null;
            break;
         case PARTIALLY_FILTERED:
            MutableText lv = Text.empty();
            int i = 0;
            boolean bl = this.mask.get(0);

            while(true) {
               int j = bl ? this.mask.nextClearBit(i) : this.mask.nextSetBit(i);
               j = j < 0 ? message.length() : j;
               if (j == i) {
                  var10000 = lv;
                  return var10000;
               }

               if (bl) {
                  lv.append((Text)Text.literal(StringUtils.repeat('#', j - i)).fillStyle(FILTERED_STYLE));
               } else {
                  lv.append(message.substring(i, j));
               }

               bl = !bl;
               i = j;
            }
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public boolean isPassThrough() {
      return this.status == FilterMask.FilterStatus.PASS_THROUGH;
   }

   public boolean isFullyFiltered() {
      return this.status == FilterMask.FilterStatus.FULLY_FILTERED;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FilterMask lv = (FilterMask)o;
         return this.mask.equals(lv.mask) && this.status == lv.status;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.mask.hashCode();
      i = 31 * i + this.status.hashCode();
      return i;
   }

   static {
      FULLY_FILTERED = new FilterMask(new BitSet(0), FilterMask.FilterStatus.FULLY_FILTERED);
      PASS_THROUGH = new FilterMask(new BitSet(0), FilterMask.FilterStatus.PASS_THROUGH);
      FILTERED_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.filtered")));
      PASS_THROUGH_CODEC = Codec.unit(PASS_THROUGH);
      FULLY_FILTERED_CODEC = Codec.unit(FULLY_FILTERED);
      PARTIALLY_FILTERED_CODEC = Codecs.BIT_SET.xmap(FilterMask::new, FilterMask::getMask);
   }

   static enum FilterStatus implements StringIdentifiable {
      PASS_THROUGH("pass_through", () -> {
         return FilterMask.PASS_THROUGH_CODEC;
      }),
      FULLY_FILTERED("fully_filtered", () -> {
         return FilterMask.FULLY_FILTERED_CODEC;
      }),
      PARTIALLY_FILTERED("partially_filtered", () -> {
         return FilterMask.PARTIALLY_FILTERED_CODEC;
      });

      private final String id;
      private final Supplier codecSupplier;

      private FilterStatus(String id, Supplier codecSupplier) {
         this.id = id;
         this.codecSupplier = codecSupplier;
      }

      public String asString() {
         return this.id;
      }

      private Codec getCodec() {
         return (Codec)this.codecSupplier.get();
      }

      // $FF: synthetic method
      private static FilterStatus[] method_45094() {
         return new FilterStatus[]{PASS_THROUGH, FULLY_FILTERED, PARTIALLY_FILTERED};
      }
   }
}
