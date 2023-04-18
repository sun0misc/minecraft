package net.minecraft.client.report;

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
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public interface AbuseReportSender {
   static AbuseReportSender create(ReporterEnvironment environment, UserApiService userApiService) {
      return new Impl(environment, userApiService);
   }

   CompletableFuture send(UUID id, AbuseReport report);

   boolean canSendReports();

   default AbuseReportLimits getLimits() {
      return AbuseReportLimits.DEFAULTS;
   }

   @Environment(EnvType.CLIENT)
   public static record Impl(ReporterEnvironment environment, UserApiService userApiService) implements AbuseReportSender {
      private static final Text SERVICE_UNAVAILABLE_ERROR_TEXT = Text.translatable("gui.abuseReport.send.service_unavailable");
      private static final Text HTTP_ERROR_TEXT = Text.translatable("gui.abuseReport.send.http_error");
      private static final Text JSON_ERROR_TEXT = Text.translatable("gui.abuseReport.send.json_error");

      public Impl(ReporterEnvironment arg, UserApiService userApiService) {
         this.environment = arg;
         this.userApiService = userApiService;
      }

      public CompletableFuture send(UUID id, AbuseReport report) {
         return CompletableFuture.supplyAsync(() -> {
            AbuseReportRequest abuseReportRequest = new AbuseReportRequest(1, id, report, this.environment.toClientInfo(), this.environment.toThirdPartyServerInfo(), this.environment.toRealmInfo());

            Text lv;
            try {
               this.userApiService.reportAbuse(abuseReportRequest);
               return Unit.INSTANCE;
            } catch (MinecraftClientHttpException var6) {
               lv = this.getErrorText(var6);
               throw new CompletionException(new AbuseReportException(lv, var6));
            } catch (MinecraftClientException var7) {
               lv = this.getErrorText(var7);
               throw new CompletionException(new AbuseReportException(lv, var7));
            }
         }, Util.getIoWorkerExecutor());
      }

      public boolean canSendReports() {
         return this.userApiService.canSendReports();
      }

      private Text getErrorText(MinecraftClientHttpException exception) {
         return Text.translatable("gui.abuseReport.send.error_message", exception.getMessage());
      }

      private Text getErrorText(MinecraftClientException exception) {
         Text var10000;
         switch (exception.getType()) {
            case SERVICE_UNAVAILABLE:
               var10000 = SERVICE_UNAVAILABLE_ERROR_TEXT;
               break;
            case HTTP_ERROR:
               var10000 = HTTP_ERROR_TEXT;
               break;
            case JSON_ERROR:
               var10000 = JSON_ERROR_TEXT;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public AbuseReportLimits getLimits() {
         return this.userApiService.getAbuseReportLimits();
      }

      public ReporterEnvironment environment() {
         return this.environment;
      }

      public UserApiService userApiService() {
         return this.userApiService;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class AbuseReportException extends TextifiedException {
      public AbuseReportException(Text arg, Throwable throwable) {
         super(arg, throwable);
      }
   }
}
