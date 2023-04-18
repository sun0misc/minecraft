package net.minecraft.nbt.scanner;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtType;

public class SelectiveNbtCollector extends NbtCollector {
   private int queriesLeft;
   private final Set allPossibleTypes;
   private final Deque selectionStack = new ArrayDeque();

   public SelectiveNbtCollector(NbtScanQuery... queries) {
      this.queriesLeft = queries.length;
      ImmutableSet.Builder builder = ImmutableSet.builder();
      NbtTreeNode lv = NbtTreeNode.createRoot();
      NbtScanQuery[] var4 = queries;
      int var5 = queries.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         NbtScanQuery lv2 = var4[var6];
         lv.add(lv2);
         builder.add(lv2.type());
      }

      this.selectionStack.push(lv);
      builder.add(NbtCompound.TYPE);
      this.allPossibleTypes = builder.build();
   }

   public NbtScanner.Result start(NbtType rootType) {
      return rootType != NbtCompound.TYPE ? NbtScanner.Result.HALT : super.start(rootType);
   }

   public NbtScanner.NestedResult visitSubNbtType(NbtType type) {
      NbtTreeNode lv = (NbtTreeNode)this.selectionStack.element();
      if (this.getDepth() > lv.depth()) {
         return super.visitSubNbtType(type);
      } else if (this.queriesLeft <= 0) {
         return NbtScanner.NestedResult.HALT;
      } else {
         return !this.allPossibleTypes.contains(type) ? NbtScanner.NestedResult.SKIP : super.visitSubNbtType(type);
      }
   }

   public NbtScanner.NestedResult startSubNbt(NbtType type, String key) {
      NbtTreeNode lv = (NbtTreeNode)this.selectionStack.element();
      if (this.getDepth() > lv.depth()) {
         return super.startSubNbt(type, key);
      } else if (lv.selectedFields().remove(key, type)) {
         --this.queriesLeft;
         return super.startSubNbt(type, key);
      } else {
         if (type == NbtCompound.TYPE) {
            NbtTreeNode lv2 = (NbtTreeNode)lv.fieldsToRecurse().get(key);
            if (lv2 != null) {
               this.selectionStack.push(lv2);
               return super.startSubNbt(type, key);
            }
         }

         return NbtScanner.NestedResult.SKIP;
      }
   }

   public NbtScanner.Result endNested() {
      if (this.getDepth() == ((NbtTreeNode)this.selectionStack.element()).depth()) {
         this.selectionStack.pop();
      }

      return super.endNested();
   }

   public int getQueriesLeft() {
      return this.queriesLeft;
   }
}
