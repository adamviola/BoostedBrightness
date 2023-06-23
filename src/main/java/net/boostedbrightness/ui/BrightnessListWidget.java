package net.boostedbrightness.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

public class BrightnessListWidget extends ElementListWidget<BrightnessListWidget.BrightnessEntry> {

   public BrightnessListWidget(MinecraftClient client, int i, int j, int k, int l, int m) {
      super(client, i, j, k, l, m);
      this.setRenderSelection(true);

      if (client.options.getGamma().getValue() != BoostedBrightness.getBrightness()) {
         BoostedBrightness.changeBrightness(client.options.getGamma().getValue());
      }

      for (int idx = 0; idx < BoostedBrightness.numBrightnesses(); idx++) {
         this.addEntry(BrightnessListWidget.BrightnessEntry.create(idx, this.width, this));
      }

      if (BoostedBrightness.numBrightnesses() < BoostedBrightness.MAX_BRIGHTNESSES) {
         this.addEntry(BrightnessListWidget.BrightnessEntry.create(-1, this.width, this));
      }
   }

   public void addBrightness() {
      List<BrightnessEntry> entries = this.children();

      BoostedBrightness.brightnesses.add(1.0);
      int size = BoostedBrightness.numBrightnesses();

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
      if (BoostedBrightness.getBrightnessIndex() == BoostedBrightness.numBrightnesses()) {
         BoostedBrightness.setBrightnessIndex(BoostedBrightness.getBrightnessIndex() - 1);
      }
   }

   public int getRowWidth() {
      return 300;
   }

   protected int getScrollbarPositionX() {
      return super.getScrollbarPositionX() + 32;
   }

   public Optional<ClickableWidget> getHoveredButton(double mouseX, double mouseY) {
      Iterator<BrightnessEntry> var5 = this.children().iterator();

      while (var5.hasNext()) {
         BrightnessListWidget.BrightnessEntry buttonEntry = var5.next();
         Iterator<ClickableWidget> var7 = buttonEntry.buttons.iterator();

         while (var7.hasNext()) {
            ClickableWidget abstractButtonWidget = var7.next();
            if (abstractButtonWidget.isMouseOver(mouseX, mouseY)) {
               return Optional.of(abstractButtonWidget);
            }
         }
      }

      return Optional.empty();
   }

   protected boolean isSelectedEntry(int index) {
      return BoostedBrightness.getBrightnessIndex() == index;
   }

   public static class BrightnessEntry extends ElementListWidget.Entry<BrightnessListWidget.BrightnessEntry> {
      private final List<ClickableWidget> buttons;
      private final BrightnessListWidget listWidget;

      private int index;

      private BrightnessEntry(List<ClickableWidget> buttons, int index, BrightnessListWidget listWidget) {
         this.buttons = buttons;
         this.listWidget = listWidget;
         this.index = index;
      }

      public static BrightnessListWidget.BrightnessEntry create(int index, int width, BrightnessListWidget listWidget) {
         ArrayList<ClickableWidget> widgets = new ArrayList<>();

         if (index >= 0) {
            widgets.add(new BrightnessSliderWidget(index, width / 2 - 120, 0, 240, 20,
                  BrightnessSliderWidget.sliderValue(BoostedBrightness.getBrightness(index))));

            if (index >= 2)
               widgets.add(ButtonWidget.builder(Text.literal("X"), (button) -> {
                  listWidget.removeBrightness(index);
               }).size(20, 20).position(width / 2 + 120 + 5, 0).build());
         } else
            widgets.add(ButtonWidget.builder(Text.literal("+"), (button) -> {
               listWidget.addBrightness();
            }).size(240, 20).position(width / 2 - 120, 0).build());

         return new BrightnessEntry(widgets, index, listWidget);
      }

      public void updateValue() {
         for (ClickableWidget button : buttons)
            if (button instanceof BrightnessSliderWidget)
               ((BrightnessSliderWidget) button).updateValue();
      }

      @Override
      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
         this.buttons.forEach((button) -> {
            button.setY(y);
            button.render(context, mouseX, mouseY, tickDelta);
         });

         if (this.index >= 0) {
            context.drawTextWithShadow(listWidget.client.textRenderer, String.valueOf(this.index + 1),
                  listWidget.width / 2 - 150 + 13, y + entryHeight / 3, 16777215);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
         boolean mouseOnButton = false;

         for (ClickableWidget button : this.buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
               mouseOnButton = true;
               break;
            }
         }
         if (!mouseOnButton && this.index >= 0) {
            BoostedBrightness.setBrightnessIndex(this.index);
         }

         return super.mouseClicked(mouseX, mouseY, mouseButton);
      }

      public List<? extends Element> children() {
         return this.buttons;
      }

      public List<? extends Selectable> selectableChildren() {
         return this.buttons;
      }
   }
}
