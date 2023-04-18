package net.minecraft.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SignBlockEntity extends BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TEXT_WIDTH = 90;
   private static final int TEXT_LINE_HEIGHT = 10;
   @Nullable
   private UUID editor;
   private SignText frontText;
   private SignText backText;
   private boolean waxed;

   public SignBlockEntity(BlockPos pos, BlockState state) {
      this(BlockEntityType.SIGN, pos, state);
   }

   public SignBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
      super(arg, arg2, arg3);
      this.frontText = this.createText();
      this.backText = this.createText();
   }

   protected SignText createText() {
      return new SignText();
   }

   public boolean isPlayerFacingFront(PlayerEntity player) {
      Block var3 = this.getCachedState().getBlock();
      if (var3 instanceof AbstractSignBlock lv) {
         Vec3d lv2 = lv.getCenter(this.getCachedState());
         double d = player.getX() - ((double)this.getPos().getX() + lv2.x);
         double e = player.getZ() - ((double)this.getPos().getZ() + lv2.z);
         float f = lv.getRotationDegrees(this.getCachedState());
         float g = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0F;
         return MathHelper.angleBetween(f, g) <= 90.0F;
      } else {
         return false;
      }
   }

   public SignText getTextFacing(PlayerEntity player) {
      return this.getText(this.isPlayerFacingFront(player));
   }

   public SignText getText(boolean front) {
      return front ? this.frontText : this.backText;
   }

   public SignText getFrontText() {
      return this.frontText;
   }

   public SignText getBackText() {
      return this.backText;
   }

   public int getTextLineHeight() {
      return 10;
   }

   public int getMaxTextWidth() {
      return 90;
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      DataResult var10000 = SignText.CODEC.encodeStart(NbtOps.INSTANCE, this.frontText);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((frontText) -> {
         nbt.put("front_text", frontText);
      });
      var10000 = SignText.CODEC.encodeStart(NbtOps.INSTANCE, this.backText);
      var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((backText) -> {
         nbt.put("back_text", backText);
      });
      nbt.putBoolean("is_waxed", this.waxed);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      DataResult var10000;
      Logger var10001;
      if (nbt.contains("front_text")) {
         var10000 = SignText.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("front_text"));
         var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((signText) -> {
            this.frontText = this.parseLines(signText);
         });
      }

      if (nbt.contains("back_text")) {
         var10000 = SignText.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("back_text"));
         var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((signText) -> {
            this.backText = this.parseLines(signText);
         });
      }

      this.waxed = nbt.getBoolean("is_waxed");
   }

   private SignText parseLines(SignText signText) {
      for(int i = 0; i < 4; ++i) {
         Text lv = this.parseLine(signText.getMessage(i, false));
         Text lv2 = this.parseLine(signText.getMessage(i, true));
         signText = signText.withMessage(i, lv, lv2);
      }

      return signText;
   }

   private Text parseLine(Text text) {
      World var3 = this.world;
      if (var3 instanceof ServerWorld lv) {
         try {
            return Texts.parse(createCommandSource((PlayerEntity)null, lv, this.pos), (Text)text, (Entity)null, 0);
         } catch (CommandSyntaxException var4) {
         }
      }

      return text;
   }

   public void tryChangeText(PlayerEntity player, boolean front, List messages) {
      if (!this.isWaxed() && player.getUuid().equals(this.getEditor()) && this.world != null) {
         this.changeText((text) -> {
            return this.getTextWithMessages(player, messages, text);
         }, front);
         this.setEditor((UUID)null);
         this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
      } else {
         LOGGER.warn("Player {} just tried to change non-editable sign", player.getName().getString());
      }
   }

   public boolean changeText(UnaryOperator textChanger, boolean front) {
      SignText lv = this.getText(front);
      return this.setText((SignText)textChanger.apply(lv), front);
   }

   private SignText getTextWithMessages(PlayerEntity player, List messages, SignText text) {
      for(int i = 0; i < messages.size(); ++i) {
         FilteredMessage lv = (FilteredMessage)messages.get(i);
         Style lv2 = text.getMessage(i, player.shouldFilterText()).getStyle();
         if (player.shouldFilterText()) {
            text = text.withMessage(i, Text.literal(lv.getString()).setStyle(lv2));
         } else {
            text = text.withMessage(i, Text.literal(lv.raw()).setStyle(lv2), Text.literal(lv.getString()).setStyle(lv2));
         }
      }

      return text;
   }

   public boolean setText(SignText text, boolean front) {
      return front ? this.setFrontText(text) : this.setBackText(text);
   }

   private boolean setBackText(SignText backText) {
      if (backText != this.backText) {
         this.backText = backText;
         this.updateListeners();
         return true;
      } else {
         return false;
      }
   }

   private boolean setFrontText(SignText frontText) {
      if (frontText != this.frontText) {
         this.frontText = frontText;
         this.updateListeners();
         return true;
      } else {
         return false;
      }
   }

   public boolean canRunCommandClickEvent(boolean front, PlayerEntity player) {
      return this.isWaxed() && this.getText(front).hasRunCommandClickEvent(player);
   }

   public boolean runCommandClickEvent(PlayerEntity player, World world, BlockPos pos, boolean front) {
      boolean bl2 = false;
      Text[] var6 = this.getText(front).getMessages(player.shouldFilterText());
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Text lv = var6[var8];
         Style lv2 = lv.getStyle();
         ClickEvent lv3 = lv2.getClickEvent();
         if (lv3 != null && lv3.getAction() == ClickEvent.Action.RUN_COMMAND) {
            player.getServer().getCommandManager().executeWithPrefix(createCommandSource(player, world, pos), lv3.getValue());
            bl2 = true;
         }
      }

      return bl2;
   }

   private static ServerCommandSource createCommandSource(@Nullable PlayerEntity player, World world, BlockPos pos) {
      String string = player == null ? "Sign" : player.getName().getString();
      Text lv = player == null ? Text.literal("Sign") : player.getDisplayName();
      return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ofCenter(pos), Vec2f.ZERO, (ServerWorld)world, 2, string, (Text)lv, world.getServer(), player);
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public boolean copyItemDataRequiresOperator() {
      return true;
   }

   public void setEditor(@Nullable UUID editor) {
      this.editor = editor;
   }

   @Nullable
   public UUID getEditor() {
      return this.editor;
   }

   private void updateListeners() {
      this.markDirty();
      this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
   }

   public boolean isWaxed() {
      return this.waxed;
   }

   public boolean setWaxed(boolean waxed) {
      if (this.waxed != waxed) {
         this.waxed = waxed;
         this.updateListeners();
         return true;
      } else {
         return false;
      }
   }

   public boolean isPlayerTooFarToEdit(UUID uuid) {
      PlayerEntity lv = this.world.getPlayerByUuid(uuid);
      return lv == null || lv.squaredDistanceTo((double)this.getPos().getX(), (double)this.getPos().getY(), (double)this.getPos().getZ()) > 64.0;
   }

   public static void tick(World world, BlockPos pos, BlockState state, SignBlockEntity blockEntity) {
      UUID uUID = blockEntity.getEditor();
      if (uUID != null) {
         blockEntity.tryClearInvalidEditor(blockEntity, world, uUID);
      }

   }

   private void tryClearInvalidEditor(SignBlockEntity blockEntity, World world, UUID uuid) {
      if (blockEntity.isPlayerTooFarToEdit(uuid)) {
         blockEntity.setEditor((UUID)null);
      }

   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
