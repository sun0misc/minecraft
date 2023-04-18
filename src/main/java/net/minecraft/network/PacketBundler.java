package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public class PacketBundler extends MessageToMessageDecoder {
   @Nullable
   private PacketBundleHandler.Bundler currentBundler;
   @Nullable
   private PacketBundleHandler bundleHandler;
   private final NetworkSide side;

   public PacketBundler(NetworkSide side) {
      this.side = side;
   }

   protected void decode(ChannelHandlerContext channelHandlerContext, Packet arg, List list) throws Exception {
      PacketBundleHandler.BundlerGetter lv = (PacketBundleHandler.BundlerGetter)channelHandlerContext.channel().attr(PacketBundleHandler.KEY).get();
      if (lv == null) {
         throw new DecoderException("Bundler not configured: " + arg);
      } else {
         PacketBundleHandler lv2 = lv.getBundler(this.side);
         if (this.currentBundler != null) {
            if (this.bundleHandler != lv2) {
               throw new DecoderException("Bundler handler changed during bundling");
            }

            Packet lv3 = this.currentBundler.add(arg);
            if (lv3 != null) {
               this.bundleHandler = null;
               this.currentBundler = null;
               list.add(lv3);
            }
         } else {
            PacketBundleHandler.Bundler lv4 = lv2.createBundler(arg);
            if (lv4 != null) {
               this.currentBundler = lv4;
               this.bundleHandler = lv2;
            } else {
               list.add(arg);
            }
         }

      }
   }

   // $FF: synthetic method
   protected void decode(ChannelHandlerContext context, Object packet, List packets) throws Exception {
      this.decode(context, (Packet)packet, packets);
   }
}
