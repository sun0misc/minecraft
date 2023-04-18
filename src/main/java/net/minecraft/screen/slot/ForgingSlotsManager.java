package net.minecraft.screen.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ForgingSlotsManager {
   private final List inputSlots;
   private final ForgingSlot resultSlot;

   ForgingSlotsManager(List inputSlots, ForgingSlot resultSlot) {
      if (!inputSlots.isEmpty() && !resultSlot.equals(ForgingSlotsManager.ForgingSlot.DEFAULT)) {
         this.inputSlots = inputSlots;
         this.resultSlot = resultSlot;
      } else {
         throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
      }
   }

   public static Builder create() {
      return new Builder();
   }

   public boolean hasSlotIndex(int index) {
      return this.inputSlots.size() >= index;
   }

   public ForgingSlot getInputSlot(int index) {
      return (ForgingSlot)this.inputSlots.get(index);
   }

   public ForgingSlot getResultSlot() {
      return this.resultSlot;
   }

   public List getInputSlots() {
      return this.inputSlots;
   }

   public int getInputSlotCount() {
      return this.inputSlots.size();
   }

   public int getResultSlotIndex() {
      return this.getInputSlotCount();
   }

   public List getInputSlotIndices() {
      return (List)this.inputSlots.stream().map(ForgingSlot::slotId).collect(Collectors.toList());
   }

   public static record ForgingSlot(int slotId, int x, int y, Predicate mayPlace) {
      static final ForgingSlot DEFAULT = new ForgingSlot(0, 0, 0, (stack) -> {
         return true;
      });

      public ForgingSlot(int i, int j, int k, Predicate predicate) {
         this.slotId = i;
         this.x = j;
         this.y = k;
         this.mayPlace = predicate;
      }

      public int slotId() {
         return this.slotId;
      }

      public int x() {
         return this.x;
      }

      public int y() {
         return this.y;
      }

      public Predicate mayPlace() {
         return this.mayPlace;
      }
   }

   public static class Builder {
      private final List inputSlots = new ArrayList();
      private ForgingSlot resultSlot;

      public Builder() {
         this.resultSlot = ForgingSlotsManager.ForgingSlot.DEFAULT;
      }

      public Builder input(int slotId, int x, int y, Predicate mayPlace) {
         this.inputSlots.add(new ForgingSlot(slotId, x, y, mayPlace));
         return this;
      }

      public Builder output(int slotId, int x, int y) {
         this.resultSlot = new ForgingSlot(slotId, x, y, (stack) -> {
            return false;
         });
         return this;
      }

      public ForgingSlotsManager build() {
         return new ForgingSlotsManager(this.inputSlots, this.resultSlot);
      }
   }
}
