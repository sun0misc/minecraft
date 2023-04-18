package net.minecraft.entity.ai.pathing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class PathNodeNavigator {
   private static final float TARGET_DISTANCE_MULTIPLIER = 1.5F;
   private final PathNode[] successors = new PathNode[32];
   private final int range;
   private final PathNodeMaker pathNodeMaker;
   private static final boolean field_31808 = false;
   private final PathMinHeap minHeap = new PathMinHeap();

   public PathNodeNavigator(PathNodeMaker pathNodeMaker, int range) {
      this.pathNodeMaker = pathNodeMaker;
      this.range = range;
   }

   @Nullable
   public Path findPathToAny(ChunkCache world, MobEntity mob, Set positions, float followRange, int distance, float rangeMultiplier) {
      this.minHeap.clear();
      this.pathNodeMaker.init(world, mob);
      PathNode lv = this.pathNodeMaker.getStart();
      if (lv == null) {
         return null;
      } else {
         Map map = (Map)positions.stream().collect(Collectors.toMap((pos) -> {
            return this.pathNodeMaker.getNode((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         }, Function.identity()));
         Path lv2 = this.findPathToAny(world.getProfiler(), lv, map, followRange, distance, rangeMultiplier);
         this.pathNodeMaker.clear();
         return lv2;
      }
   }

   @Nullable
   private Path findPathToAny(Profiler profiler, PathNode startNode, Map positions, float followRange, int distance, float rangeMultiplier) {
      profiler.push("find_path");
      profiler.markSampleType(SampleType.PATH_FINDING);
      Set set = positions.keySet();
      startNode.penalizedPathLength = 0.0F;
      startNode.distanceToNearestTarget = this.calculateDistances(startNode, set);
      startNode.heapWeight = startNode.distanceToNearestTarget;
      this.minHeap.clear();
      this.minHeap.push(startNode);
      Set set2 = ImmutableSet.of();
      int j = 0;
      Set set3 = Sets.newHashSetWithExpectedSize(set.size());
      int k = (int)((float)this.range * rangeMultiplier);

      while(!this.minHeap.isEmpty()) {
         ++j;
         if (j >= k) {
            break;
         }

         PathNode lv = this.minHeap.pop();
         lv.visited = true;
         Iterator var13 = set.iterator();

         while(var13.hasNext()) {
            TargetPathNode lv2 = (TargetPathNode)var13.next();
            if (lv.getManhattanDistance((PathNode)lv2) <= (float)distance) {
               lv2.markReached();
               set3.add(lv2);
            }
         }

         if (!set3.isEmpty()) {
            break;
         }

         if (!(lv.getDistance(startNode) >= followRange)) {
            int l = this.pathNodeMaker.getSuccessors(this.successors, lv);

            for(int m = 0; m < l; ++m) {
               PathNode lv3 = this.successors[m];
               float h = this.getDistance(lv, lv3);
               lv3.pathLength = lv.pathLength + h;
               float n = lv.penalizedPathLength + h + lv3.penalty;
               if (lv3.pathLength < followRange && (!lv3.isInHeap() || n < lv3.penalizedPathLength)) {
                  lv3.previous = lv;
                  lv3.penalizedPathLength = n;
                  lv3.distanceToNearestTarget = this.calculateDistances(lv3, set) * 1.5F;
                  if (lv3.isInHeap()) {
                     this.minHeap.setNodeWeight(lv3, lv3.penalizedPathLength + lv3.distanceToNearestTarget);
                  } else {
                     lv3.heapWeight = lv3.penalizedPathLength + lv3.distanceToNearestTarget;
                     this.minHeap.push(lv3);
                  }
               }
            }
         }
      }

      Optional optional = !set3.isEmpty() ? set3.stream().map((node) -> {
         return this.createPath(node.getNearestNode(), (BlockPos)positions.get(node), true);
      }).min(Comparator.comparingInt(Path::getLength)) : set.stream().map((arg) -> {
         return this.createPath(arg.getNearestNode(), (BlockPos)positions.get(arg), false);
      }).min(Comparator.comparingDouble(Path::getManhattanDistanceFromTarget).thenComparingInt(Path::getLength));
      profiler.pop();
      if (!optional.isPresent()) {
         return null;
      } else {
         Path lv4 = (Path)optional.get();
         return lv4;
      }
   }

   protected float getDistance(PathNode a, PathNode b) {
      return a.getDistance(b);
   }

   private float calculateDistances(PathNode node, Set targets) {
      float f = Float.MAX_VALUE;

      float g;
      for(Iterator var4 = targets.iterator(); var4.hasNext(); f = Math.min(g, f)) {
         TargetPathNode lv = (TargetPathNode)var4.next();
         g = node.getDistance((PathNode)lv);
         lv.updateNearestNode(g, node);
      }

      return f;
   }

   private Path createPath(PathNode endNode, BlockPos target, boolean reachesTarget) {
      List list = Lists.newArrayList();
      PathNode lv = endNode;
      list.add(0, endNode);

      while(lv.previous != null) {
         lv = lv.previous;
         list.add(0, lv);
      }

      return new Path(list, target, reachesTarget);
   }
}
