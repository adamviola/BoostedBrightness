package net.boostedbrightness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static net.minecraft.util.Formatting.GREEN;

public class BoostedBrightness implements ClientModInitializer {
    public static final int MAX_BRIGHTNESSES = 5;
    private static final Gson GSON = new Gson();

    public static double minBrightness = -1.0;
    public static double maxBrightness = 12.0;
    private static double step = 0.1;

    public static int selectedBrightness = 0;
    public static ArrayList<Double> brightnesses;

    private static final KeyBinding NEXT_BIND = new KeyBinding(
        "key.boosted-brightness.next",
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

    private static final KeyBinding[] SELECT_BINDS = new KeyBinding[MAX_BRIGHTNESSES];

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(NEXT_BIND);
        KeyBindingHelper.registerKeyBinding(RAISE_BIND);
        KeyBindingHelper.registerKeyBinding(LOWER_BIND);

        for (int i = 0; i < MAX_BRIGHTNESSES; i++) {
            SELECT_BINDS[i] = new KeyBinding(
                "key.boosted-brightness.select" + (i + 1),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.boosted-brightness.title"
            );

            KeyBindingHelper.registerKeyBinding(SELECT_BINDS[i]);
        }

        loadConfig();
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void loadConfig() {
        try {
            JsonObject config = GSON.fromJson(new String(Files.readAllBytes(getConfigPath())), JsonObject.class);
            asDouble(config.get("min"), min -> minBrightness = min);
            asDouble(config.get("max"), max -> maxBrightness = max);
            asDouble(config.get("step"), step -> BoostedBrightness.step = step);

            brightnesses = new ArrayList<>();
            for (int i = 1; i <= MAX_BRIGHTNESSES && config.has(String.valueOf(i)); i++) {
                asDouble(config.get(String.valueOf(i)), brightness -> brightnesses.add(brightness));
            }

            asInt(config.get("selected"), selected -> selectedBrightness = selected - 1);
            selectedBrightness = Math.max(0, Math.min(brightnesses.size() - 1, selectedBrightness));
        }
        catch (IOException | JsonSyntaxException ex) {
            logException(ex, "Failed to load BoostedBrightness config");
        }

        if (brightnesses == null || brightnesses.size() < 2) {
            brightnesses = new ArrayList<>();
            brightnesses.add(1.0);
            brightnesses.add(maxBrightness);

            selectedBrightness = 0;
        }
    }

    public static void saveConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("min", minBrightness);
        config.addProperty("max", maxBrightness);
        config.addProperty("step", step);
        config.addProperty("selected", selectedBrightness + 1);

        for (int i = 0; i < brightnesses.size(); i++) {
            config.addProperty(String.valueOf(i + 1), brightnesses.get(i));
        }

        try {
            Files.write(getConfigPath(), GSON.toJson(config).getBytes());
        }
        catch (IOException ex) {
            logException(ex, "Failed to save BoostedBrightness config");
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

    private void asInt(JsonElement element, IntConsumer onSuccess) {
        if (element != null && element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber()) {
            onSuccess.accept(element.getAsInt());
        }
    }

    private void showOverlay(MinecraftClient client) {
        client.inGameHud.setOverlayMessage(
            new TranslatableText(
                "overlay.boosted-brightness.change",
                new Object[]{
                    selectedBrightness + 1,
                    Math.round(brightnesses.get(selectedBrightness) * 100)
                }
            ).styled(s -> s.withColor(GREEN)),
            false
        );
    }

    private void onEndTick(MinecraftClient client) {
        // Check next brightness keybind
        while (NEXT_BIND.wasPressed()) {
            selectedBrightness = (selectedBrightness + 1) % brightnesses.size();
            client.options.gamma = brightnesses.get(selectedBrightness);

            showOverlay(client);
        }

        // Check set brightness keybind
        for (int i = 0; i < brightnesses.size(); i++) {
            if (SELECT_BINDS[i].isPressed()) {
                selectedBrightness = i;
                client.options.gamma = brightnesses.get(selectedBrightness);

                showOverlay(client);
            }
        }

        // Check raise/lower keybinds
        double offset = 0;
        while (RAISE_BIND.wasPressed()) {
            offset += step;
        }
        while (LOWER_BIND.wasPressed()) {
            offset -= step;
        }
        
        // Raise/lower selected brightness
        if (offset != 0) {
            double brightness = Math.max(minBrightness, Math.min(maxBrightness, client.options.gamma + offset));
            brightnesses.set(selectedBrightness, brightness);
            client.options.gamma = brightness;

            showOverlay(client);
        }
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[BoostedBrightness] %s (%s: %s)", message, ex.getClass().getSimpleName(), ex.getLocalizedMessage());
    }
}