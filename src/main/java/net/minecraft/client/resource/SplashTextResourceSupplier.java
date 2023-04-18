package net.minecraft.client.resource;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SplashTextResourceSupplier extends SinglePreparationResourceReloader {
   private static final Identifier RESOURCE_ID = new Identifier("texts/splashes.txt");
   private static final Random RANDOM = Random.create();
   private final List splashTexts = Lists.newArrayList();
   private final Session session;

   public SplashTextResourceSupplier(Session session) {
      this.session = session;
   }

   protected List prepare(ResourceManager arg, Profiler arg2) {
      try {
         BufferedReader bufferedReader = MinecraftClient.getInstance().getResourceManager().openAsReader(RESOURCE_ID);

         List var4;
         try {
            var4 = (List)bufferedReader.lines().map(String::trim).filter((splashText) -> {
               return splashText.hashCode() != 125780783;
            }).collect(Collectors.toList());
         } catch (Throwable var7) {
            if (bufferedReader != null) {
               try {
                  bufferedReader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (bufferedReader != null) {
            bufferedReader.close();
         }

         return var4;
      } catch (IOException var8) {
         return Collections.emptyList();
      }
   }

   protected void apply(List list, ResourceManager arg, Profiler arg2) {
      this.splashTexts.clear();
      this.splashTexts.addAll(list);
   }

   @Nullable
   public String get() {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
         return "Merry X-mas!";
      } else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
         return "Happy new year!";
      } else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
         return "OOoooOOOoooo! Spooky!";
      } else if (this.splashTexts.isEmpty()) {
         return null;
      } else if (this.session != null && RANDOM.nextInt(this.splashTexts.size()) == 42) {
         String var10000 = this.session.getUsername();
         return var10000.toUpperCase(Locale.ROOT) + " IS YOU";
      } else {
         return (String)this.splashTexts.get(RANDOM.nextInt(this.splashTexts.size()));
      }
   }

   // $FF: synthetic method
   protected Object prepare(ResourceManager manager, Profiler profiler) {
      return this.prepare(manager, profiler);
   }
}
