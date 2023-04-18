package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemScatterer {
   public static void spawn(World world, BlockPos pos, Inventory inventory) {
      spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), inventory);
   }

   public static void spawn(World world, Entity entity, Inventory inventory) {
      spawn(world, entity.getX(), entity.getY(), entity.getZ(), inventory);
   }

   private static void spawn(World world, double x, double y, double z, Inventory inventory) {
      for(int i = 0; i < inventory.size(); ++i) {
         spawn(world, x, y, z, inventory.getStack(i));
      }

   }

   public static void spawn(World world, BlockPos pos, DefaultedList stacks) {
      stacks.forEach((stack) -> {
         spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), stack);
      });
   }

   public static void spawn(World world, double x, double y, double z, ItemStack stack) {
      double g = (double)EntityType.ITEM.getWidth();
      double h = 1.0 - g;
      double i = g / 2.0;
      double j = Math.floor(x) + world.random.nextDouble() * h + i;
      double k = Math.floor(y) + world.random.nextDouble() * h;
      double l = Math.floor(z) + world.random.nextDouble() * h + i;

      while(!stack.isEmpty()) {
         ItemEntity lv = new ItemEntity(world, j, k, l, stack.split(world.random.nextInt(21) + 10));
         float m = 0.05F;
         lv.setVelocity(world.random.nextTriangular(0.0, 0.11485000171139836), world.random.nextTriangular(0.2, 0.11485000171139836), world.random.nextTriangular(0.0, 0.11485000171139836));
         world.spawnEntity(lv);
      }

   }
}
