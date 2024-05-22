/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.chase;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ChaseCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChaseServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String ip;
    private final int port;
    private final PlayerManager playerManager;
    private final int interval;
    private volatile boolean running;
    @Nullable
    private ServerSocket socket;
    private final CopyOnWriteArrayList<Socket> clientSockets = new CopyOnWriteArrayList();

    public ChaseServer(String ip, int port, PlayerManager playerManager, int interval) {
        this.ip = ip;
        this.port = port;
        this.playerManager = playerManager;
        this.interval = interval;
    }

    public void start() throws IOException {
        if (this.socket != null && !this.socket.isClosed()) {
            LOGGER.warn("Remote control server was asked to start, but it is already running. Will ignore.");
            return;
        }
        this.running = true;
        this.socket = new ServerSocket(this.port, 50, InetAddress.getByName(this.ip));
        Thread thread = new Thread(this::runAcceptor, "chase-server-acceptor");
        thread.setDaemon(true);
        thread.start();
        Thread thread2 = new Thread(this::runSender, "chase-server-sender");
        thread2.setDaemon(true);
        thread2.start();
    }

    private void runSender() {
        TeleportPos lv = null;
        while (this.running) {
            if (!this.clientSockets.isEmpty()) {
                TeleportPos lv2 = this.getTeleportPosition();
                if (lv2 != null && !lv2.equals(lv)) {
                    lv = lv2;
                    byte[] bs = lv2.getTeleportCommand().getBytes(StandardCharsets.US_ASCII);
                    for (Socket socket : this.clientSockets) {
                        if (socket.isClosed()) continue;
                        Util.getIoWorkerExecutor().submit(() -> {
                            try {
                                OutputStream outputStream = socket.getOutputStream();
                                outputStream.write(bs);
                                outputStream.flush();
                            } catch (IOException iOException) {
                                LOGGER.info("Remote control client socket got an IO exception and will be closed", iOException);
                                IOUtils.closeQuietly(socket);
                            }
                        });
                    }
                }
                List list = this.clientSockets.stream().filter(Socket::isClosed).collect(Collectors.toList());
                this.clientSockets.removeAll(list);
            }
            if (!this.running) continue;
            try {
                Thread.sleep(this.interval);
            } catch (InterruptedException interruptedException) {}
        }
    }

    public void stop() {
        this.running = false;
        IOUtils.closeQuietly(this.socket);
        this.socket = null;
    }

    private void runAcceptor() {
        try {
            while (this.running) {
                if (this.socket == null) continue;
                LOGGER.info("Remote control server is listening for connections on port {}", (Object)this.port);
                Socket socket = this.socket.accept();
                LOGGER.info("Remote control server received client connection on port {}", (Object)socket.getPort());
                this.clientSockets.add(socket);
            }
        } catch (ClosedByInterruptException closedByInterruptException) {
            if (this.running) {
                LOGGER.info("Remote control server closed by interrupt");
            }
        } catch (IOException iOException) {
            if (this.running) {
                LOGGER.error("Remote control server closed because of an IO exception", iOException);
            }
        } finally {
            IOUtils.closeQuietly(this.socket);
        }
        LOGGER.info("Remote control server is now stopped");
        this.running = false;
    }

    @Nullable
    private TeleportPos getTeleportPosition() {
        List<ServerPlayerEntity> list = this.playerManager.getPlayerList();
        if (list.isEmpty()) {
            return null;
        }
        ServerPlayerEntity lv = list.get(0);
        String string = (String)ChaseCommand.DIMENSIONS.inverse().get(lv.getWorld().getRegistryKey());
        if (string == null) {
            return null;
        }
        return new TeleportPos(string, lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch());
    }

    record TeleportPos(String dimensionName, double x, double y, double z, float yaw, float pitch) {
        String getTeleportCommand() {
            return String.format(Locale.ROOT, "t %s %.2f %.2f %.2f %.2f %.2f\n", this.dimensionName, this.x, this.y, this.z, Float.valueOf(this.yaw), Float.valueOf(this.pitch));
        }
    }
}

