/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ContainerComponentModifier;
import net.minecraft.registry.Registries;

public interface ContainerComponentModifiers {
    public static final ContainerComponentModifier<ContainerComponent> CONTAINER = new ContainerComponentModifier<ContainerComponent>(){

        @Override
        public ComponentType<ContainerComponent> getComponentType() {
            return DataComponentTypes.CONTAINER;
        }

        @Override
        public Stream<ItemStack> stream(ContainerComponent arg) {
            return arg.stream();
        }

        @Override
        public ContainerComponent getDefault() {
            return ContainerComponent.DEFAULT;
        }

        @Override
        public ContainerComponent create(ContainerComponent arg, Stream<ItemStack> stream) {
            return ContainerComponent.fromStacks(stream.toList());
        }

        @Override
        public /* synthetic */ Object getDefault() {
            return this.getDefault();
        }
    };
    public static final ContainerComponentModifier<BundleContentsComponent> BUNDLE_CONTENTS = new ContainerComponentModifier<BundleContentsComponent>(){

        @Override
        public ComponentType<BundleContentsComponent> getComponentType() {
            return DataComponentTypes.BUNDLE_CONTENTS;
        }

        @Override
        public BundleContentsComponent getDefault() {
            return BundleContentsComponent.DEFAULT;
        }

        @Override
        public Stream<ItemStack> stream(BundleContentsComponent arg) {
            return arg.stream();
        }

        @Override
        public BundleContentsComponent create(BundleContentsComponent arg, Stream<ItemStack> stream) {
            BundleContentsComponent.Builder lv = new BundleContentsComponent.Builder(arg).clear();
            stream.forEach(lv::add);
            return lv.build();
        }

        @Override
        public /* synthetic */ Object getDefault() {
            return this.getDefault();
        }
    };
    public static final ContainerComponentModifier<ChargedProjectilesComponent> CHARGED_PROJECTILES = new ContainerComponentModifier<ChargedProjectilesComponent>(){

        @Override
        public ComponentType<ChargedProjectilesComponent> getComponentType() {
            return DataComponentTypes.CHARGED_PROJECTILES;
        }

        @Override
        public ChargedProjectilesComponent getDefault() {
            return ChargedProjectilesComponent.DEFAULT;
        }

        @Override
        public Stream<ItemStack> stream(ChargedProjectilesComponent arg) {
            return arg.getProjectiles().stream();
        }

        @Override
        public ChargedProjectilesComponent create(ChargedProjectilesComponent arg, Stream<ItemStack> stream) {
            return ChargedProjectilesComponent.of(stream.toList());
        }

        @Override
        public /* synthetic */ Object getDefault() {
            return this.getDefault();
        }
    };
    public static final Map<ComponentType<?>, ContainerComponentModifier<?>> TYPE_TO_MODIFIER = Stream.of(CONTAINER, BUNDLE_CONTENTS, CHARGED_PROJECTILES).collect(Collectors.toMap(ContainerComponentModifier::getComponentType, arg -> arg));
    public static final Codec<ContainerComponentModifier<?>> MODIFIER_CODEC = Registries.DATA_COMPONENT_TYPE.getCodec().comapFlatMap(componentType -> {
        ContainerComponentModifier<?> lv = TYPE_TO_MODIFIER.get(componentType);
        return lv != null ? DataResult.success(lv) : DataResult.error(() -> "No items in component");
    }, ContainerComponentModifier::getComponentType);
}

