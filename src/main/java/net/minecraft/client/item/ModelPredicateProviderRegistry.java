package net.minecraft.client.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LightBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.CompassItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ModelPredicateProviderRegistry {
   private static final Map GLOBAL = Maps.newHashMap();
   private static final String CUSTOM_MODEL_DATA_KEY = "CustomModelData";
   private static final Identifier DAMAGED_ID = new Identifier("damaged");
   private static final Identifier DAMAGE_ID = new Identifier("damage");
   private static final ClampedModelPredicateProvider DAMAGED_PROVIDER = (stack, world, entity, seed) -> {
      return stack.isDamaged() ? 1.0F : 0.0F;
   };
   private static final ClampedModelPredicateProvider DAMAGE_PROVIDER = (stack, world, entity, seed) -> {
      return MathHelper.clamp((float)stack.getDamage() / (float)stack.getMaxDamage(), 0.0F, 1.0F);
   };
   private static final Map ITEM_SPECIFIC = Maps.newHashMap();

   private static ClampedModelPredicateProvider register(Identifier id, ClampedModelPredicateProvider provider) {
      GLOBAL.put(id, provider);
      return provider;
   }

   private static void registerCustomModelData(ModelPredicateProvider provider) {
      GLOBAL.put(new Identifier("custom_model_data"), provider);
   }

   private static void register(Item item, Identifier id, ClampedModelPredicateProvider provider) {
      ((Map)ITEM_SPECIFIC.computeIfAbsent(item, (key) -> {
         return Maps.newHashMap();
      })).put(id, provider);
   }

   @Nullable
   public static ModelPredicateProvider get(Item item, Identifier id) {
      if (item.getMaxDamage() > 0) {
         if (DAMAGE_ID.equals(id)) {
            return DAMAGE_PROVIDER;
         }

         if (DAMAGED_ID.equals(id)) {
            return DAMAGED_PROVIDER;
         }
      }

      ModelPredicateProvider lv = (ModelPredicateProvider)GLOBAL.get(id);
      if (lv != null) {
         return lv;
      } else {
         Map map = (Map)ITEM_SPECIFIC.get(item);
         return map == null ? null : (ModelPredicateProvider)map.get(id);
      }
   }

   static {
      register(new Identifier("lefthanded"), (stack, world, entity, seed) -> {
         return entity != null && entity.getMainArm() != Arm.RIGHT ? 1.0F : 0.0F;
      });
      register(new Identifier("cooldown"), (stack, world, entity, seed) -> {
         return entity instanceof PlayerEntity ? ((PlayerEntity)entity).getItemCooldownManager().getCooldownProgress(stack.getItem(), 0.0F) : 0.0F;
      });
      ClampedModelPredicateProvider lv = (stack, world, entity, seed) -> {
         if (!stack.isIn(ItemTags.TRIMMABLE_ARMOR)) {
            return Float.NEGATIVE_INFINITY;
         } else {
            return world == null ? 0.0F : (Float)ArmorTrim.getTrim(world.getRegistryManager(), stack).map(ArmorTrim::getMaterial).map(RegistryEntry::value).map(ArmorTrimMaterial::itemModelIndex).orElse(0.0F);
         }
      };
      register(ItemModelGenerator.TRIM_TYPE, lv);
      registerCustomModelData((stack, world, entity, seed) -> {
         return stack.hasNbt() ? (float)stack.getNbt().getInt("CustomModelData") : 0.0F;
      });
      register(Items.BOW, new Identifier("pull"), (stack, world, entity, seed) -> {
         if (entity == null) {
            return 0.0F;
         } else {
            return entity.getActiveItem() != stack ? 0.0F : (float)(stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / 20.0F;
         }
      });
      register(Items.BRUSH, new Identifier("brushing"), (stack, world, entity, seed) -> {
         return entity != null && entity.getActiveItem() == stack ? (float)(entity.getItemUseTimeLeft() % 10) / 10.0F : 0.0F;
      });
      register(Items.BOW, new Identifier("pulling"), (stack, world, entity, seed) -> {
         return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
      });
      register(Items.BUNDLE, new Identifier("filled"), (stack, world, entity, seed) -> {
         return BundleItem.getAmountFilled(stack);
      });
      register(Items.CLOCK, new Identifier("time"), new ClampedModelPredicateProvider() {
         private double time;
         private double step;
         private long lastTick;

         public float unclampedCall(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i) {
            Entity lv = arg3 != null ? arg3 : arg.getHolder();
            if (lv == null) {
               return 0.0F;
            } else {
               if (arg2 == null && ((Entity)lv).world instanceof ClientWorld) {
                  arg2 = (ClientWorld)((Entity)lv).world;
               }

               if (arg2 == null) {
                  return 0.0F;
               } else {
                  double d;
                  if (arg2.getDimension().natural()) {
                     d = (double)arg2.getSkyAngle(1.0F);
                  } else {
                     d = Math.random();
                  }

                  d = this.getTime(arg2, d);
                  return (float)d;
               }
            }
         }

         private double getTime(World world, double skyAngle) {
            if (world.getTime() != this.lastTick) {
               this.lastTick = world.getTime();
               double e = skyAngle - this.time;
               e = MathHelper.floorMod(e + 0.5, 1.0) - 0.5;
               this.step += e * 0.1;
               this.step *= 0.9;
               this.time = MathHelper.floorMod(this.time + this.step, 1.0);
            }

            return this.time;
         }
      });
      register(Items.COMPASS, new Identifier("angle"), new CompassAnglePredicateProvider((world, stack, entity) -> {
         return CompassItem.hasLodestone(stack) ? CompassItem.createLodestonePos(stack.getOrCreateNbt()) : CompassItem.createSpawnPos(world);
      }));
      register(Items.RECOVERY_COMPASS, new Identifier("angle"), new CompassAnglePredicateProvider((world, stack, entity) -> {
         if (entity instanceof PlayerEntity lv) {
            return (GlobalPos)lv.getLastDeathPos().orElse((Object)null);
         } else {
            return null;
         }
      }));
      register(Items.CROSSBOW, new Identifier("pull"), (stack, world, entity, seed) -> {
         if (entity == null) {
            return 0.0F;
         } else {
            return CrossbowItem.isCharged(stack) ? 0.0F : (float)(stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / (float)CrossbowItem.getPullTime(stack);
         }
      });
      register(Items.CROSSBOW, new Identifier("pulling"), (stack, world, entity, seed) -> {
         return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
      });
      register(Items.CROSSBOW, new Identifier("charged"), (stack, world, entity, seed) -> {
         return CrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
      });
      register(Items.CROSSBOW, new Identifier("firework"), (stack, world, entity, seed) -> {
         return CrossbowItem.isCharged(stack) && CrossbowItem.hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
      });
      register(Items.ELYTRA, new Identifier("broken"), (stack, world, entity, seed) -> {
         return ElytraItem.isUsable(stack) ? 0.0F : 1.0F;
      });
      register(Items.FISHING_ROD, new Identifier("cast"), (stack, world, entity, seed) -> {
         if (entity == null) {
            return 0.0F;
         } else {
            boolean bl = entity.getMainHandStack() == stack;
            boolean bl2 = entity.getOffHandStack() == stack;
            if (entity.getMainHandStack().getItem() instanceof FishingRodItem) {
               bl2 = false;
            }

            return (bl || bl2) && entity instanceof PlayerEntity && ((PlayerEntity)entity).fishHook != null ? 1.0F : 0.0F;
         }
      });
      register(Items.SHIELD, new Identifier("blocking"), (stack, world, entity, seed) -> {
         return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
      });
      register(Items.TRIDENT, new Identifier("throwing"), (stack, world, entity, seed) -> {
         return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
      });
      register(Items.LIGHT, new Identifier("level"), (stack, world, entity, seed) -> {
         NbtCompound lv = stack.getSubNbt("BlockStateTag");

         try {
            if (lv != null) {
               NbtElement lv2 = lv.get(LightBlock.LEVEL_15.getName());
               if (lv2 != null) {
                  return (float)Integer.parseInt(lv2.asString()) / 16.0F;
               }
            }
         } catch (NumberFormatException var6) {
         }

         return 1.0F;
      });
      register(Items.GOAT_HORN, new Identifier("tooting"), (stack, world, entity, seed) -> {
         return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
      });
   }
}
