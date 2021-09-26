package net.boostedbrightness.mixin;

import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import net.boostedbrightness.BoostedBrightness;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SliderControl.class)
public class MixinSliderControl {
    @Shadow
    @Final
    @Mutable
    private int min;

    @Shadow
    @Final
    @Mutable
    private int max;

    @Shadow
    @Final
    @Mutable
    private int interval;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(Option<Integer> option, int min, int max, int interval, ControlValueFormatter mode, CallbackInfo info) {
        if (option.getName() instanceof TranslatableText translatable && translatable.getKey().equals("options.gamma")) {
            this.min = (int) (BoostedBrightness.minBrightness * 100);
            this.max = (int) (BoostedBrightness.maxBrightness * 100);
            this.interval = (int) (BoostedBrightness.brightnessSliderInterval * 100);
        }
    }
}