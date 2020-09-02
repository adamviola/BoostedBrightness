package net.boostedbrightness.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.boostedbrightness.BoostedBrightness.KEY_BIND;
import static net.boostedbrightness.BoostedBrightness.MAX_BRIGHTNESS;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient
{
	@Shadow
	private GameOptions options;
	private boolean pressed = false;
	private double previousGamma = 1.0D;

	@Inject(at = @At("RETURN"), method = "tick")
	private void tick(CallbackInfo info)
	{
		//if state changed
		if (KEY_BIND.isPressed() != pressed)
		{
			//save state and if true then toggle
			if (pressed = KEY_BIND.isPressed())
			{
				//if max then go back to previous or default 100%
				if (options.gamma == MAX_BRIGHTNESS)
				{
					options.gamma = previousGamma;
				}
				else //else save gamma and set to max
				{
					previousGamma = options.gamma;
					options.gamma = MAX_BRIGHTNESS;
				}
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "close")
	private void close(CallbackInfo info)
	{
		//go back to normal before close
		options.gamma = previousGamma;
	}
}