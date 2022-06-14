package net.boostedbrightness.ui;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class BrightnessSliderWidget extends SliderWidget {
   private final int index;

   protected BrightnessSliderWidget(int index, int x, int y, int width, int height, double value) {
      super(x, y, width, height, Text.empty(), value);
      this.index = index;

      this.updateMessage();
   }
   
   public static double sliderValue(double brightness) {
      return (brightness - BoostedBrightness.minBrightness) / (BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness);
   }

   private double brightness() {
      double brightness = BoostedBrightness.minBrightness + this.value * (BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness);
      return Math.round(20 * brightness) / 20.0D;
   }

   public void updateValue() {
      this.value = sliderValue(BoostedBrightness.brightnesses.get(this.index));
      this.updateMessage();
   }

   @Override
   protected void applyValue() {
      BoostedBrightness.changeBrightness(this.index, brightness());
   }

   @Override
   protected void updateMessage() {
      long brightness = Math.round(brightness() * 100);
      Text text = Text.translatable("options.gamma").append(": ").append(
         brightness == 0   ? Text.translatable("options.gamma.min") :
         brightness == 100 ? Text.translatable("options.gamma.max") :
                             Text.literal(String.valueOf(brightness)));
      
      this.setMessage(text);
   }
}
