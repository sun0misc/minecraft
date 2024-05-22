/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsClientIncompatibleScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsParentalConsentScreen;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsAvailability {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static CompletableFuture<Info> currentFuture;

    public static CompletableFuture<Info> check() {
        if (currentFuture == null || RealmsAvailability.wasUnsuccessful(currentFuture)) {
            currentFuture = RealmsAvailability.checkInternal();
        }
        return currentFuture;
    }

    private static boolean wasUnsuccessful(CompletableFuture<Info> future) {
        Info lv = future.getNow(null);
        return lv != null && lv.exception() != null;
    }

    private static CompletableFuture<Info> checkInternal() {
        Session lv = MinecraftClient.getInstance().getSession();
        if (lv.getAccountType() != Session.AccountType.MSA) {
            return CompletableFuture.completedFuture(new Info(Type.AUTHENTICATION_ERROR));
        }
        return CompletableFuture.supplyAsync(() -> {
            RealmsClient lv = RealmsClient.create();
            try {
                if (lv.clientCompatible() != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                    return new Info(Type.INCOMPATIBLE_CLIENT);
                }
                if (!lv.mcoEnabled()) {
                    return new Info(Type.NEEDS_PARENTAL_CONSENT);
                }
                return new Info(Type.SUCCESS);
            } catch (RealmsServiceException lv2) {
                LOGGER.error("Couldn't connect to realms", lv2);
                if (lv2.error.getErrorCode() == 401) {
                    return new Info(Type.AUTHENTICATION_ERROR);
                }
                return new Info(lv2);
            }
        }, Util.getIoWorkerExecutor());
    }

    @Environment(value=EnvType.CLIENT)
    public record Info(Type type, @Nullable RealmsServiceException exception) {
        public Info(Type type) {
            this(type, null);
        }

        public Info(RealmsServiceException exception) {
            this(Type.UNEXPECTED_ERROR, exception);
        }

        @Nullable
        public Screen createScreen(Screen parent) {
            return switch (this.type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> null;
                case 1 -> new RealmsClientIncompatibleScreen(parent);
                case 2 -> new RealmsParentalConsentScreen(parent);
                case 3 -> new RealmsGenericErrorScreen(Text.translatable("mco.error.invalid.session.title"), Text.translatable("mco.error.invalid.session.message"), parent);
                case 4 -> new RealmsGenericErrorScreen(Objects.requireNonNull(this.exception), parent);
            };
        }

        @Nullable
        public RealmsServiceException exception() {
            return this.exception;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        SUCCESS,
        INCOMPATIBLE_CLIENT,
        NEEDS_PARENTAL_CONSENT,
        AUTHENTICATION_ERROR,
        UNEXPECTED_ERROR;

    }
}

