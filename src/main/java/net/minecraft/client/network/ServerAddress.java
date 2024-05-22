/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.google.common.net.HostAndPort;
import com.mojang.logging.LogUtils;
import java.net.IDN;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class ServerAddress {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HostAndPort hostAndPort;
    private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

    public ServerAddress(String host, int port) {
        this(HostAndPort.fromParts(host, port));
    }

    private ServerAddress(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public String getAddress() {
        try {
            return IDN.toASCII(this.hostAndPort.getHost());
        } catch (IllegalArgumentException illegalArgumentException) {
            return "";
        }
    }

    public int getPort() {
        return this.hostAndPort.getPort();
    }

    public static ServerAddress parse(String address) {
        if (address == null) {
            return INVALID;
        }
        try {
            HostAndPort hostAndPort = HostAndPort.fromString(address).withDefaultPort(25565);
            if (hostAndPort.getHost().isEmpty()) {
                return INVALID;
            }
            return new ServerAddress(hostAndPort);
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.info("Failed to parse URL {}", (Object)address, (Object)illegalArgumentException);
            return INVALID;
        }
    }

    public static boolean isValid(String address) {
        try {
            HostAndPort hostAndPort = HostAndPort.fromString(address);
            String string2 = hostAndPort.getHost();
            if (!string2.isEmpty()) {
                IDN.toASCII(string2);
                return true;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return false;
    }

    static int portOrDefault(String port) {
        try {
            return Integer.parseInt(port.trim());
        } catch (Exception exception) {
            return 25565;
        }
    }

    public String toString() {
        return this.hostAndPort.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ServerAddress) {
            return this.hostAndPort.equals(((ServerAddress)o).hostAndPort);
        }
        return false;
    }

    public int hashCode() {
        return this.hostAndPort.hashCode();
    }
}

