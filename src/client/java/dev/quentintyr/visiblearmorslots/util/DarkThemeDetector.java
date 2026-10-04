package dev.quentintyr.visiblearmorslots.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Detects dark UI themes from loaded mods and enabled resource packs.
 * Used for DarkMode.AUTO. Lists are hardcoded; unknown themes can be
 * reported to grow them.
 */
public final class DarkThemeDetector {

    private static final List<String> DARK_MOD_IDS = List.of(
            "mindfuldarkness",
            "darkmodeeverywhere"
    );

    private static final List<String> DARK_PACK_SLUGS = List.of(
            "default-dark-mode",
            "mandalas-gui-dark-mode",
            "unique-dark",
            "skyblock-dark-ui",
            "mandalas-gui-dark-mode-mod-compatibility",
            "reimaginedguidark",
            "colourful-containers-dark-mode-gui",
            "create-darkmode",
            "gui-revision-dark",
            "colourful-containers-modded-dark-mode-gui-compat",
            "recolourful-containers-gui-hud-dark",
            "unique-dark-refined",
            "default-dark-mode-legacy4j",
            "dark-everywhere",
            "dark-smooth-gui",
            "dark-transparent-gui",
            "dark-ui-enderstudent",
            "pretty-dark-ui",
            "nova-ui",
            "night-ui",
            "dark-coffe-gui",
            "modded-coffee-gui"
    );

    private DarkThemeDetector() {
    }

    public static boolean isDarkThemeActive() {
        for (String id : DARK_MOD_IDS) {
            try {
                if (FabricLoader.getInstance().isModLoaded(id)) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return false;
            }
            for (String packId : mc.getResourcePackRepository().getSelectedIds()) {
                for (String slug : DARK_PACK_SLUGS) {
                    if (matchesSlug(packId, slug)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean matchesSlug(String packId, String slug) {
        String normPack = normalize(packId);
        String[] tokens = slug.toLowerCase().split("[^a-z0-9]+");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (!normPack.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
