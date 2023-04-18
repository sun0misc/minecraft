package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.EndConfiguredFeatures;
import net.minecraft.world.gen.feature.EndGatewayFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EndGatewayBlockEntity extends EndPortalBlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_31368 = 200;
   private static final int field_31369 = 40;
   private static final int field_31370 = 2400;
   private static final int field_31371 = 1;
   private static final int field_31372 = 10;
   private long age;
   private int teleportCooldown;
   @Nullable
   private BlockPos exitPortalPos;
   private boolean exactTeleport;

   public EndGatewayBlockEntity(BlockPos arg, BlockState arg2) {
      super(BlockEntityType.END_GATEWAY, arg, arg2);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putLong("Age", this.age);
      if (this.exitPortalPos != null) {
         nbt.put("ExitPortal", NbtHelper.fromBlockPos(this.exitPortalPos));
      }

      if (this.exactTeleport) {
         nbt.putBoolean("ExactTeleport", true);
      }

   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.age = nbt.getLong("Age");
      if (nbt.contains("ExitPortal", NbtElement.COMPOUND_TYPE)) {
         BlockPos lv = NbtHelper.toBlockPos(nbt.getCompound("ExitPortal"));
         if (World.isValid(lv)) {
            this.exitPortalPos = lv;
         }
      }

      this.exactTeleport = nbt.getBoolean("ExactTeleport");
   }

   public static void clientTick(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
      ++blockEntity.age;
      if (blockEntity.needsCooldownBeforeTeleporting()) {
         --blockEntity.teleportCooldown;
      }

   }

   public static void serverTick(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
      boolean bl = blockEntity.isRecentlyGenerated();
      boolean bl2 = blockEntity.needsCooldownBeforeTeleporting();
      ++blockEntity.age;
      if (bl2) {
         --blockEntity.teleportCooldown;
      } else {
         List list = world.getEntitiesByClass(Entity.class, new Box(pos), EndGatewayBlockEntity::canTeleport);
         if (!list.isEmpty()) {
            tryTeleportingEntity(world, pos, state, (Entity)list.get(world.random.nextInt(list.size())), blockEntity);
         }

         if (blockEntity.age % 2400L == 0L) {
            startTeleportCooldown(world, pos, state, blockEntity);
         }
      }

      if (bl != blockEntity.isRecentlyGenerated() || bl2 != blockEntity.needsCooldownBeforeTeleporting()) {
         markDirty(world, pos, state);
      }

   }

   public static boolean canTeleport(Entity entity) {
      return EntityPredicates.EXCEPT_SPECTATOR.test(entity) && !entity.getRootVehicle().hasPortalCooldown();
   }

   public boolean isRecentlyGenerated() {
      return this.age < 200L;
   }

   public boolean needsCooldownBeforeTeleporting() {
      return this.teleportCooldown > 0;
   }

   public float getRecentlyGeneratedBeamHeight(float tickDelta) {
      return MathHelper.clamp(((float)this.age + tickDelta) / 200.0F, 0.0F, 1.0F);
   }

   public float getCooldownBeamHeight(float tickDelta) {
      return 1.0F - MathHelper.clamp(((float)this.teleportCooldown - tickDelta) / 40.0F, 0.0F, 1.0F);
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   private static void startTeleportCooldown(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
      if (!world.isClient) {
         blockEntity.teleportCooldown = 40;
         world.addSyncedBlockEvent(pos, state.getBlock(), 1, 0);
         markDirty(world, pos, state);
      }

   }

   public boolean onSyncedBlockEvent(int type, int data) {
      if (type == 1) {
         this.teleportCooldown = 40;
         return true;
      } else {
         return super.onSyncedBlockEvent(type, data);
      }
   }

   public static void tryTeleportingEntity(World world, BlockPos pos, BlockState state, Entity entity, EndGatewayBlockEntity blockEntity) {
      if (world instanceof ServerWorld lv && !blockEntity.needsCooldownBeforeTeleporting()) {
         blockEntity.teleportCooldown = 100;
         BlockPos lv2;
         if (blockEntity.exitPortalPos == null && world.getRegistryKey() == World.END) {
            lv2 = setupExitPortalLocation(lv, pos);
            lv2 = lv2.up(10);
            LOGGER.debug("Creating portal at {}", lv2);
            createPortal(lv, lv2, EndGatewayFeatureConfig.createConfig(pos, false));
            blockEntity.exitPortalPos = lv2;
         }

         if (blockEntity.exitPortalPos != null) {
            lv2 = blockEntity.exactTeleport ? blockEntity.exitPortalPos : findBestPortalExitPos(world, blockEntity.exitPortalPos);
            Entity lv4;
            if (entity instanceof EnderPearlEntity) {
               Entity lv3 = ((EnderPearlEntity)entity).getOwner();
               if (lv3 instanceof ServerPlayerEntity) {
                  Criteria.ENTER_BLOCK.trigger((ServerPlayerEntity)lv3, state);
               }

               if (lv3 != null) {
                  lv4 = lv3;
                  entity.discard();
               } else {
                  lv4 = entity;
               }
            } else {
               lv4 = entity.getRootVehicle();
            }

            lv4.resetPortalCooldown();
            lv4.teleport((double)lv2.getX() + 0.5, (double)lv2.getY(), (double)lv2.getZ() + 0.5);
         }

         startTeleportCooldown(world, pos, state, blockEntity);
      }
   }

   private static BlockPos findBestPortalExitPos(World world, BlockPos pos) {
      BlockPos lv = findExitPortalPos(world, pos.add(0, 2, 0), 5, false);
      LOGGER.debug("Best exit position for portal at {} is {}", pos, lv);
      return lv.up();
   }

   private static BlockPos setupExitPortalLocation(ServerWorld world, BlockPos pos) {
      Vec3d lv = findTeleportLocation(world, pos);
      WorldChunk lv2 = getChunk(world, lv);
      BlockPos lv3 = findPortalPosition(lv2);
      if (lv3 == null) {
         BlockPos lv4 = BlockPos.ofFloored(lv.x + 0.5, 75.0, lv.z + 0.5);
         LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", lv4);
         world.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE).flatMap((arg) -> {
            return arg.getEntry(EndConfiguredFeatures.END_ISLAND);
         }).ifPresent((arg3) -> {
            ((ConfiguredFeature)arg3.value()).generate(world, world.getChunkManager().getChunkGenerator(), Random.create(lv4.asLong()), lv4);
         });
         lv3 = lv4;
      } else {
         LOGGER.debug("Found suitable block to teleport to: {}", lv3);
      }

      return findExitPortalPos(world, lv3, 16, true);
   }

   private static Vec3d findTeleportLocation(ServerWorld world, BlockPos pos) {
      Vec3d lv = (new Vec3d((double)pos.getX(), 0.0, (double)pos.getZ())).normalize();
      int i = true;
      Vec3d lv2 = lv.multiply(1024.0);

      int j;
      for(j = 16; !isChunkEmpty(world, lv2) && j-- > 0; lv2 = lv2.add(lv.multiply(-16.0))) {
         LOGGER.debug("Skipping backwards past nonempty chunk at {}", lv2);
      }

      for(j = 16; isChunkEmpty(world, lv2) && j-- > 0; lv2 = lv2.add(lv.multiply(16.0))) {
         LOGGER.debug("Skipping forward past empty chunk at {}", lv2);
      }

      LOGGER.debug("Found chunk at {}", lv2);
      return lv2;
   }

   private static boolean isChunkEmpty(ServerWorld world, Vec3d pos) {
      return getChunk(world, pos).getHighestNonEmptySectionYOffset() <= world.getBottomY();
   }

   private static BlockPos findExitPortalPos(BlockView world, BlockPos pos, int searchRadius, boolean force) {
      BlockPos lv = null;

      for(int j = -searchRadius; j <= searchRadius; ++j) {
         for(int k = -searchRadius; k <= searchRadius; ++k) {
            if (j != 0 || k != 0 || force) {
               for(int l = world.getTopY() - 1; l > (lv == null ? world.getBottomY() : lv.getY()); --l) {
                  BlockPos lv2 = new BlockPos(pos.getX() + j, l, pos.getZ() + k);
                  BlockState lv3 = world.getBlockState(lv2);
                  if (lv3.isFullCube(world, lv2) && (force || !lv3.isOf(Blocks.BEDROCK))) {
                     lv = lv2;
                     break;
                  }
               }
            }
         }
      }

      return lv == null ? pos : lv;
   }

   private static WorldChunk getChunk(World world, Vec3d pos) {
      return world.getChunk(MathHelper.floor(pos.x / 16.0), MathHelper.floor(pos.z / 16.0));
   }

   @Nullable
   private static BlockPos findPortalPosition(WorldChunk chunk) {
      ChunkPos lv = chunk.getPos();
      BlockPos lv2 = new BlockPos(lv.getStartX(), 30, lv.getStartZ());
      int i = chunk.getHighestNonEmptySectionYOffset() + 16 - 1;
      BlockPos lv3 = new BlockPos(lv.getEndX(), i, lv.getEndZ());
      BlockPos lv4 = null;
      double d = 0.0;
      Iterator var8 = BlockPos.iterate(lv2, lv3).iterator();

      while(true) {
         BlockPos lv5;
         double e;
         do {
            BlockPos lv7;
            BlockPos lv8;
            do {
               BlockState lv6;
               do {
                  do {
                     if (!var8.hasNext()) {
                        return lv4;
                     }

                     lv5 = (BlockPos)var8.next();
                     lv6 = chunk.getBlockState(lv5);
                     lv7 = lv5.up();
                     lv8 = lv5.up(2);
                  } while(!lv6.isOf(Blocks.END_STONE));
               } while(chunk.getBlockState(lv7).isFullCube(chunk, lv7));
            } while(chunk.getBlockState(lv8).isFullCube(chunk, lv8));

            e = lv5.getSquaredDistanceFromCenter(0.0, 0.0, 0.0);
         } while(lv4 != null && !(e < d));

         lv4 = lv5;
         d = e;
      }
   }

   private static void createPortal(ServerWorld world, BlockPos pos, EndGatewayFeatureConfig config) {
      Feature.END_GATEWAY.generateIfValid(config, world, world.getChunkManager().getChunkGenerator(), Random.create(), pos);
   }

   public boolean shouldDrawSide(Direction direction) {
      return Block.shouldDrawSide(this.getCachedState(), this.world, this.getPos(), direction, this.getPos().offset(direction));
   }

   public int getDrawnSidesCount() {
      int i = 0;
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction lv = var2[var4];
         i += this.shouldDrawSide(lv) ? 1 : 0;
      }

      return i;
   }

   public void setExitPortalPos(BlockPos pos, boolean exactTeleport) {
      this.exactTeleport = exactTeleport;
      this.exitPortalPos = pos;
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
