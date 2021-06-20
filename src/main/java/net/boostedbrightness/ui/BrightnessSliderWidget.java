package net.boostedbrightness.ui;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class BrightnessSliderWidget extends SliderWidget {
   private final int index;

   protected BrightnessSliderWidget(int index, int x, int y, int width, int height, double value) {
      super(x, y, width, height, LiteralText.EMPTY, value);
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
      double brightness = brightness();
      // double threshold = 0.025; // TODO find actual value (half of the gamma change for a one pixel change on the slider)
      Text text = new TranslatableText("options.gamma").append(": ").append(
         brightness == 0 ? new TranslatableText("options.gamma.min") :
         brightness == 1 ? new TranslatableText("options.gamma.max") :
                           new LiteralText(Math.round(brightness * 100) + "%"));
      
      this.setMessage(text);
   }
}
