package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

import static net.boostedbrightness.BoostedBrightness.logException;
import static net.boostedbrightness.BoostedBrightness.saveConfig;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    private final long SAVE_INTERVAL = 10000;

    @Shadow
    private GameOptions options;
    private long lastSaveTime = 0;

    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo info) {
        options.write();
        saveConfig();
    }

    // manipulate the sodium mods custom options screen right when it gets opened but before it gets displayed
    @Inject(at = @At("HEAD"), method = "openScreen")
    private void openScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof GameMenuScreen && System.currentTimeMillis() - lastSaveTime > SAVE_INTERVAL) {
            saveConfig();
            lastSaveTime = System.currentTimeMillis();
        }

        if (screen != null && screen.getClass().getSimpleName().equals("SodiumOptionsGUI")) {
            try {
                // screen -> pages -> 1st page (general) -> groups -> 1st group -> options -> 2nd option (gamma) -> control (slider) -> overwrite min and max
                List<?> optionPages = (List<?>) get(screen, "me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI", "pages");
                List<?> optionGroups = (List<?>) get(optionPages.get(0), "me.jellysquid.mods.sodium.client.gui.options.OptionPage", "groups");
                List<?> options = (List<?>) get(optionGroups.get(0), "me.jellysquid.mods.sodium.client.gui.options.OptionGroup", "options");
                Object sliderControl = get(options.get(1), "me.jellysquid.mods.sodium.client.gui.options.OptionImpl", "control");
                Class<?> sliderControlClass = Class.forName("me.jellysquid.mods.sodium.client.gui.options.control.SliderControl");
                setInt(sliderControl, sliderControlClass, "min", (int) (BoostedBrightness.minBrightness * 100));
                setInt(sliderControl, sliderControlClass, "max", (int) (BoostedBrightness.maxBrightness * 100));
            }
            catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException ex) {
                logException(ex, "an exception occurred during the manipulation of the sodium options gui");
            }
        }
    }

    // make a field accessible and get its value
    private Object get(Object instance, String className, String name) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Field f = Class.forName(className).getDeclaredField(name);
        f.setAccessible(true);
        return f.get(instance);
    }

    // make an int field accessible and set its value
    private void setInt(Object instance, Class<?> clazz, String field, int value) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        f.setInt(instance, value);
    }
}