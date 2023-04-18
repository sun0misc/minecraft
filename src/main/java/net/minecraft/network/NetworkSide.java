package net.minecraft.network;

public enum NetworkSide {
   SERVERBOUND,
   CLIENTBOUND;

   public NetworkSide getOpposite() {
      return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
   }

   // $FF: synthetic method
   private static NetworkSide[] method_36947() {
      return new NetworkSide[]{SERVERBOUND, CLIENTBOUND};
   }
}
