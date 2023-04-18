package net.minecraft.block.entity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StructureBlockBlockEntity extends BlockEntity {
   private static final int field_31367 = 5;
   public static final int field_31364 = 48;
   public static final int field_31365 = 48;
   public static final String AUTHOR_KEY = "author";
   private Identifier templateName;
   private String author = "";
   private String metadata = "";
   private BlockPos offset = new BlockPos(0, 1, 0);
   private Vec3i size;
   private BlockMirror mirror;
   private BlockRotation rotation;
   private StructureBlockMode mode;
   private boolean ignoreEntities;
   private boolean powered;
   private boolean showAir;
   private boolean showBoundingBox;
   private float integrity;
   private long seed;

   public StructureBlockBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.STRUCTURE_BLOCK, pos, state);
      this.size = Vec3i.ZERO;
      this.mirror = BlockMirror.NONE;
      this.rotation = BlockRotation.NONE;
      this.ignoreEntities = true;
      this.showBoundingBox = true;
      this.integrity = 1.0F;
      this.mode = (StructureBlockMode)state.get(StructureBlock.MODE);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putString("name", this.getTemplateName());
      nbt.putString("author", this.author);
      nbt.putString("metadata", this.metadata);
      nbt.putInt("posX", this.offset.getX());
      nbt.putInt("posY", this.offset.getY());
      nbt.putInt("posZ", this.offset.getZ());
      nbt.putInt("sizeX", this.size.getX());
      nbt.putInt("sizeY", this.size.getY());
      nbt.putInt("sizeZ", this.size.getZ());
      nbt.putString("rotation", this.rotation.toString());
      nbt.putString("mirror", this.mirror.toString());
      nbt.putString("mode", this.mode.toString());
      nbt.putBoolean("ignoreEntities", this.ignoreEntities);
      nbt.putBoolean("powered", this.powered);
      nbt.putBoolean("showair", this.showAir);
      nbt.putBoolean("showboundingbox", this.showBoundingBox);
      nbt.putFloat("integrity", this.integrity);
      nbt.putLong("seed", this.seed);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.setTemplateName(nbt.getString("name"));
      this.author = nbt.getString("author");
      this.metadata = nbt.getString("metadata");
      int i = MathHelper.clamp(nbt.getInt("posX"), -48, 48);
      int j = MathHelper.clamp(nbt.getInt("posY"), -48, 48);
      int k = MathHelper.clamp(nbt.getInt("posZ"), -48, 48);
      this.offset = new BlockPos(i, j, k);
      int l = MathHelper.clamp(nbt.getInt("sizeX"), 0, 48);
      int m = MathHelper.clamp(nbt.getInt("sizeY"), 0, 48);
      int n = MathHelper.clamp(nbt.getInt("sizeZ"), 0, 48);
      this.size = new Vec3i(l, m, n);

      try {
         this.rotation = BlockRotation.valueOf(nbt.getString("rotation"));
      } catch (IllegalArgumentException var11) {
         this.rotation = BlockRotation.NONE;
      }

      try {
         this.mirror = BlockMirror.valueOf(nbt.getString("mirror"));
      } catch (IllegalArgumentException var10) {
         this.mirror = BlockMirror.NONE;
      }

      try {
         this.mode = StructureBlockMode.valueOf(nbt.getString("mode"));
      } catch (IllegalArgumentException var9) {
         this.mode = StructureBlockMode.DATA;
      }

      this.ignoreEntities = nbt.getBoolean("ignoreEntities");
      this.powered = nbt.getBoolean("powered");
      this.showAir = nbt.getBoolean("showair");
      this.showBoundingBox = nbt.getBoolean("showboundingbox");
      if (nbt.contains("integrity")) {
         this.integrity = nbt.getFloat("integrity");
      } else {
         this.integrity = 1.0F;
      }

      this.seed = nbt.getLong("seed");
      this.updateBlockMode();
   }

   private void updateBlockMode() {
      if (this.world != null) {
         BlockPos lv = this.getPos();
         BlockState lv2 = this.world.getBlockState(lv);
         if (lv2.isOf(Blocks.STRUCTURE_BLOCK)) {
            this.world.setBlockState(lv, (BlockState)lv2.with(StructureBlock.MODE, this.mode), Block.NOTIFY_LISTENERS);
         }

      }
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public boolean openScreen(PlayerEntity player) {
      if (!player.isCreativeLevelTwoOp()) {
         return false;
      } else {
         if (player.getEntityWorld().isClient) {
            player.openStructureBlockScreen(this);
         }

         return true;
      }
   }

   public String getTemplateName() {
      return this.templateName == null ? "" : this.templateName.toString();
   }

   public String getStructurePath() {
      return this.templateName == null ? "" : this.templateName.getPath();
   }

   public boolean hasStructureName() {
      return this.templateName != null;
   }

   public void setTemplateName(@Nullable String templateName) {
      this.setTemplateName(StringHelper.isEmpty(templateName) ? null : Identifier.tryParse(templateName));
   }

   public void setTemplateName(@Nullable Identifier templateName) {
      this.templateName = templateName;
   }

   public void setAuthor(LivingEntity entity) {
      this.author = entity.getName().getString();
   }

   public BlockPos getOffset() {
      return this.offset;
   }

   public void setOffset(BlockPos offset) {
      this.offset = offset;
   }

   public Vec3i getSize() {
      return this.size;
   }

   public void setSize(Vec3i size) {
      this.size = size;
   }

   public BlockMirror getMirror() {
      return this.mirror;
   }

   public void setMirror(BlockMirror mirror) {
      this.mirror = mirror;
   }

   public BlockRotation getRotation() {
      return this.rotation;
   }

   public void setRotation(BlockRotation rotation) {
      this.rotation = rotation;
   }

   public String getMetadata() {
      return this.metadata;
   }

   public void setMetadata(String metadata) {
      this.metadata = metadata;
   }

   public StructureBlockMode getMode() {
      return this.mode;
   }

   public void setMode(StructureBlockMode mode) {
      this.mode = mode;
      BlockState lv = this.world.getBlockState(this.getPos());
      if (lv.isOf(Blocks.STRUCTURE_BLOCK)) {
         this.world.setBlockState(this.getPos(), (BlockState)lv.with(StructureBlock.MODE, mode), Block.NOTIFY_LISTENERS);
      }

   }

   public boolean shouldIgnoreEntities() {
      return this.ignoreEntities;
   }

   public void setIgnoreEntities(boolean ignoreEntities) {
      this.ignoreEntities = ignoreEntities;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public void setIntegrity(float integrity) {
      this.integrity = integrity;
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.seed = seed;
   }

   public boolean detectStructureSize() {
      if (this.mode != StructureBlockMode.SAVE) {
         return false;
      } else {
         BlockPos lv = this.getPos();
         int i = true;
         BlockPos lv2 = new BlockPos(lv.getX() - 80, this.world.getBottomY(), lv.getZ() - 80);
         BlockPos lv3 = new BlockPos(lv.getX() + 80, this.world.getTopY() - 1, lv.getZ() + 80);
         Stream stream = this.streamCornerPos(lv2, lv3);
         return getStructureBox(lv, stream).filter((box) -> {
            int i = box.getMaxX() - box.getMinX();
            int j = box.getMaxY() - box.getMinY();
            int k = box.getMaxZ() - box.getMinZ();
            if (i > 1 && j > 1 && k > 1) {
               this.offset = new BlockPos(box.getMinX() - lv.getX() + 1, box.getMinY() - lv.getY() + 1, box.getMinZ() - lv.getZ() + 1);
               this.size = new Vec3i(i - 1, j - 1, k - 1);
               this.markDirty();
               BlockState lvx = this.world.getBlockState(lv);
               this.world.updateListeners(lv, lvx, lvx, Block.NOTIFY_ALL);
               return true;
            } else {
               return false;
            }
         }).isPresent();
      }
   }

   private Stream streamCornerPos(BlockPos start, BlockPos end) {
      Stream var10000 = BlockPos.stream(start, end).filter((pos) -> {
         return this.world.getBlockState(pos).isOf(Blocks.STRUCTURE_BLOCK);
      });
      World var10001 = this.world;
      Objects.requireNonNull(var10001);
      return var10000.map(var10001::getBlockEntity).filter((blockEntity) -> {
         return blockEntity instanceof StructureBlockBlockEntity;
      }).map((blockEntity) -> {
         return (StructureBlockBlockEntity)blockEntity;
      }).filter((blockEntity) -> {
         return blockEntity.mode == StructureBlockMode.CORNER && Objects.equals(this.templateName, blockEntity.templateName);
      }).map(BlockEntity::getPos);
   }

   private static Optional getStructureBox(BlockPos pos, Stream corners) {
      Iterator iterator = corners.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BlockPos lv = (BlockPos)iterator.next();
         BlockBox lv2 = new BlockBox(lv);
         if (iterator.hasNext()) {
            Objects.requireNonNull(lv2);
            iterator.forEachRemaining(lv2::encompass);
         } else {
            lv2.encompass(pos);
         }

         return Optional.of(lv2);
      }
   }

   public boolean saveStructure() {
      return this.saveStructure(true);
   }

   public boolean saveStructure(boolean bl) {
      if (this.mode == StructureBlockMode.SAVE && !this.world.isClient && this.templateName != null) {
         BlockPos lv = this.getPos().add(this.offset);
         ServerWorld lv2 = (ServerWorld)this.world;
         StructureTemplateManager lv3 = lv2.getStructureTemplateManager();

         StructureTemplate lv4;
         try {
            lv4 = lv3.getTemplateOrBlank(this.templateName);
         } catch (InvalidIdentifierException var8) {
            return false;
         }

         lv4.saveFromWorld(this.world, lv, this.size, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
         lv4.setAuthor(this.author);
         if (bl) {
            try {
               return lv3.saveTemplate(this.templateName);
            } catch (InvalidIdentifierException var7) {
               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean loadStructure(ServerWorld world) {
      return this.loadStructure(world, true);
   }

   public static Random createRandom(long seed) {
      return seed == 0L ? Random.create(Util.getMeasuringTimeMs()) : Random.create(seed);
   }

   public boolean loadStructure(ServerWorld world, boolean bl) {
      if (this.mode == StructureBlockMode.LOAD && this.templateName != null) {
         StructureTemplateManager lv = world.getStructureTemplateManager();

         Optional optional;
         try {
            optional = lv.getTemplate(this.templateName);
         } catch (InvalidIdentifierException var6) {
            return false;
         }

         return !optional.isPresent() ? false : this.place(world, bl, (StructureTemplate)optional.get());
      } else {
         return false;
      }
   }

   public boolean place(ServerWorld world, boolean bl, StructureTemplate template) {
      BlockPos lv = this.getPos();
      if (!StringHelper.isEmpty(template.getAuthor())) {
         this.author = template.getAuthor();
      }

      Vec3i lv2 = template.getSize();
      boolean bl2 = this.size.equals(lv2);
      if (!bl2) {
         this.size = lv2;
         this.markDirty();
         BlockState lv3 = world.getBlockState(lv);
         world.updateListeners(lv, lv3, lv3, Block.NOTIFY_ALL);
      }

      if (bl && !bl2) {
         return false;
      } else {
         StructurePlacementData lv4 = (new StructurePlacementData()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);
         if (this.integrity < 1.0F) {
            lv4.clearProcessors().addProcessor(new BlockRotStructureProcessor(MathHelper.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
         }

         BlockPos lv5 = lv.add(this.offset);
         template.place(world, lv5, lv5, lv4, createRandom(this.seed), 2);
         return true;
      }
   }

   public void unloadStructure() {
      if (this.templateName != null) {
         ServerWorld lv = (ServerWorld)this.world;
         StructureTemplateManager lv2 = lv.getStructureTemplateManager();
         lv2.unloadTemplate(this.templateName);
      }
   }

   public boolean isStructureAvailable() {
      if (this.mode == StructureBlockMode.LOAD && !this.world.isClient && this.templateName != null) {
         ServerWorld lv = (ServerWorld)this.world;
         StructureTemplateManager lv2 = lv.getStructureTemplateManager();

         try {
            return lv2.getTemplate(this.templateName).isPresent();
         } catch (InvalidIdentifierException var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isPowered() {
      return this.powered;
   }

   public void setPowered(boolean powered) {
      this.powered = powered;
   }

   public boolean shouldShowAir() {
      return this.showAir;
   }

   public void setShowAir(boolean showAir) {
      this.showAir = showAir;
   }

   public boolean shouldShowBoundingBox() {
      return this.showBoundingBox;
   }

   public void setShowBoundingBox(boolean showBoundingBox) {
      this.showBoundingBox = showBoundingBox;
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }

   // $FF: synthetic method
   private static void setStructureVoid(ServerWorld world, BlockPos pos) {
      world.setBlockState(pos, Blocks.STRUCTURE_VOID.getDefaultState(), Block.NOTIFY_LISTENERS);
   }

   public static enum Action {
      UPDATE_DATA,
      SAVE_AREA,
      LOAD_AREA,
      SCAN_AREA;

      // $FF: synthetic method
      private static Action[] method_36718() {
         return new Action[]{UPDATE_DATA, SAVE_AREA, LOAD_AREA, SCAN_AREA};
      }
   }
}
