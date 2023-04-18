package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class DecoderHandler extends ByteToMessageDecoder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final NetworkSide side;

   public DecoderHandler(NetworkSide side) {
      this.side = side;
   }

   protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List objects) throws Exception {
      int i = buf.readableBytes();
      if (i != 0) {
         PacketByteBuf lv = new PacketByteBuf(buf);
         int j = lv.readVarInt();
         Packet lv2 = ((NetworkState)ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get()).getPacketHandler(this.side, j, lv);
         if (lv2 == null) {
            throw new IOException("Bad packet id " + j);
         } else {
            int k = ((NetworkState)ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get()).getId();
            FlightProfiler.INSTANCE.onPacketReceived(k, j, ctx.channel().remoteAddress(), i);
            if (lv.readableBytes() > 0) {
               int var10002 = ((NetworkState)ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get()).getId();
               throw new IOException("Packet " + var10002 + "/" + j + " (" + lv2.getClass().getSimpleName() + ") was larger than I expected, found " + lv.readableBytes() + " bytes extra whilst reading packet " + j);
            } else {
               objects.add(lv2);
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug(ClientConnection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", new Object[]{ctx.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get(), j, lv2.getClass().getName()});
               }

            }
         }
      }
   }
}
