/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network;

import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.class_9812;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.PacketDecryptor;
import net.minecraft.network.encryption.PacketEncryptor;
import net.minecraft.network.handler.DecoderHandler;
import net.minecraft.network.handler.EncoderHandler;
import net.minecraft.network.handler.NetworkStateTransitions;
import net.minecraft.network.handler.NoopInboundHandler;
import net.minecraft.network.handler.NoopOutboundHandler;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.handler.PacketBundler;
import net.minecraft.network.handler.PacketDeflater;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.handler.PacketInflater;
import net.minecraft.network.handler.PacketSizeLogHandler;
import net.minecraft.network.handler.PacketSizeLogger;
import net.minecraft.network.handler.PacketUnbundler;
import net.minecraft.network.handler.SizePrepender;
import net.minecraft.network.handler.SplitterHandler;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.state.HandshakeStates;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.QueryStates;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ClientConnection
extends SimpleChannelInboundHandler<Packet<?>> {
    private static final float CURRENT_PACKET_COUNTER_WEIGHT = 0.75f;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker NETWORK_MARKER = MarkerFactory.getMarker("NETWORK");
    public static final Marker NETWORK_PACKETS_MARKER = Util.make(MarkerFactory.getMarker("NETWORK_PACKETS"), marker -> marker.add(NETWORK_MARKER));
    public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker("PACKET_RECEIVED"), marker -> marker.add(NETWORK_PACKETS_MARKER));
    public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker("PACKET_SENT"), marker -> marker.add(NETWORK_PACKETS_MARKER));
    public static final Supplier<NioEventLoopGroup> CLIENT_IO_GROUP = Suppliers.memoize(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build()));
    public static final Supplier<EpollEventLoopGroup> EPOLL_CLIENT_IO_GROUP = Suppliers.memoize(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build()));
    public static final Supplier<DefaultEventLoopGroup> LOCAL_CLIENT_IO_GROUP = Suppliers.memoize(() -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build()));
    private static final NetworkState<ServerHandshakePacketListener> C2S_HANDSHAKE_STATE = HandshakeStates.C2S;
    private final NetworkSide side;
    private volatile boolean duringLogin = true;
    private final Queue<Consumer<ClientConnection>> queuedTasks = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    @Nullable
    private volatile PacketListener prePlayStateListener;
    @Nullable
    private volatile PacketListener packetListener;
    @Nullable
    private class_9812 field_52180;
    private boolean encrypted;
    private boolean disconnected;
    private int packetsReceivedCounter;
    private int packetsSentCounter;
    private float averagePacketsReceived;
    private float averagePacketsSent;
    private int ticks;
    private boolean errored;
    @Nullable
    private volatile class_9812 pendingDisconnectionReason;
    @Nullable
    PacketSizeLogger packetSizeLogger;

    public ClientConnection(NetworkSide side) {
        this.side = side;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        this.channel = context.channel();
        this.address = this.channel.remoteAddress();
        if (this.pendingDisconnectionReason != null) {
            this.method_60924(this.pendingDisconnectionReason);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        this.disconnect(Text.translatable("disconnect.endOfStream"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable ex) {
        if (ex instanceof PacketEncoderException) {
            LOGGER.debug("Skipping packet due to errors", ex.getCause());
            return;
        }
        boolean bl = !this.errored;
        this.errored = true;
        if (!this.channel.isOpen()) {
            return;
        }
        if (ex instanceof TimeoutException) {
            LOGGER.debug("Timeout", ex);
            this.disconnect(Text.translatable("disconnect.timeout"));
        } else {
            MutableText lv = Text.translatable("disconnect.genericReason", "Internal Exception: " + String.valueOf(ex));
            PacketListener lv2 = this.packetListener;
            class_9812 lv3 = lv2 != null ? lv2.method_60881(lv, ex) : new class_9812(lv);
            if (bl) {
                LOGGER.debug("Failed to sent packet", ex);
                if (this.getOppositeSide() == NetworkSide.CLIENTBOUND) {
                    Packet<ClientLoginPacketListener> lv4 = this.duringLogin ? new LoginDisconnectS2CPacket(lv) : new DisconnectS2CPacket(lv);
                    this.send(lv4, PacketCallbacks.always(() -> this.method_60924(lv3)));
                } else {
                    this.method_60924(lv3);
                }
                this.tryDisableAutoRead();
            } else {
                LOGGER.debug("Double fault", ex);
                this.method_60924(lv3);
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> arg) {
        if (!this.channel.isOpen()) {
            return;
        }
        PacketListener lv = this.packetListener;
        if (lv == null) {
            throw new IllegalStateException("Received a packet before the packet listener was initialized");
        }
        if (lv.accepts(arg)) {
            try {
                ClientConnection.handlePacket(arg, lv);
            } catch (OffThreadException offThreadException) {
            } catch (RejectedExecutionException rejectedExecutionException) {
                this.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
            } catch (ClassCastException classCastException) {
                LOGGER.error("Received {} that couldn't be processed", (Object)arg.getClass(), (Object)classCastException);
                this.disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
            }
            ++this.packetsReceivedCounter;
        }
    }

    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
        packet.apply(listener);
    }

    private void setPacketListener(NetworkState<?> state, PacketListener listener) {
        Validate.notNull(listener, "packetListener", new Object[0]);
        NetworkSide lv = listener.getSide();
        if (lv != this.side) {
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + String.valueOf((Object)this.side) + ", but listener is " + String.valueOf((Object)lv));
        }
        NetworkPhase lv2 = listener.getPhase();
        if (state.id() != lv2) {
            throw new IllegalStateException("Listener protocol (" + String.valueOf((Object)lv2) + ") does not match requested one " + String.valueOf(state));
        }
    }

    private static void syncUninterruptibly(ChannelFuture future) {
        try {
            future.syncUninterruptibly();
        } catch (Exception exception) {
            if (exception instanceof ClosedChannelException) {
                LOGGER.info("Connection closed during protocol change");
                return;
            }
            throw exception;
        }
    }

    public <T extends PacketListener> void transitionInbound(NetworkState<T> state, T packetListener) {
        this.setPacketListener(state, packetListener);
        if (state.side() != this.getSide()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf((Object)state.id()));
        }
        this.packetListener = packetListener;
        this.prePlayStateListener = null;
        NetworkStateTransitions.DecoderTransitioner lv = NetworkStateTransitions.decoderTransitioner(state);
        PacketBundleHandler lv2 = state.bundleHandler();
        if (lv2 != null) {
            PacketBundler lv3 = new PacketBundler(lv2);
            lv = lv.andThen(context -> context.pipeline().addAfter("decoder", "bundler", lv3));
        }
        ClientConnection.syncUninterruptibly(this.channel.writeAndFlush(lv));
    }

    public void transitionOutbound(NetworkState<?> newState) {
        if (newState.side() != this.getOppositeSide()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf((Object)newState.id()));
        }
        NetworkStateTransitions.EncoderTransitioner lv = NetworkStateTransitions.encoderTransitioner(newState);
        PacketBundleHandler lv2 = newState.bundleHandler();
        if (lv2 != null) {
            PacketUnbundler lv3 = new PacketUnbundler(lv2);
            lv = lv.andThen(context -> context.pipeline().addAfter("encoder", "unbundler", lv3));
        }
        boolean bl = newState.id() == NetworkPhase.LOGIN;
        ClientConnection.syncUninterruptibly(this.channel.writeAndFlush(lv.andThen(context -> {
            this.duringLogin = bl;
        })));
    }

    public void setInitialPacketListener(PacketListener packetListener) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        }
        if (this.side != NetworkSide.SERVERBOUND || packetListener.getSide() != NetworkSide.SERVERBOUND || packetListener.getPhase() != C2S_HANDSHAKE_STATE.id()) {
            throw new IllegalStateException("Invalid initial listener");
        }
        this.packetListener = packetListener;
    }

    public void connect(String address, int port, ClientQueryPacketListener listener) {
        this.connect(address, port, QueryStates.C2S, QueryStates.S2C, listener, ConnectionIntent.STATUS);
    }

    public void connect(String address, int port, ClientLoginPacketListener listener) {
        this.connect(address, port, LoginStates.C2S, LoginStates.S2C, listener, ConnectionIntent.LOGIN);
    }

    public <S extends ServerPacketListener, C extends ClientPacketListener> void connect(String address, int port, NetworkState<S> outboundState, NetworkState<C> inboundState, C prePlayStateListener, boolean transfer) {
        this.connect(address, port, outboundState, inboundState, prePlayStateListener, transfer ? ConnectionIntent.TRANSFER : ConnectionIntent.LOGIN);
    }

    private <S extends ServerPacketListener, C extends ClientPacketListener> void connect(String address, int port, NetworkState<S> outboundState, NetworkState<C> inboundState, C prePlayStateListener, ConnectionIntent intent) {
        if (outboundState.id() != inboundState.id()) {
            throw new IllegalStateException("Mismatched initial protocols");
        }
        this.prePlayStateListener = prePlayStateListener;
        this.submit(connection -> {
            this.transitionInbound(inboundState, prePlayStateListener);
            connection.sendImmediately(new HandshakeC2SPacket(SharedConstants.getGameVersion().getProtocolVersion(), address, port, intent), null, true);
            this.transitionOutbound(outboundState);
        });
    }

    public void send(Packet<?> packet) {
        this.send(packet, null);
    }

    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
        this.send(packet, callbacks, true);
    }

    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush) {
        if (this.isOpen()) {
            this.handleQueuedTasks();
            this.sendImmediately(packet, callbacks, flush);
        } else {
            this.queuedTasks.add(connection -> connection.sendImmediately(packet, callbacks, flush));
        }
    }

    public void submit(Consumer<ClientConnection> task) {
        if (this.isOpen()) {
            this.handleQueuedTasks();
            task.accept(this);
        } else {
            this.queuedTasks.add(task);
        }
    }

    private void sendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush) {
        ++this.packetsSentCounter;
        if (this.channel.eventLoop().inEventLoop()) {
            this.sendInternal(packet, callbacks, flush);
        } else {
            this.channel.eventLoop().execute(() -> this.sendInternal(packet, callbacks, flush));
        }
    }

    private void sendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush) {
        ChannelFuture channelFuture;
        ChannelFuture channelFuture2 = channelFuture = flush ? this.channel.writeAndFlush(packet) : this.channel.write(packet);
        if (callbacks != null) {
            channelFuture.addListener((GenericFutureListener<? extends Future<? super Void>>)((GenericFutureListener<Future>)future -> {
                if (future.isSuccess()) {
                    callbacks.onSuccess();
                } else {
                    Packet<?> lv = callbacks.getFailurePacket();
                    if (lv != null) {
                        ChannelFuture channelFuture = this.channel.writeAndFlush(lv);
                        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                }
            }));
        }
        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void flush() {
        if (this.isOpen()) {
            this.flushInternal();
        } else {
            this.queuedTasks.add(ClientConnection::flushInternal);
        }
    }

    private void flushInternal() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> this.channel.flush());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleQueuedTasks() {
        if (this.channel == null || !this.channel.isOpen()) {
            return;
        }
        Queue<Consumer<ClientConnection>> queue = this.queuedTasks;
        synchronized (queue) {
            Consumer<ClientConnection> consumer;
            while ((consumer = this.queuedTasks.poll()) != null) {
                consumer.accept(this);
            }
        }
    }

    public void tick() {
        this.handleQueuedTasks();
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof TickablePacketListener) {
            TickablePacketListener lv = (TickablePacketListener)packetListener;
            lv.tick();
        }
        if (!this.isOpen() && !this.disconnected) {
            this.handleDisconnection();
        }
        if (this.channel != null) {
            this.channel.flush();
        }
        if (this.ticks++ % 20 == 0) {
            this.updateStats();
        }
        if (this.packetSizeLogger != null) {
            this.packetSizeLogger.push();
        }
    }

    protected void updateStats() {
        this.averagePacketsSent = MathHelper.lerp(0.75f, (float)this.packetsSentCounter, this.averagePacketsSent);
        this.averagePacketsReceived = MathHelper.lerp(0.75f, (float)this.packetsReceivedCounter, this.averagePacketsReceived);
        this.packetsSentCounter = 0;
        this.packetsReceivedCounter = 0;
    }

    public SocketAddress getAddress() {
        return this.address;
    }

    public String getAddressAsString(boolean logIps) {
        if (this.address == null) {
            return "local";
        }
        if (logIps) {
            return this.address.toString();
        }
        return "IP hidden";
    }

    public void disconnect(Text disconnectReason) {
        this.method_60924(new class_9812(disconnectReason));
    }

    public void method_60924(class_9812 arg) {
        if (this.channel == null) {
            this.pendingDisconnectionReason = arg;
        }
        if (this.isOpen()) {
            this.channel.close().awaitUninterruptibly();
            this.field_52180 = arg;
        }
    }

    public boolean isLocal() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public NetworkSide getSide() {
        return this.side;
    }

    public NetworkSide getOppositeSide() {
        return this.side.getOpposite();
    }

    public static ClientConnection connect(InetSocketAddress address, boolean useEpoll, @Nullable MultiValueDebugSampleLogImpl packetSizeLog) {
        ClientConnection lv = new ClientConnection(NetworkSide.CLIENTBOUND);
        if (packetSizeLog != null) {
            lv.resetPacketSizeLog(packetSizeLog);
        }
        ChannelFuture channelFuture = ClientConnection.connect(address, useEpoll, lv);
        channelFuture.syncUninterruptibly();
        return lv;
    }

    public static ChannelFuture connect(InetSocketAddress address, boolean useEpoll, final ClientConnection connection) {
        EventLoopGroup eventLoopGroup;
        Class class_;
        if (Epoll.isAvailable() && useEpoll) {
            class_ = EpollSocketChannel.class;
            eventLoopGroup = EPOLL_CLIENT_IO_GROUP.get();
        } else {
            class_ = NioSocketChannel.class;
            eventLoopGroup = CLIENT_IO_GROUP.get();
        }
        return ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(eventLoopGroup)).handler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                ClientConnection.addHandlers(channelPipeline, NetworkSide.CLIENTBOUND, false, connection.packetSizeLogger);
                connection.addFlowControlHandler(channelPipeline);
            }
        })).channel(class_)).connect(address.getAddress(), address.getPort());
    }

    private static String getOutboundHandlerName(boolean sendingSide) {
        return sendingSide ? "encoder" : "outbound_config";
    }

    private static String getInboundHandlerName(boolean receivingSide) {
        return receivingSide ? "decoder" : "inbound_config";
    }

    public void addFlowControlHandler(ChannelPipeline pipeline) {
        pipeline.addLast("hackfix", (ChannelHandler)new ChannelOutboundHandlerAdapter(this){

            @Override
            public void write(ChannelHandlerContext context, Object value, ChannelPromise promise) throws Exception {
                super.write(context, value, promise);
            }
        }).addLast("packet_handler", (ChannelHandler)this);
    }

    public static void addHandlers(ChannelPipeline pipeline, NetworkSide side, boolean local, @Nullable PacketSizeLogger packetSizeLogger) {
        NetworkSide lv = side.getOpposite();
        boolean bl2 = side == NetworkSide.SERVERBOUND;
        boolean bl3 = lv == NetworkSide.SERVERBOUND;
        pipeline.addLast("splitter", (ChannelHandler)ClientConnection.getSplitter(packetSizeLogger, local)).addLast(new FlowControlHandler()).addLast(ClientConnection.getInboundHandlerName(bl2), bl2 ? new DecoderHandler<ServerHandshakePacketListener>(C2S_HANDSHAKE_STATE) : new NetworkStateTransitions.InboundConfigurer()).addLast("prepender", (ChannelHandler)ClientConnection.getPrepender(local)).addLast(ClientConnection.getOutboundHandlerName(bl3), bl3 ? new EncoderHandler<ServerHandshakePacketListener>(C2S_HANDSHAKE_STATE) : new NetworkStateTransitions.OutboundConfigurer());
    }

    private static ChannelOutboundHandler getPrepender(boolean local) {
        return local ? new NoopOutboundHandler() : new SizePrepender();
    }

    private static ChannelInboundHandler getSplitter(@Nullable PacketSizeLogger packetSizeLogger, boolean local) {
        if (!local) {
            return new SplitterHandler(packetSizeLogger);
        }
        if (packetSizeLogger != null) {
            return new PacketSizeLogHandler(packetSizeLogger);
        }
        return new NoopInboundHandler();
    }

    public static void addLocalValidator(ChannelPipeline pipeline, NetworkSide side) {
        ClientConnection.addHandlers(pipeline, side, true, null);
    }

    public static ClientConnection connectLocal(SocketAddress address) {
        final ClientConnection lv = new ClientConnection(NetworkSide.CLIENTBOUND);
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(LOCAL_CLIENT_IO_GROUP.get())).handler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) {
                ChannelPipeline channelPipeline = channel.pipeline();
                ClientConnection.addLocalValidator(channelPipeline, NetworkSide.CLIENTBOUND);
                lv.addFlowControlHandler(channelPipeline);
            }
        })).channel(LocalChannel.class)).connect(address).syncUninterruptibly();
        return lv;
    }

    public void setupEncryption(Cipher decryptionCipher, Cipher encryptionCipher) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new PacketDecryptor(decryptionCipher));
        this.channel.pipeline().addBefore("prepender", "encrypt", new PacketEncryptor(encryptionCipher));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isChannelAbsent() {
        return this.channel == null;
    }

    @Nullable
    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Nullable
    public class_9812 method_60926() {
        return this.field_52180;
    }

    public void tryDisableAutoRead() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }
    }

    public void setCompressionThreshold(int compressionThreshold, boolean rejectsBadPackets) {
        if (compressionThreshold >= 0) {
            ChannelHandler channelHandler = this.channel.pipeline().get("decompress");
            if (channelHandler instanceof PacketInflater) {
                PacketInflater lv = (PacketInflater)channelHandler;
                lv.setCompressionThreshold(compressionThreshold, rejectsBadPackets);
            } else {
                this.channel.pipeline().addAfter("splitter", "decompress", new PacketInflater(compressionThreshold, rejectsBadPackets));
            }
            channelHandler = this.channel.pipeline().get("compress");
            if (channelHandler instanceof PacketDeflater) {
                PacketDeflater lv2 = (PacketDeflater)channelHandler;
                lv2.setCompressionThreshold(compressionThreshold);
            } else {
                this.channel.pipeline().addAfter("prepender", "compress", new PacketDeflater(compressionThreshold));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof PacketInflater) {
                this.channel.pipeline().remove("decompress");
            }
            if (this.channel.pipeline().get("compress") instanceof PacketDeflater) {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void handleDisconnection() {
        PacketListener lv2;
        if (this.channel == null || this.channel.isOpen()) {
            return;
        }
        if (this.disconnected) {
            LOGGER.warn("handleDisconnection() called twice");
            return;
        }
        this.disconnected = true;
        PacketListener lv = this.getPacketListener();
        PacketListener packetListener = lv2 = lv != null ? lv : this.prePlayStateListener;
        if (lv2 != null) {
            class_9812 lv3 = Objects.requireNonNullElseGet(this.method_60926(), () -> new class_9812(Text.translatable("multiplayer.disconnect.generic")));
            lv2.onDisconnected(lv3);
        }
    }

    public float getAveragePacketsReceived() {
        return this.averagePacketsReceived;
    }

    public float getAveragePacketsSent() {
        return this.averagePacketsSent;
    }

    public void resetPacketSizeLog(MultiValueDebugSampleLogImpl log) {
        this.packetSizeLogger = new PacketSizeLogger(log);
    }

    @Override
    protected /* synthetic */ void channelRead0(ChannelHandlerContext context, Object packet) throws Exception {
        this.channelRead0(context, (Packet)packet);
    }
}

