package net.boostedbrightness;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class BoostedBrightness implements ClientModInitializer
{
	public static final KeyBinding KEY_BIND = new KeyBinding(
		"key.boosted-brightness.brighten",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		"category.boosted-brightness.title"
	);

	public final static double MAX_BRIGHTNESS = 12.0D;


	@Override
	public void onInitializeClient()
	{
		KeyBindingHelper.registerKeyBinding(KEY_BIND);
	}
}