package net.minecraft.entity.boss;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BossBarManager {
   private final Map commandBossBars = Maps.newHashMap();

   @Nullable
   public CommandBossBar get(Identifier id) {
      return (CommandBossBar)this.commandBossBars.get(id);
   }

   public CommandBossBar add(Identifier id, Text displayName) {
      CommandBossBar lv = new CommandBossBar(id, displayName);
      this.commandBossBars.put(id, lv);
      return lv;
   }

   public void remove(CommandBossBar bossBar) {
      this.commandBossBars.remove(bossBar.getId());
   }

   public Collection getIds() {
      return this.commandBossBars.keySet();
   }

   public Collection getAll() {
      return this.commandBossBars.values();
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      Iterator var2 = this.commandBossBars.values().iterator();

      while(var2.hasNext()) {
         CommandBossBar lv2 = (CommandBossBar)var2.next();
         lv.put(lv2.getId().toString(), lv2.toNbt());
      }

      return lv;
   }

   public void readNbt(NbtCompound nbt) {
      Iterator var2 = nbt.getKeys().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         Identifier lv = new Identifier(string);
         this.commandBossBars.put(lv, CommandBossBar.fromNbt(nbt.getCompound(string), lv));
      }

   }

   public void onPlayerConnect(ServerPlayerEntity player) {
      Iterator var2 = this.commandBossBars.values().iterator();

      while(var2.hasNext()) {
         CommandBossBar lv = (CommandBossBar)var2.next();
         lv.onPlayerConnect(player);
      }

   }

   public void onPlayerDisconnect(ServerPlayerEntity player) {
      Iterator var2 = this.commandBossBars.values().iterator();

      while(var2.hasNext()) {
         CommandBossBar lv = (CommandBossBar)var2.next();
         lv.onPlayerDisconnect(player);
      }

   }
}
