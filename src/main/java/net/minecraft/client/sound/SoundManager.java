package net.minecraft.client.sound;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.FloatSupplier;
import net.minecraft.util.math.floatprovider.MultipliedFloatSupplier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SoundManager extends SinglePreparationResourceReloader {
   public static final Sound MISSING_SOUND;
   public static final Identifier INTENTIONALLY_EMPTY_ID;
   public static final WeightedSoundSet INTENTIONALLY_EMPTY_SOUND_SET;
   public static final Sound INTENTIONALLY_EMPTY_SOUND;
   static final Logger LOGGER;
   private static final String SOUNDS_JSON = "sounds.json";
   private static final Gson GSON;
   private static final TypeToken TYPE;
   private final Map sounds = Maps.newHashMap();
   private final SoundSystem soundSystem;
   private final Map soundResources = new HashMap();

   public SoundManager(GameOptions gameOptions) {
      this.soundSystem = new SoundSystem(this, gameOptions, ResourceFactory.fromMap(this.soundResources));
   }

   protected SoundList prepare(ResourceManager arg, Profiler arg2) {
      SoundList lv = new SoundList();
      arg2.startTick();
      arg2.push("list");
      lv.findSounds(arg);
      arg2.pop();

      for(Iterator var4 = arg.getAllNamespaces().iterator(); var4.hasNext(); arg2.pop()) {
         String string = (String)var4.next();
         arg2.push(string);

         try {
            List list = arg.getAllResources(new Identifier(string, "sounds.json"));

            for(Iterator var7 = list.iterator(); var7.hasNext(); arg2.pop()) {
               Resource lv2 = (Resource)var7.next();
               arg2.push(lv2.getResourcePackName());

               try {
                  Reader reader = lv2.getReader();

                  try {
                     arg2.push("parse");
                     Map map = (Map)JsonHelper.deserialize(GSON, (Reader)reader, (TypeToken)TYPE);
                     arg2.swap("register");
                     Iterator var11 = map.entrySet().iterator();

                     while(true) {
                        if (!var11.hasNext()) {
                           arg2.pop();
                           break;
                        }

                        Map.Entry entry = (Map.Entry)var11.next();
                        lv.register(new Identifier(string, (String)entry.getKey()), (SoundEntry)entry.getValue());
                     }
                  } catch (Throwable var14) {
                     if (reader != null) {
                        try {
                           reader.close();
                        } catch (Throwable var13) {
                           var14.addSuppressed(var13);
                        }
                     }

                     throw var14;
                  }

                  if (reader != null) {
                     reader.close();
                  }
               } catch (RuntimeException var15) {
                  LOGGER.warn("Invalid {} in resourcepack: '{}'", new Object[]{"sounds.json", lv2.getResourcePackName(), var15});
               }
            }
         } catch (IOException var16) {
         }
      }

      arg2.endTick();
      return lv;
   }

   protected void apply(SoundList arg, ResourceManager arg2, Profiler arg3) {
      arg.reload(this.sounds, this.soundResources, this.soundSystem);
      Iterator var4;
      Identifier lv;
      if (SharedConstants.isDevelopment) {
         var4 = this.sounds.keySet().iterator();

         while(var4.hasNext()) {
            lv = (Identifier)var4.next();
            WeightedSoundSet lv2 = (WeightedSoundSet)this.sounds.get(lv);
            if (!Texts.hasTranslation(lv2.getSubtitle()) && Registries.SOUND_EVENT.containsId(lv)) {
               LOGGER.error("Missing subtitle {} for sound event: {}", lv2.getSubtitle(), lv);
            }
         }
      }

      if (LOGGER.isDebugEnabled()) {
         var4 = this.sounds.keySet().iterator();

         while(var4.hasNext()) {
            lv = (Identifier)var4.next();
            if (!Registries.SOUND_EVENT.containsId(lv)) {
               LOGGER.debug("Not having sound event for: {}", lv);
            }
         }
      }

      this.soundSystem.reloadSounds();
   }

   public List getSoundDevices() {
      return this.soundSystem.getSoundDevices();
   }

   static boolean isSoundResourcePresent(Sound sound, Identifier id, ResourceFactory resourceFactory) {
      Identifier lv = sound.getLocation();
      if (resourceFactory.getResource(lv).isEmpty()) {
         LOGGER.warn("File {} does not exist, cannot add it to event {}", lv, id);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public WeightedSoundSet get(Identifier id) {
      return (WeightedSoundSet)this.sounds.get(id);
   }

   public Collection getKeys() {
      return this.sounds.keySet();
   }

   public void playNextTick(TickableSoundInstance sound) {
      this.soundSystem.playNextTick(sound);
   }

   public void play(SoundInstance sound) {
      this.soundSystem.play(sound);
   }

   public void play(SoundInstance sound, int delay) {
      this.soundSystem.play(sound, delay);
   }

   public void updateListenerPosition(Camera camera) {
      this.soundSystem.updateListenerPosition(camera);
   }

   public void pauseAll() {
      this.soundSystem.pauseAll();
   }

   public void stopAll() {
      this.soundSystem.stopAll();
   }

   public void close() {
      this.soundSystem.stop();
   }

   public void tick(boolean paused) {
      this.soundSystem.tick(paused);
   }

   public void resumeAll() {
      this.soundSystem.resumeAll();
   }

   public void updateSoundVolume(SoundCategory category, float volume) {
      if (category == SoundCategory.MASTER && volume <= 0.0F) {
         this.stopAll();
      }

      this.soundSystem.updateSoundVolume(category, volume);
   }

   public void stop(SoundInstance sound) {
      this.soundSystem.stop(sound);
   }

   public boolean isPlaying(SoundInstance sound) {
      return this.soundSystem.isPlaying(sound);
   }

   public void registerListener(SoundInstanceListener listener) {
      this.soundSystem.registerListener(listener);
   }

   public void unregisterListener(SoundInstanceListener listener) {
      this.soundSystem.unregisterListener(listener);
   }

   public void stopSounds(@Nullable Identifier id, @Nullable SoundCategory soundCategory) {
      this.soundSystem.stopSounds(id, soundCategory);
   }

   public String getDebugString() {
      return this.soundSystem.getDebugString();
   }

   public void reloadSounds() {
      this.soundSystem.reloadSounds();
   }

   // $FF: synthetic method
   protected Object prepare(ResourceManager manager, Profiler profiler) {
      return this.prepare(manager, profiler);
   }

   static {
      MISSING_SOUND = new Sound("minecraft:empty", ConstantFloatProvider.create(1.0F), ConstantFloatProvider.create(1.0F), 1, Sound.RegistrationType.FILE, false, false, 16);
      INTENTIONALLY_EMPTY_ID = new Identifier("minecraft", "intentionally_empty");
      INTENTIONALLY_EMPTY_SOUND_SET = new WeightedSoundSet(INTENTIONALLY_EMPTY_ID, (String)null);
      INTENTIONALLY_EMPTY_SOUND = new Sound(INTENTIONALLY_EMPTY_ID.toString(), ConstantFloatProvider.create(1.0F), ConstantFloatProvider.create(1.0F), 1, Sound.RegistrationType.FILE, false, false, 16);
      LOGGER = LogUtils.getLogger();
      GSON = (new GsonBuilder()).registerTypeHierarchyAdapter(Text.class, new Text.Serializer()).registerTypeAdapter(SoundEntry.class, new SoundEntryDeserializer()).create();
      TYPE = new TypeToken() {
      };
   }

   @Environment(EnvType.CLIENT)
   protected static class SoundList {
      final Map loadedSounds = Maps.newHashMap();
      private Map foundSounds = Map.of();

      void findSounds(ResourceManager resourceManager) {
         this.foundSounds = Sound.FINDER.findResources(resourceManager);
      }

      void register(Identifier id, SoundEntry entry) {
         WeightedSoundSet lv = (WeightedSoundSet)this.loadedSounds.get(id);
         boolean bl = lv == null;
         if (bl || entry.canReplace()) {
            if (!bl) {
               SoundManager.LOGGER.debug("Replaced sound event location {}", id);
            }

            lv = new WeightedSoundSet(id, entry.getSubtitle());
            this.loadedSounds.put(id, lv);
         }

         ResourceFactory lv2 = ResourceFactory.fromMap(this.foundSounds);
         Iterator var6 = entry.getSounds().iterator();

         while(var6.hasNext()) {
            final Sound lv3 = (Sound)var6.next();
            final Identifier lv4 = lv3.getIdentifier();
            Object lv5;
            switch (lv3.getRegistrationType()) {
               case FILE:
                  if (!SoundManager.isSoundResourcePresent(lv3, id, lv2)) {
                     continue;
                  }

                  lv5 = lv3;
                  break;
               case SOUND_EVENT:
                  lv5 = new SoundContainer() {
                     public int getWeight() {
                        WeightedSoundSet lv = (WeightedSoundSet)SoundList.this.loadedSounds.get(lv4);
                        return lv == null ? 0 : lv.getWeight();
                     }

                     public Sound getSound(Random arg) {
                        WeightedSoundSet lv = (WeightedSoundSet)SoundList.this.loadedSounds.get(lv4);
                        if (lv == null) {
                           return SoundManager.MISSING_SOUND;
                        } else {
                           Sound lv2 = lv.getSound(arg);
                           return new Sound(lv2.getIdentifier().toString(), new MultipliedFloatSupplier(new FloatSupplier[]{lv2.getVolume(), lv3.getVolume()}), new MultipliedFloatSupplier(new FloatSupplier[]{lv2.getPitch(), lv3.getPitch()}), lv3.getWeight(), Sound.RegistrationType.FILE, lv2.isStreamed() || lv3.isStreamed(), lv2.isPreloaded(), lv2.getAttenuation());
                        }
                     }

                     public void preload(SoundSystem soundSystem) {
                        WeightedSoundSet lv = (WeightedSoundSet)SoundList.this.loadedSounds.get(lv4);
                        if (lv != null) {
                           lv.preload(soundSystem);
                        }
                     }

                     // $FF: synthetic method
                     public Object getSound(Random random) {
                        return this.getSound(random);
                     }
                  };
                  break;
               default:
                  throw new IllegalStateException("Unknown SoundEventRegistration type: " + lv3.getRegistrationType());
            }

            lv.add((SoundContainer)lv5);
         }

      }

      public void reload(Map sounds, Map soundResources, SoundSystem system) {
         sounds.clear();
         soundResources.clear();
         soundResources.putAll(this.foundSounds);
         Iterator var4 = this.loadedSounds.entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry entry = (Map.Entry)var4.next();
            sounds.put((Identifier)entry.getKey(), (WeightedSoundSet)entry.getValue());
            ((WeightedSoundSet)entry.getValue()).preload(system);
         }

      }
   }
}
