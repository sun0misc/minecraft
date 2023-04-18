package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;

public class SplitterHandler extends ByteToMessageDecoder {
   protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List objects) {
      buf.markReaderIndex();
      byte[] bs = new byte[3];

      for(int i = 0; i < bs.length; ++i) {
         if (!buf.isReadable()) {
            buf.resetReaderIndex();
            return;
         }

         bs[i] = buf.readByte();
         if (bs[i] >= 0) {
            PacketByteBuf lv = new PacketByteBuf(Unpooled.wrappedBuffer(bs));

            try {
               int j = lv.readVarInt();
               if (buf.readableBytes() >= j) {
                  objects.add(buf.readBytes(j));
                  return;
               }

               buf.resetReaderIndex();
            } finally {
               lv.release();
            }

            return;
         }
      }

      throw new CorruptedFrameException("length wider than 21-bit");
   }
}
