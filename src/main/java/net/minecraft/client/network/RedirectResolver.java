/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import com.mojang.logging.LogUtils;
import java.util.Hashtable;
import java.util.Optional;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ServerAddress;
import org.slf4j.Logger;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface RedirectResolver {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final RedirectResolver INVALID = address -> Optional.empty();

    public Optional<ServerAddress> lookupRedirect(ServerAddress var1);

    public static RedirectResolver createSrv() {
        InitialDirContext dirContext;
        try {
            String string = "com.sun.jndi.dns.DnsContextFactory";
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            hashtable.put("java.naming.provider.url", "dns:");
            hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
            dirContext = new InitialDirContext(hashtable);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to initialize SRV redirect resolved, some servers might not work", throwable);
            return INVALID;
        }
        return address -> {
            if (address.getPort() == 25565) {
                try {
                    Attributes attributes = dirContext.getAttributes("_minecraft._tcp." + address.getAddress(), new String[]{"SRV"});
                    Attribute attribute = attributes.get("srv");
                    if (attribute != null) {
                        String[] strings = attribute.get().toString().split(" ", 4);
                        return Optional.of(new ServerAddress(strings[3], ServerAddress.portOrDefault(strings[2])));
                    }
                } catch (Throwable throwable) {
                    // empty catch block
                }
            }
            return Optional.empty();
        };
    }
}

