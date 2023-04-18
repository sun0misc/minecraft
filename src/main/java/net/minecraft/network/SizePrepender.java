package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class SizePrepender extends MessageToByteEncoder {
   private static final int MAX_PREPEND_LENGTH = 3;

   protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
      int i = byteBuf.readableBytes();
      int j = PacketByteBuf.getVarIntLength(i);
      if (j > 3) {
         throw new IllegalArgumentException("unable to fit " + i + " into 3");
      } else {
         PacketByteBuf lv = new PacketByteBuf(byteBuf2);
         lv.ensureWritable(j + i);
         lv.writeVarInt(i);
         lv.writeBytes(byteBuf, byteBuf.readerIndex(), i);
      }
   }

   // $FF: synthetic method
   protected void encode(ChannelHandlerContext ctx, Object input, ByteBuf output) throws Exception {
      this.encode(ctx, (ByteBuf)input, output);
   }
}
