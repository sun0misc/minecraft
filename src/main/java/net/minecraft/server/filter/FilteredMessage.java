package net.minecraft.server.filter;

import java.util.Objects;
import net.minecraft.network.message.FilterMask;
import org.jetbrains.annotations.Nullable;

public record FilteredMessage(String raw, FilterMask mask) {
   public static final FilteredMessage EMPTY = permitted("");

   public FilteredMessage(String string, FilterMask arg) {
      this.raw = string;
      this.mask = arg;
   }

   public static FilteredMessage permitted(String raw) {
      return new FilteredMessage(raw, FilterMask.PASS_THROUGH);
   }

   public static FilteredMessage censored(String raw) {
      return new FilteredMessage(raw, FilterMask.FULLY_FILTERED);
   }

   @Nullable
   public String filter() {
      return this.mask.filter(this.raw);
   }

   public String getString() {
      return (String)Objects.requireNonNullElse(this.filter(), "");
   }

   public boolean isFiltered() {
      return !this.mask.isPassThrough();
   }

   public String raw() {
      return this.raw;
   }

   public FilterMask mask() {
      return this.mask;
   }
}
