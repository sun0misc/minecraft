package net.minecraft.block.entity;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

public class JigsawBlockEntity extends BlockEntity {
   public static final String TARGET_KEY = "target";
   public static final String POOL_KEY = "pool";
   public static final String JOINT_KEY = "joint";
   public static final String NAME_KEY = "name";
   public static final String FINAL_STATE_KEY = "final_state";
   private Identifier name = new Identifier("empty");
   private Identifier target = new Identifier("empty");
   private RegistryKey pool;
   private Joint joint;
   private String finalState;

   public JigsawBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.JIGSAW, pos, state);
      this.pool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, new Identifier("empty"));
      this.joint = JigsawBlockEntity.Joint.ROLLABLE;
      this.finalState = "minecraft:air";
   }

   public Identifier getName() {
      return this.name;
   }

   public Identifier getTarget() {
      return this.target;
   }

   public RegistryKey getPool() {
      return this.pool;
   }

   public String getFinalState() {
      return this.finalState;
   }

   public Joint getJoint() {
      return this.joint;
   }

   public void setName(Identifier name) {
      this.name = name;
   }

   public void setTarget(Identifier target) {
      this.target = target;
   }

   public void setPool(RegistryKey pool) {
      this.pool = pool;
   }

   public void setFinalState(String finalState) {
      this.finalState = finalState;
   }

   public void setJoint(Joint joint) {
      this.joint = joint;
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putString("name", this.name.toString());
      nbt.putString("target", this.target.toString());
      nbt.putString("pool", this.pool.getValue().toString());
      nbt.putString("final_state", this.finalState);
      nbt.putString("joint", this.joint.asString());
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.name = new Identifier(nbt.getString("name"));
      this.target = new Identifier(nbt.getString("target"));
      this.pool = RegistryKey.of(RegistryKeys.TEMPLATE_POOL, new Identifier(nbt.getString("pool")));
      this.finalState = nbt.getString("final_state");
      this.joint = (Joint)JigsawBlockEntity.Joint.byName(nbt.getString("joint")).orElseGet(() -> {
         return JigsawBlock.getFacing(this.getCachedState()).getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE;
      });
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public void generate(ServerWorld world, int maxDepth, boolean keepJigsaws) {
      BlockPos lv = this.getPos().offset(((JigsawOrientation)this.getCachedState().get(JigsawBlock.ORIENTATION)).getFacing());
      Registry lv2 = world.getRegistryManager().get(RegistryKeys.TEMPLATE_POOL);
      RegistryEntry lv3 = lv2.entryOf(this.pool);
      StructurePoolBasedGenerator.generate(world, lv3, this.target, maxDepth, lv, keepJigsaws);
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }

   public static enum Joint implements StringIdentifiable {
      ROLLABLE("rollable"),
      ALIGNED("aligned");

      private final String name;

      private Joint(String name) {
         this.name = name;
      }

      public String asString() {
         return this.name;
      }

      public static Optional byName(String name) {
         return Arrays.stream(values()).filter((joint) -> {
            return joint.asString().equals(name);
         }).findFirst();
      }

      public Text asText() {
         return Text.translatable("jigsaw_block.joint." + this.name);
      }

      // $FF: synthetic method
      private static Joint[] method_36716() {
         return new Joint[]{ROLLABLE, ALIGNED};
      }
   }
}
