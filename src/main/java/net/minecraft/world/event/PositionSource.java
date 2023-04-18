package net.minecraft.world.event;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public interface PositionSource {
   Codec CODEC = Registries.POSITION_SOURCE_TYPE.getCodec().dispatch(PositionSource::getType, PositionSourceType::getCodec);

   Optional getPos(World world);

   PositionSourceType getType();
}
