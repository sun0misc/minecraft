/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.ForcedUsernameChangeException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9782;
import net.minecraft.class_9812;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.login.EnterConfigurationC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientLoginNetworkHandler
implements ClientLoginPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftClient client;
    @Nullable
    private final ServerInfo serverInfo;
    @Nullable
    private final Screen parentScreen;
    private final Consumer<Text> statusConsumer;
    private final ClientConnection connection;
    private final boolean newWorld;
    @Nullable
    private final Duration worldLoadTime;
    @Nullable
    private String minigameName;
    private final Map<Identifier, byte[]> serverCookies;
    private final boolean hasCookies;
    private final AtomicReference<State> state = new AtomicReference<State>(State.CONNECTING);

    public ClientLoginNetworkHandler(ClientConnection connection, MinecraftClient client, @Nullable ServerInfo serverInfo, @Nullable Screen parentScreen, boolean newWorld, @Nullable Duration worldLoadTime, Consumer<Text> statusConsumer, @Nullable CookieStorage cookieStorage) {
        this.connection = connection;
        this.client = client;
        this.serverInfo = serverInfo;
        this.parentScreen = parentScreen;
        this.statusConsumer = statusConsumer;
        this.newWorld = newWorld;
        this.worldLoadTime = worldLoadTime;
        this.serverCookies = cookieStorage != null ? new HashMap<Identifier, byte[]>(cookieStorage.cookies()) : new HashMap();
        this.hasCookies = cookieStorage != null;
    }

    private void switchTo(State state) {
        State lv = this.state.updateAndGet(currentState -> {
            if (!arg.prevStates.contains(currentState)) {
                throw new IllegalStateException("Tried to switch to " + String.valueOf((Object)state) + " from " + String.valueOf(currentState) + ", but expected one of " + String.valueOf(arg.prevStates));
            }
            return state;
        });
        this.statusConsumer.accept(lv.name);
    }

    @Override
    public void onHello(LoginHelloS2CPacket packet) {
        LoginKeyC2SPacket lv;
        Cipher cipher2;
        Cipher cipher;
        String string;
        this.switchTo(State.AUTHORIZING);
        try {
            SecretKey secretKey = NetworkEncryptionUtils.generateSecretKey();
            PublicKey publicKey = packet.getPublicKey();
            string = new BigInteger(NetworkEncryptionUtils.computeServerId(packet.getServerId(), publicKey, secretKey)).toString(16);
            cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
            cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
            byte[] bs = packet.getNonce();
            lv = new LoginKeyC2SPacket(secretKey, publicKey, bs);
        } catch (Exception exception) {
            throw new IllegalStateException("Protocol error", exception);
        }
        if (packet.needsAuthentication()) {
            Util.getIoWorkerExecutor().submit(() -> {
                Text lv = this.joinServerSession(string);
                if (lv != null) {
                    if (this.serverInfo != null && this.serverInfo.isLocal()) {
                        LOGGER.warn(lv.getString());
                    } else {
                        this.connection.disconnect(lv);
                        return;
                    }
                }
                this.setupEncryption(lv, cipher, cipher2);
            });
        } else {
            this.setupEncryption(lv, cipher, cipher2);
        }
    }

    private void setupEncryption(LoginKeyC2SPacket keyPacket, Cipher decryptionCipher, Cipher encryptionCipher) {
        this.switchTo(State.ENCRYPTING);
        this.connection.send(keyPacket, PacketCallbacks.always(() -> this.connection.setupEncryption(decryptionCipher, encryptionCipher)));
    }

    @Nullable
    private Text joinServerSession(String serverId) {
        try {
            this.getSessionService().joinServer(this.client.getSession().getUuidOrNull(), this.client.getSession().getAccessToken(), serverId);
        } catch (AuthenticationUnavailableException authenticationUnavailableException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.serversUnavailable"));
        } catch (InvalidCredentialsException invalidCredentialsException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.invalidSession"));
        } catch (InsufficientPrivilegesException insufficientPrivilegesException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
        } catch (ForcedUsernameChangeException | UserBannedException authenticationException) {
            return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.userBanned"));
        } catch (AuthenticationException authenticationException) {
            return Text.translatable("disconnect.loginFailedInfo", authenticationException.getMessage());
        }
        return null;
    }

    private MinecraftSessionService getSessionService() {
        return this.client.getSessionService();
    }

    @Override
    public void onSuccess(LoginSuccessS2CPacket packet) {
        this.switchTo(State.JOINING);
        GameProfile gameProfile = packet.profile();
        this.connection.transitionInbound(ConfigurationStates.S2C, new ClientConfigurationNetworkHandler(this.client, this.connection, new ClientConnectionState(gameProfile, this.client.getTelemetryManager().createWorldSession(this.newWorld, this.worldLoadTime, this.minigameName), ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager(), FeatureFlags.DEFAULT_ENABLED_FEATURES, null, this.serverInfo, this.parentScreen, this.serverCookies, null, packet.strictErrorHandling(), Map.of(), class_9782.field_51977)));
        this.connection.send(EnterConfigurationC2SPacket.INSTANCE);
        this.connection.transitionOutbound(ConfigurationStates.C2S);
        this.connection.send(new CustomPayloadC2SPacket(new BrandCustomPayload(ClientBrandRetriever.getClientModName())));
        this.connection.send(new ClientOptionsC2SPacket(this.client.options.getSyncedOptions()));
    }

    @Override
    public void onDisconnected(class_9812 arg) {
        Text lv;
        Text text = lv = this.hasCookies ? ScreenTexts.CONNECT_FAILED_TRANSFER : ScreenTexts.CONNECT_FAILED;
        if (this.serverInfo != null && this.serverInfo.isRealm()) {
            this.client.setScreen(new DisconnectedRealmsScreen(this.parentScreen, lv, arg.reason()));
        } else {
            this.client.setScreen(new DisconnectedScreen(this.parentScreen, lv, arg));
        }
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }

    @Override
    public void onDisconnect(LoginDisconnectS2CPacket packet) {
        this.connection.disconnect(packet.getReason());
    }

    @Override
    public void onCompression(LoginCompressionS2CPacket packet) {
        if (!this.connection.isLocal()) {
            this.connection.setCompressionThreshold(packet.getCompressionThreshold(), false);
        }
    }

    @Override
    public void onQueryRequest(LoginQueryRequestS2CPacket packet) {
        this.statusConsumer.accept(Text.translatable("connect.negotiating"));
        this.connection.send(new LoginQueryResponseC2SPacket(packet.queryId(), null));
    }

    public void setMinigameName(@Nullable String minigameName) {
        this.minigameName = minigameName;
    }

    @Override
    public void onCookieRequest(CookieRequestS2CPacket packet) {
        this.connection.send(new CookieResponseC2SPacket(packet.key(), this.serverCookies.get(packet.key())));
    }

    @Override
    public void addCustomCrashReportInfo(CrashReport arg, CrashReportSection arg2) {
        arg2.add("Server type", () -> this.serverInfo != null ? this.serverInfo.getServerType().toString() : "<unknown>");
        arg2.add("Login phase", () -> this.state.get().toString());
    }

    @Environment(value=EnvType.CLIENT)
    static enum State {
        CONNECTING(Text.translatable("connect.connecting"), Set.of()),
        AUTHORIZING(Text.translatable("connect.authorizing"), Set.of(CONNECTING)),
        ENCRYPTING(Text.translatable("connect.encrypting"), Set.of(AUTHORIZING)),
        JOINING(Text.translatable("connect.joining"), Set.of(ENCRYPTING, CONNECTING));

        final Text name;
        final Set<State> prevStates;

        private State(Text name, Set<State> prevStates) {
            this.name = name;
            this.prevStates = prevStates;
        }
    }
}

