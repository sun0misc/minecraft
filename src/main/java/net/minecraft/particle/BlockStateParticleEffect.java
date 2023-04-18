package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockStateParticleEffect implements ParticleEffect {
   public static final ParticleEffect.Factory PARAMETERS_FACTORY = new ParticleEffect.Factory() {
      public BlockStateParticleEffect read(ParticleType arg, StringReader stringReader) throws CommandSyntaxException {
         stringReader.expect(' ');
         return new BlockStateParticleEffect(arg, BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), (StringReader)stringReader, false).blockState());
      }

      public BlockStateParticleEffect read(ParticleType arg, PacketByteBuf arg2) {
         return new BlockStateParticleEffect(arg, (BlockState)arg2.readRegistryValue(Block.STATE_IDS));
      }

      // $FF: synthetic method
      public ParticleEffect read(ParticleType type, PacketByteBuf buf) {
         return this.read(type, buf);
      }

      // $FF: synthetic method
      public ParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException {
         return this.read(type, reader);
      }
   };
   private final ParticleType type;
   private final BlockState blockState;

   public static Codec createCodec(ParticleType type) {
      return BlockState.CODEC.xmap((state) -> {
         return new BlockStateParticleEffect(type, state);
      }, (effect) -> {
         return effect.blockState;
      });
   }

   public BlockStateParticleEffect(ParticleType type, BlockState blockState) {
      this.type = type;
      this.blockState = blockState;
   }

   public void write(PacketByteBuf buf) {
      buf.writeRegistryValue(Block.STATE_IDS, this.blockState);
   }

   public String asString() {
      Identifier var10000 = Registries.PARTICLE_TYPE.getId(this.getType());
      return "" + var10000 + " " + BlockArgumentParser.stringifyBlockState(this.blockState);
   }

   public ParticleType getType() {
      return this.type;
   }

   public BlockState getBlockState() {
      return this.blockState;
   }
}
