package net.minecraft.advancement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PlayerAdvancementTracker {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(Identifier.class, new Identifier.Serializer()).setPrettyPrinting().create();
   private static final TypeToken JSON_TYPE = new TypeToken() {
   };
   private final DataFixer dataFixer;
   private final PlayerManager playerManager;
   private final Path filePath;
   private final Map progress = new LinkedHashMap();
   private final Set visibleAdvancements = new HashSet();
   private final Set progressUpdates = new HashSet();
   private final Set updatedRoots = new HashSet();
   private ServerPlayerEntity owner;
   @Nullable
   private Advancement currentDisplayTab;
   private boolean dirty = true;

   public PlayerAdvancementTracker(DataFixer dataFixer, PlayerManager playerManager, ServerAdvancementLoader advancementLoader, Path filePath, ServerPlayerEntity owner) {
      this.dataFixer = dataFixer;
      this.playerManager = playerManager;
      this.filePath = filePath;
      this.owner = owner;
      this.load(advancementLoader);
   }

   public void setOwner(ServerPlayerEntity owner) {
      this.owner = owner;
   }

   public void clearCriteria() {
      Iterator var1 = Criteria.getCriteria().iterator();

      while(var1.hasNext()) {
         Criterion lv = (Criterion)var1.next();
         lv.endTracking(this);
      }

   }

   public void reload(ServerAdvancementLoader advancementLoader) {
      this.clearCriteria();
      this.progress.clear();
      this.visibleAdvancements.clear();
      this.updatedRoots.clear();
      this.progressUpdates.clear();
      this.dirty = true;
      this.currentDisplayTab = null;
      this.load(advancementLoader);
   }

   private void beginTrackingAllAdvancements(ServerAdvancementLoader advancementLoader) {
      Iterator var2 = advancementLoader.getAdvancements().iterator();

      while(var2.hasNext()) {
         Advancement lv = (Advancement)var2.next();
         this.beginTracking(lv);
      }

   }

   private void rewardEmptyAdvancements(ServerAdvancementLoader advancementLoader) {
      Iterator var2 = advancementLoader.getAdvancements().iterator();

      while(var2.hasNext()) {
         Advancement lv = (Advancement)var2.next();
         if (lv.getCriteria().isEmpty()) {
            this.grantCriterion(lv, "");
            lv.getRewards().apply(this.owner);
         }
      }

   }

   private void load(ServerAdvancementLoader advancementLoader) {
      if (Files.isRegularFile(this.filePath, new LinkOption[0])) {
         try {
            JsonReader jsonReader = new JsonReader(Files.newBufferedReader(this.filePath, StandardCharsets.UTF_8));

            try {
               jsonReader.setLenient(false);
               Dynamic dynamic = new Dynamic(JsonOps.INSTANCE, Streams.parse(jsonReader));
               int i = dynamic.get("DataVersion").asInt(1343);
               dynamic = dynamic.remove("DataVersion");
               dynamic = DataFixTypes.ADVANCEMENTS.update(this.dataFixer, dynamic, i);
               Map map = (Map)GSON.getAdapter(JSON_TYPE).fromJsonTree((JsonElement)dynamic.getValue());
               if (map == null) {
                  throw new JsonParseException("Found null for advancements");
               }

               map.entrySet().stream().sorted(Entry.comparingByValue()).forEach((entry) -> {
                  Advancement lv = advancementLoader.get((Identifier)entry.getKey());
                  if (lv == null) {
                     LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", entry.getKey(), this.filePath);
                  } else {
                     this.initProgress(lv, (AdvancementProgress)entry.getValue());
                     this.progressUpdates.add(lv);
                     this.onStatusUpdate(lv);
                  }
               });
            } catch (Throwable var7) {
               try {
                  jsonReader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            jsonReader.close();
         } catch (JsonParseException var8) {
            LOGGER.error("Couldn't parse player advancements in {}", this.filePath, var8);
         } catch (IOException var9) {
            LOGGER.error("Couldn't access player advancements in {}", this.filePath, var9);
         }
      }

      this.rewardEmptyAdvancements(advancementLoader);
      this.beginTrackingAllAdvancements(advancementLoader);
   }

   public void save() {
      Map map = new LinkedHashMap();
      Iterator var2 = this.progress.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         AdvancementProgress lv = (AdvancementProgress)entry.getValue();
         if (lv.isAnyObtained()) {
            map.put(((Advancement)entry.getKey()).getId(), lv);
         }
      }

      JsonElement jsonElement = GSON.toJsonTree(map);
      jsonElement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());

      try {
         PathUtil.createDirectories(this.filePath.getParent());
         Writer writer = Files.newBufferedWriter(this.filePath, StandardCharsets.UTF_8);

         try {
            GSON.toJson(jsonElement, writer);
         } catch (Throwable var7) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException var8) {
         LOGGER.error("Couldn't save player advancements to {}", this.filePath, var8);
      }

   }

   public boolean grantCriterion(Advancement advancement, String criterionName) {
      boolean bl = false;
      AdvancementProgress lv = this.getProgress(advancement);
      boolean bl2 = lv.isDone();
      if (lv.obtain(criterionName)) {
         this.endTrackingCompleted(advancement);
         this.progressUpdates.add(advancement);
         bl = true;
         if (!bl2 && lv.isDone()) {
            advancement.getRewards().apply(this.owner);
            if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && this.owner.world.getGameRules().getBoolean(GameRules.ANNOUNCE_ADVANCEMENTS)) {
               this.playerManager.broadcast(Text.translatable("chat.type.advancement." + advancement.getDisplay().getFrame().getId(), this.owner.getDisplayName(), advancement.toHoverableText()), false);
            }
         }
      }

      if (!bl2 && lv.isDone()) {
         this.onStatusUpdate(advancement);
      }

      return bl;
   }

   public boolean revokeCriterion(Advancement advancement, String criterionName) {
      boolean bl = false;
      AdvancementProgress lv = this.getProgress(advancement);
      boolean bl2 = lv.isDone();
      if (lv.reset(criterionName)) {
         this.beginTracking(advancement);
         this.progressUpdates.add(advancement);
         bl = true;
      }

      if (bl2 && !lv.isDone()) {
         this.onStatusUpdate(advancement);
      }

      return bl;
   }

   private void onStatusUpdate(Advancement advancement) {
      this.updatedRoots.add(advancement.getRoot());
   }

   private void beginTracking(Advancement advancement) {
      AdvancementProgress lv = this.getProgress(advancement);
      if (!lv.isDone()) {
         Iterator var3 = advancement.getCriteria().entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            CriterionProgress lv2 = lv.getCriterionProgress((String)entry.getKey());
            if (lv2 != null && !lv2.isObtained()) {
               CriterionConditions lv3 = ((AdvancementCriterion)entry.getValue()).getConditions();
               if (lv3 != null) {
                  Criterion lv4 = Criteria.getById(lv3.getId());
                  if (lv4 != null) {
                     lv4.beginTrackingCondition(this, new Criterion.ConditionsContainer(lv3, advancement, (String)entry.getKey()));
                  }
               }
            }
         }

      }
   }

   private void endTrackingCompleted(Advancement advancement) {
      AdvancementProgress lv = this.getProgress(advancement);
      Iterator var3 = advancement.getCriteria().entrySet().iterator();

      while(true) {
         Map.Entry entry;
         CriterionProgress lv2;
         do {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               entry = (Map.Entry)var3.next();
               lv2 = lv.getCriterionProgress((String)entry.getKey());
            } while(lv2 == null);
         } while(!lv2.isObtained() && !lv.isDone());

         CriterionConditions lv3 = ((AdvancementCriterion)entry.getValue()).getConditions();
         if (lv3 != null) {
            Criterion lv4 = Criteria.getById(lv3.getId());
            if (lv4 != null) {
               lv4.endTrackingCondition(this, new Criterion.ConditionsContainer(lv3, advancement, (String)entry.getKey()));
            }
         }
      }
   }

   public void sendUpdate(ServerPlayerEntity player) {
      if (this.dirty || !this.updatedRoots.isEmpty() || !this.progressUpdates.isEmpty()) {
         Map map = new HashMap();
         Set set = new HashSet();
         Set set2 = new HashSet();
         Iterator var5 = this.updatedRoots.iterator();

         Advancement lv;
         while(var5.hasNext()) {
            lv = (Advancement)var5.next();
            this.calculateDisplay(lv, set, set2);
         }

         this.updatedRoots.clear();
         var5 = this.progressUpdates.iterator();

         while(var5.hasNext()) {
            lv = (Advancement)var5.next();
            if (this.visibleAdvancements.contains(lv)) {
               map.put(lv.getId(), (AdvancementProgress)this.progress.get(lv));
            }
         }

         this.progressUpdates.clear();
         if (!map.isEmpty() || !set.isEmpty() || !set2.isEmpty()) {
            player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(this.dirty, set, set2, map));
         }
      }

      this.dirty = false;
   }

   public void setDisplayTab(@Nullable Advancement advancement) {
      Advancement lv = this.currentDisplayTab;
      if (advancement != null && advancement.getParent() == null && advancement.getDisplay() != null) {
         this.currentDisplayTab = advancement;
      } else {
         this.currentDisplayTab = null;
      }

      if (lv != this.currentDisplayTab) {
         this.owner.networkHandler.sendPacket(new SelectAdvancementTabS2CPacket(this.currentDisplayTab == null ? null : this.currentDisplayTab.getId()));
      }

   }

   public AdvancementProgress getProgress(Advancement advancement) {
      AdvancementProgress lv = (AdvancementProgress)this.progress.get(advancement);
      if (lv == null) {
         lv = new AdvancementProgress();
         this.initProgress(advancement, lv);
      }

      return lv;
   }

   private void initProgress(Advancement advancement, AdvancementProgress progress) {
      progress.init(advancement.getCriteria(), advancement.getRequirements());
      this.progress.put(advancement, progress);
   }

   private void calculateDisplay(Advancement root, Set added, Set removed) {
      AdvancementDisplays.calculateDisplay(root, (advancement) -> {
         return this.getProgress(advancement).isDone();
      }, (advancement, displayed) -> {
         if (displayed) {
            if (this.visibleAdvancements.add(advancement)) {
               added.add(advancement);
               if (this.progress.containsKey(advancement)) {
                  this.progressUpdates.add(advancement);
               }
            }
         } else if (this.visibleAdvancements.remove(advancement)) {
            removed.add(advancement.getId());
         }

      });
   }
}
