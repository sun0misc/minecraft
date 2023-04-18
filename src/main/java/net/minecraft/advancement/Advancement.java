package net.minecraft.advancement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public class Advancement {
   @Nullable
   private final Advancement parent;
   @Nullable
   private final AdvancementDisplay display;
   private final AdvancementRewards rewards;
   private final Identifier id;
   private final Map criteria;
   private final String[][] requirements;
   private final Set children = Sets.newLinkedHashSet();
   private final Text text;

   public Advancement(Identifier id, @Nullable Advancement parent, @Nullable AdvancementDisplay display, AdvancementRewards rewards, Map criteria, String[][] requirements) {
      this.id = id;
      this.display = display;
      this.criteria = ImmutableMap.copyOf(criteria);
      this.parent = parent;
      this.rewards = rewards;
      this.requirements = requirements;
      if (parent != null) {
         parent.addChild(this);
      }

      if (display == null) {
         this.text = Text.literal(id.toString());
      } else {
         Text lv = display.getTitle();
         Formatting lv2 = display.getFrame().getTitleFormat();
         Text lv3 = Texts.setStyleIfAbsent(lv.copy(), Style.EMPTY.withColor(lv2)).append("\n").append(display.getDescription());
         Text lv4 = lv.copy().styled((style) -> {
            return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lv3));
         });
         this.text = Texts.bracketed(lv4).formatted(lv2);
      }

   }

   public Builder createTask() {
      return new Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
   }

   @Nullable
   public Advancement getParent() {
      return this.parent;
   }

   public Advancement getRoot() {
      return getRoot(this);
   }

   public static Advancement getRoot(Advancement advancement) {
      Advancement lv = advancement;

      while(true) {
         Advancement lv2 = lv.getParent();
         if (lv2 == null) {
            return lv;
         }

         lv = lv2;
      }
   }

   @Nullable
   public AdvancementDisplay getDisplay() {
      return this.display;
   }

   public AdvancementRewards getRewards() {
      return this.rewards;
   }

   public String toString() {
      Identifier var10000 = this.getId();
      return "SimpleAdvancement{id=" + var10000 + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + "}";
   }

   public Iterable getChildren() {
      return this.children;
   }

   public Map getCriteria() {
      return this.criteria;
   }

   public int getRequirementCount() {
      return this.requirements.length;
   }

   public void addChild(Advancement child) {
      this.children.add(child);
   }

   public Identifier getId() {
      return this.id;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Advancement)) {
         return false;
      } else {
         Advancement lv = (Advancement)o;
         return this.id.equals(lv.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public String[][] getRequirements() {
      return this.requirements;
   }

   public Text toHoverableText() {
      return this.text;
   }

   public static class Builder {
      @Nullable
      private Identifier parentId;
      @Nullable
      private Advancement parentObj;
      @Nullable
      private AdvancementDisplay display;
      private AdvancementRewards rewards;
      private Map criteria;
      @Nullable
      private String[][] requirements;
      private CriterionMerger merger;

      Builder(@Nullable Identifier parentId, @Nullable AdvancementDisplay display, AdvancementRewards rewards, Map criteria, String[][] requirements) {
         this.rewards = AdvancementRewards.NONE;
         this.criteria = Maps.newLinkedHashMap();
         this.merger = CriterionMerger.AND;
         this.parentId = parentId;
         this.display = display;
         this.rewards = rewards;
         this.criteria = criteria;
         this.requirements = requirements;
      }

      private Builder() {
         this.rewards = AdvancementRewards.NONE;
         this.criteria = Maps.newLinkedHashMap();
         this.merger = CriterionMerger.AND;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder parent(Advancement parent) {
         this.parentObj = parent;
         return this;
      }

      public Builder parent(Identifier parentId) {
         this.parentId = parentId;
         return this;
      }

      public Builder display(ItemStack icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
         return this.display(new AdvancementDisplay(icon, title, description, background, frame, showToast, announceToChat, hidden));
      }

      public Builder display(ItemConvertible icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
         return this.display(new AdvancementDisplay(new ItemStack(icon.asItem()), title, description, background, frame, showToast, announceToChat, hidden));
      }

      public Builder display(AdvancementDisplay display) {
         this.display = display;
         return this;
      }

      public Builder rewards(AdvancementRewards.Builder builder) {
         return this.rewards(builder.build());
      }

      public Builder rewards(AdvancementRewards rewards) {
         this.rewards = rewards;
         return this;
      }

      public Builder criterion(String name, CriterionConditions conditions) {
         return this.criterion(name, new AdvancementCriterion(conditions));
      }

      public Builder criterion(String name, AdvancementCriterion criterion) {
         if (this.criteria.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate criterion " + name);
         } else {
            this.criteria.put(name, criterion);
            return this;
         }
      }

      public Builder criteriaMerger(CriterionMerger merger) {
         this.merger = merger;
         return this;
      }

      public Builder requirements(String[][] requirements) {
         this.requirements = requirements;
         return this;
      }

      public boolean findParent(Function parentProvider) {
         if (this.parentId == null) {
            return true;
         } else {
            if (this.parentObj == null) {
               this.parentObj = (Advancement)parentProvider.apply(this.parentId);
            }

            return this.parentObj != null;
         }
      }

      public Advancement build(Identifier id) {
         if (!this.findParent((idx) -> {
            return null;
         })) {
            throw new IllegalStateException("Tried to build incomplete advancement!");
         } else {
            if (this.requirements == null) {
               this.requirements = this.merger.createRequirements(this.criteria.keySet());
            }

            return new Advancement(id, this.parentObj, this.display, this.rewards, this.criteria, this.requirements);
         }
      }

      public Advancement build(Consumer exporter, String id) {
         Advancement lv = this.build(new Identifier(id));
         exporter.accept(lv);
         return lv;
      }

      public JsonObject toJson() {
         if (this.requirements == null) {
            this.requirements = this.merger.createRequirements(this.criteria.keySet());
         }

         JsonObject jsonObject = new JsonObject();
         if (this.parentObj != null) {
            jsonObject.addProperty("parent", this.parentObj.getId().toString());
         } else if (this.parentId != null) {
            jsonObject.addProperty("parent", this.parentId.toString());
         }

         if (this.display != null) {
            jsonObject.add("display", this.display.toJson());
         }

         jsonObject.add("rewards", this.rewards.toJson());
         JsonObject jsonObject2 = new JsonObject();
         Iterator var3 = this.criteria.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            jsonObject2.add((String)entry.getKey(), ((AdvancementCriterion)entry.getValue()).toJson());
         }

         jsonObject.add("criteria", jsonObject2);
         JsonArray jsonArray = new JsonArray();
         String[][] var14 = this.requirements;
         int var5 = var14.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String[] strings = var14[var6];
            JsonArray jsonArray2 = new JsonArray();
            String[] var9 = strings;
            int var10 = strings.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               String string = var9[var11];
               jsonArray2.add(string);
            }

            jsonArray.add(jsonArray2);
         }

         jsonObject.add("requirements", jsonArray);
         return jsonObject;
      }

      public void toPacket(PacketByteBuf buf) {
         if (this.requirements == null) {
            this.requirements = this.merger.createRequirements(this.criteria.keySet());
         }

         buf.writeNullable(this.parentId, PacketByteBuf::writeIdentifier);
         buf.writeNullable(this.display, (buf2, display) -> {
            display.toPacket(buf2);
         });
         AdvancementCriterion.criteriaToPacket(this.criteria, buf);
         buf.writeVarInt(this.requirements.length);
         String[][] var2 = this.requirements;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String[] strings = var2[var4];
            buf.writeVarInt(strings.length);
            String[] var6 = strings;
            int var7 = strings.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               String string = var6[var8];
               buf.writeString(string);
            }
         }

      }

      public String toString() {
         Identifier var10000 = this.parentId;
         return "Task Advancement{parentId=" + var10000 + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + "}";
      }

      public static Builder fromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer) {
         Identifier lv = obj.has("parent") ? new Identifier(JsonHelper.getString(obj, "parent")) : null;
         AdvancementDisplay lv2 = obj.has("display") ? AdvancementDisplay.fromJson(JsonHelper.getObject(obj, "display")) : null;
         AdvancementRewards lv3 = obj.has("rewards") ? AdvancementRewards.fromJson(JsonHelper.getObject(obj, "rewards")) : AdvancementRewards.NONE;
         Map map = AdvancementCriterion.criteriaFromJson(JsonHelper.getObject(obj, "criteria"), predicateDeserializer);
         if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
         } else {
            JsonArray jsonArray = JsonHelper.getArray(obj, "requirements", new JsonArray());
            String[][] strings = new String[jsonArray.size()][];

            int i;
            int j;
            for(i = 0; i < jsonArray.size(); ++i) {
               JsonArray jsonArray2 = JsonHelper.asArray(jsonArray.get(i), "requirements[" + i + "]");
               strings[i] = new String[jsonArray2.size()];

               for(j = 0; j < jsonArray2.size(); ++j) {
                  strings[i][j] = JsonHelper.asString(jsonArray2.get(j), "requirements[" + i + "][" + j + "]");
               }
            }

            if (strings.length == 0) {
               strings = new String[map.size()][];
               i = 0;

               String string;
               for(Iterator var16 = map.keySet().iterator(); var16.hasNext(); strings[i++] = new String[]{string}) {
                  string = (String)var16.next();
               }
            }

            String[][] var17 = strings;
            int var18 = strings.length;

            int var13;
            for(j = 0; j < var18; ++j) {
               String[] strings2 = var17[j];
               if (strings2.length == 0 && map.isEmpty()) {
                  throw new JsonSyntaxException("Requirement entry cannot be empty");
               }

               String[] var12 = strings2;
               var13 = strings2.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  String string2 = var12[var14];
                  if (!map.containsKey(string2)) {
                     throw new JsonSyntaxException("Unknown required criterion '" + string2 + "'");
                  }
               }
            }

            Iterator var19 = map.keySet().iterator();

            String string3;
            boolean bl;
            do {
               if (!var19.hasNext()) {
                  return new Builder(lv, lv2, lv3, map, strings);
               }

               string3 = (String)var19.next();
               bl = false;
               String[][] var22 = strings;
               int var24 = strings.length;

               for(var13 = 0; var13 < var24; ++var13) {
                  String[] strings3 = var22[var13];
                  if (ArrayUtils.contains(strings3, string3)) {
                     bl = true;
                     break;
                  }
               }
            } while(bl);

            throw new JsonSyntaxException("Criterion '" + string3 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
         }
      }

      public static Builder fromPacket(PacketByteBuf buf) {
         Identifier lv = (Identifier)buf.readNullable(PacketByteBuf::readIdentifier);
         AdvancementDisplay lv2 = (AdvancementDisplay)buf.readNullable(AdvancementDisplay::fromPacket);
         Map map = AdvancementCriterion.criteriaFromPacket(buf);
         String[][] strings = new String[buf.readVarInt()][];

         for(int i = 0; i < strings.length; ++i) {
            strings[i] = new String[buf.readVarInt()];

            for(int j = 0; j < strings[i].length; ++j) {
               strings[i][j] = buf.readString();
            }
         }

         return new Builder(lv, lv2, AdvancementRewards.NONE, map, strings);
      }

      public Map getCriteria() {
         return this.criteria;
      }
   }
}
