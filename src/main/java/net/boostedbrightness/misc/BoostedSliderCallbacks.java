package net.boostedbrightness.misc;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.option.SimpleOption.SliderCallbacks;
import com.mojang.datafixers.util.Either;

public enum BoostedSliderCallbacks implements SliderCallbacks<Double>
{
    INSTANCE;

    @Override
    public Optional<Double> validate(Double double_) {
        return double_ >= BoostedBrightness.minBrightness && double_ <= BoostedBrightness.maxBrightness ? Optional.of(double_) : Optional.empty();
    }

    @Override
    public double toSliderProgress(Double double_) {
        double range = BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness;
        double offset = BoostedBrightness.minBrightness;
        return (double_ - offset) / range;
    }

    @Override
    public Double toValue(double d) {
        double range = BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness;
        double offset = BoostedBrightness.minBrightness;
        return d * range + offset;
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(Codec.doubleRange(BoostedBrightness.minBrightness, BoostedBrightness.maxBrightness), Codec.BOOL).xmap(either -> either.map(value -> value, value -> value != false ? 1.0 : 0.0), Either::left);
    }
}
