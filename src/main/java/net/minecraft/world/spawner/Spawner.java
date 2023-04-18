package net.minecraft.world.spawner;

import net.minecraft.server.world.ServerWorld;

public interface Spawner {
   int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals);
}
