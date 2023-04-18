package net.minecraft.entity.boss.dragon;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.EndConfiguredFeatures;
import net.minecraft.world.gen.feature.EndPortalFeature;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EnderDragonFight {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CHECK_DRAGON_SEEN_INTERVAL = 1200;
   private static final int CRYSTAL_COUNTING_INTERVAL = 100;
   private static final int field_31445 = 20;
   private static final int ISLAND_SIZE = 8;
   public static final int field_31441 = 9;
   private static final int PLAYER_COUNTING_INTERVAL = 20;
   private static final int field_31448 = 96;
   public static final int SPAWN_Y = 128;
   private static final Predicate VALID_ENTITY;
   private final ServerBossBar bossBar;
   private final ServerWorld world;
   private final ObjectArrayList gateways;
   private final BlockPattern endPortalPattern;
   private int dragonSeenTimer;
   private int endCrystalsAlive;
   private int crystalCountTimer;
   private int playerUpdateTimer;
   private boolean dragonKilled;
   private boolean previouslyKilled;
   @Nullable
   private UUID dragonUuid;
   private boolean doLegacyCheck;
   @Nullable
   private BlockPos exitPortalLocation;
   @Nullable
   private EnderDragonSpawnState dragonSpawnState;
   private int spawnStateTimer;
   @Nullable
   private List crystals;

   public EnderDragonFight(ServerWorld world, long gatewaysSeed, NbtCompound nbt) {
      this.bossBar = (ServerBossBar)(new ServerBossBar(Text.translatable("entity.minecraft.ender_dragon"), BossBar.Color.PINK, BossBar.Style.PROGRESS)).setDragonMusic(true).setThickenFog(true);
      this.gateways = new ObjectArrayList();
      this.doLegacyCheck = true;
      this.world = world;
      if (nbt.contains("NeedsStateScanning")) {
         this.doLegacyCheck = nbt.getBoolean("NeedsStateScanning");
      }

      if (nbt.contains("DragonKilled", NbtElement.NUMBER_TYPE)) {
         if (nbt.containsUuid("Dragon")) {
            this.dragonUuid = nbt.getUuid("Dragon");
         }

         this.dragonKilled = nbt.getBoolean("DragonKilled");
         this.previouslyKilled = nbt.getBoolean("PreviouslyKilled");
         if (nbt.getBoolean("IsRespawning")) {
            this.dragonSpawnState = EnderDragonSpawnState.START;
         }

         if (nbt.contains("ExitPortalLocation", NbtElement.COMPOUND_TYPE)) {
            this.exitPortalLocation = NbtHelper.toBlockPos(nbt.getCompound("ExitPortalLocation"));
         }
      } else {
         this.dragonKilled = true;
         this.previouslyKilled = true;
      }

      if (nbt.contains("Gateways", NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList("Gateways", NbtElement.INT_TYPE);

         for(int i = 0; i < lv.size(); ++i) {
            this.gateways.add(lv.getInt(i));
         }
      } else {
         this.gateways.addAll(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
         Util.shuffle(this.gateways, Random.create(gatewaysSeed));
      }

      this.endPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', CachedBlockPosition.matchesBlockState(BlockPredicate.make(Blocks.BEDROCK))).build();
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      lv.putBoolean("NeedsStateScanning", this.doLegacyCheck);
      if (this.dragonUuid != null) {
         lv.putUuid("Dragon", this.dragonUuid);
      }

      lv.putBoolean("DragonKilled", this.dragonKilled);
      lv.putBoolean("PreviouslyKilled", this.previouslyKilled);
      if (this.exitPortalLocation != null) {
         lv.put("ExitPortalLocation", NbtHelper.fromBlockPos(this.exitPortalLocation));
      }

      NbtList lv2 = new NbtList();
      ObjectListIterator var3 = this.gateways.iterator();

      while(var3.hasNext()) {
         int i = (Integer)var3.next();
         lv2.add(NbtInt.of(i));
      }

      lv.put("Gateways", lv2);
      return lv;
   }

   public void tick() {
      this.bossBar.setVisible(!this.dragonKilled);
      if (++this.playerUpdateTimer >= 20) {
         this.updatePlayers();
         this.playerUpdateTimer = 0;
      }

      if (!this.bossBar.getPlayers().isEmpty()) {
         this.world.getChunkManager().addTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
         boolean bl = this.loadChunks();
         if (this.doLegacyCheck && bl) {
            this.convertFromLegacy();
            this.doLegacyCheck = false;
         }

         if (this.dragonSpawnState != null) {
            if (this.crystals == null && bl) {
               this.dragonSpawnState = null;
               this.respawnDragon();
            }

            this.dragonSpawnState.run(this.world, this, this.crystals, this.spawnStateTimer++, this.exitPortalLocation);
         }

         if (!this.dragonKilled) {
            if ((this.dragonUuid == null || ++this.dragonSeenTimer >= 1200) && bl) {
               this.checkDragonSeen();
               this.dragonSeenTimer = 0;
            }

            if (++this.crystalCountTimer >= 100 && bl) {
               this.countAliveCrystals();
               this.crystalCountTimer = 0;
            }
         }
      } else {
         this.world.getChunkManager().removeTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
      }

   }

   private void convertFromLegacy() {
      LOGGER.info("Scanning for legacy world dragon fight...");
      boolean bl = this.worldContainsEndPortal();
      if (bl) {
         LOGGER.info("Found that the dragon has been killed in this world already.");
         this.previouslyKilled = true;
      } else {
         LOGGER.info("Found that the dragon has not yet been killed in this world.");
         this.previouslyKilled = false;
         if (this.findEndPortal() == null) {
            this.generateEndPortal(false);
         }
      }

      List list = this.world.getAliveEnderDragons();
      if (list.isEmpty()) {
         this.dragonKilled = true;
      } else {
         EnderDragonEntity lv = (EnderDragonEntity)list.get(0);
         this.dragonUuid = lv.getUuid();
         LOGGER.info("Found that there's a dragon still alive ({})", lv);
         this.dragonKilled = false;
         if (!bl) {
            LOGGER.info("But we didn't have a portal, let's remove it.");
            lv.discard();
            this.dragonUuid = null;
         }
      }

      if (!this.previouslyKilled && this.dragonKilled) {
         this.dragonKilled = false;
      }

   }

   private void checkDragonSeen() {
      List list = this.world.getAliveEnderDragons();
      if (list.isEmpty()) {
         LOGGER.debug("Haven't seen the dragon, respawning it");
         this.createDragon();
      } else {
         LOGGER.debug("Haven't seen our dragon, but found another one to use.");
         this.dragonUuid = ((EnderDragonEntity)list.get(0)).getUuid();
      }

   }

   protected void setSpawnState(EnderDragonSpawnState spawnState) {
      if (this.dragonSpawnState == null) {
         throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
      } else {
         this.spawnStateTimer = 0;
         if (spawnState == EnderDragonSpawnState.END) {
            this.dragonSpawnState = null;
            this.dragonKilled = false;
            EnderDragonEntity lv = this.createDragon();
            if (lv != null) {
               Iterator var3 = this.bossBar.getPlayers().iterator();

               while(var3.hasNext()) {
                  ServerPlayerEntity lv2 = (ServerPlayerEntity)var3.next();
                  Criteria.SUMMONED_ENTITY.trigger(lv2, lv);
               }
            }
         } else {
            this.dragonSpawnState = spawnState;
         }

      }
   }

   private boolean worldContainsEndPortal() {
      for(int i = -8; i <= 8; ++i) {
         for(int j = -8; j <= 8; ++j) {
            WorldChunk lv = this.world.getChunk(i, j);
            Iterator var4 = lv.getBlockEntities().values().iterator();

            while(var4.hasNext()) {
               BlockEntity lv2 = (BlockEntity)var4.next();
               if (lv2 instanceof EndPortalBlockEntity) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Nullable
   private BlockPattern.Result findEndPortal() {
      int i;
      int j;
      for(i = -8; i <= 8; ++i) {
         for(j = -8; j <= 8; ++j) {
            WorldChunk lv = this.world.getChunk(i, j);
            Iterator var4 = lv.getBlockEntities().values().iterator();

            while(var4.hasNext()) {
               BlockEntity lv2 = (BlockEntity)var4.next();
               if (lv2 instanceof EndPortalBlockEntity) {
                  BlockPattern.Result lv3 = this.endPortalPattern.searchAround(this.world, lv2.getPos());
                  if (lv3 != null) {
                     BlockPos lv4 = lv3.translate(3, 3, 3).getBlockPos();
                     if (this.exitPortalLocation == null) {
                        this.exitPortalLocation = lv4;
                     }

                     return lv3;
                  }
               }
            }
         }
      }

      i = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN).getY();

      for(j = i; j >= this.world.getBottomY(); --j) {
         BlockPattern.Result lv5 = this.endPortalPattern.searchAround(this.world, new BlockPos(EndPortalFeature.ORIGIN.getX(), j, EndPortalFeature.ORIGIN.getZ()));
         if (lv5 != null) {
            if (this.exitPortalLocation == null) {
               this.exitPortalLocation = lv5.translate(3, 3, 3).getBlockPos();
            }

            return lv5;
         }
      }

      return null;
   }

   private boolean loadChunks() {
      for(int i = -8; i <= 8; ++i) {
         for(int j = 8; j <= 8; ++j) {
            Chunk lv = this.world.getChunk(i, j, ChunkStatus.FULL, false);
            if (!(lv instanceof WorldChunk)) {
               return false;
            }

            ChunkHolder.LevelType lv2 = ((WorldChunk)lv).getLevelType();
            if (!lv2.isAfter(ChunkHolder.LevelType.TICKING)) {
               return false;
            }
         }
      }

      return true;
   }

   private void updatePlayers() {
      Set set = Sets.newHashSet();
      Iterator var2 = this.world.getPlayers(VALID_ENTITY).iterator();

      while(var2.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var2.next();
         this.bossBar.addPlayer(lv);
         set.add(lv);
      }

      Set set2 = Sets.newHashSet(this.bossBar.getPlayers());
      set2.removeAll(set);
      Iterator var6 = set2.iterator();

      while(var6.hasNext()) {
         ServerPlayerEntity lv2 = (ServerPlayerEntity)var6.next();
         this.bossBar.removePlayer(lv2);
      }

   }

   private void countAliveCrystals() {
      this.crystalCountTimer = 0;
      this.endCrystalsAlive = 0;

      EndSpikeFeature.Spike lv;
      for(Iterator var1 = EndSpikeFeature.getSpikes(this.world).iterator(); var1.hasNext(); this.endCrystalsAlive += this.world.getNonSpectatingEntities(EndCrystalEntity.class, lv.getBoundingBox()).size()) {
         lv = (EndSpikeFeature.Spike)var1.next();
      }

      LOGGER.debug("Found {} end crystals still alive", this.endCrystalsAlive);
   }

   public void dragonKilled(EnderDragonEntity dragon) {
      if (dragon.getUuid().equals(this.dragonUuid)) {
         this.bossBar.setPercent(0.0F);
         this.bossBar.setVisible(false);
         this.generateEndPortal(true);
         this.generateNewEndGateway();
         if (!this.previouslyKilled) {
            this.world.setBlockState(this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN), Blocks.DRAGON_EGG.getDefaultState());
         }

         this.previouslyKilled = true;
         this.dragonKilled = true;
      }

   }

   private void generateNewEndGateway() {
      if (!this.gateways.isEmpty()) {
         int i = (Integer)this.gateways.remove(this.gateways.size() - 1);
         int j = MathHelper.floor(96.0 * Math.cos(2.0 * (-3.141592653589793 + 0.15707963267948966 * (double)i)));
         int k = MathHelper.floor(96.0 * Math.sin(2.0 * (-3.141592653589793 + 0.15707963267948966 * (double)i)));
         this.generateEndGateway(new BlockPos(j, 75, k));
      }
   }

   private void generateEndGateway(BlockPos pos) {
      this.world.syncWorldEvent(WorldEvents.END_GATEWAY_SPAWNS, pos, 0);
      this.world.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE).flatMap((arg) -> {
         return arg.getEntry(EndConfiguredFeatures.END_GATEWAY_DELAYED);
      }).ifPresent((arg2) -> {
         ((ConfiguredFeature)arg2.value()).generate(this.world, this.world.getChunkManager().getChunkGenerator(), Random.create(), pos);
      });
   }

   private void generateEndPortal(boolean previouslyKilled) {
      EndPortalFeature lv = new EndPortalFeature(previouslyKilled);
      if (this.exitPortalLocation == null) {
         for(this.exitPortalLocation = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN).down(); this.world.getBlockState(this.exitPortalLocation).isOf(Blocks.BEDROCK) && this.exitPortalLocation.getY() > this.world.getSeaLevel(); this.exitPortalLocation = this.exitPortalLocation.down()) {
         }
      }

      lv.generateIfValid(FeatureConfig.DEFAULT, this.world, this.world.getChunkManager().getChunkGenerator(), Random.create(), this.exitPortalLocation);
   }

   @Nullable
   private EnderDragonEntity createDragon() {
      this.world.getWorldChunk(new BlockPos(0, 128, 0));
      EnderDragonEntity lv = (EnderDragonEntity)EntityType.ENDER_DRAGON.create(this.world);
      if (lv != null) {
         lv.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
         lv.refreshPositionAndAngles(0.0, 128.0, 0.0, this.world.random.nextFloat() * 360.0F, 0.0F);
         this.world.spawnEntity(lv);
         this.dragonUuid = lv.getUuid();
      }

      return lv;
   }

   public void updateFight(EnderDragonEntity dragon) {
      if (dragon.getUuid().equals(this.dragonUuid)) {
         this.bossBar.setPercent(dragon.getHealth() / dragon.getMaxHealth());
         this.dragonSeenTimer = 0;
         if (dragon.hasCustomName()) {
            this.bossBar.setName(dragon.getDisplayName());
         }
      }

   }

   public int getAliveEndCrystals() {
      return this.endCrystalsAlive;
   }

   public void crystalDestroyed(EndCrystalEntity enderCrystal, DamageSource source) {
      if (this.dragonSpawnState != null && this.crystals.contains(enderCrystal)) {
         LOGGER.debug("Aborting respawn sequence");
         this.dragonSpawnState = null;
         this.spawnStateTimer = 0;
         this.resetEndCrystals();
         this.generateEndPortal(true);
      } else {
         this.countAliveCrystals();
         Entity lv = this.world.getEntity(this.dragonUuid);
         if (lv instanceof EnderDragonEntity) {
            ((EnderDragonEntity)lv).crystalDestroyed(enderCrystal, enderCrystal.getBlockPos(), source);
         }
      }

   }

   public boolean hasPreviouslyKilled() {
      return this.previouslyKilled;
   }

   public void respawnDragon() {
      if (this.dragonKilled && this.dragonSpawnState == null) {
         BlockPos lv = this.exitPortalLocation;
         if (lv == null) {
            LOGGER.debug("Tried to respawn, but need to find the portal first.");
            BlockPattern.Result lv2 = this.findEndPortal();
            if (lv2 == null) {
               LOGGER.debug("Couldn't find a portal, so we made one.");
               this.generateEndPortal(true);
            } else {
               LOGGER.debug("Found the exit portal & saved its location for next time.");
            }

            lv = this.exitPortalLocation;
         }

         List list = Lists.newArrayList();
         BlockPos lv3 = lv.up(1);
         Iterator var4 = Direction.Type.HORIZONTAL.iterator();

         while(var4.hasNext()) {
            Direction lv4 = (Direction)var4.next();
            List list2 = this.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(lv3.offset((Direction)lv4, 2)));
            if (list2.isEmpty()) {
               return;
            }

            list.addAll(list2);
         }

         LOGGER.debug("Found all crystals, respawning dragon.");
         this.respawnDragon(list);
      }

   }

   private void respawnDragon(List crystals) {
      if (this.dragonKilled && this.dragonSpawnState == null) {
         for(BlockPattern.Result lv = this.findEndPortal(); lv != null; lv = this.findEndPortal()) {
            for(int i = 0; i < this.endPortalPattern.getWidth(); ++i) {
               for(int j = 0; j < this.endPortalPattern.getHeight(); ++j) {
                  for(int k = 0; k < this.endPortalPattern.getDepth(); ++k) {
                     CachedBlockPosition lv2 = lv.translate(i, j, k);
                     if (lv2.getBlockState().isOf(Blocks.BEDROCK) || lv2.getBlockState().isOf(Blocks.END_PORTAL)) {
                        this.world.setBlockState(lv2.getBlockPos(), Blocks.END_STONE.getDefaultState());
                     }
                  }
               }
            }
         }

         this.dragonSpawnState = EnderDragonSpawnState.START;
         this.spawnStateTimer = 0;
         this.generateEndPortal(false);
         this.crystals = crystals;
      }

   }

   public void resetEndCrystals() {
      Iterator var1 = EndSpikeFeature.getSpikes(this.world).iterator();

      while(var1.hasNext()) {
         EndSpikeFeature.Spike lv = (EndSpikeFeature.Spike)var1.next();
         List list = this.world.getNonSpectatingEntities(EndCrystalEntity.class, lv.getBoundingBox());
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            EndCrystalEntity lv2 = (EndCrystalEntity)var4.next();
            lv2.setInvulnerable(false);
            lv2.setBeamTarget((BlockPos)null);
         }
      }

   }

   static {
      VALID_ENTITY = EntityPredicates.VALID_ENTITY.and(EntityPredicates.maxDistance(0.0, 128.0, 0.0, 192.0));
   }
}
