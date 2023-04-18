package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemStackParticleEffect implements ParticleEffect {
   public static final ParticleEffect.Factory PARAMETERS_FACTORY = new ParticleEffect.Factory() {
      public ItemStackParticleEffect read(ParticleType arg, StringReader stringReader) throws CommandSyntaxException {
         stringReader.expect(' ');
         ItemStringReader.ItemResult lv = ItemStringReader.item(Registries.ITEM.getReadOnlyWrapper(), stringReader);
         ItemStack lv2 = (new ItemStackArgument(lv.item(), lv.nbt())).createStack(1, false);
         return new ItemStackParticleEffect(arg, lv2);
      }

      public ItemStackParticleEffect read(ParticleType arg, PacketByteBuf arg2) {
         return new ItemStackParticleEffect(arg, arg2.readItemStack());
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
   private final ItemStack stack;

   public static Codec createCodec(ParticleType type) {
      return ItemStack.CODEC.xmap((stack) -> {
         return new ItemStackParticleEffect(type, stack);
      }, (effect) -> {
         return effect.stack;
      });
   }

   public ItemStackParticleEffect(ParticleType type, ItemStack stack) {
      this.type = type;
      this.stack = stack;
   }

   public void write(PacketByteBuf buf) {
      buf.writeItemStack(this.stack);
   }

   public String asString() {
      Identifier var10000 = Registries.PARTICLE_TYPE.getId(this.getType());
      return "" + var10000 + " " + (new ItemStackArgument(this.stack.getRegistryEntry(), this.stack.getNbt())).asString();
   }

   public ParticleType getType() {
      return this.type;
   }

   public ItemStack getItemStack() {
      return this.stack;
   }
}
