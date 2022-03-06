package net.boostedbrightness.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.boostedbrightness.BoostedBrightness.saveConfig;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    private final long SAVE_INTERVAL = 2000;

    @Shadow
    private GameOptions options;
    private long lastSaveTime = 0;

    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo info) {
        options.write();
        saveConfig();
    }

    @Inject(at = @At("HEAD"), method = "setScreen")
    private void setScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof OptionsScreen && System.currentTimeMillis() - lastSaveTime > SAVE_INTERVAL) {
            saveConfig();
            lastSaveTime = System.currentTimeMillis();
        }
    }
} 