package net.boostedbrightness.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
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

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
                this.client.setScreen(this.parent);
            }).size(240, 20).position(this.width / 2 - 120, this.height - 27).build());
        }
    
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderBackground(matrices);
            this.list.render(matrices, mouseX, mouseY, delta);
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
            super.render(matrices, mouseX, mouseY, delta);
        }
    
        public void removed() {
            BoostedBrightness.saveConfig();
            super.removed();
        }
    }
}
