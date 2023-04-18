package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public record BlockSetType(String name, boolean canOpenByHand, BlockSoundGroup soundType, SoundEvent doorClose, SoundEvent doorOpen, SoundEvent trapdoorClose, SoundEvent trapdoorOpen, SoundEvent pressurePlateClickOff, SoundEvent pressurePlateClickOn, SoundEvent buttonClickOff, SoundEvent buttonClickOn) {
   private static final Set VALUES = new ObjectArraySet();
   public static final BlockSetType IRON;
   public static final BlockSetType GOLD;
   public static final BlockSetType STONE;
   public static final BlockSetType POLISHED_BLACKSTONE;
   public static final BlockSetType OAK;
   public static final BlockSetType SPRUCE;
   public static final BlockSetType BIRCH;
   public static final BlockSetType ACACIA;
   public static final BlockSetType CHERRY;
   public static final BlockSetType JUNGLE;
   public static final BlockSetType DARK_OAK;
   public static final BlockSetType CRIMSON;
   public static final BlockSetType WARPED;
   public static final BlockSetType MANGROVE;
   public static final BlockSetType BAMBOO;

   public BlockSetType(String name) {
      this(name, true, BlockSoundGroup.WOOD, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON);
   }

   public BlockSetType(String string, boolean bl, BlockSoundGroup arg, SoundEvent arg2, SoundEvent arg3, SoundEvent arg4, SoundEvent arg5, SoundEvent arg6, SoundEvent arg7, SoundEvent arg8, SoundEvent arg9) {
      this.name = string;
      this.canOpenByHand = bl;
      this.soundType = arg;
      this.doorClose = arg2;
      this.doorOpen = arg3;
      this.trapdoorClose = arg4;
      this.trapdoorOpen = arg5;
      this.pressurePlateClickOff = arg6;
      this.pressurePlateClickOn = arg7;
      this.buttonClickOff = arg8;
      this.buttonClickOn = arg9;
   }

   private static BlockSetType register(BlockSetType blockSetType) {
      VALUES.add(blockSetType);
      return blockSetType;
   }

   public static Stream stream() {
      return VALUES.stream();
   }

   public String name() {
      return this.name;
   }

   public boolean canOpenByHand() {
      return this.canOpenByHand;
   }

   public BlockSoundGroup soundType() {
      return this.soundType;
   }

   public SoundEvent doorClose() {
      return this.doorClose;
   }

   public SoundEvent doorOpen() {
      return this.doorOpen;
   }

   public SoundEvent trapdoorClose() {
      return this.trapdoorClose;
   }

   public SoundEvent trapdoorOpen() {
      return this.trapdoorOpen;
   }

   public SoundEvent pressurePlateClickOff() {
      return this.pressurePlateClickOff;
   }

   public SoundEvent pressurePlateClickOn() {
      return this.pressurePlateClickOn;
   }

   public SoundEvent buttonClickOff() {
      return this.buttonClickOff;
   }

   public SoundEvent buttonClickOn() {
      return this.buttonClickOn;
   }

   static {
      IRON = register(new BlockSetType("iron", false, BlockSoundGroup.METAL, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON));
      GOLD = register(new BlockSetType("gold", false, BlockSoundGroup.METAL, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON));
      STONE = register(new BlockSetType("stone", true, BlockSoundGroup.STONE, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON));
      POLISHED_BLACKSTONE = register(new BlockSetType("polished_blackstone", true, BlockSoundGroup.STONE, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON));
      OAK = register(new BlockSetType("oak"));
      SPRUCE = register(new BlockSetType("spruce"));
      BIRCH = register(new BlockSetType("birch"));
      ACACIA = register(new BlockSetType("acacia"));
      CHERRY = register(new BlockSetType("cherry", true, BlockSoundGroup.CHERRY_WOOD, SoundEvents.BLOCK_CHERRY_WOOD_DOOR_CLOSE, SoundEvents.BLOCK_CHERRY_WOOD_DOOR_OPEN, SoundEvents.BLOCK_CHERRY_WOOD_TRAPDOOR_CLOSE, SoundEvents.BLOCK_CHERRY_WOOD_TRAPDOOR_OPEN, SoundEvents.BLOCK_CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_CHERRY_WOOD_BUTTON_CLICK_OFF, SoundEvents.BLOCK_CHERRY_WOOD_BUTTON_CLICK_ON));
      JUNGLE = register(new BlockSetType("jungle"));
      DARK_OAK = register(new BlockSetType("dark_oak"));
      CRIMSON = register(new BlockSetType("crimson", true, BlockSoundGroup.NETHER_WOOD, SoundEvents.BLOCK_NETHER_WOOD_DOOR_CLOSE, SoundEvents.BLOCK_NETHER_WOOD_DOOR_OPEN, SoundEvents.BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.BLOCK_NETHER_WOOD_TRAPDOOR_OPEN, SoundEvents.BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.BLOCK_NETHER_WOOD_BUTTON_CLICK_ON));
      WARPED = register(new BlockSetType("warped", true, BlockSoundGroup.NETHER_WOOD, SoundEvents.BLOCK_NETHER_WOOD_DOOR_CLOSE, SoundEvents.BLOCK_NETHER_WOOD_DOOR_OPEN, SoundEvents.BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.BLOCK_NETHER_WOOD_TRAPDOOR_OPEN, SoundEvents.BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.BLOCK_NETHER_WOOD_BUTTON_CLICK_ON));
      MANGROVE = register(new BlockSetType("mangrove"));
      BAMBOO = register(new BlockSetType("bamboo", true, BlockSoundGroup.BAMBOO_WOOD, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_CLOSE, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_OPEN, SoundEvents.BLOCK_BAMBOO_WOOD_TRAPDOOR_CLOSE, SoundEvents.BLOCK_BAMBOO_WOOD_TRAPDOOR_OPEN, SoundEvents.BLOCK_BAMBOO_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_BAMBOO_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_OFF, SoundEvents.BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON));
   }
}
