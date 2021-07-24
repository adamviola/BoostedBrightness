package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.GameOptions;

import java.util.List;
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
    private BiFunction<GameOptions, DoubleOption, Text> displayStringGetter;

    @Shadow
    @Final
    @Mutable
    private BiConsumer<GameOptions, Double> setter;

    @Shadow
    @Final
    @Mutable
    protected double min;

    @Shadow
    @Mutable
    protected double max;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void init(String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter, Function<MinecraftClient, List<OrderedText>> tooltipsGetter, CallbackInfo info) {
        if (key.equals("options.gamma")) {
            this.min = BoostedBrightness.minBrightness;
            this.max = BoostedBrightness.maxBrightness;
            this.setter = this::setter;
            this.displayStringGetter = this::displayStringGetter;
        }
    }

    private void setter(GameOptions gameOptions, Double brightness) {
        // Round to nearest 0.05
        brightness = Math.round(20 * brightness) / 20.0D;
        BoostedBrightness.changeBrightness(brightness);
    }

    private Text displayStringGetter(GameOptions gameOptions, DoubleOption doubleOption) {
        return new TranslatableText("options.gamma").append(": ").append(
            gameOptions.gamma == 0 ? new TranslatableText("options.gamma.min") :
            gameOptions.gamma == 1 ? new TranslatableText("options.gamma.max") :
                                     new LiteralText(Math.round(gameOptions.gamma * 100) + "%"));
    }
}