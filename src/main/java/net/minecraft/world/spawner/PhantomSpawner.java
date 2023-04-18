package net.minecraft.world.spawner;

import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;

public class PhantomSpawner implements Spawner {
   private int cooldown;

   public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
      if (!spawnMonsters) {
         return 0;
      } else if (!world.getGameRules().getBoolean(GameRules.DO_INSOMNIA)) {
         return 0;
      } else {
         Random lv = world.random;
         --this.cooldown;
         if (this.cooldown > 0) {
            return 0;
         } else {
            this.cooldown += (60 + lv.nextInt(60)) * 20;
            if (world.getAmbientDarkness() < 5 && world.getDimension().hasSkyLight()) {
               return 0;
            } else {
               int i = 0;
               Iterator var6 = world.getPlayers().iterator();

               while(true) {
                  LocalDifficulty lv4;
                  BlockPos lv6;
                  BlockState lv7;
                  FluidState lv8;
                  do {
                     BlockPos lv3;
                     int j;
                     do {
                        PlayerEntity lv2;
                        do {
                           do {
                              do {
                                 if (!var6.hasNext()) {
                                    return i;
                                 }

                                 lv2 = (PlayerEntity)var6.next();
                              } while(lv2.isSpectator());

                              lv3 = lv2.getBlockPos();
                           } while(world.getDimension().hasSkyLight() && (lv3.getY() < world.getSeaLevel() || !world.isSkyVisible(lv3)));

                           lv4 = world.getLocalDifficulty(lv3);
                        } while(!lv4.isHarderThan(lv.nextFloat() * 3.0F));

                        ServerStatHandler lv5 = ((ServerPlayerEntity)lv2).getStatHandler();
                        j = MathHelper.clamp(lv5.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                        int k = true;
                     } while(lv.nextInt(j) < 72000);

                     lv6 = lv3.up(20 + lv.nextInt(15)).east(-10 + lv.nextInt(21)).south(-10 + lv.nextInt(21));
                     lv7 = world.getBlockState(lv6);
                     lv8 = world.getFluidState(lv6);
                  } while(!SpawnHelper.isClearForSpawn(world, lv6, lv7, lv8, EntityType.PHANTOM));

                  EntityData lv9 = null;
                  int l = 1 + lv.nextInt(lv4.getGlobalDifficulty().getId() + 1);

                  for(int m = 0; m < l; ++m) {
                     PhantomEntity lv10 = (PhantomEntity)EntityType.PHANTOM.create(world);
                     if (lv10 != null) {
                        lv10.refreshPositionAndAngles(lv6, 0.0F, 0.0F);
                        lv9 = lv10.initialize(world, lv4, SpawnReason.NATURAL, lv9, (NbtCompound)null);
                        world.spawnEntityAndPassengers(lv10);
                        ++i;
                     }
                  }
               }
            }
         }
      }
   }
}
