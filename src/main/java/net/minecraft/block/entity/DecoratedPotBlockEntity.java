package net.minecraft.block.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DecoratedPotBlockEntity extends BlockEntity {
   public static final String SHARDS_NBT_KEY = "shards";
   private static final int SHARD_COUNT = 4;
   private final List shards = (List)Util.make(new ArrayList(4), (shards) -> {
      shards.add(Items.BRICK);
      shards.add(Items.BRICK);
      shards.add(Items.BRICK);
      shards.add(Items.BRICK);
   });

   public DecoratedPotBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.DECORATED_POT, pos, state);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      writeShardsToNbt(this.shards, nbt);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("shards", NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList("shards", NbtElement.STRING_TYPE);
         this.shards.clear();
         int i = Math.min(4, lv.size());

         int j;
         for(j = 0; j < i; ++j) {
            NbtElement var6 = lv.get(j);
            if (var6 instanceof NbtString) {
               NbtString lv2 = (NbtString)var6;
               this.shards.add((Item)Registries.ITEM.get(new Identifier(lv2.asString())));
            } else {
               this.shards.add(Items.BRICK);
            }
         }

         j = 4 - i;

         for(int k = 0; k < j; ++k) {
            this.shards.add(Items.BRICK);
         }
      }

   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public static void writeShardsToNbt(List shards, NbtCompound nbt) {
      NbtList lv = new NbtList();
      Iterator var3 = shards.iterator();

      while(var3.hasNext()) {
         Item lv2 = (Item)var3.next();
         lv.add(NbtString.of(Registries.ITEM.getId(lv2).toString()));
      }

      nbt.put("shards", lv);
   }

   public ItemStack asStack() {
      ItemStack lv = new ItemStack(Blocks.DECORATED_POT);
      NbtCompound lv2 = new NbtCompound();
      writeShardsToNbt(this.shards, lv2);
      BlockItem.setBlockEntityNbt(lv, BlockEntityType.DECORATED_POT, lv2);
      return lv;
   }

   public List getShards() {
      return this.shards;
   }

   public Direction getHorizontalFacing() {
      return (Direction)this.getCachedState().get(Properties.HORIZONTAL_FACING);
   }

   public void readNbtFromStack(ItemStack stack) {
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      if (lv != null) {
         this.readNbt(lv);
      } else {
         this.shards.clear();

         for(int i = 0; i < 4; ++i) {
            this.shards.add(Items.BRICK);
         }
      }

   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
