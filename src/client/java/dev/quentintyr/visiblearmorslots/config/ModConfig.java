package dev.quentintyr.visiblearmorslots.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration options for the mod (JSON-backed)
 */
public class ModConfig {

    public enum Side {
        LEFT, RIGHT
    }

    public enum DarkMode {
        AUTO, ON, OFF
    }

    private Side positioning = Side.LEFT;
    private int marginX = 4;
    private int marginY = 0;
    private boolean enabled = true;
    private boolean showTooltips = true;
    private boolean showOffhandSlot = true;
    private DarkMode darkMode = DarkMode.AUTO;
    private Set<String> disabledContainers = new HashSet<>();

    private static final String FILE_NAME = "visiblearmorslots.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CONFIG_VERSION = 4; 
    
    private static ModConfig instance;

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
    }

    private ModConfig() {
        // Blocklist: empty means everything is allowed.
    }

    public Side getPositioning() {
        return positioning;
    }

    public int getMarginX() {
        return marginX;
    }

    public int getMarginY() {
        return marginY;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean shouldShowTooltips() {
        return showTooltips;
    }

    public boolean isShowOffhandSlot() {
        return showOffhandSlot;
    }

    /**
     * Whether the recipe book state matters at all. Only the LEFT side can
     * collide with the book (it opens on the left), so on RIGHT the whole
     * book check is disabled instead of being consulted every frame.
     */
    public boolean shouldMirrorForRecipeBook() {
        return positioning == Side.LEFT;
    }

    public DarkMode getDarkMode() {
        return darkMode;
    }

    /**
     * Effective dark mode: ON always, OFF never, AUTO follows detected themes.
     */
    public boolean isDarkModeEffective() {
        return switch (darkMode) {
            case ON -> true;
            case OFF -> false;
            case AUTO -> dev.quentintyr.visiblearmorslots.util.DarkThemeDetector.isDarkThemeActive();
        };
    }

    public Set<String> getDisabledContainers() {
        return Collections.unmodifiableSet(disabledContainers);
    }

    public void setPositioning(Side positioning) {
        this.positioning = positioning;
    }

    public void setMarginX(int marginX) {
        this.marginX = Math.max(0, marginX);
    }

    public void setMarginY(int marginY) {
        this.marginY = marginY;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setShowTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    public void setDarkMode(DarkMode darkMode) {
        this.darkMode = darkMode;
    }

    public void setShowOffhandSlot(boolean showOffhandSlot) {
        this.showOffhandSlot = showOffhandSlot;
    }

    public boolean isContainerAllowed(Identifier id) {
        return !disabledContainers.contains(id.toString());
    }

    /**
     * Check if a container is explicitly disabled.
     */
    public boolean isContainerDisabled(String containerId) {
        return disabledContainers.contains(containerId);
    }

    /**
     * Enable or disable a specific container
     */
    public void setContainerEnabled(String containerId, boolean enabled) {
        if (enabled) {
            disabledContainers.remove(containerId);
        } else {
            disabledContainers.add(containerId);
        }
    }

    /**
     * Reset containers to default (everything allowed)
     */
    public void resetContainersToDefault() {
        disabledContainers.clear();
    }
    
    /**
     * Get the config file path for debugging
     */
    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            // Save defaults
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null)
                return;
                
            // Check config version
            int version = root.has("configVersion") ? root.get("configVersion").getAsInt() : 1;
            if (version < CONFIG_VERSION) {
                dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.info(
                    "Updating config from version {} to {}", version, CONFIG_VERSION
                );
            }
            
            ModConfig cfg = getInstance();
            if (root.has("enabled"))
                cfg.enabled = root.get("enabled").getAsBoolean();
            if (root.has("showOffhandSlot"))
                cfg.showOffhandSlot = root.get("showOffhandSlot").getAsBoolean();
            if (root.has("showTooltips"))
                cfg.showTooltips = root.get("showTooltips").getAsBoolean();
            if (root.has("darkMode")) {
                try {
                    cfg.darkMode = DarkMode.valueOf(root.get("darkMode").getAsString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Legacy boolean: true meant ON, anything else becomes AUTO.
                    cfg.darkMode = root.get("darkMode").getAsBoolean() ? DarkMode.ON : DarkMode.AUTO;
                }
            }
            if (root.has("positioning")) {
                try {
                    cfg.positioning = Side.valueOf(root.get("positioning").getAsString().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (root.has("marginX"))
                cfg.setMarginX(root.get("marginX").getAsInt());
            if (root.has("marginY"))
                cfg.setMarginY(root.get("marginY").getAsInt());
            if (root.has("disabledContainers")) {
                cfg.disabledContainers.clear();
                JsonArray arr = root.getAsJsonArray("disabledContainers");
                arr.forEach(e -> cfg.disabledContainers.add(e.getAsString()));
            }
            // Legacy allowlist from v2 and earlier is intentionally ignored:
            // semantics flipped to blocklist, old entries do not transfer.
        } catch (IOException e) {
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.warn("Failed to load config file, using defaults: {}", e.getMessage());
        } catch (Exception e) {
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.error("Error parsing config file, using defaults", e);
        }
    }

    public static void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        JsonObject root = new JsonObject();
        ModConfig cfg = getInstance();
        
        root.addProperty("configVersion", CONFIG_VERSION);
        
        // Core settings
        root.addProperty("_comment_1", "=== Core Settings ===");
        root.addProperty("enabled", cfg.enabled);
        root.addProperty("showTooltips", cfg.showTooltips);
        
        // Display settings
        root.addProperty("_comment_2", "=== Display Settings ===");
        root.addProperty("positioning", cfg.positioning.name());
        root.addProperty("marginX", cfg.marginX);
        root.addProperty("marginY", cfg.marginY);
        root.addProperty("showOffhandSlot", cfg.showOffhandSlot);
        root.addProperty("darkMode", cfg.darkMode.name());
        
        // Container blocklist
        root.addProperty("_comment_3", "=== Container Blocklist (leave empty to allow all) ===");
        JsonArray arr = new JsonArray();
        cfg.disabledContainers.forEach(arr::add);
        root.add("disabledContainers", arr);
        
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.error("Failed to save config file: {}", e.getMessage());
        }
    }
}
