package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DoubleOption.class)
public class MixinDoubleOption {
    @Shadow
    @Final
    @Mutable
    private BiFunction<GameOptions, DoubleOption, String> displayStringGetter;
    @Shadow
    private double max;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(String key, double min, double max, float step, Function<GameOptions, Double> getter,
            BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, String> displayStringGetter,
            CallbackInfo info) {
        // Modifies the max and displayStringGetter of the brightness slider
        if (key.equals("options.gamma")) {
            this.max = BoostedBrightness.MAX_BRIGHTNESS;
            this.displayStringGetter = (gameOptions, doubleOption) -> {
                double d = doubleOption.getRatio(doubleOption.get(gameOptions));
                String string = doubleOption.getDisplayPrefix();
                if (d == 0.0D) {
                    return string + I18n.translate("options.gamma.min");
                } else {
                    return d == 1.0D ? string + I18n.translate("options.gamma.max")
                            : string + "+" + (int) (d * BoostedBrightness.MAX_BRIGHTNESS * 100) + "%";
                }
            };
        }
    }
}