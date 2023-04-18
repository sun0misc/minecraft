package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OperatorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPlayerInteractionManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftClient client;
   private final ClientPlayNetworkHandler networkHandler;
   private BlockPos currentBreakingPos = new BlockPos(-1, -1, -1);
   private ItemStack selectedStack;
   private float currentBreakingProgress;
   private float blockBreakingSoundCooldown;
   private int blockBreakingCooldown;
   private boolean breakingBlock;
   private GameMode gameMode;
   @Nullable
   private GameMode previousGameMode;
   private int lastSelectedSlot;

   public ClientPlayerInteractionManager(MinecraftClient client, ClientPlayNetworkHandler networkHandler) {
      this.selectedStack = ItemStack.EMPTY;
      this.gameMode = GameMode.DEFAULT;
      this.client = client;
      this.networkHandler = networkHandler;
   }

   public void copyAbilities(PlayerEntity player) {
      this.gameMode.setAbilities(player.getAbilities());
   }

   public void setGameModes(GameMode gameMode, @Nullable GameMode previousGameMode) {
      this.gameMode = gameMode;
      this.previousGameMode = previousGameMode;
      this.gameMode.setAbilities(this.client.player.getAbilities());
   }

   public void setGameMode(GameMode gameMode) {
      if (gameMode != this.gameMode) {
         this.previousGameMode = this.gameMode;
      }

      this.gameMode = gameMode;
      this.gameMode.setAbilities(this.client.player.getAbilities());
   }

   public boolean hasStatusBars() {
      return this.gameMode.isSurvivalLike();
   }

   public boolean breakBlock(BlockPos pos) {
      if (this.client.player.isBlockBreakingRestricted(this.client.world, pos, this.gameMode)) {
         return false;
      } else {
         World lv = this.client.world;
         BlockState lv2 = lv.getBlockState(pos);
         if (!this.client.player.getMainHandStack().getItem().canMine(lv2, lv, pos, this.client.player)) {
            return false;
         } else {
            Block lv3 = lv2.getBlock();
            if (lv3 instanceof OperatorBlock && !this.client.player.isCreativeLevelTwoOp()) {
               return false;
            } else if (lv2.isAir()) {
               return false;
            } else {
               lv3.onBreak(lv, pos, lv2, this.client.player);
               FluidState lv4 = lv.getFluidState(pos);
               boolean bl = lv.setBlockState(pos, lv4.getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
               if (bl) {
                  lv3.onBroken(lv, pos, lv2);
               }

               return bl;
            }
         }
      }
   }

   public boolean attackBlock(BlockPos pos, Direction direction) {
      if (this.client.player.isBlockBreakingRestricted(this.client.world, pos, this.gameMode)) {
         return false;
      } else if (!this.client.world.getWorldBorder().contains(pos)) {
         return false;
      } else {
         BlockState lv;
         if (this.gameMode.isCreative()) {
            lv = this.client.world.getBlockState(pos);
            this.client.getTutorialManager().onBlockBreaking(this.client.world, pos, lv, 1.0F);
            this.sendSequencedPacket(this.client.world, (sequence) -> {
               this.breakBlock(pos);
               return new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
            });
            this.blockBreakingCooldown = 5;
         } else if (!this.breakingBlock || !this.isCurrentlyBreaking(pos)) {
            if (this.breakingBlock) {
               this.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.currentBreakingPos, direction));
            }

            lv = this.client.world.getBlockState(pos);
            this.client.getTutorialManager().onBlockBreaking(this.client.world, pos, lv, 0.0F);
            this.sendSequencedPacket(this.client.world, (sequence) -> {
               boolean bl = !lv.isAir();
               if (bl && this.currentBreakingProgress == 0.0F) {
                  lv.onBlockBreakStart(this.client.world, pos, this.client.player);
               }

               if (bl && lv.calcBlockBreakingDelta(this.client.player, this.client.player.world, pos) >= 1.0F) {
                  this.breakBlock(pos);
               } else {
                  this.breakingBlock = true;
                  this.currentBreakingPos = pos;
                  this.selectedStack = this.client.player.getMainHandStack();
                  this.currentBreakingProgress = 0.0F;
                  this.blockBreakingSoundCooldown = 0.0F;
                  this.client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, (int)(this.currentBreakingProgress * 10.0F) - 1);
               }

               return new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
            });
         }

         return true;
      }
   }

   public void cancelBlockBreaking() {
      if (this.breakingBlock) {
         BlockState lv = this.client.world.getBlockState(this.currentBreakingPos);
         this.client.getTutorialManager().onBlockBreaking(this.client.world, this.currentBreakingPos, lv, -1.0F);
         this.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.currentBreakingPos, Direction.DOWN));
         this.breakingBlock = false;
         this.currentBreakingProgress = 0.0F;
         this.client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, -1);
         this.client.player.resetLastAttackedTicks();
      }

   }

   public boolean updateBlockBreakingProgress(BlockPos pos, Direction direction) {
      this.syncSelectedSlot();
      if (this.blockBreakingCooldown > 0) {
         --this.blockBreakingCooldown;
         return true;
      } else {
         BlockState lv;
         if (this.gameMode.isCreative() && this.client.world.getWorldBorder().contains(pos)) {
            this.blockBreakingCooldown = 5;
            lv = this.client.world.getBlockState(pos);
            this.client.getTutorialManager().onBlockBreaking(this.client.world, pos, lv, 1.0F);
            this.sendSequencedPacket(this.client.world, (sequence) -> {
               this.breakBlock(pos);
               return new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
            });
            return true;
         } else if (this.isCurrentlyBreaking(pos)) {
            lv = this.client.world.getBlockState(pos);
            if (lv.isAir()) {
               this.breakingBlock = false;
               return false;
            } else {
               this.currentBreakingProgress += lv.calcBlockBreakingDelta(this.client.player, this.client.player.world, pos);
               if (this.blockBreakingSoundCooldown % 4.0F == 0.0F) {
                  BlockSoundGroup lv2 = lv.getSoundGroup();
                  this.client.getSoundManager().play(new PositionedSoundInstance(lv2.getHitSound(), SoundCategory.BLOCKS, (lv2.getVolume() + 1.0F) / 8.0F, lv2.getPitch() * 0.5F, SoundInstance.createRandom(), pos));
               }

               ++this.blockBreakingSoundCooldown;
               this.client.getTutorialManager().onBlockBreaking(this.client.world, pos, lv, MathHelper.clamp(this.currentBreakingProgress, 0.0F, 1.0F));
               if (this.currentBreakingProgress >= 1.0F) {
                  this.breakingBlock = false;
                  this.sendSequencedPacket(this.client.world, (sequence) -> {
                     this.breakBlock(pos);
                     return new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction, sequence);
                  });
                  this.currentBreakingProgress = 0.0F;
                  this.blockBreakingSoundCooldown = 0.0F;
                  this.blockBreakingCooldown = 5;
               }

               this.client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, (int)(this.currentBreakingProgress * 10.0F) - 1);
               return true;
            }
         } else {
            return this.attackBlock(pos, direction);
         }
      }
   }

   private void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator) {
      PendingUpdateManager lv = world.getPendingUpdateManager().incrementSequence();

      try {
         int i = lv.getSequence();
         Packet lv2 = packetCreator.predict(i);
         this.networkHandler.sendPacket(lv2);
      } catch (Throwable var7) {
         if (lv != null) {
            try {
               lv.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (lv != null) {
         lv.close();
      }

   }

   public float getReachDistance() {
      return this.gameMode.isCreative() ? 5.0F : 4.5F;
   }

   public void tick() {
      this.syncSelectedSlot();
      if (this.networkHandler.getConnection().isOpen()) {
         this.networkHandler.getConnection().tick();
      } else {
         this.networkHandler.getConnection().handleDisconnection();
      }

   }

   private boolean isCurrentlyBreaking(BlockPos pos) {
      ItemStack lv = this.client.player.getMainHandStack();
      boolean bl = this.selectedStack.isEmpty() && lv.isEmpty();
      if (!this.selectedStack.isEmpty() && !lv.isEmpty()) {
         bl = lv.isOf(this.selectedStack.getItem()) && ItemStack.areNbtEqual(lv, this.selectedStack) && (lv.isDamageable() || lv.getDamage() == this.selectedStack.getDamage());
      }

      return pos.equals(this.currentBreakingPos) && bl;
   }

   private void syncSelectedSlot() {
      int i = this.client.player.getInventory().selectedSlot;
      if (i != this.lastSelectedSlot) {
         this.lastSelectedSlot = i;
         this.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.lastSelectedSlot));
      }

   }

   public ActionResult interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
      this.syncSelectedSlot();
      if (!this.client.world.getWorldBorder().contains(hitResult.getBlockPos())) {
         return ActionResult.FAIL;
      } else {
         MutableObject mutableObject = new MutableObject();
         this.sendSequencedPacket(this.client.world, (sequence) -> {
            mutableObject.setValue(this.interactBlockInternal(player, hand, hitResult));
            return new PlayerInteractBlockC2SPacket(hand, hitResult, sequence);
         });
         return (ActionResult)mutableObject.getValue();
      }
   }

   private ActionResult interactBlockInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
      BlockPos lv = hitResult.getBlockPos();
      ItemStack lv2 = player.getStackInHand(hand);
      if (this.gameMode == GameMode.SPECTATOR) {
         return ActionResult.SUCCESS;
      } else {
         boolean bl = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
         boolean bl2 = player.shouldCancelInteraction() && bl;
         if (!bl2) {
            BlockState lv3 = this.client.world.getBlockState(lv);
            if (!this.networkHandler.hasFeature(lv3.getBlock().getRequiredFeatures())) {
               return ActionResult.FAIL;
            }

            ActionResult lv4 = lv3.onUse(this.client.world, player, hand, hitResult);
            if (lv4.isAccepted()) {
               return lv4;
            }
         }

         if (!lv2.isEmpty() && !player.getItemCooldownManager().isCoolingDown(lv2.getItem())) {
            ItemUsageContext lv5 = new ItemUsageContext(player, hand, hitResult);
            ActionResult lv6;
            if (this.gameMode.isCreative()) {
               int i = lv2.getCount();
               lv6 = lv2.useOnBlock(lv5);
               lv2.setCount(i);
            } else {
               lv6 = lv2.useOnBlock(lv5);
            }

            return lv6;
         } else {
            return ActionResult.PASS;
         }
      }
   }

   public ActionResult interactItem(PlayerEntity player, Hand hand) {
      if (this.gameMode == GameMode.SPECTATOR) {
         return ActionResult.PASS;
      } else {
         this.syncSelectedSlot();
         this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), player.isOnGround()));
         MutableObject mutableObject = new MutableObject();
         this.sendSequencedPacket(this.client.world, (sequence) -> {
            PlayerInteractItemC2SPacket lv = new PlayerInteractItemC2SPacket(hand, sequence);
            ItemStack lv2 = player.getStackInHand(hand);
            if (player.getItemCooldownManager().isCoolingDown(lv2.getItem())) {
               mutableObject.setValue(ActionResult.PASS);
               return lv;
            } else {
               TypedActionResult lv3 = lv2.use(this.client.world, player, hand);
               ItemStack lv4 = (ItemStack)lv3.getValue();
               if (lv4 != lv2) {
                  player.setStackInHand(hand, lv4);
               }

               mutableObject.setValue(lv3.getResult());
               return lv;
            }
         });
         return (ActionResult)mutableObject.getValue();
      }
   }

   public ClientPlayerEntity createPlayer(ClientWorld world, StatHandler statHandler, ClientRecipeBook recipeBook) {
      return this.createPlayer(world, statHandler, recipeBook, false, false);
   }

   public ClientPlayerEntity createPlayer(ClientWorld world, StatHandler statHandler, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
      return new ClientPlayerEntity(this.client, world, this.networkHandler, statHandler, recipeBook, lastSneaking, lastSprinting);
   }

   public void attackEntity(PlayerEntity player, Entity target) {
      this.syncSelectedSlot();
      this.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
      if (this.gameMode != GameMode.SPECTATOR) {
         player.attack(target);
         player.resetLastAttackedTicks();
      }

   }

   public ActionResult interactEntity(PlayerEntity player, Entity entity, Hand hand) {
      this.syncSelectedSlot();
      this.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, player.isSneaking(), hand));
      return this.gameMode == GameMode.SPECTATOR ? ActionResult.PASS : player.interact(entity, hand);
   }

   public ActionResult interactEntityAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand) {
      this.syncSelectedSlot();
      Vec3d lv = hitResult.getPos().subtract(entity.getX(), entity.getY(), entity.getZ());
      this.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interactAt(entity, player.isSneaking(), hand, lv));
      return this.gameMode == GameMode.SPECTATOR ? ActionResult.PASS : entity.interactAt(player, lv, hand);
   }

   public void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player) {
      ScreenHandler lv = player.currentScreenHandler;
      if (syncId != lv.syncId) {
         LOGGER.warn("Ignoring click in mismatching container. Click in {}, player has {}.", syncId, lv.syncId);
      } else {
         DefaultedList lv2 = lv.slots;
         int l = lv2.size();
         List list = Lists.newArrayListWithCapacity(l);
         Iterator var10 = lv2.iterator();

         while(var10.hasNext()) {
            Slot lv3 = (Slot)var10.next();
            list.add(lv3.getStack().copy());
         }

         lv.onSlotClick(slotId, button, actionType, player);
         Int2ObjectMap int2ObjectMap = new Int2ObjectOpenHashMap();

         for(int m = 0; m < l; ++m) {
            ItemStack lv4 = (ItemStack)list.get(m);
            ItemStack lv5 = ((Slot)lv2.get(m)).getStack();
            if (!ItemStack.areEqual(lv4, lv5)) {
               int2ObjectMap.put(m, lv5.copy());
            }
         }

         this.networkHandler.sendPacket(new ClickSlotC2SPacket(syncId, lv.getRevision(), slotId, button, actionType, lv.getCursorStack().copy(), int2ObjectMap));
      }
   }

   public void clickRecipe(int syncId, Recipe recipe, boolean craftAll) {
      this.networkHandler.sendPacket(new CraftRequestC2SPacket(syncId, recipe, craftAll));
   }

   public void clickButton(int syncId, int buttonId) {
      this.networkHandler.sendPacket(new ButtonClickC2SPacket(syncId, buttonId));
   }

   public void clickCreativeStack(ItemStack stack, int slotId) {
      if (this.gameMode.isCreative() && this.networkHandler.hasFeature(stack.getItem().getRequiredFeatures())) {
         this.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slotId, stack));
      }

   }

   public void dropCreativeStack(ItemStack stack) {
      if (this.gameMode.isCreative() && !stack.isEmpty() && this.networkHandler.hasFeature(stack.getItem().getRequiredFeatures())) {
         this.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(-1, stack));
      }

   }

   public void stopUsingItem(PlayerEntity player) {
      this.syncSelectedSlot();
      this.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
      player.stopUsingItem();
   }

   public boolean hasExperienceBar() {
      return this.gameMode.isSurvivalLike();
   }

   public boolean hasLimitedAttackSpeed() {
      return !this.gameMode.isCreative();
   }

   public boolean hasCreativeInventory() {
      return this.gameMode.isCreative();
   }

   public boolean hasExtendedReach() {
      return this.gameMode.isCreative();
   }

   public boolean hasRidingInventory() {
      return this.client.player.hasVehicle() && this.client.player.getVehicle() instanceof RideableInventory;
   }

   public boolean isFlyingLocked() {
      return this.gameMode == GameMode.SPECTATOR;
   }

   @Nullable
   public GameMode getPreviousGameMode() {
      return this.previousGameMode;
   }

   public GameMode getCurrentGameMode() {
      return this.gameMode;
   }

   public boolean isBreakingBlock() {
      return this.breakingBlock;
   }

   public void pickFromInventory(int slot) {
      this.networkHandler.sendPacket(new PickFromInventoryC2SPacket(slot));
   }
}
