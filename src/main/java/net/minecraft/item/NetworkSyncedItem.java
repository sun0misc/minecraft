package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NetworkSyncedItem extends Item {
   public NetworkSyncedItem(Item.Settings arg) {
      super(arg);
   }

   public boolean isNetworkSynced() {
      return true;
   }

   @Nullable
   public Packet createSyncPacket(ItemStack stack, World world, PlayerEntity player) {
      return null;
   }
}
