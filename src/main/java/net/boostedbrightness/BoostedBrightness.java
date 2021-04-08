package net.boostedbrightness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.DoubleConsumer;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static java.lang.Double.POSITIVE_INFINITY;
import static net.minecraft.util.Formatting.GREEN;

public class BoostedBrightness implements ClientModInitializer {
    private static final Gson GSON = new Gson();
    public static double minBrightness = -1.5;
    public static double maxBrightness = 12.0;
    private static double prevGamma = POSITIVE_INFINITY; // will clamped to max brightness
    private static double step = 0.5;

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
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(BRIGHTEN_BIND);
        KeyBindingHelper.registerKeyBinding(RAISE_BIND);
        KeyBindingHelper.registerKeyBinding(LOWER_BIND);
        loadConfig();
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void loadConfig() {
        try {
            JsonObject config = GSON.fromJson(new String(Files.readAllBytes(getConfigPath())), JsonObject.class);
            asDouble(config.get("min"), min -> minBrightness = min);
            asDouble(config.get("max"), max -> maxBrightness = max);
            asDouble(config.get("step"), step -> BoostedBrightness.step = step);
            asDouble(config.get("previous"), prev -> prevGamma = prev);
        }
        catch (IOException | JsonSyntaxException ex) {
            logException(ex, "failed to load config");
        }
    }

    public static void saveConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("min", minBrightness);
        config.addProperty("max", maxBrightness);
        config.addProperty("step", step);
        config.addProperty("previous", prevGamma);
        try {
            Files.write(getConfigPath(), GSON.toJson(config).getBytes());
        }
        catch (IOException ex) {
            logException(ex, "failed to save config");
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("boostedbrightness.json");
    }

    private void asDouble(JsonElement element, DoubleConsumer onSuccess) {
        if (element != null && element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber()) {
            onSuccess.accept(element.getAsDouble());
        }
    }

    private void onEndTick(MinecraftClient client) {
        boolean show = false;
        while (BRIGHTEN_BIND.wasPressed()) {
            double temp = client.options.gamma;
            client.options.gamma = MathHelper.clamp(prevGamma, minBrightness, maxBrightness);
            prevGamma = temp;
            show = true;
        }
        double gamma = client.options.gamma;
        while (RAISE_BIND.wasPressed()) {
            gamma += step;
        }
        while (LOWER_BIND.wasPressed()) {
            gamma -= step;
        }
        gamma = MathHelper.clamp(gamma, minBrightness, maxBrightness);
        if (client.options.gamma != gamma) {
            client.options.gamma = gamma;
            show = true;
        }
        if (show) {
            client.inGameHud.setOverlayMessage(new TranslatableText("overlay.boosted-brightness.set")
                .append(String.format(" %d%%", Math.round(gamma * 100))).styled(s -> s.withColor(GREEN)), false);
        }
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[BoostedBrightness] %s (%s: %s)", message, ex.getClass().getSimpleName(), ex.getLocalizedMessage());
    }
}