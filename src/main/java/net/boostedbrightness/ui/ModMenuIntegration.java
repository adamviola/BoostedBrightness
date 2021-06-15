package net.boostedbrightness.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.options.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuOptionsScreen::new;
	}

    public class ModMenuOptionsScreen extends GameOptionsScreen {
        private Screen previous;
        private BrightnessListWidget list;

        public ModMenuOptionsScreen(Screen previous) {
            super(previous, MinecraftClient.getInstance().options, new TranslatableText("options.boosted-brightness.title"));
            this.previous = previous;
        }
    
        protected void init() {

            this.list = new BrightnessListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);

            this.children.add(this.list);

            this.addButton(new ButtonWidget(this.width / 2 - 120, this.height - 27, 240, 20, ScreenTexts.DONE, (button) -> {
                BoostedBrightness.saveConfig();
                this.client.openScreen(this.previous);
            }));
        }
    
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderBackground(matrices);
            this.list.render(matrices, mouseX, mouseY, delta);
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
            super.render(matrices, mouseX, mouseY, delta);
        }
    
        public void removed() {
            BoostedBrightness.saveConfig();
        }
    }
}
