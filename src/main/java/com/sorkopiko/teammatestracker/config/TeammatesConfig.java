package com.sorkopiko.teammatestracker.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class TeammatesConfig {
    public static ConfigClassHandler<TeammatesConfig> HANDLER = ConfigClassHandler.createBuilder(TeammatesConfig.class)
            .id(Identifier.of("teammates", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("teammates.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public boolean enabled = true;
}
