package net.boostedbrightness;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.InputUtil;

public class BoostedBrightness implements ClientModInitializer {

    public static boolean done = false;

    public final static double MAX_BRIGHTNESS = 12.0D;

    private GameOptions gameOptions;
    private boolean maxBrightToggled = false;
    private double prevBrightness;
    private boolean prevPressed;
    private FabricKeyBinding brightnessBind;
    

	@Override
	public void onInitializeClient() {
        SetupKeybinds(); 
    }


    private void SetupKeybinds() {

        KeyBindingRegistry.INSTANCE.addCategory("Boosted Brightness");

        // Uses fabric's keybinding system to add the max brightness toggle
        brightnessBind = FabricKeyBinding.Builder.create(
            new Identifier("boosted-brightness", "brighten"),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "Boosted Brightness"
        ).build();
        KeyBindingRegistry.INSTANCE.register(brightnessBind);
        
        // Callback that toggles brightness between set value and maximum
        ClientTickCallback.EVENT.register(e -> {
            // When onInitializeClient is called, client.options is null, so we grab it here
            if (gameOptions == null) {
                MinecraftClient client = MinecraftClient.getInstance();
                gameOptions = client.options;
            } 

            if (brightnessBind.isPressed()) {
                if (!prevPressed) {
                    if (!maxBrightToggled) {
                        prevBrightness = gameOptions.gamma;
                        gameOptions.gamma = MAX_BRIGHTNESS;
                    } else {
                        gameOptions.gamma = prevBrightness;
                    }
                    maxBrightToggled = !maxBrightToggled;
                    prevPressed = true;
                }
            } else {
                prevPressed = false;
            } // The ClientTickCallback is called every tick, so this logic prevents rapid toggling when key is held >1 tick
        });
    }
}