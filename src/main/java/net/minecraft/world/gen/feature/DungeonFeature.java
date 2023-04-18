package net.minecraft.world.gen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.slf4j.Logger;

public class DungeonFeature extends Feature {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityType[] MOB_SPAWNER_ENTITIES;
   private static final BlockState AIR;

   public DungeonFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      Predicate predicate = Feature.notInBlockTagPredicate(BlockTags.FEATURES_CANNOT_REPLACE);
      BlockPos lv = context.getOrigin();
      Random lv2 = context.getRandom();
      StructureWorldAccess lv3 = context.getWorld();
      int i = true;
      int j = lv2.nextInt(2) + 2;
      int k = -j - 1;
      int l = j + 1;
      int m = true;
      int n = true;
      int o = lv2.nextInt(2) + 2;
      int p = -o - 1;
      int q = o + 1;
      int r = 0;

      int s;
      int t;
      int u;
      BlockPos lv4;
      for(s = k; s <= l; ++s) {
         for(t = -1; t <= 4; ++t) {
            for(u = p; u <= q; ++u) {
               lv4 = lv.add(s, t, u);
               Material lv5 = lv3.getBlockState(lv4).getMaterial();
               boolean bl = lv5.isSolid();
               if (t == -1 && !bl) {
                  return false;
               }

               if (t == 4 && !bl) {
                  return false;
               }

               if ((s == k || s == l || u == p || u == q) && t == 0 && lv3.isAir(lv4) && lv3.isAir(lv4.up())) {
                  ++r;
               }
            }
         }
      }

      if (r >= 1 && r <= 5) {
         for(s = k; s <= l; ++s) {
            for(t = 3; t >= -1; --t) {
               for(u = p; u <= q; ++u) {
                  lv4 = lv.add(s, t, u);
                  BlockState lv6 = lv3.getBlockState(lv4);
                  if (s != k && t != -1 && u != p && s != l && t != 4 && u != q) {
                     if (!lv6.isOf(Blocks.CHEST) && !lv6.isOf(Blocks.SPAWNER)) {
                        this.setBlockStateIf(lv3, lv4, AIR, predicate);
                     }
                  } else if (lv4.getY() >= lv3.getBottomY() && !lv3.getBlockState(lv4.down()).getMaterial().isSolid()) {
                     lv3.setBlockState(lv4, AIR, Block.NOTIFY_LISTENERS);
                  } else if (lv6.getMaterial().isSolid() && !lv6.isOf(Blocks.CHEST)) {
                     if (t == -1 && lv2.nextInt(4) != 0) {
                        this.setBlockStateIf(lv3, lv4, Blocks.MOSSY_COBBLESTONE.getDefaultState(), predicate);
                     } else {
                        this.setBlockStateIf(lv3, lv4, Blocks.COBBLESTONE.getDefaultState(), predicate);
                     }
                  }
               }
            }
         }

         for(s = 0; s < 2; ++s) {
            for(t = 0; t < 3; ++t) {
               u = lv.getX() + lv2.nextInt(j * 2 + 1) - j;
               int v = lv.getY();
               int w = lv.getZ() + lv2.nextInt(o * 2 + 1) - o;
               BlockPos lv7 = new BlockPos(u, v, w);
               if (lv3.isAir(lv7)) {
                  int x = 0;
                  Iterator var23 = Direction.Type.HORIZONTAL.iterator();

                  while(var23.hasNext()) {
                     Direction lv8 = (Direction)var23.next();
                     if (lv3.getBlockState(lv7.offset(lv8)).getMaterial().isSolid()) {
                        ++x;
                     }
                  }

                  if (x == 1) {
                     this.setBlockStateIf(lv3, lv7, StructurePiece.orientateChest(lv3, lv7, Blocks.CHEST.getDefaultState()), predicate);
                     LootableContainerBlockEntity.setLootTable(lv3, lv2, lv7, LootTables.SIMPLE_DUNGEON_CHEST);
                     break;
                  }
               }
            }
         }

         this.setBlockStateIf(lv3, lv, Blocks.SPAWNER.getDefaultState(), predicate);
         BlockEntity lv9 = lv3.getBlockEntity(lv);
         if (lv9 instanceof MobSpawnerBlockEntity) {
            MobSpawnerBlockEntity lv10 = (MobSpawnerBlockEntity)lv9;
            lv10.setEntityType(this.getMobSpawnerEntity(lv2), lv2);
         } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", new Object[]{lv.getX(), lv.getY(), lv.getZ()});
         }

         return true;
      } else {
         return false;
      }
   }

   private EntityType getMobSpawnerEntity(Random random) {
      return (EntityType)Util.getRandom((Object[])MOB_SPAWNER_ENTITIES, random);
   }

   static {
      MOB_SPAWNER_ENTITIES = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
      AIR = Blocks.CAVE_AIR.getDefaultState();
   }
}
