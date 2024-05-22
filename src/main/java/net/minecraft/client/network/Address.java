/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import java.net.InetSocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface Address {
    public String getHostName();

    public String getHostAddress();

    public int getPort();

    public InetSocketAddress getInetSocketAddress();

    public static Address create(final InetSocketAddress address) {
        return new Address(){

            @Override
            public String getHostName() {
                return address.getAddress().getHostName();
            }

            @Override
            public String getHostAddress() {
                return address.getAddress().getHostAddress();
            }

            @Override
            public int getPort() {
                return address.getPort();
            }

            @Override
            public InetSocketAddress getInetSocketAddress() {
                return address;
            }
        };
    }
}

