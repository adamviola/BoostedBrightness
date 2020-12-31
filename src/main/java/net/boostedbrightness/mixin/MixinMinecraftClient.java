package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

import static net.boostedbrightness.BoostedBrightness.prevGamma;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient
{
	@Shadow
	private GameOptions options;

	// Save cached brightness if brightness keybind is toggled-on when client is closed
	@Inject(at = @At("HEAD"), method = "close")
	private void close(CallbackInfo info) {
		if (prevGamma != null)
			options.gamma = prevGamma;
	}

	// manipulate the sodium mods custom options screen right when it gets opened but before it gets displayed
	@Inject(at = @At("HEAD"), method = "openScreen")
	private void openScreen(Screen screen, CallbackInfo info)
	{
		try
		{
			// screen -> pages -> 1st page (general) -> groups -> 1st group -> options -> 2nd option (gamma) -> control (slider) -> overwrite min and max
			List<?> optionPages = (List<?>) get(screen, "me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI", "pages");
			List<?> optionGroups = (List<?>) get(optionPages.get(0), "me.jellysquid.mods.sodium.client.gui.options.OptionPage", "groups");
			List<?> options = (List<?>) get(optionGroups.get(0), "me.jellysquid.mods.sodium.client.gui.options.OptionGroup", "options");
			Object sliderControl = get(options.get(1), "me.jellysquid.mods.sodium.client.gui.options.OptionImpl", "control");
			Class<?> sliderControlClass = Class.forName("me.jellysquid.mods.sodium.client.gui.options.control.SliderControl");
			setInt(sliderControl, sliderControlClass, "min", (int) (BoostedBrightness.MIN_BRIGHTNESS * 100));
			setInt(sliderControl, sliderControlClass, "max", (int) (BoostedBrightness.MAX_BRIGHTNESS * 100));
		}
		catch (ClassNotFoundException ignored) { } // sodium mod not used
		catch (Exception ex)
		{
			System.err.printf("[Boosted Brightness]: %s: %s\n", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
		}
	}

	// make a field accessible and get its value
	private Object get(Object instance, String className, String name) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException
	{
		Field f = Class.forName(className).getDeclaredField(name);
		f.setAccessible(true);
		return f.get(instance);
	}

	// make an int field accessible and set its value
	private void setInt(Object instance, Class<?> clazz, String field, int value) throws NoSuchFieldException, IllegalAccessException
	{
		Field f = clazz.getDeclaredField(field);
		f.setAccessible(true);
		f.setInt(instance, value);
	}
}