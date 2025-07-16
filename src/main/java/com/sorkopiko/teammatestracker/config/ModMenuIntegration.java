package com.sorkopiko.teammatestracker.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
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
                        .name(Text.literal("Display"))
                        .tooltip(Text.literal("Configure how the mod displays information"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("General"))
                                .description(OptionDescription.of(Text.literal("General settings for the mod")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enabled"))
                                        .description(OptionDescription.of(Text.literal("Enable or disable rendering")))
                                        .binding(
                                                true,
                                                () -> TeammatesConfig.HANDLER.instance().enabled,
                                                enabled -> TeammatesConfig.HANDLER.instance().enabled = enabled)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Markers"))
                                .description(OptionDescription.of(Text.literal("Control how teammate markers are rendered")))
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Scale"))
                                        .description(OptionDescription.of(Text.literal("Scale of the markers")))
                                        .binding(
                                                1.0f,
                                                () -> TeammatesConfig.HANDLER.instance().markerScale,
                                                scale -> TeammatesConfig.HANDLER.instance().markerScale = scale)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.25f, 4.0f)
                                                .step(0.05f)
                                                .formatValue(val -> Text.literal(String.format("%.2f", val))))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Y Offset"))
                                        .description(OptionDescription.of(Text.literal("Vertical offset for the markers")))
                                        .binding(
                                                0.5f,
                                                () -> TeammatesConfig.HANDLER.instance().yOffset,
                                                offset -> TeammatesConfig.HANDLER.instance().yOffset = offset)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.0f, 2.0f)
                                                .step(0.01f)
                                                .formatValue(val -> Text.literal(String.format("%.2f", val))))
                                        .build())
                                .build())
                        .build())
                .save(TeammatesConfig.HANDLER::save)
                .build()
                .generateScreen(parentScreen);
    }
}
