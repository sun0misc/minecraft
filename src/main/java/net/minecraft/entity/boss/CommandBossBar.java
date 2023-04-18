package net.minecraft.entity.boss;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CommandBossBar extends ServerBossBar {
   private final Identifier id;
   private final Set playerUuids = Sets.newHashSet();
   private int value;
   private int maxValue = 100;

   public CommandBossBar(Identifier id, Text displayName) {
      super(displayName, BossBar.Color.WHITE, BossBar.Style.PROGRESS);
      this.id = id;
      this.setPercent(0.0F);
   }

   public Identifier getId() {
      return this.id;
   }

   public void addPlayer(ServerPlayerEntity player) {
      super.addPlayer(player);
      this.playerUuids.add(player.getUuid());
   }

   public void addPlayer(UUID uuid) {
      this.playerUuids.add(uuid);
   }

   public void removePlayer(ServerPlayerEntity player) {
      super.removePlayer(player);
      this.playerUuids.remove(player.getUuid());
   }

   public void clearPlayers() {
      super.clearPlayers();
      this.playerUuids.clear();
   }

   public int getValue() {
      return this.value;
   }

   public int getMaxValue() {
      return this.maxValue;
   }

   public void setValue(int value) {
      this.value = value;
      this.setPercent(MathHelper.clamp((float)value / (float)this.maxValue, 0.0F, 1.0F));
   }

   public void setMaxValue(int maxValue) {
      this.maxValue = maxValue;
      this.setPercent(MathHelper.clamp((float)this.value / (float)maxValue, 0.0F, 1.0F));
   }

   public final Text toHoverableText() {
      return Texts.bracketed(this.getName()).styled((style) -> {
         return style.withColor(this.getColor().getTextFormat()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(this.getId().toString()))).withInsertion(this.getId().toString());
      });
   }

   public boolean addPlayers(Collection players) {
      Set set = Sets.newHashSet();
      Set set2 = Sets.newHashSet();
      Iterator var4 = this.playerUuids.iterator();

      UUID uUID;
      boolean bl;
      Iterator var7;
      while(var4.hasNext()) {
         uUID = (UUID)var4.next();
         bl = false;
         var7 = players.iterator();

         while(var7.hasNext()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var7.next();
            if (lv.getUuid().equals(uUID)) {
               bl = true;
               break;
            }
         }

         if (!bl) {
            set.add(uUID);
         }
      }

      var4 = players.iterator();

      ServerPlayerEntity lv2;
      while(var4.hasNext()) {
         lv2 = (ServerPlayerEntity)var4.next();
         bl = false;
         var7 = this.playerUuids.iterator();

         while(var7.hasNext()) {
            UUID uUID2 = (UUID)var7.next();
            if (lv2.getUuid().equals(uUID2)) {
               bl = true;
               break;
            }
         }

         if (!bl) {
            set2.add(lv2);
         }
      }

      for(var4 = set.iterator(); var4.hasNext(); this.playerUuids.remove(uUID)) {
         uUID = (UUID)var4.next();
         Iterator var11 = this.getPlayers().iterator();

         while(var11.hasNext()) {
            ServerPlayerEntity lv3 = (ServerPlayerEntity)var11.next();
            if (lv3.getUuid().equals(uUID)) {
               this.removePlayer(lv3);
               break;
            }
         }
      }

      var4 = set2.iterator();

      while(var4.hasNext()) {
         lv2 = (ServerPlayerEntity)var4.next();
         this.addPlayer(lv2);
      }

      return !set.isEmpty() || !set2.isEmpty();
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      lv.putString("Name", Text.Serializer.toJson(this.name));
      lv.putBoolean("Visible", this.isVisible());
      lv.putInt("Value", this.value);
      lv.putInt("Max", this.maxValue);
      lv.putString("Color", this.getColor().getName());
      lv.putString("Overlay", this.getStyle().getName());
      lv.putBoolean("DarkenScreen", this.shouldDarkenSky());
      lv.putBoolean("PlayBossMusic", this.hasDragonMusic());
      lv.putBoolean("CreateWorldFog", this.shouldThickenFog());
      NbtList lv2 = new NbtList();
      Iterator var3 = this.playerUuids.iterator();

      while(var3.hasNext()) {
         UUID uUID = (UUID)var3.next();
         lv2.add(NbtHelper.fromUuid(uUID));
      }

      lv.put("Players", lv2);
      return lv;
   }

   public static CommandBossBar fromNbt(NbtCompound nbt, Identifier id) {
      CommandBossBar lv = new CommandBossBar(id, Text.Serializer.fromJson(nbt.getString("Name")));
      lv.setVisible(nbt.getBoolean("Visible"));
      lv.setValue(nbt.getInt("Value"));
      lv.setMaxValue(nbt.getInt("Max"));
      lv.setColor(BossBar.Color.byName(nbt.getString("Color")));
      lv.setStyle(BossBar.Style.byName(nbt.getString("Overlay")));
      lv.setDarkenSky(nbt.getBoolean("DarkenScreen"));
      lv.setDragonMusic(nbt.getBoolean("PlayBossMusic"));
      lv.setThickenFog(nbt.getBoolean("CreateWorldFog"));
      NbtList lv2 = nbt.getList("Players", NbtElement.INT_ARRAY_TYPE);

      for(int i = 0; i < lv2.size(); ++i) {
         lv.addPlayer(NbtHelper.toUuid(lv2.get(i)));
      }

      return lv;
   }

   public void onPlayerConnect(ServerPlayerEntity player) {
      if (this.playerUuids.contains(player.getUuid())) {
         this.addPlayer(player);
      }

   }

   public void onPlayerDisconnect(ServerPlayerEntity player) {
      super.removePlayer(player);
   }
}
