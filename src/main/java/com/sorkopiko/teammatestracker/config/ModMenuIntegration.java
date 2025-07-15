package com.sorkopiko.teammatestracker.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.minecraft.text.Text;

@Entrypoint
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Teammates Tracker Configuration"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .tooltip(Text.literal("General settings for the mod"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("General"))
                                .description(OptionDescription.of(Text.literal("General settings for the mod")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enabled"))
                                        .description(OptionDescription.of(Text.literal("Enable or disable the mod")))
                                        .binding(
                                                true,
                                                () -> TeammatesConfig.HANDLER.instance().enabled,
                                                enabled -> TeammatesConfig.HANDLER.instance().enabled = enabled)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .save(TeammatesConfig.HANDLER::save)
                .build()
                .generateScreen(parentScreen);
    }
}
