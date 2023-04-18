package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;

public record PredicatedStateProvider(BlockStateProvider fallback, List rules) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockStateProvider.TYPE_CODEC.fieldOf("fallback").forGetter(PredicatedStateProvider::fallback), PredicatedStateProvider.Rule.CODEC.listOf().fieldOf("rules").forGetter(PredicatedStateProvider::rules)).apply(instance, PredicatedStateProvider::new);
   });

   public PredicatedStateProvider(BlockStateProvider arg, List list) {
      this.fallback = arg;
      this.rules = list;
   }

   public static PredicatedStateProvider of(BlockStateProvider stateProvider) {
      return new PredicatedStateProvider(stateProvider, List.of());
   }

   public static PredicatedStateProvider of(Block block) {
      return of((BlockStateProvider)BlockStateProvider.of(block));
   }

   public BlockState getBlockState(StructureWorldAccess world, Random random, BlockPos pos) {
      Iterator var4 = this.rules.iterator();

      Rule lv;
      do {
         if (!var4.hasNext()) {
            return this.fallback.get(random, pos);
         }

         lv = (Rule)var4.next();
      } while(!lv.ifTrue().test(world, pos));

      return lv.then().get(random, pos);
   }

   public BlockStateProvider fallback() {
      return this.fallback;
   }

   public List rules() {
      return this.rules;
   }

   public static record Rule(BlockPredicate ifTrue, BlockStateProvider then) {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(BlockPredicate.BASE_CODEC.fieldOf("if_true").forGetter(Rule::ifTrue), BlockStateProvider.TYPE_CODEC.fieldOf("then").forGetter(Rule::then)).apply(instance, Rule::new);
      });

      public Rule(BlockPredicate arg, BlockStateProvider arg2) {
         this.ifTrue = arg;
         this.then = arg2;
      }

      public BlockPredicate ifTrue() {
         return this.ifTrue;
      }

      public BlockStateProvider then() {
         return this.then;
      }
   }
}
