package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.packet.Packet;

public class PacketUnbundler extends MessageToMessageEncoder {
   private final NetworkSide side;

   public PacketUnbundler(NetworkSide side) {
      this.side = side;
   }

   protected void encode(ChannelHandlerContext channelHandlerContext, Packet arg, List list) throws Exception {
      PacketBundleHandler.BundlerGetter lv = (PacketBundleHandler.BundlerGetter)channelHandlerContext.channel().attr(PacketBundleHandler.KEY).get();
      if (lv == null) {
         throw new EncoderException("Bundler not configured: " + arg);
      } else {
         PacketBundleHandler var10000 = lv.getBundler(this.side);
         Objects.requireNonNull(list);
         var10000.forEachPacket(arg, list::add);
      }
   }

   // $FF: synthetic method
   protected void encode(ChannelHandlerContext context, Object packet, List packets) throws Exception {
      this.encode(context, (Packet)packet, packets);
   }
}
