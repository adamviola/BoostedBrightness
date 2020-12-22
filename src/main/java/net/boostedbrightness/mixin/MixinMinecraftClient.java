package net.boostedbrightness.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}