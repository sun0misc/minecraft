package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public record WoodType(String name, BlockSetType setType, BlockSoundGroup soundType, BlockSoundGroup hangingSignSoundType, SoundEvent fenceGateClose, SoundEvent fenceGateOpen) {
   private static final Set VALUES = new ObjectArraySet();
   public static final WoodType OAK;
   public static final WoodType SPRUCE;
   public static final WoodType BIRCH;
   public static final WoodType ACACIA;
   public static final WoodType CHERRY;
   public static final WoodType JUNGLE;
   public static final WoodType DARK_OAK;
   public static final WoodType CRIMSON;
   public static final WoodType WARPED;
   public static final WoodType MANGROVE;
   public static final WoodType BAMBOO;

   public WoodType(String name, BlockSetType setType) {
      this(name, setType, BlockSoundGroup.WOOD, BlockSoundGroup.HANGING_SIGN, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundEvents.BLOCK_FENCE_GATE_OPEN);
   }

   public WoodType(String string, BlockSetType arg, BlockSoundGroup arg2, BlockSoundGroup arg3, SoundEvent arg4, SoundEvent arg5) {
      this.name = string;
      this.setType = arg;
      this.soundType = arg2;
      this.hangingSignSoundType = arg3;
      this.fenceGateClose = arg4;
      this.fenceGateOpen = arg5;
   }

   private static WoodType register(WoodType type) {
      VALUES.add(type);
      return type;
   }

   public static Stream stream() {
      return VALUES.stream();
   }

   public String name() {
      return this.name;
   }

   public BlockSetType setType() {
      return this.setType;
   }

   public BlockSoundGroup soundType() {
      return this.soundType;
   }

   public BlockSoundGroup hangingSignSoundType() {
      return this.hangingSignSoundType;
   }

   public SoundEvent fenceGateClose() {
      return this.fenceGateClose;
   }

   public SoundEvent fenceGateOpen() {
      return this.fenceGateOpen;
   }

   static {
      OAK = register(new WoodType("oak", BlockSetType.OAK));
      SPRUCE = register(new WoodType("spruce", BlockSetType.SPRUCE));
      BIRCH = register(new WoodType("birch", BlockSetType.BIRCH));
      ACACIA = register(new WoodType("acacia", BlockSetType.ACACIA));
      CHERRY = register(new WoodType("cherry", BlockSetType.CHERRY, BlockSoundGroup.CHERRY_WOOD, BlockSoundGroup.CHERRY_WOOD_HANGING_SIGN, SoundEvents.BLOCK_CHERRY_WOOD_FENCE_GATE_CLOSE, SoundEvents.BLOCK_CHERRY_WOOD_FENCE_GATE_OPEN));
      JUNGLE = register(new WoodType("jungle", BlockSetType.JUNGLE));
      DARK_OAK = register(new WoodType("dark_oak", BlockSetType.DARK_OAK));
      CRIMSON = register(new WoodType("crimson", BlockSetType.CRIMSON, BlockSoundGroup.NETHER_WOOD, BlockSoundGroup.NETHER_WOOD_HANGING_SIGN, SoundEvents.BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE, SoundEvents.BLOCK_NETHER_WOOD_FENCE_GATE_OPEN));
      WARPED = register(new WoodType("warped", BlockSetType.WARPED, BlockSoundGroup.NETHER_WOOD, BlockSoundGroup.NETHER_WOOD_HANGING_SIGN, SoundEvents.BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE, SoundEvents.BLOCK_NETHER_WOOD_FENCE_GATE_OPEN));
      MANGROVE = register(new WoodType("mangrove", BlockSetType.MANGROVE));
      BAMBOO = register(new WoodType("bamboo", BlockSetType.BAMBOO, BlockSoundGroup.BAMBOO_WOOD, BlockSoundGroup.BAMBOO_WOOD_HANGING_SIGN, SoundEvents.BLOCK_BAMBOO_WOOD_FENCE_GATE_CLOSE, SoundEvents.BLOCK_BAMBOO_WOOD_FENCE_GATE_OPEN));
   }
}
