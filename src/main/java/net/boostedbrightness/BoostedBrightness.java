package net.boostedbrightness;

import java.lang.Math;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class BoostedBrightness implements ClientModInitializer {

	public final static double MIN_BRIGHTNESS = -1.5D;
	public final static double MAX_BRIGHTNESS = 12.0D;
	public static Double prevGamma;

	private static final KeyBinding BRIGHTEN_BIND = new KeyBinding(
		"key.boosted-brightness.brighten",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		"category.boosted-brightness.title"
	);

	private static final KeyBinding RAISE_BIND = new KeyBinding(
		"key.boosted-brightness.raise",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_RIGHT_BRACKET,
		"category.boosted-brightness.title"
	);

	private static final KeyBinding LOWER_BIND = new KeyBinding(
		"key.boosted-brightness.lower",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_LEFT_BRACKET,
		"category.boosted-brightness.title"
	);

	@Override
	public void onInitializeClient()
	{
		KeyBindingHelper.registerKeyBinding(BRIGHTEN_BIND);
		KeyBindingHelper.registerKeyBinding(RAISE_BIND);
		KeyBindingHelper.registerKeyBinding(LOWER_BIND);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (BRIGHTEN_BIND.wasPressed()) {
                if (prevGamma == null) {
					prevGamma = client.options.gamma;
					client.options.gamma = MAX_BRIGHTNESS;
				} else {
					client.options.gamma = prevGamma;
					prevGamma = null;
				}
			}

			while (RAISE_BIND.wasPressed()) {
				if (prevGamma == null) {
					double gamma = client.options.gamma;
					client.options.gamma = Math.min(gamma + 0.5D, MAX_BRIGHTNESS);
				} else {
					prevGamma = Math.min(prevGamma + 0.5D, MAX_BRIGHTNESS);
				}
			}
			
			while (LOWER_BIND.wasPressed()) {
				if (prevGamma == null) {
					double gamma = client.options.gamma;
					client.options.gamma = Math.max(gamma - 0.5D, MIN_BRIGHTNESS);
				} else {
					prevGamma = Math.max(prevGamma - 0.5D, MIN_BRIGHTNESS);
				}
			}
        });
	}
}