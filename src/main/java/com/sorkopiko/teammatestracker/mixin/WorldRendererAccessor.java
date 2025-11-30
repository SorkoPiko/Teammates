//? if >= 1.21.9 {
package com.sorkopiko.teammatestracker.mixin;

import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("framebufferSet")
    DefaultFramebufferSet getFramebufferSet();
}
//?}