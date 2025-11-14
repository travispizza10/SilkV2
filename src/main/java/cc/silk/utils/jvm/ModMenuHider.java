package cc.silk.utils.jvm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public final class ModMenuHider {
    private static final String MOD_ID = "silk";

    public static void hideFromModMenu() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        File modMenuConfig = new File(configDir, "modmenu.json");

        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject config;

            if (modMenuConfig.exists()) {
                try (FileReader reader = new FileReader(modMenuConfig, StandardCharsets.UTF_8)) {
                    config = gson.fromJson(reader, JsonObject.class);
                    if (config == null) {
                        config = new JsonObject();
                    }
                } catch (Exception e) {
                    config = new JsonObject();
                }
            } else {
                config = new JsonObject();
            }

            JsonArray hiddenMods;
            if (config.has("hidden_mods") && config.get("hidden_mods").isJsonArray()) {
                hiddenMods = config.getAsJsonArray("hidden_mods");
            } else {
                hiddenMods = new JsonArray();
                config.add("hidden_mods", hiddenMods);
            }

            Set<String> hiddenSet = new HashSet<>();
            for (JsonElement element : hiddenMods) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    hiddenSet.add(element.getAsString());
                }
            }

            if (hiddenSet.contains(MOD_ID)) {
                return;
            }

            hiddenMods.add(MOD_ID);

            try (FileWriter writer = new FileWriter(modMenuConfig, StandardCharsets.UTF_8)) {
                gson.toJson(config, writer);
                writer.flush();
            }

        } catch (IOException e) {
        }
    }
}