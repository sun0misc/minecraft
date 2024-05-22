/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.player;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public class HungerManager {
    private int foodLevel = 20;
    private float saturationLevel = 5.0f;
    private float exhaustion;
    private int foodTickTimer;
    private int prevFoodLevel = 20;

    private void addInternal(int nutrition, float saturation) {
        this.foodLevel = MathHelper.clamp(nutrition + this.foodLevel, 0, 20);
        this.saturationLevel = MathHelper.clamp(saturation + this.saturationLevel, 0.0f, (float)this.foodLevel);
    }

    public void add(int food, float saturationModifier) {
        this.addInternal(food, HungerConstants.calculateSaturation(food, saturationModifier));
    }

    public void eat(FoodComponent foodComponent) {
        this.addInternal(foodComponent.nutrition(), foodComponent.saturation());
    }

    public void update(PlayerEntity player) {
        boolean bl;
        Difficulty lv = player.getWorld().getDifficulty();
        this.prevFoodLevel = this.foodLevel;
        if (this.exhaustion > 4.0f) {
            this.exhaustion -= 4.0f;
            if (this.saturationLevel > 0.0f) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0f, 0.0f);
            } else if (lv != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        if ((bl = player.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) && this.saturationLevel > 0.0f && player.canFoodHeal() && this.foodLevel >= 20) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 10) {
                float f = Math.min(this.saturationLevel, 6.0f);
                player.heal(f / 6.0f);
                this.addExhaustion(f);
                this.foodTickTimer = 0;
            }
        } else if (bl && this.foodLevel >= 18 && player.canFoodHeal()) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                player.heal(1.0f);
                this.addExhaustion(6.0f);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                if (player.getHealth() > 10.0f || lv == Difficulty.HARD || player.getHealth() > 1.0f && lv == Difficulty.NORMAL) {
                    player.damage(player.getDamageSources().starve(), 1.0f);
                }
                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("foodLevel", NbtElement.NUMBER_TYPE)) {
            this.foodLevel = nbt.getInt("foodLevel");
            this.foodTickTimer = nbt.getInt("foodTickTimer");
            this.saturationLevel = nbt.getFloat("foodSaturationLevel");
            this.exhaustion = nbt.getFloat("foodExhaustionLevel");
        }
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("foodLevel", this.foodLevel);
        nbt.putInt("foodTickTimer", this.foodTickTimer);
        nbt.putFloat("foodSaturationLevel", this.saturationLevel);
        nbt.putFloat("foodExhaustionLevel", this.exhaustion);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public int getPrevFoodLevel() {
        return this.prevFoodLevel;
    }

    public boolean isNotFull() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float exhaustion) {
        this.exhaustion = Math.min(this.exhaustion + exhaustion, 40.0f);
    }

    public float getExhaustion() {
        return this.exhaustion;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public void setSaturationLevel(float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }
}

