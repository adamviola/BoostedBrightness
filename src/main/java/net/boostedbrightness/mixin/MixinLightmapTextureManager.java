package net.boostedbrightness.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @Redirect(
        method = "update",
        at = @At(
          value = "INVOKE",
          target = "Ljava/lang/Math;max(FF)F",
          ordinal = 2
        )
    )
    private float max(float arg0, float arg1) {
        return arg1;
    }
} 