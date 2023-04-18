package net.minecraft.client.option;

import com.google.common.collect.ForwardingList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class HotbarStorageEntry extends ForwardingList {
   private final DefaultedList delegate;

   public HotbarStorageEntry() {
      this.delegate = DefaultedList.ofSize(PlayerInventory.getHotbarSize(), ItemStack.EMPTY);
   }

   protected List delegate() {
      return this.delegate;
   }

   public NbtList toNbtList() {
      NbtList lv = new NbtList();
      Iterator var2 = this.delegate().iterator();

      while(var2.hasNext()) {
         ItemStack lv2 = (ItemStack)var2.next();
         lv.add(lv2.writeNbt(new NbtCompound()));
      }

      return lv;
   }

   public void readNbtList(NbtList list) {
      List list = this.delegate();

      for(int i = 0; i < list.size(); ++i) {
         list.set(i, ItemStack.fromNbt(list.getCompound(i)));
      }

   }

   public boolean isEmpty() {
      Iterator var1 = this.delegate().iterator();

      ItemStack lv;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         lv = (ItemStack)var1.next();
      } while(lv.isEmpty());

      return false;
   }
}
