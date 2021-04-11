package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.LightmapTextureManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    private boolean before = true;
    private double gamma;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(at = {@At("HEAD"), @At("RETURN")}, method = "update")
    private void update(float delta, CallbackInfo info) {
        if (BoostedBrightness.toggled) {
            if (before) {
                gamma = client.options.gamma;
                client.options.gamma = BoostedBrightness.toggledBrightness;
            } else {
                client.options.gamma = gamma;
            }
            before = !before;
        }
    }

}