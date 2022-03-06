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
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static net.minecraft.util.Formatting.GREEN;

public class BoostedBrightness implements ClientModInitializer {
    public static final int MAX_BRIGHTNESSES = 5;
    private static final Gson GSON = new Gson();

    public static double minBrightness = -1.0;
    public static double maxBrightness = 12.0;
    public static double brightnessSliderInterval = 0.05;
    private static double step = 0.1;

    public static ArrayList<Double> brightnesses;
    private static int brightnessIndex = 0;
    private static int lastBrightnessIndex = 0;

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

    public static MinecraftClient client;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(NEXT_BIND);
        KeyBindingHelper.registerKeyBinding(RAISE_BIND);
        KeyBindingHelper.registerKeyBinding(LOWER_BIND);

        // Register binds for each brightness setting
        for (int i = 0; i < MAX_BRIGHTNESSES; i++) {
            SELECT_BINDS[i] = new KeyBinding(
                "key.boosted-brightness.select" + (i + 1),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.boosted-brightness.title"
            );

            KeyBindingHelper.registerKeyBinding(SELECT_BINDS[i]);
        }

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        loadConfig();
        client = MinecraftClient.getInstance();
    }

    public static int numBrightnesses() {
        return brightnesses.size();
    }

    public static int getBrightnessIndex() {
        return brightnessIndex;
    }

    public static void setBrightnessIndex(int index) {
        brightnessIndex = index;
        client.options.gamma = getBrightness();
    }

    public static double getBrightness() {
        return brightnesses.get(brightnessIndex);
    }

    public static double getBrightness(int index) {
        return brightnesses.get(index);
    }

    public static void changeBrightness(double brightness) {
        brightnesses.set(getBrightnessIndex(), brightness);
        client.options.gamma = getBrightness();
    } 

    public static void changeBrightness(int index, double brightness) {
        if (index == brightnessIndex)
            changeBrightness(brightness);
        else
            brightnesses.set(index, brightness);
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

            asInt(config.get("selected"), selected -> brightnessIndex = selected - 1);
            brightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, brightnessIndex));
            
            if (config.has("last")) {
                asInt(config.get("last"), last -> lastBrightnessIndex = last - 1);
                lastBrightnessIndex = Math.max(0, Math.min(numBrightnesses() - 1, lastBrightnessIndex));
            } else {
                lastBrightnessIndex = 0;
            }
        }
        catch (IOException | JsonSyntaxException ex) {
            logException(ex, "Failed to load BoostedBrightness config");
        }

        // If the config file fails to properly load, default to 2 brightness levels
        if (brightnesses == null || brightnesses.size() < 2) {
            brightnesses = new ArrayList<>();
            brightnesses.add(1.0);
            brightnesses.add(maxBrightness);
            brightnessIndex = 0;
            lastBrightnessIndex = 0;
        }
    }

    public static void saveConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("min", minBrightness);
        config.addProperty("max", maxBrightness);
        config.addProperty("step", step);
        // Store selectedBrightness + 1 for human readability
        config.addProperty("selected", brightnessIndex + 1);
        config.addProperty("last", lastBrightnessIndex + 1);

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
                    getBrightnessIndex() + 1,
                    Math.round(getBrightness() * 100)
                }
            ).styled(s -> s.withColor(GREEN)),
            false
        );
    }

    private void onEndTick(MinecraftClient client) {
        // Check next brightness keybind
        while (NEXT_BIND.wasPressed()) {
            lastBrightnessIndex = getBrightnessIndex();
            setBrightnessIndex((lastBrightnessIndex + 1) % numBrightnesses());
            showOverlay(client);
        }
 
        // Check set brightness keybind
        for (int i = 0; i < numBrightnesses(); i++) {
            while (SELECT_BINDS[i].wasPressed()) {
                int nextBrightnessIndex = (i != brightnessIndex) ? i : lastBrightnessIndex;
                lastBrightnessIndex = getBrightnessIndex();
                setBrightnessIndex(nextBrightnessIndex);
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
            double brightness = Math.max(minBrightness, Math.min(maxBrightness, getBrightness() + offset));
            changeBrightness(brightness);
            showOverlay(client);
        }
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[BoostedBrightness] %s (%s: %s)", message, ex.getClass().getSimpleName(), ex.getLocalizedMessage());
    }
}