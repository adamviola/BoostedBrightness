package net.boostedbrightness.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuOptionsScreen::new;
    }

    public class ModMenuOptionsScreen extends GameOptionsScreen {
        private BrightnessListWidget list;

        public ModMenuOptionsScreen(Screen parent) {
            super(parent, MinecraftClient.getInstance().options, Text.translatable("options.boosted-brightness.title"));
        }

        protected void init() {
            this.list = new BrightnessListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
            this.addSelectableChild(this.list);

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.close())
                    .size(240, 20).position(this.width / 2 - 120, this.height - 27).build());
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            this.list.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xFFFFFF);
        }

        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackgroundTexture(context);
        }

        public void removed() {
            BoostedBrightness.saveConfig();
            super.removed();
        }
    }
}
