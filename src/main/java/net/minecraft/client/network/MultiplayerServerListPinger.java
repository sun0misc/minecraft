package net.minecraft.client.network;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class MultiplayerServerListPinger {
   static final Splitter ZERO_SPLITTER = Splitter.on('\u0000').limit(6);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Text CANNOT_CONNECT_TEXT = Text.translatable("multiplayer.status.cannot_connect").styled((style) -> {
      return style.withColor(-65536);
   });
   private final List clientConnections = Collections.synchronizedList(Lists.newArrayList());

   public void add(final ServerInfo entry, final Runnable saver) throws UnknownHostException {
      ServerAddress lv = ServerAddress.parse(entry.address);
      Optional optional = AllowedAddressResolver.DEFAULT.resolve(lv).map(Address::getInetSocketAddress);
      if (!optional.isPresent()) {
         this.showError(ConnectScreen.BLOCKED_HOST_TEXT, entry);
      } else {
         final InetSocketAddress inetSocketAddress = (InetSocketAddress)optional.get();
         final ClientConnection lv2 = ClientConnection.connect(inetSocketAddress, false);
         this.clientConnections.add(lv2);
         entry.label = Text.translatable("multiplayer.status.pinging");
         entry.ping = -1L;
         entry.playerListSummary = Collections.emptyList();
         lv2.setPacketListener(new ClientQueryPacketListener() {
            private boolean sentQuery;
            private boolean received;
            private long startTime;

            public void onResponse(QueryResponseS2CPacket packet) {
               if (this.received) {
                  lv2.disconnect(Text.translatable("multiplayer.status.unrequested"));
               } else {
                  this.received = true;
                  ServerMetadata lv = packet.metadata();
                  entry.label = lv.description();
                  lv.version().ifPresentOrElse((version) -> {
                     entry.version = Text.literal(version.gameVersion());
                     entry.protocolVersion = version.protocolVersion();
                  }, () -> {
                     entry.version = Text.translatable("multiplayer.status.old");
                     entry.protocolVersion = 0;
                  });
                  lv.players().ifPresentOrElse((players) -> {
                     entry.playerCountLabel = MultiplayerServerListPinger.createPlayerCountText(players.online(), players.max());
                     entry.players = players;
                     if (!players.sample().isEmpty()) {
                        List list = new ArrayList(players.sample().size());
                        Iterator var3 = players.sample().iterator();

                        while(var3.hasNext()) {
                           GameProfile gameProfile = (GameProfile)var3.next();
                           list.add(Text.literal(gameProfile.getName()));
                        }

                        if (players.sample().size() < players.online()) {
                           list.add(Text.translatable("multiplayer.status.and_more", players.online() - players.sample().size()));
                        }

                        entry.playerListSummary = list;
                     } else {
                        entry.playerListSummary = List.of();
                     }

                  }, () -> {
                     entry.playerCountLabel = Text.translatable("multiplayer.status.unknown").formatted(Formatting.DARK_GRAY);
                  });
                  lv.favicon().ifPresent((favicon) -> {
                     if (!Arrays.equals(favicon.iconBytes(), entry.getFavicon())) {
                        entry.setFavicon(favicon.iconBytes());
                        saver.run();
                     }

                  });
                  this.startTime = Util.getMeasuringTimeMs();
                  lv2.send(new QueryPingC2SPacket(this.startTime));
                  this.sentQuery = true;
               }
            }

            public void onPong(QueryPongS2CPacket packet) {
               long l = this.startTime;
               long m = Util.getMeasuringTimeMs();
               entry.ping = m - l;
               lv2.disconnect(Text.translatable("multiplayer.status.finished"));
            }

            public void onDisconnected(Text reason) {
               if (!this.sentQuery) {
                  MultiplayerServerListPinger.this.showError(reason, entry);
                  MultiplayerServerListPinger.this.ping(inetSocketAddress, entry);
               }

            }

            public boolean isConnectionOpen() {
               return lv2.isOpen();
            }
         });

         try {
            lv2.send(new HandshakeC2SPacket(lv.getAddress(), lv.getPort(), NetworkState.STATUS));
            lv2.send(new QueryRequestC2SPacket());
         } catch (Throwable var8) {
            LOGGER.error("Failed to ping server {}", lv, var8);
         }

      }
   }

   void showError(Text error, ServerInfo info) {
      LOGGER.error("Can't ping {}: {}", info.address, error.getString());
      info.label = CANNOT_CONNECT_TEXT;
      info.playerCountLabel = ScreenTexts.EMPTY;
   }

   void ping(final InetSocketAddress address, final ServerInfo info) {
      ((Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).group((EventLoopGroup)ClientConnection.CLIENT_IO_GROUP.get())).handler(new ChannelInitializer() {
         protected void initChannel(Channel channel) {
            try {
               channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException var3) {
            }

            channel.pipeline().addLast(new ChannelHandler[]{new SimpleChannelInboundHandler() {
               public void channelActive(ChannelHandlerContext context) throws Exception {
                  super.channelActive(context);
                  ByteBuf byteBuf = Unpooled.buffer();

                  try {
                     byteBuf.writeByte(254);
                     byteBuf.writeByte(1);
                     byteBuf.writeByte(250);
                     char[] cs = "MC|PingHost".toCharArray();
                     byteBuf.writeShort(cs.length);
                     char[] var4 = cs;
                     int var5 = cs.length;

                     int var6;
                     char c;
                     for(var6 = 0; var6 < var5; ++var6) {
                        c = var4[var6];
                        byteBuf.writeChar(c);
                     }

                     byteBuf.writeShort(7 + 2 * address.getHostName().length());
                     byteBuf.writeByte(127);
                     cs = address.getHostName().toCharArray();
                     byteBuf.writeShort(cs.length);
                     var4 = cs;
                     var5 = cs.length;

                     for(var6 = 0; var6 < var5; ++var6) {
                        c = var4[var6];
                        byteBuf.writeChar(c);
                     }

                     byteBuf.writeInt(address.getPort());
                     context.channel().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                  } finally {
                     byteBuf.release();
                  }
               }

               protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
                  short s = byteBuf.readUnsignedByte();
                  if (s == 255) {
                     String string = new String(byteBuf.readBytes(byteBuf.readShort() * 2).array(), StandardCharsets.UTF_16BE);
                     String[] strings = (String[])Iterables.toArray(MultiplayerServerListPinger.ZERO_SPLITTER.split(string), String.class);
                     if ("§1".equals(strings[0])) {
                        int i = MathHelper.parseInt(strings[1], 0);
                        String string2 = strings[2];
                        String string3 = strings[3];
                        int j = MathHelper.parseInt(strings[4], -1);
                        int k = MathHelper.parseInt(strings[5], -1);
                        info.protocolVersion = -1;
                        info.version = Text.literal(string2);
                        info.label = Text.literal(string3);
                        info.playerCountLabel = MultiplayerServerListPinger.createPlayerCountText(j, k);
                        info.players = new ServerMetadata.Players(k, j, List.of());
                     }
                  }

                  channelHandlerContext.close();
               }

               public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) {
                  context.close();
               }

               // $FF: synthetic method
               protected void channelRead0(ChannelHandlerContext context, Object buf) throws Exception {
                  this.channelRead0(context, (ByteBuf)buf);
               }
            }});
         }
      })).channel(NioSocketChannel.class)).connect(address.getAddress(), address.getPort());
   }

   static Text createPlayerCountText(int current, int max) {
      return Text.literal(Integer.toString(current)).append((Text)Text.literal("/").formatted(Formatting.DARK_GRAY)).append(Integer.toString(max)).formatted(Formatting.GRAY);
   }

   public void tick() {
      synchronized(this.clientConnections) {
         Iterator iterator = this.clientConnections.iterator();

         while(iterator.hasNext()) {
            ClientConnection lv = (ClientConnection)iterator.next();
            if (lv.isOpen()) {
               lv.tick();
            } else {
               iterator.remove();
               lv.handleDisconnection();
            }
         }

      }
   }

   public void cancel() {
      synchronized(this.clientConnections) {
         Iterator iterator = this.clientConnections.iterator();

         while(iterator.hasNext()) {
            ClientConnection lv = (ClientConnection)iterator.next();
            if (lv.isOpen()) {
               iterator.remove();
               lv.disconnect(Text.translatable("multiplayer.status.cancelled"));
            }
         }

      }
   }
}
