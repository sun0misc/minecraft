package net.minecraft.nbt.scanner;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtType;

public class ExclusiveNbtCollector extends NbtCollector {
   private final Deque treeStack = new ArrayDeque();

   public ExclusiveNbtCollector(NbtScanQuery... excludedQueries) {
      NbtTreeNode lv = NbtTreeNode.createRoot();
      NbtScanQuery[] var3 = excludedQueries;
      int var4 = excludedQueries.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         NbtScanQuery lv2 = var3[var5];
         lv.add(lv2);
      }

      this.treeStack.push(lv);
   }

   public NbtScanner.NestedResult startSubNbt(NbtType type, String key) {
      NbtTreeNode lv = (NbtTreeNode)this.treeStack.element();
      if (lv.isTypeEqual(type, key)) {
         return NbtScanner.NestedResult.SKIP;
      } else {
         if (type == NbtCompound.TYPE) {
            NbtTreeNode lv2 = (NbtTreeNode)lv.fieldsToRecurse().get(key);
            if (lv2 != null) {
               this.treeStack.push(lv2);
            }
         }

         return super.startSubNbt(type, key);
      }
   }

   public NbtScanner.Result endNested() {
      if (this.getDepth() == ((NbtTreeNode)this.treeStack.element()).depth()) {
         this.treeStack.pop();
      }

      return super.endNested();
   }
}
