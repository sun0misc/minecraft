package net.minecraft.test;

import com.mojang.authlib.GameProfile;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class TestContext {
   private final GameTestState test;
   private boolean hasFinalClause;

   public TestContext(GameTestState test) {
      this.test = test;
   }

   public ServerWorld getWorld() {
      return this.test.getWorld();
   }

   public BlockState getBlockState(BlockPos pos) {
      return this.getWorld().getBlockState(this.getAbsolutePos(pos));
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.getWorld().getBlockEntity(this.getAbsolutePos(pos));
   }

   public void killAllEntities() {
      Box lv = this.getTestBox();
      List list = this.getWorld().getEntitiesByClass(Entity.class, lv.expand(1.0), (entity) -> {
         return !(entity instanceof PlayerEntity);
      });
      list.forEach(Entity::kill);
   }

   public ItemEntity spawnItem(Item item, float x, float y, float z) {
      ServerWorld lv = this.getWorld();
      Vec3d lv2 = this.getAbsolute(new Vec3d((double)x, (double)y, (double)z));
      ItemEntity lv3 = new ItemEntity(lv, lv2.x, lv2.y, lv2.z, new ItemStack(item, 1));
      lv3.setVelocity(0.0, 0.0, 0.0);
      lv.spawnEntity(lv3);
      return lv3;
   }

   public ItemEntity spawnItem(Item item, BlockPos pos) {
      return this.spawnItem(item, (float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
   }

   public Entity spawnEntity(EntityType type, BlockPos pos) {
      return this.spawnEntity(type, Vec3d.ofBottomCenter(pos));
   }

   public Entity spawnEntity(EntityType type, Vec3d pos) {
      ServerWorld lv = this.getWorld();
      Entity lv2 = type.create(lv);
      if (lv2 == null) {
         throw new NullPointerException("Failed to create entity " + type.getRegistryEntry().registryKey().getValue());
      } else {
         if (lv2 instanceof MobEntity) {
            MobEntity lv3 = (MobEntity)lv2;
            lv3.setPersistent();
         }

         Vec3d lv4 = this.getAbsolute(pos);
         lv2.refreshPositionAndAngles(lv4.x, lv4.y, lv4.z, lv2.getYaw(), lv2.getPitch());
         lv.spawnEntity(lv2);
         return lv2;
      }
   }

   public Entity spawnEntity(EntityType type, int x, int y, int z) {
      return this.spawnEntity(type, new BlockPos(x, y, z));
   }

   public Entity spawnEntity(EntityType type, float x, float y, float z) {
      return this.spawnEntity(type, new Vec3d((double)x, (double)y, (double)z));
   }

   public MobEntity spawnMob(EntityType type, BlockPos pos) {
      MobEntity lv = (MobEntity)this.spawnEntity(type, pos);
      lv.clearGoalsAndTasks();
      return lv;
   }

   public MobEntity spawnMob(EntityType type, int x, int y, int z) {
      return this.spawnMob(type, new BlockPos(x, y, z));
   }

   public MobEntity spawnMob(EntityType type, Vec3d pos) {
      MobEntity lv = (MobEntity)this.spawnEntity(type, pos);
      lv.clearGoalsAndTasks();
      return lv;
   }

   public MobEntity spawnMob(EntityType type, float x, float y, float z) {
      return this.spawnMob(type, new Vec3d((double)x, (double)y, (double)z));
   }

   public TimedTaskRunner startMovingTowards(MobEntity entity, BlockPos pos, float speed) {
      return this.createTimedTaskRunner().expectMinDurationAndRun(2, () -> {
         Path lv = entity.getNavigation().findPathTo((BlockPos)this.getAbsolutePos(pos), 0);
         entity.getNavigation().startMovingAlong(lv, (double)speed);
      });
   }

   public void pushButton(int x, int y, int z) {
      this.pushButton(new BlockPos(x, y, z));
   }

   public void pushButton(BlockPos pos) {
      this.checkBlockState(pos, (state) -> {
         return state.isIn(BlockTags.BUTTONS);
      }, () -> {
         return "Expected button";
      });
      BlockPos lv = this.getAbsolutePos(pos);
      BlockState lv2 = this.getWorld().getBlockState(lv);
      ButtonBlock lv3 = (ButtonBlock)lv2.getBlock();
      lv3.powerOn(lv2, this.getWorld(), lv);
   }

   public void useBlock(BlockPos pos) {
      this.useBlock(pos, this.createMockCreativePlayer());
   }

   public void useBlock(BlockPos pos, PlayerEntity player) {
      BlockPos lv = this.getAbsolutePos(pos);
      this.useBlock(pos, player, new BlockHitResult(Vec3d.ofCenter(lv), Direction.NORTH, lv, true));
   }

   public void useBlock(BlockPos pos, PlayerEntity player, BlockHitResult result) {
      BlockPos lv = this.getAbsolutePos(pos);
      BlockState lv2 = this.getWorld().getBlockState(lv);
      ActionResult lv3 = lv2.onUse(this.getWorld(), player, Hand.MAIN_HAND, result);
      if (!lv3.isAccepted()) {
         ItemUsageContext lv4 = new ItemUsageContext(player, Hand.MAIN_HAND, result);
         player.getStackInHand(Hand.MAIN_HAND).useOnBlock(lv4);
      }

   }

   public LivingEntity drown(LivingEntity entity) {
      entity.setAir(0);
      entity.setHealth(0.25F);
      return entity;
   }

   public PlayerEntity createMockSurvivalPlayer() {
      return new PlayerEntity(this.getWorld(), BlockPos.ORIGIN, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return false;
         }
      };
   }

   public PlayerEntity createMockCreativePlayer() {
      return new PlayerEntity(this.getWorld(), BlockPos.ORIGIN, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return true;
         }

         public boolean isMainPlayer() {
            return true;
         }
      };
   }

   public void toggleLever(int x, int y, int z) {
      this.toggleLever(new BlockPos(x, y, z));
   }

   public void toggleLever(BlockPos pos) {
      this.expectBlock(Blocks.LEVER, pos);
      BlockPos lv = this.getAbsolutePos(pos);
      BlockState lv2 = this.getWorld().getBlockState(lv);
      LeverBlock lv3 = (LeverBlock)lv2.getBlock();
      lv3.togglePower(lv2, this.getWorld(), lv);
   }

   public void putAndRemoveRedstoneBlock(BlockPos pos, long delay) {
      this.setBlockState(pos, Blocks.REDSTONE_BLOCK);
      this.waitAndRun(delay, () -> {
         this.setBlockState(pos, Blocks.AIR);
      });
   }

   public void removeBlock(BlockPos pos) {
      this.getWorld().breakBlock(this.getAbsolutePos(pos), false, (Entity)null);
   }

   public void setBlockState(int x, int y, int z, Block block) {
      this.setBlockState(new BlockPos(x, y, z), block);
   }

   public void setBlockState(int x, int y, int z, BlockState state) {
      this.setBlockState(new BlockPos(x, y, z), state);
   }

   public void setBlockState(BlockPos pos, Block block) {
      this.setBlockState(pos, block.getDefaultState());
   }

   public void setBlockState(BlockPos pos, BlockState state) {
      this.getWorld().setBlockState(this.getAbsolutePos(pos), state, Block.NOTIFY_ALL);
   }

   public void useNightTime() {
      this.setTime(13000);
   }

   public void setTime(int timeOfDay) {
      this.getWorld().setTimeOfDay((long)timeOfDay);
   }

   public void expectBlock(Block block, int x, int y, int z) {
      this.expectBlock(block, new BlockPos(x, y, z));
   }

   public void expectBlock(Block block, BlockPos pos) {
      BlockState lv = this.getBlockState(pos);
      Predicate var10002 = (block1) -> {
         return lv.isOf(block);
      };
      String var10003 = block.getName().getString();
      this.checkBlock(pos, var10002, "Expected " + var10003 + ", got " + lv.getBlock().getName().getString());
   }

   public void dontExpectBlock(Block block, int x, int y, int z) {
      this.dontExpectBlock(block, new BlockPos(x, y, z));
   }

   public void dontExpectBlock(Block block, BlockPos pos) {
      this.checkBlock(pos, (block1) -> {
         return !this.getBlockState(pos).isOf(block);
      }, "Did not expect " + block.getName().getString());
   }

   public void expectBlockAtEnd(Block block, int x, int y, int z) {
      this.expectBlockAtEnd(block, new BlockPos(x, y, z));
   }

   public void expectBlockAtEnd(Block block, BlockPos pos) {
      this.addInstantFinalTask(() -> {
         this.expectBlock(block, pos);
      });
   }

   public void checkBlock(BlockPos pos, Predicate predicate, String errorMessage) {
      this.checkBlock(pos, predicate, () -> {
         return errorMessage;
      });
   }

   public void checkBlock(BlockPos pos, Predicate predicate, Supplier errorMessageSupplier) {
      this.checkBlockState(pos, (state) -> {
         return predicate.test(state.getBlock());
      }, errorMessageSupplier);
   }

   public void expectBlockProperty(BlockPos pos, Property property, Comparable value) {
      BlockState lv = this.getBlockState(pos);
      boolean bl = lv.contains(property);
      if (!bl || !lv.get(property).equals(value)) {
         String string = bl ? "was " + lv.get(property) : "property " + property.getName() + " is missing";
         String string2 = String.format(Locale.ROOT, "Expected property %s to be %s, %s", property.getName(), value, string);
         throw new PositionedException(string2, this.getAbsolutePos(pos), pos, this.test.getTick());
      }
   }

   public void checkBlockProperty(BlockPos pos, Property property, Predicate predicate, String errorMessage) {
      this.checkBlockState(pos, (state) -> {
         if (!state.contains(property)) {
            return false;
         } else {
            Comparable comparable = state.get(property);
            return predicate.test(comparable);
         }
      }, () -> {
         return errorMessage;
      });
   }

   public void checkBlockState(BlockPos pos, Predicate predicate, Supplier errorMessageSupplier) {
      BlockState lv = this.getBlockState(pos);
      if (!predicate.test(lv)) {
         throw new PositionedException((String)errorMessageSupplier.get(), this.getAbsolutePos(pos), pos, this.test.getTick());
      }
   }

   public void expectEntity(EntityType type) {
      List list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
      if (list.isEmpty()) {
         throw new GameTestException("Expected " + type.getUntranslatedName() + " to exist");
      }
   }

   public void expectEntityAt(EntityType type, int x, int y, int z) {
      this.expectEntityAt(type, new BlockPos(x, y, z));
   }

   public void expectEntityAt(EntityType type, BlockPos pos) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
      if (list.isEmpty()) {
         throw new PositionedException("Expected " + type.getUntranslatedName(), lv, pos, this.test.getTick());
      }
   }

   public void expectEntityInside(EntityType type, Vec3d pos1, Vec3d pos2) {
      List list = this.getWorld().getEntitiesByType(type, new Box(pos1, pos2), Entity::isAlive);
      if (list.isEmpty()) {
         throw new PositionedException("Expected " + type.getUntranslatedName() + " between ", BlockPos.ofFloored(pos1), BlockPos.ofFloored(pos2), this.test.getTick());
      }
   }

   public void expectEntitiesAround(EntityType type, BlockPos pos, int amount, double radius) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getEntitiesAround(type, pos, radius);
      if (list.size() != amount) {
         throw new PositionedException("Expected " + amount + " entities of type " + type.getUntranslatedName() + ", actual number of entities found=" + list.size(), lv, pos, this.test.getTick());
      }
   }

   public void expectEntityAround(EntityType type, BlockPos pos, double radius) {
      List list = this.getEntitiesAround(type, pos, radius);
      if (list.isEmpty()) {
         BlockPos lv = this.getAbsolutePos(pos);
         throw new PositionedException("Expected " + type.getUntranslatedName(), lv, pos, this.test.getTick());
      }
   }

   public List getEntitiesAround(EntityType type, BlockPos pos, double radius) {
      BlockPos lv = this.getAbsolutePos(pos);
      return this.getWorld().getEntitiesByType(type, (new Box(lv)).expand(radius), Entity::isAlive);
   }

   public void expectEntityAt(Entity entity, int x, int y, int z) {
      this.expectEntityAt(entity, new BlockPos(x, y, z));
   }

   public void expectEntityAt(Entity entity, BlockPos pos) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(entity.getType(), new Box(lv), Entity::isAlive);
      list.stream().filter((e) -> {
         return e == entity;
      }).findFirst().orElseThrow(() -> {
         return new PositionedException("Expected " + entity.getType().getUntranslatedName(), lv, pos, this.test.getTick());
      });
   }

   public void expectItemsAt(Item item, BlockPos pos, double radius, int amount) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(EntityType.ITEM, (new Box(lv)).expand(radius), Entity::isAlive);
      int j = 0;
      Iterator var9 = list.iterator();

      while(var9.hasNext()) {
         Entity lv2 = (Entity)var9.next();
         ItemEntity lv3 = (ItemEntity)lv2;
         if (lv3.getStack().getItem().equals(item)) {
            j += lv3.getStack().getCount();
         }
      }

      if (j != amount) {
         throw new PositionedException("Expected " + amount + " " + item.getName().getString() + " items to exist (found " + j + ")", lv, pos, this.test.getTick());
      }
   }

   public void expectItemAt(Item item, BlockPos pos, double radius) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(EntityType.ITEM, (new Box(lv)).expand(radius), Entity::isAlive);
      Iterator var7 = list.iterator();

      ItemEntity lv3;
      do {
         if (!var7.hasNext()) {
            throw new PositionedException("Expected " + item.getName().getString() + " item", lv, pos, this.test.getTick());
         }

         Entity lv2 = (Entity)var7.next();
         lv3 = (ItemEntity)lv2;
      } while(!lv3.getStack().getItem().equals(item));

   }

   public void dontExpectItemAt(Item item, BlockPos pos, double radius) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(EntityType.ITEM, (new Box(lv)).expand(radius), Entity::isAlive);
      Iterator var7 = list.iterator();

      ItemEntity lv3;
      do {
         if (!var7.hasNext()) {
            return;
         }

         Entity lv2 = (Entity)var7.next();
         lv3 = (ItemEntity)lv2;
      } while(!lv3.getStack().getItem().equals(item));

      throw new PositionedException("Did not expect " + item.getName().getString() + " item", lv, pos, this.test.getTick());
   }

   public void dontExpectEntity(EntityType type) {
      List list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
      if (!list.isEmpty()) {
         throw new GameTestException("Did not expect " + type.getUntranslatedName() + " to exist");
      }
   }

   public void dontExpectEntityAt(EntityType type, int x, int y, int z) {
      this.dontExpectEntityAt(type, new BlockPos(x, y, z));
   }

   public void dontExpectEntityAt(EntityType type, BlockPos pos) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
      if (!list.isEmpty()) {
         throw new PositionedException("Did not expect " + type.getUntranslatedName(), lv, pos, this.test.getTick());
      }
   }

   public void expectEntityToTouch(EntityType type, double x, double y, double z) {
      Vec3d lv = new Vec3d(x, y, z);
      Vec3d lv2 = this.getAbsolute(lv);
      Predicate predicate = (entity) -> {
         return entity.getBoundingBox().intersects(lv2, lv2);
      };
      List list = this.getWorld().getEntitiesByType(type, this.getTestBox(), predicate);
      if (list.isEmpty()) {
         throw new GameTestException("Expected " + type.getUntranslatedName() + " to touch " + lv2 + " (relative " + lv + ")");
      }
   }

   public void dontExpectEntityToTouch(EntityType type, double x, double y, double z) {
      Vec3d lv = new Vec3d(x, y, z);
      Vec3d lv2 = this.getAbsolute(lv);
      Predicate predicate = (entity) -> {
         return !entity.getBoundingBox().intersects(lv2, lv2);
      };
      List list = this.getWorld().getEntitiesByType(type, this.getTestBox(), predicate);
      if (list.isEmpty()) {
         throw new GameTestException("Did not expect " + type.getUntranslatedName() + " to touch " + lv2 + " (relative " + lv + ")");
      }
   }

   public void expectEntityWithData(BlockPos pos, EntityType type, Function entityDataGetter, @Nullable Object data) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(type, new Box(lv), Entity::isAlive);
      if (list.isEmpty()) {
         throw new PositionedException("Expected " + type.getUntranslatedName(), lv, pos, this.test.getTick());
      } else {
         Iterator var7 = list.iterator();

         while(var7.hasNext()) {
            Entity lv2 = (Entity)var7.next();
            Object object2 = entityDataGetter.apply(lv2);
            if (object2 == null) {
               if (data != null) {
                  throw new GameTestException("Expected entity data to be: " + data + ", but was: " + object2);
               }
            } else if (!object2.equals(data)) {
               throw new GameTestException("Expected entity data to be: " + data + ", but was: " + object2);
            }
         }

      }
   }

   public void expectEntityHoldingItem(BlockPos pos, EntityType entityType, Item item) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(entityType, new Box(lv), Entity::isAlive);
      if (list.isEmpty()) {
         throw new PositionedException("Expected entity of type: " + entityType, lv, pos, this.getTick());
      } else {
         Iterator var6 = list.iterator();

         LivingEntity lv2;
         do {
            if (!var6.hasNext()) {
               throw new PositionedException("Entity should be holding: " + item, lv, pos, this.getTick());
            }

            lv2 = (LivingEntity)var6.next();
         } while(!lv2.isHolding(item));

      }
   }

   public void expectEntityWithItem(BlockPos pos, EntityType entityType, Item item) {
      BlockPos lv = this.getAbsolutePos(pos);
      List list = this.getWorld().getEntitiesByType(entityType, new Box(lv), (entity) -> {
         return ((Entity)entity).isAlive();
      });
      if (list.isEmpty()) {
         throw new PositionedException("Expected " + entityType.getUntranslatedName() + " to exist", lv, pos, this.getTick());
      } else {
         Iterator var6 = list.iterator();

         Entity lv2;
         do {
            if (!var6.hasNext()) {
               throw new PositionedException("Entity inventory should contain: " + item, lv, pos, this.getTick());
            }

            lv2 = (Entity)var6.next();
         } while(!((InventoryOwner)lv2).getInventory().containsAny((stack) -> {
            return stack.isOf(item);
         }));

      }
   }

   public void expectEmptyContainer(BlockPos pos) {
      BlockPos lv = this.getAbsolutePos(pos);
      BlockEntity lv2 = this.getWorld().getBlockEntity(lv);
      if (lv2 instanceof LockableContainerBlockEntity && !((LockableContainerBlockEntity)lv2).isEmpty()) {
         throw new GameTestException("Container should be empty");
      }
   }

   public void expectContainerWith(BlockPos pos, Item item) {
      BlockPos lv = this.getAbsolutePos(pos);
      BlockEntity lv2 = this.getWorld().getBlockEntity(lv);
      if (!(lv2 instanceof LockableContainerBlockEntity)) {
         throw new GameTestException("Expected a container at " + pos + ", found " + Registries.BLOCK_ENTITY_TYPE.getId(lv2.getType()));
      } else if (((LockableContainerBlockEntity)lv2).count(item) != 1) {
         throw new GameTestException("Container should contain: " + item);
      }
   }

   public void expectSameStates(BlockBox checkedBlockBox, BlockPos correctStatePos) {
      BlockPos.stream(checkedBlockBox).forEach((checkedPos) -> {
         BlockPos lv = correctStatePos.add(checkedPos.getX() - checkedBlockBox.getMinX(), checkedPos.getY() - checkedBlockBox.getMinY(), checkedPos.getZ() - checkedBlockBox.getMinZ());
         this.expectSameStates(checkedPos, lv);
      });
   }

   public void expectSameStates(BlockPos checkedPos, BlockPos correctStatePos) {
      BlockState lv = this.getBlockState(checkedPos);
      BlockState lv2 = this.getBlockState(correctStatePos);
      if (lv != lv2) {
         this.throwPositionedException("Incorrect state. Expected " + lv2 + ", got " + lv, checkedPos);
      }

   }

   public void expectContainerWith(long delay, BlockPos pos, Item item) {
      this.runAtTick(delay, () -> {
         this.expectContainerWith(pos, item);
      });
   }

   public void expectEmptyContainer(long delay, BlockPos pos) {
      this.runAtTick(delay, () -> {
         this.expectEmptyContainer(pos);
      });
   }

   public void expectEntityWithDataEnd(BlockPos pos, EntityType type, Function entityDataGetter, Object data) {
      this.addInstantFinalTask(() -> {
         this.expectEntityWithData(pos, type, entityDataGetter, data);
      });
   }

   public void testEntity(Entity entity, Predicate predicate, String testName) {
      if (!predicate.test(entity)) {
         throw new GameTestException("Entity " + entity + " failed " + testName + " test");
      }
   }

   public void testEntityProperty(Entity entity, Function propertyGetter, String propertyName, Object expectedValue) {
      Object object2 = propertyGetter.apply(entity);
      if (!object2.equals(expectedValue)) {
         throw new GameTestException("Entity " + entity + " value " + propertyName + "=" + object2 + " is not equal to expected " + expectedValue);
      }
   }

   public void expectEntityAtEnd(EntityType type, int x, int y, int z) {
      this.expectEntityAtEnd(type, new BlockPos(x, y, z));
   }

   public void expectEntityAtEnd(EntityType type, BlockPos pos) {
      this.addInstantFinalTask(() -> {
         this.expectEntityAt(type, pos);
      });
   }

   public void dontExpectEntityAtEnd(EntityType type, int x, int y, int z) {
      this.dontExpectEntityAtEnd(type, new BlockPos(x, y, z));
   }

   public void dontExpectEntityAtEnd(EntityType type, BlockPos pos) {
      this.addInstantFinalTask(() -> {
         this.dontExpectEntityAt(type, pos);
      });
   }

   public void complete() {
      this.test.completeIfSuccessful();
   }

   private void markFinalCause() {
      if (this.hasFinalClause) {
         throw new IllegalStateException("This test already has final clause");
      } else {
         this.hasFinalClause = true;
      }
   }

   public void addFinalTask(Runnable runnable) {
      this.markFinalCause();
      this.test.createTimedTaskRunner().createAndAdd(0L, runnable).completeIfSuccessful();
   }

   public void addInstantFinalTask(Runnable runnable) {
      this.markFinalCause();
      this.test.createTimedTaskRunner().createAndAdd(runnable).completeIfSuccessful();
   }

   public void addFinalTaskWithDuration(int duration, Runnable runnable) {
      this.markFinalCause();
      this.test.createTimedTaskRunner().createAndAdd((long)duration, runnable).completeIfSuccessful();
   }

   public void runAtTick(long tick, Runnable runnable) {
      this.test.runAtTick(tick, runnable);
   }

   public void waitAndRun(long ticks, Runnable runnable) {
      this.runAtTick(this.test.getTick() + ticks, runnable);
   }

   public void forceRandomTick(BlockPos pos) {
      BlockPos lv = this.getAbsolutePos(pos);
      ServerWorld lv2 = this.getWorld();
      lv2.getBlockState(lv).randomTick(lv2, lv, lv2.random);
   }

   public int getRelativeTopY(Heightmap.Type heightmap, int x, int z) {
      BlockPos lv = this.getAbsolutePos(new BlockPos(x, 0, z));
      return this.getRelativePos(this.getWorld().getTopPosition(heightmap, lv)).getY();
   }

   public void throwPositionedException(String message, BlockPos pos) {
      throw new PositionedException(message, this.getAbsolutePos(pos), pos, this.getTick());
   }

   public void throwPositionedException(String message, Entity entity) {
      throw new PositionedException(message, entity.getBlockPos(), this.getRelativePos(entity.getBlockPos()), this.getTick());
   }

   public void throwGameTestException(String message) {
      throw new GameTestException(message);
   }

   public void addTask(Runnable task) {
      this.test.createTimedTaskRunner().createAndAdd(task).fail(() -> {
         return new GameTestException("Fail conditions met");
      });
   }

   public void runAtEveryTick(Runnable task) {
      LongStream.range(this.test.getTick(), (long)this.test.getTicksLeft()).forEach((tick) -> {
         GameTestState var10000 = this.test;
         Objects.requireNonNull(task);
         var10000.runAtTick(tick, task::run);
      });
   }

   public TimedTaskRunner createTimedTaskRunner() {
      return this.test.createTimedTaskRunner();
   }

   public BlockPos getAbsolutePos(BlockPos pos) {
      BlockPos lv = this.test.getPos();
      BlockPos lv2 = lv.add(pos);
      return StructureTemplate.transformAround(lv2, BlockMirror.NONE, this.test.getRotation(), lv);
   }

   public BlockPos getRelativePos(BlockPos pos) {
      BlockPos lv = this.test.getPos();
      BlockRotation lv2 = this.test.getRotation().rotate(BlockRotation.CLOCKWISE_180);
      BlockPos lv3 = StructureTemplate.transformAround(pos, BlockMirror.NONE, lv2, lv);
      return lv3.subtract(lv);
   }

   public Vec3d getAbsolute(Vec3d pos) {
      Vec3d lv = Vec3d.of(this.test.getPos());
      return StructureTemplate.transformAround(lv.add(pos), BlockMirror.NONE, this.test.getRotation(), this.test.getPos());
   }

   public Vec3d getRelative(Vec3d pos) {
      Vec3d lv = Vec3d.of(this.test.getPos());
      return StructureTemplate.transformAround(pos.subtract(lv), BlockMirror.NONE, this.test.getRotation(), this.test.getPos());
   }

   public void assertTrue(boolean condition, String message) {
      if (!condition) {
         throw new GameTestException(message);
      }
   }

   public void assertFalse(boolean condition, String message) {
      if (condition) {
         throw new GameTestException(message);
      }
   }

   public long getTick() {
      return this.test.getTick();
   }

   private Box getTestBox() {
      return this.test.getBoundingBox();
   }

   private Box getRelativeTestBox() {
      Box lv = this.test.getBoundingBox();
      return lv.offset(BlockPos.ORIGIN.subtract(this.getAbsolutePos(BlockPos.ORIGIN)));
   }

   public void forEachRelativePos(Consumer posConsumer) {
      Box lv = this.getRelativeTestBox();
      BlockPos.Mutable.stream(lv.offset(0.0, 1.0, 0.0)).forEach(posConsumer);
   }

   public void forEachRemainingTick(Runnable runnable) {
      LongStream.range(this.test.getTick(), (long)this.test.getTicksLeft()).forEach((tick) -> {
         GameTestState var10000 = this.test;
         Objects.requireNonNull(runnable);
         var10000.runAtTick(tick, runnable::run);
      });
   }

   public void useStackOnBlock(PlayerEntity player, ItemStack stack, BlockPos pos, Direction direction) {
      BlockPos lv = this.getAbsolutePos(pos.offset(direction));
      BlockHitResult lv2 = new BlockHitResult(Vec3d.ofCenter(lv), direction, lv, false);
      ItemUsageContext lv3 = new ItemUsageContext(player, Hand.MAIN_HAND, lv2);
      stack.useOnBlock(lv3);
   }
}
