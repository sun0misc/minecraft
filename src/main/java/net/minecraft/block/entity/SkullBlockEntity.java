package net.minecraft.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SkullBlockEntity extends BlockEntity {
   public static final String SKULL_OWNER_KEY = "SkullOwner";
   public static final String NOTE_BLOCK_SOUND_KEY = "note_block_sound";
   @Nullable
   private static UserCache userCache;
   @Nullable
   private static MinecraftSessionService sessionService;
   @Nullable
   private static Executor executor;
   @Nullable
   private GameProfile owner;
   @Nullable
   private Identifier noteBlockSound;
   private int poweredTicks;
   private boolean powered;

   public SkullBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.SKULL, pos, state);
   }

   public static void setServices(ApiServices apiServices, Executor executor) {
      userCache = apiServices.userCache();
      sessionService = apiServices.sessionService();
      SkullBlockEntity.executor = executor;
   }

   public static void clearServices() {
      userCache = null;
      sessionService = null;
      executor = null;
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.owner != null) {
         NbtCompound lv = new NbtCompound();
         NbtHelper.writeGameProfile(lv, this.owner);
         nbt.put("SkullOwner", lv);
      }

      if (this.noteBlockSound != null) {
         nbt.putString("note_block_sound", this.noteBlockSound.toString());
      }

   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
         this.setOwner(NbtHelper.toGameProfile(nbt.getCompound("SkullOwner")));
      } else if (nbt.contains("ExtraType", NbtElement.STRING_TYPE)) {
         String string = nbt.getString("ExtraType");
         if (!StringHelper.isEmpty(string)) {
            this.setOwner(new GameProfile((UUID)null, string));
         }
      }

      if (nbt.contains("note_block_sound", NbtElement.STRING_TYPE)) {
         this.noteBlockSound = Identifier.tryParse(nbt.getString("note_block_sound"));
      }

   }

   public static void tick(World world, BlockPos pos, BlockState state, SkullBlockEntity blockEntity) {
      if (world.isReceivingRedstonePower(pos)) {
         blockEntity.powered = true;
         ++blockEntity.poweredTicks;
      } else {
         blockEntity.powered = false;
      }

   }

   public float getPoweredTicks(float tickDelta) {
      return this.powered ? (float)this.poweredTicks + tickDelta : (float)this.poweredTicks;
   }

   @Nullable
   public GameProfile getOwner() {
      return this.owner;
   }

   @Nullable
   public Identifier getNoteBlockSound() {
      return this.noteBlockSound;
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public void setOwner(@Nullable GameProfile owner) {
      synchronized(this) {
         this.owner = owner;
      }

      this.loadOwnerProperties();
   }

   private void loadOwnerProperties() {
      loadProperties(this.owner, (owner) -> {
         this.owner = owner;
         this.markDirty();
      });
   }

   public static void loadProperties(@Nullable GameProfile owner, Consumer callback) {
      if (owner != null && !StringHelper.isEmpty(owner.getName()) && (!owner.isComplete() || !owner.getProperties().containsKey("textures")) && userCache != null && sessionService != null) {
         userCache.findByNameAsync(owner.getName(), (profile) -> {
            Util.getMainWorkerExecutor().execute(() -> {
               Util.ifPresentOrElse(profile, (profilex) -> {
                  Property property = (Property)Iterables.getFirst(profilex.getProperties().get("textures"), (Object)null);
                  if (property == null) {
                     MinecraftSessionService minecraftSessionService = sessionService;
                     if (minecraftSessionService == null) {
                        return;
                     }

                     profilex = minecraftSessionService.fillProfileProperties(profilex, true);
                  }

                  Executor executor = SkullBlockEntity.executor;
                  if (executor != null) {
                     executor.execute(() -> {
                        UserCache lv = userCache;
                        if (lv != null) {
                           lv.add(profilex);
                           callback.accept(profilex);
                        }

                     });
                  }

               }, () -> {
                  Executor executor = SkullBlockEntity.executor;
                  if (executor != null) {
                     executor.execute(() -> {
                        callback.accept(owner);
                     });
                  }

               });
            });
         });
      } else {
         callback.accept(owner);
      }
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
