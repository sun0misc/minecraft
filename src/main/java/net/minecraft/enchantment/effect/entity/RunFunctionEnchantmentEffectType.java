/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffectType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;

public record RunFunctionEnchantmentEffectType(Identifier function) implements EnchantmentEntityEffectType
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<RunFunctionEnchantmentEffectType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("function")).forGetter(RunFunctionEnchantmentEffectType::function)).apply((Applicative<RunFunctionEnchantmentEffectType, ?>)instance, RunFunctionEnchantmentEffectType::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        MinecraftServer minecraftServer = world.getServer();
        CommandFunctionManager lv = minecraftServer.getCommandFunctionManager();
        Optional<CommandFunction<ServerCommandSource>> optional = lv.getFunction(this.function);
        if (optional.isPresent()) {
            ServerCommandSource lv2 = minecraftServer.getCommandSource().withLevel(2).withSilent().withEntity(user).withWorld(world).withPosition(pos).withRotation(user.getRotationClient());
            lv.execute(optional.get(), lv2);
        } else {
            LOGGER.error("Enchantment run_function effect failed for non-existent function {}", (Object)this.function);
        }
    }

    public MapCodec<RunFunctionEnchantmentEffectType> getCodec() {
        return CODEC;
    }
}

