/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.session.report;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.datafixers.util.Unit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.report.AbuseReportType;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public interface AbuseReportSender {
    public static AbuseReportSender create(ReporterEnvironment environment, UserApiService userApiService) {
        return new Impl(environment, userApiService);
    }

    public CompletableFuture<Unit> send(UUID var1, AbuseReportType var2, AbuseReport var3);

    public boolean canSendReports();

    default public AbuseReportLimits getLimits() {
        return AbuseReportLimits.DEFAULTS;
    }

    @Environment(value=EnvType.CLIENT)
    public record Impl(ReporterEnvironment environment, UserApiService userApiService) implements AbuseReportSender
    {
        private static final Text SERVICE_UNAVAILABLE_ERROR_TEXT = Text.translatable("gui.abuseReport.send.service_unavailable");
        private static final Text HTTP_ERROR_TEXT = Text.translatable("gui.abuseReport.send.http_error");
        private static final Text JSON_ERROR_TEXT = Text.translatable("gui.abuseReport.send.json_error");

        @Override
        public CompletableFuture<Unit> send(UUID id, AbuseReportType type, AbuseReport report) {
            return CompletableFuture.supplyAsync(() -> {
                AbuseReportRequest abuseReportRequest = new AbuseReportRequest(1, id, report, this.environment.toClientInfo(), this.environment.toThirdPartyServerInfo(), this.environment.toRealmInfo(), type.getName());
                try {
                    this.userApiService.reportAbuse(abuseReportRequest);
                    return Unit.INSTANCE;
                } catch (MinecraftClientHttpException minecraftClientHttpException) {
                    Text lv = this.getErrorText(minecraftClientHttpException);
                    throw new CompletionException(new AbuseReportException(lv, (Throwable)minecraftClientHttpException));
                } catch (MinecraftClientException minecraftClientException) {
                    Text lv = this.getErrorText(minecraftClientException);
                    throw new CompletionException(new AbuseReportException(lv, (Throwable)minecraftClientException));
                }
            }, Util.getIoWorkerExecutor());
        }

        @Override
        public boolean canSendReports() {
            return this.userApiService.canSendReports();
        }

        private Text getErrorText(MinecraftClientHttpException exception) {
            return Text.translatable("gui.abuseReport.send.error_message", exception.getMessage());
        }

        private Text getErrorText(MinecraftClientException exception) {
            return switch (exception.getType()) {
                default -> throw new MatchException(null, null);
                case MinecraftClientException.ErrorType.SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE_ERROR_TEXT;
                case MinecraftClientException.ErrorType.HTTP_ERROR -> HTTP_ERROR_TEXT;
                case MinecraftClientException.ErrorType.JSON_ERROR -> JSON_ERROR_TEXT;
            };
        }

        @Override
        public AbuseReportLimits getLimits() {
            return this.userApiService.getAbuseReportLimits();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class AbuseReportException
    extends TextifiedException {
        public AbuseReportException(Text arg, Throwable throwable) {
            super(arg, throwable);
        }
    }
}

