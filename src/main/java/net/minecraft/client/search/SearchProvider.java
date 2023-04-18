package net.minecraft.client.search;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SearchProvider {
   List findAll(String text);
}
