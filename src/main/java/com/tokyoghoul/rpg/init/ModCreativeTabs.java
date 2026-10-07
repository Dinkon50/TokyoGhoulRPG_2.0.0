package com.tokyoghoul.rpg.init;

import com.tokyoghoul.rpg.TokyoGhoulRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TokyoGhoulRPG.MODID);

    public static final RegistryObject<CreativeModeTab> TOKYO_GHOUL = TABS.register("tokyo_ghoul",
        () -> CreativeModeTab.builder()
            .title(Component.literal("Tokyo Ghoul RPG"))
            .icon(() -> new ItemStack(ModItems.QUINQUE_BLADE.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.RC_CELL.get());
                output.accept(ModItems.RC_INJECTOR.get());
                output.accept(ModItems.HALF_GHOUL_ORGAN.get());
                output.accept(ModItems.SKILL_TOKEN.get());
                output.accept(ModItems.GHOUL_MASK.get());
                output.accept(ModItems.QUINQUE_BLADE.get());
                output.accept(ModItems.GHOUL_COAT.get());
                output.accept(ModItems.INVESTIGATOR_COAT.get());
                output.accept(ModItems.GHOUL_SPAWN_EGG.get());
                output.accept(ModItems.CCG_SPAWN_EGG.get());
            }).build());

    private ModCreativeTabs() {}
}
