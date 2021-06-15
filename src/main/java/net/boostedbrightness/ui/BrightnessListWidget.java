package net.boostedbrightness.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class BrightnessListWidget extends ElementListWidget<BrightnessListWidget.BrightnessEntry> {

   public BrightnessListWidget(MinecraftClient client, int i, int j, int k, int l, int m) {
      super(client, i, j, k, l, m);
      this.setRenderSelection(true);

      if (client.options.gamma != BoostedBrightness.brightnesses.get(BoostedBrightness.selectedBrightness)) {
         BoostedBrightness.brightnesses.set(BoostedBrightness.selectedBrightness, client.options.gamma);
      }

      for (int idx = 0; idx < BoostedBrightness.brightnesses.size(); idx++) {
         this.addEntry(BrightnessListWidget.BrightnessEntry.create(idx, this.width, this));
      }

      if (BoostedBrightness.brightnesses.size() < BoostedBrightness.MAX_BRIGHTNESSES) {
         this.addEntry(BrightnessListWidget.BrightnessEntry.create(-1, this.width, this));
      }
   }

   public void addBrightness() {
      List<BrightnessEntry> entries = this.children();

      BoostedBrightness.brightnesses.add(1.0);
      int size = BoostedBrightness.brightnesses.size();

      entries.add(size - 1, BrightnessEntry.create(size - 1, this.width, this));

      if (size == BoostedBrightness.MAX_BRIGHTNESSES) {
         entries.remove(BoostedBrightness.MAX_BRIGHTNESSES);
      }
      
   }

   public void removeBrightness(int index) {
      List<BrightnessEntry> entries = this.children();

      int oldSize = BoostedBrightness.brightnesses.size();

      BoostedBrightness.brightnesses.remove(index);
      entries.remove(oldSize - 1);

      for (int i = index; i < oldSize - 1; i++) {
         entries.get(i).updateValue();
      }

      if (oldSize == BoostedBrightness.MAX_BRIGHTNESSES) {
         entries.add(BrightnessEntry.create(-1, this.width, this));
      }
      if (BoostedBrightness.selectedBrightness == BoostedBrightness.brightnesses.size()) {
         BoostedBrightness.selectedBrightness -= 1;
      }
   }

   public int getRowWidth() {
      return 300;
   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 32;
   }

   public Optional<AbstractButtonWidget> getHoveredButton(double mouseX, double mouseY) {
      Iterator<BrightnessEntry> var5 = this.children().iterator();

      while(var5.hasNext()) {
         BrightnessListWidget.BrightnessEntry buttonEntry = var5.next();
         Iterator<AbstractButtonWidget> var7 = buttonEntry.buttons.iterator();

         while(var7.hasNext()) {
            AbstractButtonWidget abstractButtonWidget = var7.next();
            if (abstractButtonWidget.isMouseOver(mouseX, mouseY)) {
               return Optional.of(abstractButtonWidget);
            }
         }
      }

      return Optional.empty();
   }

   protected boolean isSelectedEntry(int index) {
      return BoostedBrightness.selectedBrightness == index;
   }

   public static class BrightnessEntry extends ElementListWidget.Entry<BrightnessListWidget.BrightnessEntry> {
      private final List<AbstractButtonWidget> buttons;
      private final BrightnessListWidget listWidget;

      private int index;

      private BrightnessEntry(List<AbstractButtonWidget> buttons, int index, BrightnessListWidget listWidget) {
         this.buttons = buttons;
         this.listWidget = listWidget;
         this.index = index;
      }

      public static BrightnessListWidget.BrightnessEntry create(int index, int width, BrightnessListWidget listWidget) {
         ArrayList<AbstractButtonWidget> widgets = new ArrayList<>();
         
         if (index >= 0) {
            widgets.add(new BrightnessSliderWidget(index, width / 2 - 120, 0, 240, 20, BrightnessSliderWidget.sliderValue(BoostedBrightness.brightnesses.get(index))));
            
            if (index >= 2)
               widgets.add(new ButtonWidget(width / 2 + 120 + 5, 0, 20, 20, new LiteralText("X"), (buttonWidget) -> { listWidget.removeBrightness(index); }));
         }
         else
            widgets.add(new ButtonWidget(width / 2 - 120, 0, 240, 20, new LiteralText("+"), (buttonWidget) -> { listWidget.addBrightness(); }));

         return new BrightnessEntry(widgets, index, listWidget);
      }

      public void updateValue() {
         for (AbstractButtonWidget button : buttons)
            if (button instanceof BrightnessSliderWidget) 
               ((BrightnessSliderWidget) button).updateValue();
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {         
         this.buttons.forEach((button) -> {
            button.y = y;
            button.render(matrices, mouseX, mouseY, tickDelta);
         });

         if (this.index >= 0) {
            listWidget.client.textRenderer.draw(matrices, String.valueOf(this.index + 1), listWidget.width / 2 - 150 + 13, y + entryHeight / 3, 16777215);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
         boolean mouseOnButton = false;

         for (AbstractButtonWidget button : this.buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
               mouseOnButton = true;
               break;
            }
         }
         if (!mouseOnButton && this.index >= 0) {
            BoostedBrightness.selectedBrightness = this.index;
            listWidget.client.options.gamma = BoostedBrightness.brightnesses.get(this.index);
         }

         return super.mouseClicked(mouseX, mouseY, mouseButton);
      }

      public List<? extends Element> children() {
         return this.buttons;
      }
   }
}
