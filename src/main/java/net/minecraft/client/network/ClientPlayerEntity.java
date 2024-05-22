/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.class_9797;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HangingSignEditScreen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.screen.ingame.MinecartCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.sound.AmbientSoundLoops;
import net.minecraft.client.sound.AmbientSoundPlayer;
import net.minecraft.client.sound.BiomeEffectSoundPlayer;
import net.minecraft.client.sound.BubbleColumnSoundPlayer;
import net.minecraft.client.sound.ElytraSoundInstance;
import net.minecraft.client.sound.MinecartInsideSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientPlayerEntity
extends AbstractClientPlayerEntity {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_32671 = 20;
    private static final int field_32672 = 600;
    private static final int field_32673 = 100;
    private static final float field_32674 = 0.6f;
    private static final double field_32675 = 0.35;
    private static final double MAX_SOFT_COLLISION_RADIANS = 0.13962633907794952;
    public final ClientPlayNetworkHandler networkHandler;
    private final StatHandler statHandler;
    private final ClientRecipeBook recipeBook;
    private final List<ClientPlayerTickable> tickables = Lists.newArrayList();
    private int clientPermissionLevel = 0;
    private double lastX;
    private double lastBaseY;
    private double lastZ;
    private float lastYaw;
    private float lastPitch;
    private boolean lastOnGround;
    private boolean inSneakingPose;
    private boolean lastSneaking;
    private boolean lastSprinting;
    private int ticksSinceLastPositionPacketSent;
    private boolean healthInitialized;
    public Input input;
    protected final MinecraftClient client;
    protected int ticksLeftToDoubleTapSprint;
    public float renderYaw;
    public float renderPitch;
    public float lastRenderYaw;
    public float lastRenderPitch;
    private int field_3938;
    private float mountJumpStrength;
    public float nauseaIntensity;
    public float prevNauseaIntensity;
    private boolean usingItem;
    @Nullable
    private Hand activeHand;
    private boolean riding;
    private boolean autoJumpEnabled = true;
    private int ticksToNextAutojump;
    private boolean falling;
    private int underwaterVisibilityTicks;
    private boolean showsDeathScreen = true;
    private boolean limitedCraftingEnabled = false;

    public ClientPlayerEntity(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
        super(world, networkHandler.getProfile());
        this.client = client;
        this.networkHandler = networkHandler;
        this.statHandler = stats;
        this.recipeBook = recipeBook;
        this.lastSneaking = lastSneaking;
        this.lastSprinting = lastSprinting;
        this.tickables.add(new AmbientSoundPlayer(this, client.getSoundManager()));
        this.tickables.add(new BubbleColumnSoundPlayer(this));
        this.tickables.add(new BiomeEffectSoundPlayer(this, client.getSoundManager(), world.getBiomeAccess()));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void heal(float amount) {
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        if (!super.startRiding(entity, force)) {
            return false;
        }
        if (entity instanceof AbstractMinecartEntity) {
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, true));
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, false));
        }
        return true;
    }

    @Override
    public void dismountVehicle() {
        super.dismountVehicle();
        this.riding = false;
    }

    @Override
    public float getPitch(float tickDelta) {
        return this.getPitch();
    }

    @Override
    public float getYaw(float tickDelta) {
        if (this.hasVehicle()) {
            return super.getYaw(tickDelta);
        }
        return this.getYaw();
    }

    @Override
    public void tick() {
        if (!this.getWorld().isPosLoaded(this.getBlockX(), this.getBlockZ())) {
            return;
        }
        super.tick();
        if (this.hasVehicle()) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround()));
            this.networkHandler.sendPacket(new PlayerInputC2SPacket(this.sidewaysSpeed, this.forwardSpeed, this.input.jumping, this.input.sneaking));
            Entity lv = this.getRootVehicle();
            if (lv != this && lv.isLogicalSideForUpdatingMovement()) {
                this.networkHandler.sendPacket(new VehicleMoveC2SPacket(lv));
                this.sendSprintingPacket();
            }
        } else {
            this.sendMovementPackets();
        }
        for (ClientPlayerTickable lv2 : this.tickables) {
            lv2.tick();
        }
    }

    public float getMoodPercentage() {
        for (ClientPlayerTickable lv : this.tickables) {
            if (!(lv instanceof BiomeEffectSoundPlayer)) continue;
            return ((BiomeEffectSoundPlayer)lv).getMoodPercentage();
        }
        return 0.0f;
    }

    private void sendMovementPackets() {
        this.sendSprintingPacket();
        boolean bl = this.isSneaking();
        if (bl != this.lastSneaking) {
            ClientCommandC2SPacket.Mode lv = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, lv));
            this.lastSneaking = bl;
        }
        if (this.isCamera()) {
            boolean bl3;
            double d = this.getX() - this.lastX;
            double e = this.getY() - this.lastBaseY;
            double f = this.getZ() - this.lastZ;
            double g = this.getYaw() - this.lastYaw;
            double h = this.getPitch() - this.lastPitch;
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl4 = bl3 = g != 0.0 || h != 0.0;
            if (this.hasVehicle()) {
                Vec3d lv2 = this.getVelocity();
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(lv2.x, -999.0, lv2.z, this.getYaw(), this.getPitch(), this.isOnGround()));
                bl2 = false;
            } else if (bl2 && bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch(), this.isOnGround()));
            } else if (bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getX(), this.getY(), this.getZ(), this.isOnGround()));
            } else if (bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround()));
            } else if (this.lastOnGround != this.isOnGround()) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround()));
            }
            if (bl2) {
                this.lastX = this.getX();
                this.lastBaseY = this.getY();
                this.lastZ = this.getZ();
                this.ticksSinceLastPositionPacketSent = 0;
            }
            if (bl3) {
                this.lastYaw = this.getYaw();
                this.lastPitch = this.getPitch();
            }
            this.lastOnGround = this.isOnGround();
            this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
        }
    }

    private void sendSprintingPacket() {
        boolean bl = this.isSprinting();
        if (bl != this.lastSprinting) {
            ClientCommandC2SPacket.Mode lv = bl ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, lv));
            this.lastSprinting = bl;
        }
    }

    public boolean dropSelectedItem(boolean entireStack) {
        PlayerActionC2SPacket.Action lv = entireStack ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
        ItemStack lv2 = this.getInventory().dropSelectedItem(entireStack);
        this.networkHandler.sendPacket(new PlayerActionC2SPacket(lv, BlockPos.ORIGIN, Direction.DOWN));
        return !lv2.isEmpty();
    }

    @Override
    public void swingHand(Hand hand) {
        super.swingHand(hand);
        this.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
    }

    @Override
    public void requestRespawn() {
        this.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
        KeyBinding.untoggleStickyKeys();
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return;
        }
        this.setHealth(this.getHealth() - amount);
    }

    @Override
    public void closeHandledScreen() {
        this.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(this.currentScreenHandler.syncId));
        this.closeScreen();
    }

    public void closeScreen() {
        super.closeHandledScreen();
        this.client.setScreen(null);
    }

    public void updateHealth(float health) {
        if (this.healthInitialized) {
            float g = this.getHealth() - health;
            if (g <= 0.0f) {
                this.setHealth(health);
                if (g < 0.0f) {
                    this.timeUntilRegen = 10;
                }
            } else {
                this.lastDamageTaken = g;
                this.timeUntilRegen = 20;
                this.setHealth(health);
                this.hurtTime = this.maxHurtTime = 10;
            }
        } else {
            this.setHealth(health);
            this.healthInitialized = true;
        }
    }

    @Override
    public void sendAbilitiesUpdate() {
        this.networkHandler.sendPacket(new UpdatePlayerAbilitiesC2SPacket(this.getAbilities()));
    }

    @Override
    public boolean isMainPlayer() {
        return true;
    }

    @Override
    public boolean isHoldingOntoLadder() {
        return !this.getAbilities().flying && super.isHoldingOntoLadder();
    }

    @Override
    public boolean shouldSpawnSprintingParticles() {
        return !this.getAbilities().flying && super.shouldSpawnSprintingParticles();
    }

    protected void startRidingJump() {
        this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_RIDING_JUMP, MathHelper.floor(this.getMountJumpStrength() * 100.0f)));
    }

    public void openRidingInventory() {
        this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.OPEN_INVENTORY));
    }

    public StatHandler getStatHandler() {
        return this.statHandler;
    }

    public ClientRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    public void onRecipeDisplayed(RecipeEntry<?> recipe) {
        if (this.recipeBook.shouldDisplay(recipe)) {
            this.recipeBook.onRecipeDisplayed(recipe);
            this.networkHandler.sendPacket(new RecipeBookDataC2SPacket(recipe));
        }
    }

    @Override
    protected int getPermissionLevel() {
        return this.clientPermissionLevel;
    }

    public void setClientPermissionLevel(int clientPermissionLevel) {
        this.clientPermissionLevel = clientPermissionLevel;
    }

    @Override
    public void sendMessage(Text message, boolean overlay) {
        this.client.getMessageHandler().onGameMessage(message, overlay);
    }

    private void pushOutOfBlocks(double x, double z) {
        Direction[] lvs;
        BlockPos lv = BlockPos.ofFloored(x, this.getY(), z);
        if (!this.wouldCollideAt(lv)) {
            return;
        }
        double f = x - (double)lv.getX();
        double g = z - (double)lv.getZ();
        Direction lv2 = null;
        double h = Double.MAX_VALUE;
        for (Direction lv3 : lvs = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}) {
            double j;
            double i = lv3.getAxis().choose(f, 0.0, g);
            double d = j = lv3.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - i : i;
            if (!(j < h) || this.wouldCollideAt(lv.offset(lv3))) continue;
            h = j;
            lv2 = lv3;
        }
        if (lv2 != null) {
            Vec3d lv4 = this.getVelocity();
            if (lv2.getAxis() == Direction.Axis.X) {
                this.setVelocity(0.1 * (double)lv2.getOffsetX(), lv4.y, lv4.z);
            } else {
                this.setVelocity(lv4.x, lv4.y, 0.1 * (double)lv2.getOffsetZ());
            }
        }
    }

    private boolean wouldCollideAt(BlockPos pos) {
        Box lv = this.getBoundingBox();
        Box lv2 = new Box(pos.getX(), lv.minY, pos.getZ(), (double)pos.getX() + 1.0, lv.maxY, (double)pos.getZ() + 1.0).contract(1.0E-7);
        return this.getWorld().canCollide(this, lv2);
    }

    public void setExperience(float progress, int total, int level) {
        this.experienceProgress = progress;
        this.totalExperience = total;
        this.experienceLevel = level;
    }

    @Override
    public void sendMessage(Text message) {
        this.client.inGameHud.getChatHud().addMessage(message);
    }

    @Override
    public void handleStatus(byte status) {
        if (status >= EntityStatuses.SET_OP_LEVEL_0 && status <= EntityStatuses.SET_OP_LEVEL_4) {
            this.setClientPermissionLevel(status - EntityStatuses.SET_OP_LEVEL_0);
        } else {
            super.handleStatus(status);
        }
    }

    public void setShowsDeathScreen(boolean showsDeathScreen) {
        this.showsDeathScreen = showsDeathScreen;
    }

    public boolean showsDeathScreen() {
        return this.showsDeathScreen;
    }

    public void setLimitedCraftingEnabled(boolean limitedCraftingEnabled) {
        this.limitedCraftingEnabled = limitedCraftingEnabled;
    }

    public boolean isLimitedCraftingEnabled() {
        return this.limitedCraftingEnabled;
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch, false);
    }

    @Override
    public void playSoundToPlayer(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), sound, category, volume, pitch, false);
    }

    @Override
    public boolean canMoveVoluntarily() {
        return true;
    }

    @Override
    public void setCurrentHand(Hand hand) {
        ItemStack lv = this.getStackInHand(hand);
        if (lv.isEmpty() || this.isUsingItem()) {
            return;
        }
        super.setCurrentHand(hand);
        this.usingItem = true;
        this.activeHand = hand;
    }

    @Override
    public boolean isUsingItem() {
        return this.usingItem;
    }

    @Override
    public void clearActiveItem() {
        super.clearActiveItem();
        this.usingItem = false;
    }

    @Override
    public Hand getActiveHand() {
        return Objects.requireNonNullElse(this.activeHand, Hand.MAIN_HAND);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (LIVING_FLAGS.equals(data)) {
            Hand lv;
            boolean bl = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
            Hand hand = lv = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
            if (bl && !this.usingItem) {
                this.setCurrentHand(lv);
            } else if (!bl && this.usingItem) {
                this.clearActiveItem();
            }
        }
        if (FLAGS.equals(data) && this.isFallFlying() && !this.falling) {
            this.client.getSoundManager().play(new ElytraSoundInstance(this));
        }
    }

    @Nullable
    public JumpingMount getJumpingMount() {
        JumpingMount lv;
        Entity entity = this.getControllingVehicle();
        return entity instanceof JumpingMount && (lv = (JumpingMount)((Object)entity)).canJump() ? lv : null;
    }

    public float getMountJumpStrength() {
        return this.mountJumpStrength;
    }

    @Override
    public boolean shouldFilterText() {
        return this.client.shouldFilterText();
    }

    @Override
    public void openEditSignScreen(SignBlockEntity sign, boolean front) {
        if (sign instanceof HangingSignBlockEntity) {
            HangingSignBlockEntity lv = (HangingSignBlockEntity)sign;
            this.client.setScreen(new HangingSignEditScreen(lv, front, this.client.shouldFilterText()));
        } else {
            this.client.setScreen(new SignEditScreen(sign, front, this.client.shouldFilterText()));
        }
    }

    @Override
    public void openCommandBlockMinecartScreen(CommandBlockExecutor commandBlockExecutor) {
        this.client.setScreen(new MinecartCommandBlockScreen(commandBlockExecutor));
    }

    @Override
    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
        this.client.setScreen(new CommandBlockScreen(commandBlock));
    }

    @Override
    public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
        this.client.setScreen(new StructureBlockScreen(structureBlock));
    }

    @Override
    public void openJigsawScreen(JigsawBlockEntity jigsaw) {
        this.client.setScreen(new JigsawBlockScreen(jigsaw));
    }

    @Override
    public void useBook(ItemStack book, Hand hand) {
        if (book.isOf(Items.WRITABLE_BOOK)) {
            this.client.setScreen(new BookEditScreen(this, book, hand));
        }
    }

    @Override
    public void addCritParticles(Entity target) {
        this.client.particleManager.addEmitter(target, ParticleTypes.CRIT);
    }

    @Override
    public void addEnchantedHitParticles(Entity target) {
        this.client.particleManager.addEmitter(target, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public boolean isSneaking() {
        return this.input != null && this.input.sneaking;
    }

    @Override
    public boolean isInSneakingPose() {
        return this.inSneakingPose;
    }

    public boolean shouldSlowDown() {
        return this.isInSneakingPose() || this.isCrawling();
    }

    @Override
    public void tickNewAi() {
        super.tickNewAi();
        if (this.isCamera()) {
            this.sidewaysSpeed = this.input.movementSideways;
            this.forwardSpeed = this.input.movementForward;
            this.jumping = this.input.jumping;
            this.lastRenderYaw = this.renderYaw;
            this.lastRenderPitch = this.renderPitch;
            this.renderPitch += (this.getPitch() - this.renderPitch) * 0.5f;
            this.renderYaw += (this.getYaw() - this.renderYaw) * 0.5f;
        }
    }

    protected boolean isCamera() {
        return this.client.getCameraEntity() == this;
    }

    public void init() {
        this.setPose(EntityPose.STANDING);
        if (this.getWorld() != null) {
            for (double d = this.getY(); d > (double)this.getWorld().getBottomY() && d < (double)this.getWorld().getTopY(); d += 1.0) {
                this.setPosition(this.getX(), d, this.getZ());
                if (this.getWorld().isSpaceEmpty(this)) break;
            }
            this.setVelocity(Vec3d.ZERO);
            this.setPitch(0.0f);
        }
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Override
    public void tickMovement() {
        JumpingMount lv3;
        int i;
        ItemStack lv2;
        boolean bl8;
        boolean bl7;
        if (this.ticksLeftToDoubleTapSprint > 0) {
            --this.ticksLeftToDoubleTapSprint;
        }
        if (!(this.client.currentScreen instanceof DownloadingTerrainScreen)) {
            this.method_60887(this.method_60886() == class_9797.class_9798.CONFUSION);
            this.tickPortalCooldown();
        }
        boolean bl = this.input.jumping;
        boolean bl2 = this.input.sneaking;
        boolean bl3 = this.isWalking();
        PlayerAbilities lv = this.getAbilities();
        this.inSneakingPose = !lv.flying && !this.isSwimming() && !this.hasVehicle() && this.canChangeIntoPose(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.canChangeIntoPose(EntityPose.STANDING));
        float f = (float)this.getAttributeValue(EntityAttributes.PLAYER_SNEAKING_SPEED);
        this.input.tick(this.shouldSlowDown(), f);
        this.client.getTutorialManager().onMovement(this.input);
        if (this.isUsingItem() && !this.hasVehicle()) {
            this.input.movementSideways *= 0.2f;
            this.input.movementForward *= 0.2f;
            this.ticksLeftToDoubleTapSprint = 0;
        }
        boolean bl4 = false;
        if (this.ticksToNextAutojump > 0) {
            --this.ticksToNextAutojump;
            bl4 = true;
            this.input.jumping = true;
        }
        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
        }
        if (bl2) {
            this.ticksLeftToDoubleTapSprint = 0;
        }
        boolean bl5 = this.canStartSprinting();
        boolean bl6 = this.hasVehicle() ? this.getVehicle().isOnGround() : this.isOnGround();
        boolean bl9 = bl7 = !bl2 && !bl3;
        if ((bl6 || this.isSubmergedInWater()) && bl7 && bl5) {
            if (this.ticksLeftToDoubleTapSprint > 0 || this.client.options.sprintKey.isPressed()) {
                this.setSprinting(true);
            } else {
                this.ticksLeftToDoubleTapSprint = 7;
            }
        }
        if ((!this.isTouchingWater() || this.isSubmergedInWater()) && bl5 && this.client.options.sprintKey.isPressed()) {
            this.setSprinting(true);
        }
        if (this.isSprinting()) {
            boolean bl92;
            bl8 = !this.input.hasForwardMovement() || !this.canSprint();
            boolean bl10 = bl92 = bl8 || this.horizontalCollision && !this.collidedSoftly || this.isTouchingWater() && !this.isSubmergedInWater();
            if (this.isSwimming()) {
                if (!this.isOnGround() && !this.input.sneaking && bl8 || !this.isTouchingWater()) {
                    this.setSprinting(false);
                }
            } else if (bl92) {
                this.setSprinting(false);
            }
        }
        bl8 = false;
        if (lv.allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!lv.flying) {
                    lv.flying = true;
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!bl && this.input.jumping && !bl4) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    boolean bl11 = lv.flying = !lv.flying;
                    if (lv.flying && this.isOnGround()) {
                        this.jump();
                    }
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }
        if (this.input.jumping && !bl8 && !bl && !lv.flying && !this.hasVehicle() && !this.isClimbing() && (lv2 = this.getEquippedStack(EquipmentSlot.CHEST)).isOf(Items.ELYTRA) && ElytraItem.isUsable(lv2) && this.checkFallFlying()) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        this.falling = this.isFallFlying();
        if (this.isTouchingWater() && this.input.sneaking && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
        if (this.isSubmergedIn(FluidTags.WATER)) {
            i = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + i, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }
        if (lv.flying && this.isCamera()) {
            i = 0;
            if (this.input.sneaking) {
                --i;
            }
            if (this.input.jumping) {
                ++i;
            }
            if (i != 0) {
                this.setVelocity(this.getVelocity().add(0.0, (float)i * lv.getFlySpeed() * 3.0f, 0.0));
            }
        }
        if ((lv3 = this.getJumpingMount()) != null && lv3.getJumpCooldown() == 0) {
            if (this.field_3938 < 0) {
                ++this.field_3938;
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0f;
                }
            }
            if (bl && !this.input.jumping) {
                this.field_3938 = -10;
                lv3.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0f));
                this.startRidingJump();
            } else if (!bl && this.input.jumping) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0f;
            } else if (bl) {
                ++this.field_3938;
                this.mountJumpStrength = this.field_3938 < 10 ? (float)this.field_3938 * 0.1f : 0.8f + 2.0f / (float)(this.field_3938 - 9) * 0.1f;
            }
        } else {
            this.mountJumpStrength = 0.0f;
        }
        super.tickMovement();
        if (this.isOnGround() && lv.flying && !this.client.interactionManager.isFlyingLocked()) {
            lv.flying = false;
            this.sendAbilitiesUpdate();
        }
    }

    public class_9797.class_9798 method_60886() {
        return this.field_51994 == null ? class_9797.class_9798.NONE : this.field_51994.method_60700();
    }

    @Override
    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    private void method_60887(boolean bl) {
        this.prevNauseaIntensity = this.nauseaIntensity;
        float f = 0.0f;
        if (bl && this.field_51994 != null && this.field_51994.method_60709()) {
            if (!(this.client.currentScreen == null || this.client.currentScreen.shouldPause() || this.client.currentScreen instanceof DeathScreen || this.client.currentScreen instanceof CreditsScreen)) {
                if (this.client.currentScreen instanceof HandledScreen) {
                    this.closeHandledScreen();
                }
                this.client.setScreen(null);
            }
            if (this.nauseaIntensity == 0.0f) {
                this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRIGGER, this.random.nextFloat() * 0.4f + 0.8f, 0.25f));
            }
            f = 0.0125f;
            this.field_51994.method_60705(false);
        } else if (this.hasStatusEffect(StatusEffects.NAUSEA) && !this.getStatusEffect(StatusEffects.NAUSEA).isDurationBelow(60)) {
            f = 0.006666667f;
        } else if (this.nauseaIntensity > 0.0f) {
            f = -0.05f;
        }
        this.nauseaIntensity = MathHelper.clamp(this.nauseaIntensity + f, 0.0f, 1.0f);
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.riding = false;
        Entity entity = this.getControllingVehicle();
        if (entity instanceof BoatEntity) {
            BoatEntity lv = (BoatEntity)entity;
            lv.setInputs(this.input.pressingLeft, this.input.pressingRight, this.input.pressingForward, this.input.pressingBack);
            this.riding |= this.input.pressingLeft || this.input.pressingRight || this.input.pressingForward || this.input.pressingBack;
        }
    }

    public boolean isRiding() {
        return this.riding;
    }

    @Override
    @Nullable
    public StatusEffectInstance removeStatusEffectInternal(RegistryEntry<StatusEffect> effect) {
        if (effect.matches(StatusEffects.NAUSEA)) {
            this.prevNauseaIntensity = 0.0f;
            this.nauseaIntensity = 0.0f;
        }
        return super.removeStatusEffectInternal(effect);
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        double d = this.getX();
        double e = this.getZ();
        super.move(movementType, movement);
        this.autoJump((float)(this.getX() - d), (float)(this.getZ() - e));
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    protected void autoJump(float dx, float dz) {
        float l;
        if (!this.shouldAutoJump()) {
            return;
        }
        Vec3d lv = this.getPos();
        Vec3d lv2 = lv.add(dx, 0.0, dz);
        Vec3d lv3 = new Vec3d(dx, 0.0, dz);
        float h = this.getMovementSpeed();
        float i = (float)lv3.lengthSquared();
        if (i <= 0.001f) {
            Vec2f lv4 = this.input.getMovementInput();
            float j = h * lv4.x;
            float k = h * lv4.y;
            l = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float m = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            lv3 = new Vec3d(j * m - k * l, lv3.y, k * m + j * l);
            i = (float)lv3.lengthSquared();
            if (i <= 0.001f) {
                return;
            }
        }
        float n = MathHelper.inverseSqrt(i);
        Vec3d lv5 = lv3.multiply(n);
        Vec3d lv6 = this.getRotationVecClient();
        l = (float)(lv6.x * lv5.x + lv6.z * lv5.z);
        if (l < -0.15f) {
            return;
        }
        ShapeContext lv7 = ShapeContext.of(this);
        BlockPos lv8 = BlockPos.ofFloored(this.getX(), this.getBoundingBox().maxY, this.getZ());
        BlockState lv9 = this.getWorld().getBlockState(lv8);
        if (!lv9.getCollisionShape(this.getWorld(), lv8, lv7).isEmpty()) {
            return;
        }
        lv8 = lv8.up();
        BlockState lv10 = this.getWorld().getBlockState(lv8);
        if (!lv10.getCollisionShape(this.getWorld(), lv8, lv7).isEmpty()) {
            return;
        }
        float o = 7.0f;
        float p = 1.2f;
        if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            p += (float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75f;
        }
        float q = Math.max(h * 7.0f, 1.0f / n);
        Vec3d lv11 = lv;
        Vec3d lv12 = lv2.add(lv5.multiply(q));
        float r = this.getWidth();
        float s = this.getHeight();
        Box lv13 = new Box(lv11, lv12.add(0.0, s, 0.0)).expand(r, 0.0, r);
        lv11 = lv11.add(0.0, 0.51f, 0.0);
        lv12 = lv12.add(0.0, 0.51f, 0.0);
        Vec3d lv14 = lv5.crossProduct(new Vec3d(0.0, 1.0, 0.0));
        Vec3d lv15 = lv14.multiply(r * 0.5f);
        Vec3d lv16 = lv11.subtract(lv15);
        Vec3d lv17 = lv12.subtract(lv15);
        Vec3d lv18 = lv11.add(lv15);
        Vec3d lv19 = lv12.add(lv15);
        Iterable<VoxelShape> iterable = this.getWorld().getCollisions(this, lv13);
        Iterator iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap(shape -> shape.getBoundingBoxes().stream()).iterator();
        float t = Float.MIN_VALUE;
        while (iterator.hasNext()) {
            Box lv20 = (Box)iterator.next();
            if (!lv20.intersects(lv16, lv17) && !lv20.intersects(lv18, lv19)) continue;
            t = (float)lv20.maxY;
            Vec3d lv21 = lv20.getCenter();
            BlockPos lv22 = BlockPos.ofFloored(lv21);
            int u = 1;
            while ((float)u < p) {
                BlockPos lv23 = lv22.up(u);
                BlockState lv24 = this.getWorld().getBlockState(lv23);
                VoxelShape lv25 = lv24.getCollisionShape(this.getWorld(), lv23, lv7);
                if (!lv25.isEmpty() && (double)(t = (float)lv25.getMax(Direction.Axis.Y) + (float)lv23.getY()) - this.getY() > (double)p) {
                    return;
                }
                if (u > 1) {
                    lv8 = lv8.up();
                    BlockState lv26 = this.getWorld().getBlockState(lv8);
                    if (!lv26.getCollisionShape(this.getWorld(), lv8, lv7).isEmpty()) {
                        return;
                    }
                }
                ++u;
            }
            break block0;
        }
        if (t == Float.MIN_VALUE) {
            return;
        }
        float v = (float)((double)t - this.getY());
        if (v <= 0.5f || v > p) {
            return;
        }
        this.ticksToNextAutojump = 1;
    }

    @Override
    protected boolean hasCollidedSoftly(Vec3d adjustedMovement) {
        float f = this.getYaw() * ((float)Math.PI / 180);
        double d = MathHelper.sin(f);
        double e = MathHelper.cos(f);
        double g = (double)this.sidewaysSpeed * e - (double)this.forwardSpeed * d;
        double h = (double)this.forwardSpeed * e + (double)this.sidewaysSpeed * d;
        double i = MathHelper.square(g) + MathHelper.square(h);
        double j = MathHelper.square(adjustedMovement.x) + MathHelper.square(adjustedMovement.z);
        if (i < (double)1.0E-5f || j < (double)1.0E-5f) {
            return false;
        }
        double k = g * adjustedMovement.x + h * adjustedMovement.z;
        double l = Math.acos(k / Math.sqrt(i * j));
        return l < 0.13962633907794952;
    }

    private boolean shouldAutoJump() {
        return this.isAutoJumpEnabled() && this.ticksToNextAutojump <= 0 && this.isOnGround() && !this.clipAtLedge() && !this.hasVehicle() && this.hasMovementInput() && (double)this.getJumpVelocityMultiplier() >= 1.0;
    }

    private boolean hasMovementInput() {
        Vec2f lv = this.input.getMovementInput();
        return lv.x != 0.0f || lv.y != 0.0f;
    }

    private boolean canStartSprinting() {
        return !this.isSprinting() && this.isWalking() && this.canSprint() && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && (!this.hasVehicle() || this.canVehicleSprint(this.getVehicle())) && !this.isFallFlying();
    }

    private boolean canVehicleSprint(Entity vehicle) {
        return vehicle.canSprintAsVehicle() && vehicle.isLogicalSideForUpdatingMovement();
    }

    private boolean isWalking() {
        double d = 0.8;
        return this.isSubmergedInWater() ? this.input.hasForwardMovement() : (double)this.input.movementForward >= 0.8;
    }

    private boolean canSprint() {
        return this.hasVehicle() || (float)this.getHungerManager().getFoodLevel() > 6.0f || this.getAbilities().allowFlying;
    }

    public float getUnderwaterVisibility() {
        if (!this.isSubmergedIn(FluidTags.WATER)) {
            return 0.0f;
        }
        float f = 600.0f;
        float g = 100.0f;
        if ((float)this.underwaterVisibilityTicks >= 600.0f) {
            return 1.0f;
        }
        float h = MathHelper.clamp((float)this.underwaterVisibilityTicks / 100.0f, 0.0f, 1.0f);
        float i = (float)this.underwaterVisibilityTicks < 100.0f ? 0.0f : MathHelper.clamp(((float)this.underwaterVisibilityTicks - 100.0f) / 500.0f, 0.0f, 1.0f);
        return h * 0.6f + i * 0.39999998f;
    }

    public void onGameModeChanged(GameMode gameMode) {
        if (gameMode == GameMode.SPECTATOR) {
            this.setVelocity(this.getVelocity().withAxis(Direction.Axis.Y, 0.0));
        }
    }

    @Override
    public boolean isSubmergedInWater() {
        return this.isSubmergedInWater;
    }

    @Override
    protected boolean updateWaterSubmersionState() {
        boolean bl = this.isSubmergedInWater;
        boolean bl2 = super.updateWaterSubmersionState();
        if (this.isSpectator()) {
            return this.isSubmergedInWater;
        }
        if (!bl && bl2) {
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
            this.client.getSoundManager().play(new AmbientSoundLoops.Underwater(this));
        }
        if (bl && !bl2) {
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
        }
        return this.isSubmergedInWater;
    }

    @Override
    public Vec3d getLeashPos(float delta) {
        if (this.client.options.getPerspective().isFirstPerson()) {
            float g = MathHelper.lerp(delta * 0.5f, this.getYaw(), this.prevYaw) * ((float)Math.PI / 180);
            float h = MathHelper.lerp(delta * 0.5f, this.getPitch(), this.prevPitch) * ((float)Math.PI / 180);
            double d = this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0;
            Vec3d lv = new Vec3d(0.39 * d, -0.6, 0.3);
            return lv.rotateX(-h).rotateY(-g).add(this.getCameraPosVec(delta));
        }
        return super.getLeashPos(delta);
    }

    @Override
    public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
        this.client.getTutorialManager().onPickupSlotClick(cursorStack, slotStack, clickType);
    }

    @Override
    public float getBodyYaw() {
        return this.getYaw();
    }
}

