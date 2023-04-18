package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class PacketEncoder extends MessageToByteEncoder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final NetworkSide side;

   public PacketEncoder(NetworkSide side) {
      this.side = side;
   }

   protected void encode(ChannelHandlerContext channelHandlerContext, Packet arg, ByteBuf byteBuf) throws Exception {
      NetworkState lv = (NetworkState)channelHandlerContext.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get();
      if (lv == null) {
         throw new RuntimeException("ConnectionProtocol unknown: " + arg);
      } else {
         int i = lv.getPacketId(this.side, arg);
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(ClientConnection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", new Object[]{channelHandlerContext.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get(), i, arg.getClass().getName()});
         }

         if (i == -1) {
            throw new IOException("Can't serialize unregistered packet");
         } else {
            PacketByteBuf lv2 = new PacketByteBuf(byteBuf);
            lv2.writeVarInt(i);

            try {
               int j = lv2.writerIndex();
               arg.write(lv2);
               int k = lv2.writerIndex() - j;
               if (k > 8388608) {
                  throw new IllegalArgumentException("Packet too big (is " + k + ", should be less than 8388608): " + arg);
               } else {
                  int l = ((NetworkState)channelHandlerContext.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get()).getId();
                  FlightProfiler.INSTANCE.onPacketSent(l, i, channelHandlerContext.channel().remoteAddress(), k);
               }
            } catch (Throwable var10) {
               LOGGER.error("Error receiving packet {}", i, var10);
               if (arg.isWritingErrorSkippable()) {
                  throw new PacketEncoderException(var10);
               } else {
                  throw var10;
               }
            }
         }
      }
   }

   // $FF: synthetic method
   protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf out) throws Exception {
      this.encode(ctx, (Packet)packet, out);
   }
}
