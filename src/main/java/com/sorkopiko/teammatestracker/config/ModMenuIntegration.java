package com.sorkopiko.teammatestracker.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.gui.controllers.slider.FloatSliderController;
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
                                .name(Text.literal("Display Settings"))
                                .description(OptionDescription.of(Text.literal("Configure how the mod displays information")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enabled"))
                                        .description(OptionDescription.of(Text.literal("Enable or disable rendering")))
                                        .binding(
                                                true,
                                                () -> TeammatesConfig.HANDLER.instance().enabled,
                                                enabled -> TeammatesConfig.HANDLER.instance().enabled = enabled)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Marker Scale"))
                                        .description(OptionDescription.of(Text.literal("Scale of the teammate markers")))
                                        .binding(
                                                1.0f,
                                                () -> TeammatesConfig.HANDLER.instance().markerScale,
                                                scale -> TeammatesConfig.HANDLER.instance().markerScale = scale)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.25f, 4.0f)
                                                .step(0.1f)
                                                .formatValue(val -> Text.literal(String.format("%.1f", val))))
                                        .build())
                                .build())
                        .build())
                .save(TeammatesConfig.HANDLER::save)
                .build()
                .generateScreen(parentScreen);
    }
}
