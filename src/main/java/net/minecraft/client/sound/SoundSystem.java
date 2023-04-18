package net.minecraft.client.sound;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Environment(EnvType.CLIENT)
public class SoundSystem {
   private static final Marker MARKER = MarkerFactory.getMarker("SOUNDS");
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float MIN_PITCH = 0.5F;
   private static final float MAX_PITCH = 2.0F;
   private static final float MIN_VOLUME = 0.0F;
   private static final float MAX_VOLUME = 1.0F;
   private static final int field_33025 = 20;
   private static final Set UNKNOWN_SOUNDS = Sets.newHashSet();
   private static final long MIN_TIME_INTERVAL_TO_RELOAD_SOUNDS = 1000L;
   public static final String FOR_THE_DEBUG = "FOR THE DEBUG!";
   public static final String OPENAL_SOFT_ON = "OpenAL Soft on ";
   public static final int OPENAL_SOFT_ON_LENGTH = "OpenAL Soft on ".length();
   private final SoundManager loader;
   private final GameOptions settings;
   private boolean started;
   private final SoundEngine soundEngine = new SoundEngine();
   private final SoundListener listener;
   private final SoundLoader soundLoader;
   private final SoundExecutor taskQueue;
   private final Channel channel;
   private int ticks;
   private long lastSoundDeviceCheckTime;
   private final AtomicReference deviceChangeStatus;
   private final Map sources;
   private final Multimap sounds;
   private final List tickingSounds;
   private final Map startTicks;
   private final Map soundEndTicks;
   private final List listeners;
   private final List soundsToPlayNextTick;
   private final List preloadedSounds;

   public SoundSystem(SoundManager loader, GameOptions settings, ResourceFactory resourceFactory) {
      this.listener = this.soundEngine.getListener();
      this.taskQueue = new SoundExecutor();
      this.channel = new Channel(this.soundEngine, this.taskQueue);
      this.deviceChangeStatus = new AtomicReference(SoundSystem.DeviceChangeStatus.NO_CHANGE);
      this.sources = Maps.newHashMap();
      this.sounds = HashMultimap.create();
      this.tickingSounds = Lists.newArrayList();
      this.startTicks = Maps.newHashMap();
      this.soundEndTicks = Maps.newHashMap();
      this.listeners = Lists.newArrayList();
      this.soundsToPlayNextTick = Lists.newArrayList();
      this.preloadedSounds = Lists.newArrayList();
      this.loader = loader;
      this.settings = settings;
      this.soundLoader = new SoundLoader(resourceFactory);
   }

   public void reloadSounds() {
      UNKNOWN_SOUNDS.clear();
      Iterator var1 = Registries.SOUND_EVENT.iterator();

      while(var1.hasNext()) {
         SoundEvent lv = (SoundEvent)var1.next();
         if (lv != SoundEvents.INTENTIONALLY_EMPTY) {
            Identifier lv2 = lv.getId();
            if (this.loader.get(lv2) == null) {
               LOGGER.warn("Missing sound for event: {}", Registries.SOUND_EVENT.getId(lv));
               UNKNOWN_SOUNDS.add(lv2);
            }
         }
      }

      this.stop();
      this.start();
   }

   private synchronized void start() {
      if (!this.started) {
         try {
            String string = (String)this.settings.getSoundDevice().getValue();
            this.soundEngine.init("".equals(string) ? null : string, (Boolean)this.settings.getDirectionalAudio().getValue());
            this.listener.init();
            this.listener.setVolume(this.settings.getSoundVolume(SoundCategory.MASTER));
            CompletableFuture var10000 = this.soundLoader.loadStatic((Collection)this.preloadedSounds);
            List var10001 = this.preloadedSounds;
            Objects.requireNonNull(var10001);
            var10000.thenRun(var10001::clear);
            this.started = true;
            LOGGER.info(MARKER, "Sound engine started");
         } catch (RuntimeException var2) {
            LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", var2);
         }

      }
   }

   private float getSoundVolume(@Nullable SoundCategory category) {
      return category != null && category != SoundCategory.MASTER ? this.settings.getSoundVolume(category) : 1.0F;
   }

   public void updateSoundVolume(SoundCategory category, float volume) {
      if (this.started) {
         if (category == SoundCategory.MASTER) {
            this.listener.setVolume(volume);
         } else {
            this.sources.forEach((source, sourceManager) -> {
               float f = this.getAdjustedVolume(source);
               sourceManager.run((sourcex) -> {
                  if (f <= 0.0F) {
                     sourcex.stop();
                  } else {
                     sourcex.setVolume(f);
                  }

               });
            });
         }
      }
   }

   public void stop() {
      if (this.started) {
         this.stopAll();
         this.soundLoader.close();
         this.soundEngine.close();
         this.started = false;
      }

   }

   public void stop(SoundInstance sound) {
      if (this.started) {
         Channel.SourceManager lv = (Channel.SourceManager)this.sources.get(sound);
         if (lv != null) {
            lv.run(Source::stop);
         }
      }

   }

   public void stopAll() {
      if (this.started) {
         this.taskQueue.restart();
         this.sources.values().forEach((source) -> {
            source.run(Source::stop);
         });
         this.sources.clear();
         this.channel.close();
         this.startTicks.clear();
         this.tickingSounds.clear();
         this.sounds.clear();
         this.soundEndTicks.clear();
         this.soundsToPlayNextTick.clear();
      }

   }

   public void registerListener(SoundInstanceListener listener) {
      this.listeners.add(listener);
   }

   public void unregisterListener(SoundInstanceListener listener) {
      this.listeners.remove(listener);
   }

   private boolean shouldReloadSounds() {
      if (this.soundEngine.isDeviceUnavailable()) {
         LOGGER.info("Audio device was lost!");
         return true;
      } else {
         long l = Util.getMeasuringTimeMs();
         boolean bl = l - this.lastSoundDeviceCheckTime >= 1000L;
         if (bl) {
            this.lastSoundDeviceCheckTime = l;
            if (this.deviceChangeStatus.compareAndSet(SoundSystem.DeviceChangeStatus.NO_CHANGE, SoundSystem.DeviceChangeStatus.ONGOING)) {
               String string = (String)this.settings.getSoundDevice().getValue();
               Util.getIoWorkerExecutor().execute(() -> {
                  if ("".equals(string)) {
                     if (this.soundEngine.updateDeviceSpecifier()) {
                        LOGGER.info("System default audio device has changed!");
                        this.deviceChangeStatus.compareAndSet(SoundSystem.DeviceChangeStatus.ONGOING, SoundSystem.DeviceChangeStatus.CHANGE_DETECTED);
                     }
                  } else if (!this.soundEngine.getCurrentDeviceName().equals(string) && this.soundEngine.getSoundDevices().contains(string)) {
                     LOGGER.info("Preferred audio device has become available!");
                     this.deviceChangeStatus.compareAndSet(SoundSystem.DeviceChangeStatus.ONGOING, SoundSystem.DeviceChangeStatus.CHANGE_DETECTED);
                  }

                  this.deviceChangeStatus.compareAndSet(SoundSystem.DeviceChangeStatus.ONGOING, SoundSystem.DeviceChangeStatus.NO_CHANGE);
               });
            }
         }

         return this.deviceChangeStatus.compareAndSet(SoundSystem.DeviceChangeStatus.CHANGE_DETECTED, SoundSystem.DeviceChangeStatus.NO_CHANGE);
      }
   }

   public void tick(boolean paused) {
      if (this.shouldReloadSounds()) {
         this.reloadSounds();
      }

      if (!paused) {
         this.tick();
      }

      this.channel.tick();
   }

   private void tick() {
      ++this.ticks;
      this.soundsToPlayNextTick.stream().filter(SoundInstance::canPlay).forEach(this::play);
      this.soundsToPlayNextTick.clear();
      Iterator iterator = this.tickingSounds.iterator();

      while(iterator.hasNext()) {
         TickableSoundInstance lv = (TickableSoundInstance)iterator.next();
         if (!lv.canPlay()) {
            this.stop(lv);
         }

         lv.tick();
         if (lv.isDone()) {
            this.stop(lv);
         } else {
            float f = this.getAdjustedVolume(lv);
            float g = this.getAdjustedPitch(lv);
            Vec3d lv2 = new Vec3d(lv.getX(), lv.getY(), lv.getZ());
            Channel.SourceManager lv3 = (Channel.SourceManager)this.sources.get(lv);
            if (lv3 != null) {
               lv3.run((source) -> {
                  source.setVolume(f);
                  source.setPitch(g);
                  source.setPosition(lv2);
               });
            }
         }
      }

      iterator = this.sources.entrySet().iterator();

      SoundInstance lv5;
      while(iterator.hasNext()) {
         Map.Entry entry = (Map.Entry)iterator.next();
         Channel.SourceManager lv4 = (Channel.SourceManager)entry.getValue();
         lv5 = (SoundInstance)entry.getKey();
         float h = this.settings.getSoundVolume(lv5.getCategory());
         if (h <= 0.0F) {
            lv4.run(Source::stop);
            iterator.remove();
         } else if (lv4.isStopped()) {
            int i = (Integer)this.soundEndTicks.get(lv5);
            if (i <= this.ticks) {
               if (isRepeatDelayed(lv5)) {
                  this.startTicks.put(lv5, this.ticks + lv5.getRepeatDelay());
               }

               iterator.remove();
               LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", lv4);
               this.soundEndTicks.remove(lv5);

               try {
                  this.sounds.remove(lv5.getCategory(), lv5);
               } catch (RuntimeException var8) {
               }

               if (lv5 instanceof TickableSoundInstance) {
                  this.tickingSounds.remove(lv5);
               }
            }
         }
      }

      Iterator iterator2 = this.startTicks.entrySet().iterator();

      while(iterator2.hasNext()) {
         Map.Entry entry2 = (Map.Entry)iterator2.next();
         if (this.ticks >= (Integer)entry2.getValue()) {
            lv5 = (SoundInstance)entry2.getKey();
            if (lv5 instanceof TickableSoundInstance) {
               ((TickableSoundInstance)lv5).tick();
            }

            this.play(lv5);
            iterator2.remove();
         }
      }

   }

   private static boolean canRepeatInstantly(SoundInstance sound) {
      return sound.getRepeatDelay() > 0;
   }

   private static boolean isRepeatDelayed(SoundInstance sound) {
      return sound.isRepeatable() && canRepeatInstantly(sound);
   }

   private static boolean shouldRepeatInstantly(SoundInstance sound) {
      return sound.isRepeatable() && !canRepeatInstantly(sound);
   }

   public boolean isPlaying(SoundInstance sound) {
      if (!this.started) {
         return false;
      } else {
         return this.soundEndTicks.containsKey(sound) && (Integer)this.soundEndTicks.get(sound) <= this.ticks ? true : this.sources.containsKey(sound);
      }
   }

   public void play(SoundInstance sound) {
      if (this.started) {
         if (sound.canPlay()) {
            WeightedSoundSet lv = sound.getSoundSet(this.loader);
            Identifier lv2 = sound.getId();
            if (lv == null) {
               if (UNKNOWN_SOUNDS.add(lv2)) {
                  LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", lv2);
               }

            } else {
               Sound lv3 = sound.getSound();
               if (lv3 != SoundManager.INTENTIONALLY_EMPTY_SOUND) {
                  if (lv3 == SoundManager.MISSING_SOUND) {
                     if (UNKNOWN_SOUNDS.add(lv2)) {
                        LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", lv2);
                     }

                  } else {
                     float f = sound.getVolume();
                     float g = Math.max(f, 1.0F) * (float)lv3.getAttenuation();
                     SoundCategory lv4 = sound.getCategory();
                     float h = this.getAdjustedVolume(f, lv4);
                     float i = this.getAdjustedPitch(sound);
                     SoundInstance.AttenuationType lv5 = sound.getAttenuationType();
                     boolean bl = sound.isRelative();
                     if (h == 0.0F && !sound.shouldAlwaysPlay()) {
                        LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", lv3.getIdentifier());
                     } else {
                        Vec3d lv6 = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
                        boolean bl2;
                        if (!this.listeners.isEmpty()) {
                           bl2 = bl || lv5 == SoundInstance.AttenuationType.NONE || this.listener.getPos().squaredDistanceTo(lv6) < (double)(g * g);
                           if (bl2) {
                              Iterator var14 = this.listeners.iterator();

                              while(var14.hasNext()) {
                                 SoundInstanceListener lv7 = (SoundInstanceListener)var14.next();
                                 lv7.onSoundPlayed(sound, lv);
                              }
                           } else {
                              LOGGER.debug(MARKER, "Did not notify listeners of soundEvent: {}, it is too far away to hear", lv2);
                           }
                        }

                        if (this.listener.getVolume() <= 0.0F) {
                           LOGGER.debug(MARKER, "Skipped playing soundEvent: {}, master volume was zero", lv2);
                        } else {
                           bl2 = shouldRepeatInstantly(sound);
                           boolean bl3 = lv3.isStreamed();
                           CompletableFuture completableFuture = this.channel.createSource(lv3.isStreamed() ? SoundEngine.RunMode.STREAMING : SoundEngine.RunMode.STATIC);
                           Channel.SourceManager lv8 = (Channel.SourceManager)completableFuture.join();
                           if (lv8 == null) {
                              if (SharedConstants.isDevelopment) {
                                 LOGGER.warn("Failed to create new sound handle");
                              }

                           } else {
                              LOGGER.debug(MARKER, "Playing sound {} for event {}", lv3.getIdentifier(), lv2);
                              this.soundEndTicks.put(sound, this.ticks + 20);
                              this.sources.put(sound, lv8);
                              this.sounds.put(lv4, sound);
                              lv8.run((source) -> {
                                 source.setPitch(i);
                                 source.setVolume(h);
                                 if (lv5 == SoundInstance.AttenuationType.LINEAR) {
                                    source.setAttenuation(g);
                                 } else {
                                    source.disableAttenuation();
                                 }

                                 source.setLooping(bl2 && !bl3);
                                 source.setPosition(lv6);
                                 source.setRelative(bl);
                              });
                              if (!bl3) {
                                 this.soundLoader.loadStatic(lv3.getLocation()).thenAccept((soundx) -> {
                                    lv8.run((source) -> {
                                       source.setBuffer(soundx);
                                       source.play();
                                    });
                                 });
                              } else {
                                 this.soundLoader.loadStreamed(lv3.getLocation(), bl2).thenAccept((stream) -> {
                                    lv8.run((source) -> {
                                       source.setStream(stream);
                                       source.play();
                                    });
                                 });
                              }

                              if (sound instanceof TickableSoundInstance) {
                                 this.tickingSounds.add((TickableSoundInstance)sound);
                              }

                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void playNextTick(TickableSoundInstance sound) {
      this.soundsToPlayNextTick.add(sound);
   }

   public void addPreloadedSound(Sound sound) {
      this.preloadedSounds.add(sound);
   }

   private float getAdjustedPitch(SoundInstance sound) {
      return MathHelper.clamp(sound.getPitch(), 0.5F, 2.0F);
   }

   private float getAdjustedVolume(SoundInstance sound) {
      return this.getAdjustedVolume(sound.getVolume(), sound.getCategory());
   }

   private float getAdjustedVolume(float volume, SoundCategory category) {
      return MathHelper.clamp(volume * this.getSoundVolume(category), 0.0F, 1.0F);
   }

   public void pauseAll() {
      if (this.started) {
         this.channel.execute((sources) -> {
            sources.forEach(Source::pause);
         });
      }

   }

   public void resumeAll() {
      if (this.started) {
         this.channel.execute((sources) -> {
            sources.forEach(Source::resume);
         });
      }

   }

   public void play(SoundInstance sound, int delay) {
      this.startTicks.put(sound, this.ticks + delay);
   }

   public void updateListenerPosition(Camera camera) {
      if (this.started && camera.isReady()) {
         Vec3d lv = camera.getPos();
         Vector3f vector3f = camera.getHorizontalPlane();
         Vector3f vector3f2 = camera.getVerticalPlane();
         this.taskQueue.execute(() -> {
            this.listener.setPosition(lv);
            this.listener.setOrientation(vector3f, vector3f2);
         });
      }
   }

   public void stopSounds(@Nullable Identifier id, @Nullable SoundCategory category) {
      Iterator var3;
      SoundInstance lv;
      if (category != null) {
         var3 = this.sounds.get(category).iterator();

         while(true) {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               lv = (SoundInstance)var3.next();
            } while(id != null && !lv.getId().equals(id));

            this.stop(lv);
         }
      } else if (id == null) {
         this.stopAll();
      } else {
         var3 = this.sources.keySet().iterator();

         while(var3.hasNext()) {
            lv = (SoundInstance)var3.next();
            if (lv.getId().equals(id)) {
               this.stop(lv);
            }
         }
      }

   }

   public String getDebugString() {
      return this.soundEngine.getDebugString();
   }

   public List getSoundDevices() {
      return this.soundEngine.getSoundDevices();
   }

   @Environment(EnvType.CLIENT)
   private static enum DeviceChangeStatus {
      ONGOING,
      CHANGE_DETECTED,
      NO_CHANGE;

      // $FF: synthetic method
      private static DeviceChangeStatus[] method_38939() {
         return new DeviceChangeStatus[]{ONGOING, CHANGE_DETECTED, NO_CHANGE};
      }
   }
}
