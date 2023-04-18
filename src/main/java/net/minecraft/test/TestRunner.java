package net.minecraft.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;

public class TestRunner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BlockPos pos;
   final ServerWorld world;
   private final TestManager testManager;
   private final int sizeZ;
   private final List tests;
   private final List batches;
   private final BlockPos.Mutable reusablePos;

   public TestRunner(Collection batches, BlockPos pos, BlockRotation rotation, ServerWorld world, TestManager testManager, int sizeZ) {
      this.reusablePos = pos.mutableCopy();
      this.pos = pos;
      this.world = world;
      this.testManager = testManager;
      this.sizeZ = sizeZ;
      this.batches = (List)batches.stream().map((batch) -> {
         Collection collection = (Collection)batch.getTestFunctions().stream().map((testFunction) -> {
            return new GameTestState(testFunction, rotation, world);
         }).collect(ImmutableList.toImmutableList());
         return Pair.of(batch, collection);
      }).collect(ImmutableList.toImmutableList());
      this.tests = (List)this.batches.stream().flatMap((batch) -> {
         return ((Collection)batch.getSecond()).stream();
      }).collect(ImmutableList.toImmutableList());
   }

   public List getTests() {
      return this.tests;
   }

   public void run() {
      this.runBatch(0);
   }

   void runBatch(final int index) {
      if (index < this.batches.size()) {
         Pair pair = (Pair)this.batches.get(index);
         final GameTestBatch lv = (GameTestBatch)pair.getFirst();
         Collection collection = (Collection)pair.getSecond();
         Map map = this.alignTestStructures(collection);
         String string = lv.getId();
         LOGGER.info("Running test batch '{}' ({} tests)...", string, collection.size());
         lv.startBatch(this.world);
         final TestSet lv2 = new TestSet();
         Objects.requireNonNull(lv2);
         collection.forEach(lv2::add);
         lv2.addListener(new TestListener() {
            private void onFinished() {
               if (lv2.isDone()) {
                  lv.finishBatch(TestRunner.this.world);
                  TestRunner.this.runBatch(index + 1);
               }

            }

            public void onStarted(GameTestState test) {
            }

            public void onPassed(GameTestState test) {
               this.onFinished();
            }

            public void onFailed(GameTestState test) {
               this.onFinished();
            }
         });
         collection.forEach((gameTest) -> {
            BlockPos lv = (BlockPos)map.get(gameTest);
            TestUtil.startTest(gameTest, lv, this.testManager);
         });
      }
   }

   private Map alignTestStructures(Collection gameTests) {
      Map map = Maps.newHashMap();
      int i = 0;
      Box lv = new Box(this.reusablePos);
      Iterator var5 = gameTests.iterator();

      while(var5.hasNext()) {
         GameTestState lv2 = (GameTestState)var5.next();
         BlockPos lv3 = new BlockPos(this.reusablePos);
         StructureBlockBlockEntity lv4 = StructureTestUtil.createStructureTemplate(lv2.getTemplateName(), lv3, lv2.getRotation(), 2, this.world, true);
         Box lv5 = StructureTestUtil.getStructureBoundingBox(lv4);
         lv2.setPos(lv4.getPos());
         map.put(lv2, new BlockPos(this.reusablePos));
         lv = lv.union(lv5);
         this.reusablePos.move((int)lv5.getXLength() + 5, 0, 0);
         if (i++ % this.sizeZ == this.sizeZ - 1) {
            this.reusablePos.move(0, 0, (int)lv.getZLength() + 6);
            this.reusablePos.setX(this.pos.getX());
            lv = new Box(this.reusablePos);
         }
      }

      return map;
   }
}
